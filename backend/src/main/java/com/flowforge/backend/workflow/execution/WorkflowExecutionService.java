package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.outbox.WorkflowOutboxService;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.entity.Workflow;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;
import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionRequest;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import com.flowforge.backend.workflow.execution.entity.WorkflowNodeExecution;
import com.flowforge.backend.workflow.repository.WorkflowEdgeRepository;
import com.flowforge.backend.workflow.repository.WorkflowNodeRepository;
import com.flowforge.backend.workflow.repository.WorkflowRepository;
import com.flowforge.backend.workflow.repository.WorkflowVersionRepository;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionService {

    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final List<NodeExecutor> nodeExecutors;
    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowExecutionPersistenceService persistenceService;
    private final WorkspaceContext workspaceContext;
    private final ObjectMapper objectMapper;
    private final ConditionEvaluator conditionEvaluator;
    private final WorkflowOutboxService outboxService;

    public WorkflowExecutionResult execute(
        UUID workflowVersionId,
        WorkflowExecutionRequest request
    ) {

        WorkflowVersion version =
            workflowVersionRepository
                .findById(workflowVersionId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Workflow version not found"
                    )
                );

        List<WorkflowNode> nodes =
            nodeRepository.findAllForExecution(
                workflowVersionId
            );

        List<WorkflowEdge> edges =
            edgeRepository.findAllForExecution(
                workflowVersionId
            );

        WorkflowExecution execution =
            persistenceService.startExecution(
                version,
                request.input()
            );

        WorkflowExecutionResult result =
            executeGraph(
                execution.getId(),
                nodes,
                edges,
                request.input()
            );

        if (result.isSuccess()) {

            persistenceService.completeExecution(
                execution.getId(),
                result.getOutput() == null
                    ? null
                    : result.getOutput().toString()
            );

        } else {

            persistenceService.failExecution(
                execution.getId(),
                result.getErrorMessage()
            );
        }

        return result;
    }

    public WorkflowExecutionResult executeWorkflow(
        UUID workspaceId,
        UUID workflowId,
        UUID userId,
        WorkflowExecutionRequest request
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        Workflow workflow =
            workflowRepository
                .findByIdAndWorkspaceId(
                    workflowId,
                    workspaceId
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow not found"
                    )
                );

        WorkflowVersion activeVersion =
            workflow.getActiveVersion();

        if (activeVersion == null) {
            throw new IllegalStateException(
                "Workflow has no published version"
            );
        }

        return execute(
            activeVersion.getId(),
            request
        );
    }

    private WorkflowExecutionResult executeGraph(
        UUID executionId,
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges,
        Map<String, Object> input
    ) {

        WorkflowExecutionContext context =
            new WorkflowExecutionContext(
                objectMapper
            );

        context.putInputAll(
            input == null
                ? Map.of()
                : input
        );

        Map<UUID, WorkflowNode> nodesById =
            new HashMap<>();

        /*
         * IMPORTANT:
         *
         * We store WorkflowEdge here rather than
         * WorkflowNode because the condition belongs
         * to the edge.
         */
        Map<UUID, List<WorkflowEdge>> adjacency =
            new HashMap<>();

        /*
         * Number of incoming edges that are still
         * unresolved.
         */
        Map<UUID, Integer> remainingIncoming =
            new HashMap<>();

        /*
         * Whether at least one incoming edge actually
         * selected this node for execution.
         */
        Map<UUID, Boolean> activated =
            new HashMap<>();

        for (WorkflowNode node : nodes) {

            UUID nodeId =
                node.getId();

            nodesById.put(
                nodeId,
                node
            );

            adjacency.put(
                nodeId,
                new ArrayList<>()
            );

            remainingIncoming.put(
                nodeId,
                0
            );

            activated.put(
                nodeId,
                false
            );
        }

        /*
         * Build graph.
         */
        for (WorkflowEdge edge : edges) {

            UUID sourceId =
                edge.getSourceNode().getId();

            UUID targetId =
                edge.getTargetNode().getId();

            if (!nodesById.containsKey(sourceId)) {

                throw new IllegalStateException(
                    "Source node not found: "
                        + sourceId
                );
            }

            if (!nodesById.containsKey(targetId)) {

                throw new IllegalStateException(
                    "Target node not found: "
                        + targetId
                );
            }

            adjacency
                .get(sourceId)
                .add(edge);

            remainingIncoming.put(
                targetId,
                remainingIncoming.get(targetId) + 1
            );
        }

        /*
         * Root nodes have no incoming edges and therefore
         * are automatically activated.
         */
        Queue<WorkflowNode> queue =
            new ArrayDeque<>();

        nodes.stream()
            .filter(node ->
                remainingIncoming.get(
                    node.getId()
                ) == 0
            )
            .forEach(node -> {

                activated.put(
                    node.getId(),
                    true
                );

                queue.add(node);
            });

        int resolvedNodes = 0;

        while (!queue.isEmpty()) {

            WorkflowNode node =
                queue.poll();

            UUID nodeId =
                node.getId();

            boolean shouldExecute =
                activated.get(nodeId);

            String nodeInput;

            try {

                nodeInput =
                    objectMapper.writeValueAsString(
                        context.snapshot()
                    );

            } catch (Exception exception) {

                throw new IllegalStateException(
                    "Failed to serialize node input",
                    exception
                );
            }

            /*
             * Node has no active incoming path.
             *
             * Persist SKIPPED instead of pretending
             * that the node was never evaluated.
             */
            if (!shouldExecute) {

                persistenceService.skipNodeExecution(
                    executionId,
                    node,
                    nodeInput
                );

                resolvedNodes++;

                propagateNodeResult(
                    node,
                    false,
                    context,
                    adjacency,
                    remainingIncoming,
                    activated,
                    queue
                );

                continue;
            }

            WorkflowNodeExecution nodeExecution =
                persistenceService.startNodeExecution(
                    executionId,
                    node,
                    nodeInput
                );

            try {

                NodeExecutor executor =
                    findExecutor(node);

                NodeExecutionResult result =
                    executor.execute(
                        node,
                        context
                    );

                JsonNode outputJson = null;

                if (result.output() != null) {

                    outputJson =
                        objectMapper.readTree(
                            result.output()
                        );

                    context.putNodeOutput(
                        node.getNodeKey(),
                        outputJson
                    );
                }

                persistenceService.completeNodeExecution(
                    nodeExecution.getId(),
                    result.output()
                );

                resolvedNodes++;

                propagateNodeResult(
                    node,
                    true,
                    context,
                    adjacency,
                    remainingIncoming,
                    activated,
                    queue
                );

            } catch (Exception exception) {

                log.error(
                    "Workflow execution failed at node {}",
                    node.getNodeKey(),
                    exception
                );

                persistenceService.failNodeExecution(
                    nodeExecution.getId(),
                    exception.getMessage()
                );

                return WorkflowExecutionResult.failure(
                    executionId,
                    node.getNodeKey(),
                    exception.getMessage()
                );
            }
        }

        /*
         * If some nodes were never resolved, the graph
         * contains a cycle or otherwise invalid topology.
         */
        if (resolvedNodes != nodes.size()) {

            return WorkflowExecutionResult.failure(
                executionId,
                null,
                "Workflow graph contains a cycle or unresolved dependency"
            );
        }

        JsonNode finalOutput = null;

        if (!nodes.isEmpty()) {

            WorkflowNode lastNode =
                nodes.stream()
                    .filter(node ->
                        context.getNodeOutput(
                            node.getNodeKey()
                        ) != null
                    )
                    .reduce((first, second) -> second)
                    .orElse(null);

            if (lastNode != null) {
                finalOutput =
                    context.getNodeOutput(
                        lastNode.getNodeKey()
                    );
            }
        }

        return WorkflowExecutionResult.success(
            executionId,
            finalOutput
        );
    }

    private void propagateNodeResult(
        WorkflowNode node,
        boolean nodeExecuted,
        WorkflowExecutionContext context,
        Map<UUID, List<WorkflowEdge>> adjacency,
        Map<UUID, Integer> remainingIncoming,
        Map<UUID, Boolean> activated,
        Queue<WorkflowNode> queue
    ) {

        for (
            WorkflowEdge edge :
            adjacency.get(node.getId())
        ) {

            UUID targetId =
                edge.getTargetNode().getId();

            boolean edgeSelected =
                false;

            /*
             * A skipped source cannot activate
             * downstream nodes.
             */
            if (nodeExecuted) {

                edgeSelected =
                    conditionEvaluator.evaluate(
                        edge.getCondition(),
                        context
                    );
            }

            if (edgeSelected) {

                activated.put(
                    targetId,
                    true
                );
            }

            int remaining =
                remainingIncoming.get(targetId) - 1;

            remainingIncoming.put(
                targetId,
                remaining
            );

            /*
             * Only after ALL incoming edges have been
             * resolved do we decide whether the node
             * executes or gets skipped.
             */
            if (remaining == 0) {

                queue.add(
                    edge.getTargetNode()
                );
            }
        }
    }

    private NodeExecutor findExecutor(
        WorkflowNode node
    ) {

        return nodeExecutors.stream()
            .filter(executor ->
                executor.supports(node)
            )
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                    "No executor found for node type: "
                        + node.getType()
                )
            );
    }

    @Transactional
    public WorkflowExecutionResult requestExecution(
        UUID workflowVersionId,
        WorkflowExecutionRequest request
    ) {

        WorkflowVersion version =
            workflowVersionRepository
                .findById(workflowVersionId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow version not found"
                    )
                );

        WorkflowExecution execution =
            persistenceService.startExecution(
                version,
                request.input()
            );

        outboxService.createExecutionRequestedEvent(
            execution.getId(),
            version.getId()
        );

        return WorkflowExecutionResult.queued(
            execution.getId()
        );
    }
}

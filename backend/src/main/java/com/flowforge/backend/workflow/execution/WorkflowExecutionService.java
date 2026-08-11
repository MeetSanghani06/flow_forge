package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.entity.Workflow;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;
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
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionService {

    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final List<NodeExecutor> nodeExecutors;
    private final WorkflowRepository  workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowExecutionPersistenceService persistenceService;
    private final WorkspaceContext workspaceContext;
    private final ObjectMapper objectMapper;

    public WorkflowExecutionResult execute(
        UUID workflowVersionId
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
                version
            );

        WorkflowExecutionResult result =
            executeGraph(
                execution.getId(),
                nodes,
                edges
            );

        if (result.isSuccess()) {

            persistenceService.completeExecution(
                execution.getId(),
                null
            );

        } else {

            persistenceService.failExecution(
                execution.getId(),
                result.getErrorMessage()
            );
        }

        return result;
    }

    @Transactional
    public WorkflowExecutionResult executeWorkflow(
        UUID workspaceId,
        UUID workflowId,
        UUID userId
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
            activeVersion.getId()
        );
    }

    private WorkflowExecutionResult executeGraph(
        UUID executionId,
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges
    ) {

        WorkflowExecutionContext context =
            new WorkflowExecutionContext();

        Map<UUID, WorkflowNode> nodesById =
            new HashMap<>();

        Map<UUID, List<WorkflowNode>> adjacency =
            new HashMap<>();

        Map<UUID, Integer> indegree =
            new HashMap<>();

        for (WorkflowNode node : nodes) {

            nodesById.put(
                node.getId(),
                node
            );

            adjacency.put(
                node.getId(),
                new ArrayList<>()
            );

            indegree.put(
                node.getId(),
                0
            );
        }

        for (WorkflowEdge edge : edges) {

            UUID sourceId =
                edge.getSourceNode().getId();

            UUID targetId =
                edge.getTargetNode().getId();

            WorkflowNode targetNode =
                nodesById.get(targetId);

            if (targetNode == null) {
                throw new IllegalStateException(
                    "Target node not found: " + targetId
                );
            }

            List<WorkflowNode> outgoing =
                adjacency.get(sourceId);

            if (outgoing == null) {
                throw new IllegalStateException(
                    "Source node not found: " + sourceId
                );
            }

            outgoing.add(targetNode);

            indegree.put(
                targetId,
                indegree.get(targetId) + 1
            );
        }

        Queue<WorkflowNode> queue =
            new ArrayDeque<>();

        nodes.stream()
            .filter(node ->
                indegree.get(node.getId()) == 0
            )
            .forEach(queue::add);

        int executed = 0;

        while (!queue.isEmpty()) {

            WorkflowNode node = queue.poll();

            String input =
                objectMapper.writeValueAsString(
                    context.snapshot()
                );

            WorkflowNodeExecution nodeExecution =
                persistenceService.startNodeExecution(
                    executionId,
                    node,
                    input
                );

            try {

                NodeExecutor executor =
                    findExecutor(node);

                NodeExecutionResult result =
                    executor.execute(
                        node,
                        context
                    );

                if (result.output() != null) {
                    context.put(
                        node.getNodeKey(),
                        result.output()
                    );
                }

                persistenceService.completeNodeExecution(
                    nodeExecution.getId(),
                    result.output()
                );

                executed++;

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

            for (
                WorkflowNode next :
                adjacency.get(node.getId())
            ) {

                UUID nextId =
                    next.getId();

                int newIndegree =
                    indegree.get(nextId) - 1;

                indegree.put(
                    nextId,
                    newIndegree
                );

                if (newIndegree == 0) {
                    queue.add(next);
                }
            }
        }

        if (executed != nodes.size()) {

            return WorkflowExecutionResult.failure(
                executionId,
                null,
                "Workflow graph contains a cycle"
            );
        }

        return WorkflowExecutionResult.success(
            executionId
        );
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

    private void executeNode(
        WorkflowNode node,
        WorkflowExecutionContext context
    ) {

        NodeExecutor executor =
            nodeExecutors.stream()
                .filter(candidate ->
                    candidate.supports(node)
                )
                .findFirst()
                .orElseThrow(() ->
                    new IllegalStateException(
                        "No executor found for node type: "
                            + node.getType()
                    )
                );

        executor.execute(node, context);
    }
}

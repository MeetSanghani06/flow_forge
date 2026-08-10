package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import com.flowforge.backend.workflow.execution.entity.WorkflowNodeExecution;
import com.flowforge.backend.workflow.repository.WorkflowEdgeRepository;
import com.flowforge.backend.workflow.repository.WorkflowNodeRepository;
import com.flowforge.backend.workflow.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionService {

    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final List<NodeExecutor> nodeExecutors;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowExecutionPersistenceService persistenceService;

    @Transactional
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

        try {

            WorkflowExecutionResult result =
                executeGraph(
                    execution,
                    nodes,
                    edges
                );

            if (result.isSuccess()) {

                persistenceService.completeExecution(
                    execution,
                    null
                );

            } else {

                persistenceService.failExecution(
                    execution,
                    result.getErrorMessage()
                );
            }

            return result;

        } catch (Exception exception) {

            log.error(
                "Workflow execution failed | executionId={}",
                execution.getId(),
                exception
            );

            persistenceService.failExecution(
                execution,
                exception.getMessage()
            );

            return WorkflowExecutionResult.failure(
                execution.getId(),
                null,
                exception.getMessage()
            );
        }
    }

    private WorkflowExecutionResult executeGraph(
        WorkflowExecution execution,
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges
    ) {

        WorkflowExecutionContext context =
            WorkflowExecutionContext.builder()
                .build();

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

            WorkflowNodeExecution nodeExecution =
                persistenceService.startNodeExecution(
                    execution,
                    node
                );

            try {

                executeNode(
                    node,
                    context
                );

                persistenceService.completeNodeExecution(
                    nodeExecution,
                    null
                );

                executed++;

            } catch (Exception exception) {

                log.error(
                    "Workflow execution failed at node {}",
                    node.getNodeKey(),
                    exception
                );

                persistenceService.failNodeExecution(
                    nodeExecution,
                    exception.getMessage()
                );

                return WorkflowExecutionResult.failure(
                    execution.getId(),
                    node.getNodeKey(),
                    exception.getMessage()
                );
            }

            for (WorkflowNode next :
                adjacency.get(node.getId())) {

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
                execution.getId(),
                null,
                "Workflow graph contains a cycle"
            );
        }

        return WorkflowExecutionResult.success(
            execution.getId()
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

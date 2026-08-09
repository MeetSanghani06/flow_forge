package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionService {

    private final List<NodeExecutor> nodeExecutors;

    public WorkflowExecutionResult execute(
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

            nodesById.put(node.getId(), node);
            adjacency.put(node.getId(), new ArrayList<>());
            indegree.put(node.getId(), 0);
        }

        for (WorkflowEdge edge : edges) {

            UUID sourceId =
                edge.getSourceNode().getId();

            UUID targetId =
                edge.getTargetNode().getId();

            adjacency
                .get(sourceId)
                .add(edge.getTargetNode());

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

            try {

                executeNode(node, context);

                executed++;

            } catch (Exception exception) {

                log.error(
                    "Workflow execution failed at node {}",
                    node.getNodeKey(),
                    exception
                );

                return WorkflowExecutionResult.failure(
                    node.getNodeKey(),
                    exception.getMessage()
                );
            }

            for (WorkflowNode next :
                adjacency.get(node.getId())) {

                UUID nextId = next.getId();

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
                null,
                "Workflow graph contains a cycle"
            );
        }

        return WorkflowExecutionResult.success();
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

package com.flowforge.backend.workflow.validator;

import com.flowforge.backend.workflow.entity.NodeType;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkflowPublishValidator {

    public void validate(
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges
    ) {

        if (nodes.isEmpty()) {
            throw new IllegalStateException(
                "Workflow must contain at least one node"
            );
        }

        validateTrigger(nodes);

        validateEdges(nodes, edges);

        validateGraphAcyclic(
            nodes,
            edges
        );
    }

    private void validateTrigger(
        List<WorkflowNode> nodes
    ) {

        long triggerCount =
            nodes.stream()
                .filter(node ->
                    node.getType() == NodeType.TRIGGER
                )
                .count();

        if (triggerCount == 0) {
            throw new IllegalStateException(
                "Workflow must contain a trigger node"
            );
        }

        if (triggerCount > 1) {
            throw new IllegalStateException(
                "Workflow cannot contain multiple trigger nodes"
            );
        }
    }

    private void validateEdges(
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges
    ) {

        Set<UUID> nodeIds =
            nodes.stream()
                .map(WorkflowNode::getId)
                .collect(Collectors.toSet());

        for (WorkflowEdge edge : edges) {

            if (!nodeIds.contains(
                edge.getSourceNode().getId()
            )) {
                throw new IllegalStateException(
                    "Edge references a source node outside the workflow version"
                );
            }

            if (!nodeIds.contains(
                edge.getTargetNode().getId()
            )) {
                throw new IllegalStateException(
                    "Edge references a target node outside the workflow version"
                );
            }
        }
    }

    private void validateGraphAcyclic(
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges
    ) {

        Map<UUID, Integer> indegree =
            new HashMap<>();

        Map<UUID, List<UUID>> adjacency =
            new HashMap<>();

        for (WorkflowNode node : nodes) {

            indegree.put(node.getId(), 0);

            adjacency.put(
                node.getId(),
                new ArrayList<>()
            );
        }

        for (WorkflowEdge edge : edges) {

            UUID source =
                edge.getSourceNode().getId();

            UUID target =
                edge.getTargetNode().getId();

            adjacency
                .get(source)
                .add(target);

            indegree.put(
                target,
                indegree.get(target) + 1
            );
        }

        Queue<UUID> queue =
            new ArrayDeque<>();

        indegree.forEach(
            (nodeId, degree) -> {
                if (degree == 0) {
                    queue.add(nodeId);
                }
            }
        );

        int visited = 0;

        while (!queue.isEmpty()) {

            UUID nodeId = queue.poll();

            visited++;

            for (UUID next :
                adjacency.get(nodeId)) {

                int newDegree =
                    indegree.get(next) - 1;

                indegree.put(
                    next,
                    newDegree
                );

                if (newDegree == 0) {
                    queue.add(next);
                }
            }
        }

        if (visited != nodes.size()) {
            throw new IllegalStateException(
                "Workflow graph contains a cycle"
            );
        }
    }
}

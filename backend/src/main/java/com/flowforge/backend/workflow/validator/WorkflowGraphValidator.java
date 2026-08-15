package com.flowforge.backend.workflow.validation;

import com.flowforge.backend.workflow.dto.WorkflowEdgeRequest;
import com.flowforge.backend.workflow.dto.WorkflowNodeRequest;
import com.flowforge.backend.workflow.dto.SaveWorkflowGraphRequest;
import com.flowforge.backend.workflow.entity.NodeType;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WorkflowGraphValidator {

    public void validate(SaveWorkflowGraphRequest request) {

        validateNodeKeys(request.nodes());
        validateEdges(request);
        validateTrigger(request.nodes());
        validateDuplicateEdges(request.edges());
        validateNoCycles(request);
        validateReachability(request);
    }

    private void validateNodeKeys(
        List<WorkflowNodeRequest> nodes
    ) {

        Set<String> keys = new HashSet<>();

        for (WorkflowNodeRequest node : nodes) {

            String key = node.nodeKey().trim();

            if (!keys.add(key)) {
                throw new IllegalArgumentException(
                    "Duplicate node key: " + key
                );
            }
        }
    }

    private void validateEdges(
        SaveWorkflowGraphRequest request
    ) {

        Set<String> nodeKeys =
            request.nodes()
                .stream()
                .map(WorkflowNodeRequest::nodeKey)
                .map(String::trim)
                .collect(java.util.stream.Collectors.toSet());

        for (WorkflowEdgeRequest edge : request.edges()) {

            String source = edge.source().trim();
            String target = edge.target().trim();

            if (!nodeKeys.contains(source)) {
                throw new IllegalArgumentException(
                    "Edge references unknown source node: " + source
                );
            }

            if (!nodeKeys.contains(target)) {
                throw new IllegalArgumentException(
                    "Edge references unknown target node: " + target
                );
            }

            if (source.equals(target)) {
                throw new IllegalArgumentException(
                    "A node cannot point to itself: " + source
                );
            }
        }
    }

    private void validateTrigger(
        List<WorkflowNodeRequest> nodes
    ) {

        long triggerCount =
            nodes.stream()
                .filter(node -> node.type() == NodeType.TRIGGER)
                .count();

        if (triggerCount == 0) {
            throw new IllegalArgumentException(
                "Workflow must contain exactly one TRIGGER node"
            );
        }

        if (triggerCount > 1) {
            throw new IllegalArgumentException(
                "Workflow cannot contain more than one TRIGGER node"
            );
        }
    }

    private void validateDuplicateEdges(
        List<WorkflowEdgeRequest> edges
    ) {

        Set<String> uniqueEdges = new HashSet<>();

        for (WorkflowEdgeRequest edge : edges) {

            String edgeKey =
                edge.source().trim()
                    + "->"
                    + edge.target().trim();

            if (!uniqueEdges.add(edgeKey)) {
                throw new IllegalArgumentException(
                    "Duplicate edge: " + edgeKey
                );
            }
        }
    }

    private void validateNoCycles(
        SaveWorkflowGraphRequest request
    ) {

        Map<String, List<String>> adjacency =
            buildAdjacencyList(request);

        Map<String, Integer> indegree =
            new HashMap<>();

        for (WorkflowNodeRequest node : request.nodes()) {
            indegree.put(node.nodeKey().trim(), 0);
        }

        for (WorkflowEdgeRequest edge : request.edges()) {

            String target = edge.target().trim();

            indegree.put(
                target,
                indegree.get(target) + 1
            );
        }

        Queue<String> queue = new ArrayDeque<>();

        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {

            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int processedNodes = 0;

        while (!queue.isEmpty()) {

            String current = queue.poll();

            processedNodes++;

            for (String neighbour :
                adjacency.getOrDefault(
                    current,
                    List.of()
                )) {

                int newIndegree =
                    indegree.get(neighbour) - 1;

                indegree.put(
                    neighbour,
                    newIndegree
                );

                if (newIndegree == 0) {
                    queue.add(neighbour);
                }
            }
        }

        if (processedNodes != request.nodes().size()) {
            throw new IllegalArgumentException(
                "Workflow graph contains a cycle"
            );
        }
    }

    private void validateReachability(
        SaveWorkflowGraphRequest request
    ) {

        WorkflowNodeRequest trigger =
            request.nodes()
                .stream()
                .filter(node ->
                    node.type() == NodeType.TRIGGER
                )
                .findFirst()
                .orElseThrow();

        Map<String, List<String>> adjacency =
            buildAdjacencyList(request);

        Set<String> visited =
            new HashSet<>();

        Queue<String> queue =
            new ArrayDeque<>();

        String triggerKey =
            trigger.nodeKey().trim();

        queue.add(triggerKey);
        visited.add(triggerKey);

        while (!queue.isEmpty()) {

            String current = queue.poll();

            for (String neighbour :
                adjacency.getOrDefault(
                    current,
                    List.of()
                )) {

                if (visited.add(neighbour)) {
                    queue.add(neighbour);
                }
            }
        }

        if (visited.size() != request.nodes().size()) {

            List<String> unreachable =
                request.nodes()
                    .stream()
                    .map(WorkflowNodeRequest::nodeKey)
                    .map(String::trim)
                    .filter(key -> !visited.contains(key))
                    .toList();

            throw new IllegalArgumentException(
                "Workflow contains unreachable nodes: "
                    + unreachable
            );
        }
    }

    private Map<String, List<String>> buildAdjacencyList(
        SaveWorkflowGraphRequest request
    ) {

        Map<String, List<String>> adjacency =
            new HashMap<>();

        for (WorkflowNodeRequest node : request.nodes()) {

            adjacency.put(
                node.nodeKey().trim(),
                new ArrayList<>()
            );
        }

        for (WorkflowEdgeRequest edge : request.edges()) {

            adjacency
                .get(edge.source().trim())
                .add(edge.target().trim());
        }

        return adjacency;
    }
}

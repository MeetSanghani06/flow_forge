package com.flowforge.backend.workflow.service;

import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.dto.SaveWorkflowGraphRequest;
import com.flowforge.backend.workflow.dto.WorkflowEdgeRequest;
import com.flowforge.backend.workflow.dto.WorkflowGraphResponse;
import com.flowforge.backend.workflow.dto.WorkflowNodeRequest;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.execution.repository.WorkflowExecutionRepository;
import com.flowforge.backend.workflow.repository.WorkflowEdgeRepository;
import com.flowforge.backend.workflow.repository.WorkflowNodeRepository;
import com.flowforge.backend.workflow.repository.WorkflowRepository;
import com.flowforge.backend.workflow.repository.WorkflowVersionRepository;
import com.flowforge.backend.workflow.validation.WorkflowGraphValidator;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowGraphService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final WorkspaceContext workspaceContext;
    private final WorkflowGraphValidator graphValidator;
    private final ObjectMapper objectMapper;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowGraphCacheService cacheService;

    @CacheEvict(
        value = "workflow-graphs",
        key = "#workflowId + ':' + #versionNumber"
    )
    @Transactional
    public WorkflowGraphResponse saveGraph(
        UUID workspaceId,
        UUID workflowId,
        int versionNumber,
        UUID userId,
        SaveWorkflowGraphRequest request
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

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

        WorkflowVersion version =
            versionRepository
                .findByWorkflowIdAndVersionNumber(
                    workflowId,
                    versionNumber
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow version not found"
                    )
                );

        // Protection 1: published versions are immutable
        if (version.isPublished()) {
            throw new IllegalStateException(
                "Published workflow versions cannot be modified"
            );
        }

        // Protection 2: executed versions are immutable
        if (executionRepository.existsByWorkflowVersionId(version.getId())) {
            throw new IllegalStateException(
                "Workflow version cannot be modified after execution. "
                    + "Create a new version instead."
            );
        }

        graphValidator.validate(request);
        validateConnectorIds(request.nodes());

        /*
         * Safe because this version has:
         * - not been published
         * - never been executed
         *
         * Therefore, it is still an editable draft snapshot.
         */
        edgeRepository.deleteAllByWorkflowVersionId(version.getId());
        nodeRepository.deleteAllByWorkflowVersionId(version.getId());

        nodeRepository.flush();

        Map<String, WorkflowNode> nodesByKey =
            new HashMap<>();

        for (WorkflowNodeRequest nodeRequest : request.nodes()) {

            WorkflowNode node = new WorkflowNode();

            node.setWorkflowVersion(version);
            node.setNodeKey(nodeRequest.nodeKey().trim());
            node.setName(nodeRequest.name().trim());
            node.setType(nodeRequest.type());

            node.setConfiguration(
                objectMapper.writeValueAsString(
                    nodeRequest.configuration()
                )
            );

            if (nodeRequest.connectorId() != null &&
                !nodeRequest.connectorId().isBlank()) {

                node.setConnectorId(
                    UUID.fromString(
                        nodeRequest.connectorId()
                    )
                );
            }

            WorkflowNode saved =
                nodeRepository.save(node);

            nodesByKey.put(
                saved.getNodeKey(),
                saved
            );
        }

        for (WorkflowEdgeRequest edgeRequest : request.edges()) {

            WorkflowNode source =
                nodesByKey.get(edgeRequest.source());

            WorkflowNode target =
                nodesByKey.get(edgeRequest.target());

            if (source == null || target == null) {
                throw new IllegalArgumentException(
                    "Edge references an unknown node"
                );
            }

            WorkflowEdge edge = new WorkflowEdge();

            edge.setWorkflowVersion(version);
            edge.setSourceNode(source);
            edge.setTargetNode(target);
            edge.setCondition(edgeRequest.condition());

            edgeRepository.save(edge);
        }

        return getGraph(version);
    }

    @Cacheable(
        value = "workflow-graphs",
        key = "#workflowId + ':' + #versionNumber"
    )
    @Transactional(readOnly = true)
    public WorkflowGraphResponse getGraph(
        UUID workspaceId,
        UUID workflowId,
        int versionNumber,
        UUID userId
    ) {
        cacheService.logCacheStatus(
            workflowId,
            versionNumber
        );

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        WorkflowVersion version =
            versionRepository
                .findByWorkflowIdAndVersionNumber(
                    workflowId,
                    versionNumber
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow version not found"
                    )
                );

        return getGraph(version);
    }

    private WorkflowGraphResponse getGraph(
        WorkflowVersion version
    ) {

        List<WorkflowNode> nodes =
            nodeRepository
                .findAllByWorkflowVersionId(
                    version.getId()
                );

        List<WorkflowEdge> edges =
            edgeRepository
                .findAllByWorkflowVersionId(
                    version.getId()
                );

        return new WorkflowGraphResponse(
            version.getId(),
            version.getVersionNumber(),
            nodes.stream()
                .map(node ->
                    new WorkflowGraphResponse.NodeResponse(
                        node.getId(),
                        node.getNodeKey(),
                        node.getName(),
                        node.getType(),
                        node.getConnectorId(),
                        node.getConfiguration()
                    )
                )
                .toList(),
            edges.stream()
                .map(edge ->
                    new WorkflowGraphResponse.EdgeResponse(
                        edge.getId(),
                        edge.getSourceNode().getNodeKey(),
                        edge.getTargetNode().getNodeKey(),
                        edge.getCondition()
                    )
                )
                .toList()
        );
    }

    private void validateNodeKeys(
        List<WorkflowNodeRequest> nodes
    ) {

        Set<String> keys = new HashSet<>();

        for (WorkflowNodeRequest node : nodes) {

            if (!keys.add(node.nodeKey())) {
                throw new IllegalArgumentException(
                    "Duplicate node key: " + node.nodeKey()
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
                .collect(Collectors.toSet());

        for (WorkflowEdgeRequest edge : request.edges()) {

            if (!nodeKeys.contains(edge.source())) {
                throw new IllegalArgumentException(
                    "Unknown source node: " + edge.source()
                );
            }

            if (!nodeKeys.contains(edge.target())) {
                throw new IllegalArgumentException(
                    "Unknown target node: " + edge.target()
                );
            }

            if (edge.source().equals(edge.target())) {
                throw new IllegalArgumentException(
                    "A node cannot point to itself"
                );
            }
        }
    }

    private void validateConnectorIds(
        List<WorkflowNodeRequest> nodes
    ) {

        for (WorkflowNodeRequest node : nodes) {

            if (node.connectorId() == null ||
                node.connectorId().isBlank()) {
                continue;
            }

            try {
                UUID.fromString(node.connectorId());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                    "Invalid connector ID: "
                        + node.connectorId()
                );
            }
        }
    }
}

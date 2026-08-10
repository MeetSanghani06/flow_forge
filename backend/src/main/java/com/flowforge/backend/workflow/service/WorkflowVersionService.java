package com.flowforge.backend.workflow.service;

import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.dto.WorkflowVersionResponse;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.mapper.WorkflowVersionMapper;
import com.flowforge.backend.workflow.repository.WorkflowEdgeRepository;
import com.flowforge.backend.workflow.repository.WorkflowNodeRepository;
import com.flowforge.backend.workflow.repository.WorkflowRepository;
import com.flowforge.backend.workflow.repository.WorkflowVersionRepository;
import com.flowforge.backend.workflow.validator.WorkflowPublishValidator;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowVersionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final WorkspaceContext workspaceContext;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final WorkflowVersionMapper workflowVersionMapper;
    private final WorkflowPublishValidator workflowPublishValidator;

    @Transactional
    public WorkflowVersionResponse createVersion(
        UUID workspaceId,
        UUID workflowId,
        UUID userId
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        var workflow =
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

        int nextVersion =
            versionRepository
                .findFirstByWorkflowIdOrderByVersionNumberDesc(
                    workflowId
                )
                .map(version ->
                    version.getVersionNumber() + 1
                )
                .orElse(1);

        WorkflowVersion version = new WorkflowVersion();

        version.setWorkflow(workflow);
        version.setVersionNumber(nextVersion);
        version.setPublished(false);

        return toResponse(
            versionRepository.save(version)
        );
    }

    @Transactional(readOnly = true)
    public List<WorkflowVersionResponse> getVersions(
        UUID workspaceId,
        UUID workflowId,
        UUID userId
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

        return versionRepository
            .findAllByWorkflowIdOrderByVersionNumberDesc(
                workflowId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private WorkflowVersionResponse toResponse(
        WorkflowVersion version
    ) {

        return new WorkflowVersionResponse(
            version.getId(),
            version.getWorkflow().getId(),
            version.getVersionNumber(),
            version.isPublished(),
            version.getCreatedAt(),
            version.getUpdatedAt()
        );
    }

    @Transactional
    public WorkflowVersionResponse cloneVersion(
        UUID workspaceId,
        UUID workflowId,
        int sourceVersionNumber,
        UUID userId
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

        WorkflowVersion sourceVersion =
            versionRepository
                .findByWorkflowIdAndVersionNumber(
                    workflowId,
                    sourceVersionNumber
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow version not found"
                    )
                );

        int nextVersionNumber =
            versionRepository
                .findMaxVersionNumberByWorkflowId(
                    workflowId
                )
                .orElse(0) + 1;

        WorkflowVersion newVersion =
            new WorkflowVersion();

        newVersion.setWorkflow(
            sourceVersion.getWorkflow()
        );

        newVersion.setVersionNumber(
            nextVersionNumber
        );

        newVersion.setPublished(false);

        WorkflowVersion savedVersion =
            versionRepository.save(newVersion);

        List<WorkflowNode> sourceNodes =
            nodeRepository.findAllByWorkflowVersionId(
                sourceVersion.getId()
            );

        Map<UUID, WorkflowNode> nodeMapping =
            new HashMap<>();

        Map<String, WorkflowNode> nodesByKey =
            new HashMap<>();

        for (WorkflowNode sourceNode : sourceNodes) {

            WorkflowNode newNode =
                new WorkflowNode();

            newNode.setWorkflowVersion(
                savedVersion
            );

            newNode.setNodeKey(
                sourceNode.getNodeKey()
            );

            newNode.setName(
                sourceNode.getName()
            );

            newNode.setType(
                sourceNode.getType()
            );

            newNode.setConnectorId(
                sourceNode.getConnectorId()
            );

            newNode.setConfiguration(
                sourceNode.getConfiguration()
            );

            WorkflowNode savedNode =
                nodeRepository.save(newNode);

            nodeMapping.put(
                sourceNode.getId(),
                savedNode
            );

            nodesByKey.put(
                savedNode.getNodeKey(),
                savedNode
            );
        }

        List<WorkflowEdge> sourceEdges =
            edgeRepository.findAllByWorkflowVersionId(
                sourceVersion.getId()
            );

        for (WorkflowEdge sourceEdge : sourceEdges) {

            WorkflowNode source =
                nodeMapping.get(
                    sourceEdge.getSourceNode().getId()
                );

            WorkflowNode target =
                nodeMapping.get(
                    sourceEdge.getTargetNode().getId()
                );

            if (source == null || target == null) {
                throw new IllegalStateException(
                    "Unable to clone workflow edge"
                );
            }

            WorkflowEdge newEdge =
                new WorkflowEdge();

            newEdge.setWorkflowVersion(
                savedVersion
            );

            newEdge.setSourceNode(source);
            newEdge.setTargetNode(target);

            newEdge.setCondition(
                sourceEdge.getCondition()
            );

            edgeRepository.save(newEdge);
        }

        return workflowVersionMapper.toResponse(
            savedVersion
        );
    }

    @Transactional
    public WorkflowVersionResponse publishVersion(
        UUID workspaceId,
        UUID workflowId,
        int versionNumber,
        UUID userId
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

        if (version.isPublished()) {
            throw new IllegalStateException(
                "Workflow version is already published"
            );
        }

        List<WorkflowNode> nodes =
            nodeRepository.findAllByWorkflowVersionId(
                version.getId()
            );

        List<WorkflowEdge> edges =
            edgeRepository.findAllByWorkflowVersionId(
                version.getId()
            );

        workflowPublishValidator.validate(
            nodes,
            edges
        );

        version.setPublished(true);

        WorkflowVersion saved =
            versionRepository.save(version);

        return workflowVersionMapper.toResponse(
            saved
        );
    }
}

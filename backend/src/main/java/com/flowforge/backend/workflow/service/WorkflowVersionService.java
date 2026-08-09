package com.flowforge.backend.workflow.service;

import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.dto.WorkflowVersionResponse;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.repository.WorkflowRepository;
import com.flowforge.backend.workflow.repository.WorkflowVersionRepository;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowVersionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final WorkspaceContext workspaceContext;

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
}

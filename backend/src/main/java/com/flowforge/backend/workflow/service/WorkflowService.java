package com.flowforge.backend.workflow.service;

import com.flowforge.backend.common.exception.DuplicateResourceException;
import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.dto.CreateWorkflowRequest;
import com.flowforge.backend.workflow.dto.WorkflowResponse;
import com.flowforge.backend.workflow.entity.Workflow;
import com.flowforge.backend.workflow.repository.WorkflowRepository;
import com.flowforge.backend.workspace.entity.Workspace;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkspaceContext workspaceContext;

    @Transactional
    public WorkflowResponse create(
        UUID workspaceId,
        UUID userId,
        CreateWorkflowRequest request
    ) {

        var membership =
            workspaceContext.requireMembership(
                workspaceId,
                userId
            );

        String name = request.name().trim();

        if (workflowRepository.existsByWorkspaceIdAndName(
            workspaceId,
            name
        )) {
            throw new DuplicateResourceException(
                "A workflow with this name already exists"
            );
        }

        Workspace workspace = membership.getWorkspace();

        Workflow workflow = new Workflow();
        workflow.setWorkspace(workspace);
        workflow.setName(name);
        workflow.setDescription(request.description());
        workflow.setStatus(
            com.flowforge.backend.workflow.entity.WorkflowStatus.DRAFT
        );

        return toResponse(
            workflowRepository.save(workflow)
        );
    }

    @Transactional(readOnly = true)
    public List<WorkflowResponse> getWorkflows(
        UUID workspaceId,
        UUID userId
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        return workflowRepository
            .findAllByWorkspaceId(workspaceId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(
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

        return toResponse(workflow);
    }

    private WorkflowResponse toResponse(
        Workflow workflow
    ) {

        return new WorkflowResponse(
            workflow.getId(),
            workflow.getWorkspace().getId(),
            workflow.getName(),
            workflow.getDescription(),
            workflow.getStatus(),
            workflow.getCreatedAt(),
            workflow.getUpdatedAt()
        );
    }
}

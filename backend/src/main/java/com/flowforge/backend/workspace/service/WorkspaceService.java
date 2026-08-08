package com.flowforge.backend.workspace.service;

import com.flowforge.backend.auth.entity.User;
import com.flowforge.backend.auth.repository.UserRepository;
import com.flowforge.backend.common.exception.DuplicateResourceException;
import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workspace.dto.CreateWorkspaceRequest;
import com.flowforge.backend.workspace.dto.WorkspaceResponse;
import com.flowforge.backend.workspace.entity.Workspace;
import com.flowforge.backend.workspace.entity.WorkspaceMember;
import com.flowforge.backend.workspace.entity.WorkspaceRole;
import com.flowforge.backend.workspace.repository.WorkspaceMemberRepository;
import com.flowforge.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final WorkspaceContext workspaceContext;

    @Transactional
    public WorkspaceResponse create(
        UUID userId,
        CreateWorkspaceRequest request
    ) {

        String slug = generateSlug(request.name());

        if (workspaceRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException(
                "A workspace with this name already exists"
            );
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "User not found"
                )
            );

        Workspace workspace = new Workspace();

        workspace.setName(request.name().trim());
        workspace.setSlug(slug);
        workspace.setActive(true);

        Workspace savedWorkspace =
            workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();

        member.setWorkspace(savedWorkspace);
        member.setUser(user);
        member.setRole(WorkspaceRole.OWNER);
        member.setActive(true);

        memberRepository.save(member);

        return new WorkspaceResponse(
            savedWorkspace.getId(),
            savedWorkspace.getName(),
            savedWorkspace.getSlug(),
            WorkspaceRole.OWNER
        );
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces(
        UUID userId
    ) {

        return memberRepository
            .findAllByUserIdAndActiveTrue(userId)
            .stream()
            .map(member ->
                new WorkspaceResponse(
                    member.getWorkspace().getId(),
                    member.getWorkspace().getName(),
                    member.getWorkspace().getSlug(),
                    member.getRole()
                )
            )
            .toList();
    }

    private String generateSlug(String name) {

        return name
            .trim()
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(
        UUID workspaceId,
        UUID userId
    ) {

        WorkspaceMember member =
            workspaceContext.requireMembership(
                workspaceId,
                userId
            );

        Workspace workspace = member.getWorkspace();

        return new WorkspaceResponse(
            workspace.getId(),
            workspace.getName(),
            workspace.getSlug(),
            member.getRole()
        );
    }
}

package com.flowforge.backend.workspace.service;

import com.flowforge.backend.workspace.entity.WorkspaceMember;
import com.flowforge.backend.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkspaceContext {

    private final WorkspaceMemberRepository memberRepository;

    public WorkspaceMember requireMembership(
        UUID workspaceId,
        UUID userId
    ) {

        return memberRepository
            .findByWorkspaceIdAndUserIdAndActiveTrue(
                workspaceId,
                userId
            )
            .orElseThrow(
                WorkspaceAccessDeniedException::new
            );
    }
}

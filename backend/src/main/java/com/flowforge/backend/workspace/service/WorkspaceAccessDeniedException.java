package com.flowforge.backend.workspace.service;

public class WorkspaceAccessDeniedException
    extends RuntimeException {

    public WorkspaceAccessDeniedException() {
        super("You do not have access to this workspace");
    }
}

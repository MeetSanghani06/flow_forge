package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowNode;

public interface NodeExecutor {

    boolean supports(WorkflowNode node);

    void execute(
        WorkflowNode node,
        WorkflowExecutionContext context
    );
}

package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;

public interface NodeExecutor {

    boolean supports(WorkflowNode node);

    NodeExecutionResult execute(
        WorkflowNode node,
        WorkflowExecutionContext context
    );
}

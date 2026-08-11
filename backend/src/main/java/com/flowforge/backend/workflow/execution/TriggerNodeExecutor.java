package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.NodeType;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TriggerNodeExecutor
    implements NodeExecutor {

    @Override
    public boolean supports(
        WorkflowNode node
    ) {
        return node.getType() == NodeType.TRIGGER;
    }

    @Override
    public NodeExecutionResult execute(
        WorkflowNode node,
        WorkflowExecutionContext context
    ) {

        log.info(
            "TRIGGER_NODE_EXECUTED | key={}",
            node.getNodeKey()
        );

        return NodeExecutionResult.empty();
    }
}

package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TriggerNodeExecutor implements NodeExecutor {

    @Override
    public boolean supports(WorkflowNode node) {
        return "TRIGGER".equals(node.getType().name());
    }

    @Override
    public void execute(
        WorkflowNode node,
        WorkflowExecutionContext context
    ) {

        log.info(
            "TRIGGER_NODE_EXECUTED | key={}",
            node.getNodeKey()
        );
    }
}

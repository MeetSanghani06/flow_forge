package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingNodeExecutor implements NodeExecutor {

    @Override
    public boolean supports(WorkflowNode node) {
        return true;
    }

    @Override
    public void execute(
        WorkflowNode node,
        WorkflowExecutionContext context
    ) {

        log.info(
            "Executing node: key={}, type={}",
            node.getNodeKey(),
            node.getType()
        );
    }
}

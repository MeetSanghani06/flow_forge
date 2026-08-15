package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiPromptNodeExecutor implements NodeExecutor {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ExpressionResolver expressionResolver;

    @Override
    public boolean supports(WorkflowNode node) {
        return "AI_PROMPT".equals(node.getType().name());
    }

    @Override
    public NodeExecutionResult execute(
        WorkflowNode node,
        WorkflowExecutionContext context
    ) {

        try {

            JsonNode configuration =
                objectMapper.readTree(
                    node.getConfiguration()
                );

            String prompt =
                configuration
                    .path("prompt")
                    .asText();

            prompt =
                expressionResolver.resolve(
                    prompt,
                    context
                );

            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException(
                    "AI prompt node requires a prompt"
                );
            }

            log.info(
                "AI_NODE_START | key={}",
                node.getNodeKey()
            );

            String response =
                chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null) {
                response = "";
            }

            log.info(
                "AI_NODE_SUCCESS | key={}",
                node.getNodeKey()
            );

            return NodeExecutionResult.of(response);

        } catch (Exception exception) {

            log.error(
                "AI_NODE_FAILED | key={} | error={}",
                node.getNodeKey(),
                exception.getMessage(),
                exception
            );

            throw new IllegalStateException(
                "AI prompt failed for node "
                    + node.getNodeKey(),
                exception
            );
        }
    }
}

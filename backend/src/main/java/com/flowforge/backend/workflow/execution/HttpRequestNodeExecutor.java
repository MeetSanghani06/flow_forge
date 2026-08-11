package com.flowforge.backend.workflow.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpRequestNodeExecutor implements NodeExecutor {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Override
    public boolean supports(WorkflowNode node) {
        return "HTTP_REQUEST".equals(node.getType().name());
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

            String method =
                configuration
                    .path("method")
                    .asText("GET");

            String url =
                configuration
                    .path("url")
                    .asText();

            if (url.isBlank()) {
                throw new IllegalArgumentException(
                    "HTTP request node requires a URL"
                );
            }

            log.info(
                "HTTP_NODE_START | key={} | method={} | url={}",
                node.getNodeKey(),
                method,
                url
            );

            String response;

            if ("POST".equalsIgnoreCase(method)) {

                JsonNode body =
                    configuration.path("body");

                response =
                    restClient.post()
                        .uri(url)
                        .body(
                            body.isMissingNode()
                                ? "{}"
                                : body.toString()
                        )
                        .retrieve()
                        .body(String.class);

            } else {

                response =
                    restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(String.class);
            }

            context.put(
                node.getNodeKey() + ".response",
                response
            );

            log.info(
                "HTTP_NODE_SUCCESS | key={}",
                node.getNodeKey()
            );

            return NodeExecutionResult.of(response);

        } catch (Exception exception) {

            log.error(
                "HTTP_NODE_FAILED | key={} | error={}",
                node.getNodeKey(),
                exception.getMessage(),
                exception
            );

            throw new IllegalStateException(
                "HTTP request failed for node "
                    + node.getNodeKey(),
                exception
            );
        }
    }
}

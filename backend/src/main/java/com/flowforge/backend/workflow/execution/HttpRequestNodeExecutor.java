package com.flowforge.backend.workflow.execution;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.execution.dto.NodeExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpRequestNodeExecutor
    implements NodeExecutor {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final ExpressionResolver expressionResolver;

    @Override
    public boolean supports(
        WorkflowNode node
    ) {

        return "HTTP_REQUEST".equals(
            node.getType().name()
        );
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
                expressionResolver.resolve(
                    configuration
                        .path("url")
                        .asText(),
                    context
                );

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
                    expressionResolver.resolveJson(
                        configuration.path("body"),
                        context
                    );

                response =
                    restClient.post()
                        .uri(url)
                        .body(
                            body == null ||
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

            /*
             * Store the response as JSON when possible.
             *
             * This allows downstream nodes to use:
             *
             * {{ nodes.fetch_data.id }}
             */
            JsonNode responseNode =
                objectMapper.readTree(response);

            context.putNodeOutput(
                node.getNodeKey(),
                responseNode
            );

            log.info(
                "HTTP_NODE_SUCCESS | key={}",
                node.getNodeKey()
            );

            return NodeExecutionResult.of(
                response
            );

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

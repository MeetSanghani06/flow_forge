package com.flowforge.backend.workflow.execution;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ExpressionResolver {

    private static final Pattern EXPRESSION =
        Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");

    private final ObjectMapper objectMapper;

    /**
     * Resolves expressions inside a String.
     *
     * Examples:
     *
     * "{{ input.userId }}"
     *       -> "123"
     *
     * "https://api.com/users/{{ input.userId }}"
     *       -> "https://api.com/users/123"
     */
    public String resolve(
        String template,
        WorkflowExecutionContext context
    ) {

        if (template == null || template.isBlank()) {
            return template;
        }

        Matcher matcher =
            EXPRESSION.matcher(template);

        StringBuffer result =
            new StringBuffer();

        while (matcher.find()) {

            String expression =
                matcher.group(1).trim();

            JsonNode value =
                resolveExpressionAsJson(
                    expression,
                    context
                );

            String replacement =
                value == null || value.isNull()
                    ? ""
                    : value.isTextual()
                      ? value.asText()
                      : value.toString();

            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(
                    replacement
                )
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Recursively resolves expressions inside a JSON tree.
     *
     * Entire expression:
     *
     * {
     *   "userId": "{{ input.userId }}"
     * }
     *
     * becomes:
     *
     * {
     *   "userId": 123
     * }
     *
     * Embedded expression:
     *
     * {
     *   "url": "https://api.com/users/{{ input.userId }}"
     * }
     *
     * remains a String.
     */
    public JsonNode resolveJson(
        JsonNode node,
        WorkflowExecutionContext context
    ) {

        if (node == null || node.isNull()) {
            return node;
        }

        if (node.isTextual()) {

            return resolveTextNode(
                node.asText(),
                context
            );
        }

        if (node.isObject()) {

            ObjectNode resolved =
                objectMapper.createObjectNode();

            node.forEachEntry((key, value) -> {
                resolved.set(
                    key,
                    resolveJson(value, context)
                );
            });

            return resolved;
        }

        if (node.isArray()) {

            ArrayNode resolved =
                objectMapper.createArrayNode();

            for (JsonNode element : node) {

                resolved.add(
                    resolveJson(
                        element,
                        context
                    )
                );
            }

            return resolved;
        }

        // Numbers, booleans, etc. require no resolution.
        return node;
    }

    private JsonNode resolveTextNode(
        String value,
        WorkflowExecutionContext context
    ) {

        Matcher matcher =
            EXPRESSION.matcher(value);

        /*
         * Entire value is an expression.
         *
         * "{{ input.userId }}"
         *
         * If userId = 123,
         * return NumericNode(123), not TextNode("123").
         */
        if (matcher.matches()) {

            String expression =
                matcher.group(1).trim();

            JsonNode resolved =
                resolveExpressionAsJson(
                    expression,
                    context
                );

            return resolved == null
                ? objectMapper.valueToTree("")
                : resolved;
        }

        /*
         * Embedded expression.
         *
         * "https://api.com/users/{{ input.userId }}"
         */
        return objectMapper.valueToTree(
            resolve(value, context)
        );
    }

    /**
     * Resolves expressions such as:
     *
     * input.userId
     * input.user.id
     * nodes.get_user.name
     * nodes.get_user.address.city
     * nodes.fetch_data.response.id
     */
    private JsonNode resolveExpressionAsJson(
        String expression,
        WorkflowExecutionContext context
    ) {

        String[] parts =
            expression.split("\\.");

        if (parts.length < 2) {

            throw new IllegalArgumentException(
                "Invalid expression: " + expression
            );
        }

        return switch (parts[0]) {

            case "input" ->
                resolvePath(
                    context.getInput(),
                    parts,
                    1,
                    expression
                );

            case "nodes" ->
                resolvePath(
                    context.getNodeOutputs(),
                    parts,
                    1,
                    expression
                );

            default ->
                throw new IllegalArgumentException(
                    "Unknown expression root: "
                        + parts[0]
                );
        };
    }

    /**
     * Resolves a property path against Map<String, Object>.
     *
     * The execution context intentionally remains
     * Map<String, Object>.
     *
     * Each value is converted to JsonNode before
     * traversing nested properties.
     */
    private JsonNode resolvePath(
        Map<String, JsonNode> source,
        String[] parts,
        int startIndex,
        String expression
    ) {

        Object rawValue =
            source.get(parts[startIndex]);

        if (rawValue == null) {

            throw new IllegalArgumentException(
                "Expression value not found: "
                    + expression
            );
        }

        JsonNode current =
            toJsonNode(rawValue);

        for (
            int index = startIndex + 1;
            index < parts.length;
            index++
        ) {

            String property =
                parts[index];

            if (!current.isObject()) {

                throw new IllegalArgumentException(
                    "Cannot resolve property '"
                        + property
                        + "' in expression: "
                        + expression
                );
            }

            current =
                current.get(property);

            if (current == null ||
                current.isNull()) {

                throw new IllegalArgumentException(
                    "Expression value not found: "
                        + expression
                );
            }
        }

        return current;
    }

    /**
     * Converts execution-context values into JsonNode.
     */
    private JsonNode toJsonNode(
        Object value
    ) {

        if (value instanceof JsonNode jsonNode) {
            return jsonNode;
        }

        return objectMapper.valueToTree(value);
    }
}

package com.flowforge.backend.workflow.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
     * Example:
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
     * Resolves expressions recursively inside a JSON tree.
     *
     * Entire expression:
     *
     * {
     *   "userId": "{{ input.userId }}"
     * }
     *
     * preserves the original type:
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

            Iterator<Map.Entry<String, JsonNode>> fields =
                node.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> field =
                    fields.next();

                resolved.set(
                    field.getKey(),
                    resolveJson(
                        field.getValue(),
                        context
                    )
                );
            }

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

        /*
         * Numbers, booleans, etc.
         * are already concrete values.
         */
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
         * Example:
         *
         * "{{ input.userId }}"
         *
         * If userId = 123,
         * return NumericNode(123), NOT TextNode("123").
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
                ? TextNode.valueOf("")
                : resolved;
        }

        /*
         * Expression embedded inside text.
         *
         * Example:
         *
         * "https://api.com/{{ input.userId }}"
         */
        return TextNode.valueOf(
            resolve(
                value,
                context
            )
        );
    }

    /**
     * Resolves:
     *
     * input.userId
     * input.user.id
     * nodes.fetch_data.response
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
     * Resolves a path against Map<String, Object>.
     *
     * We convert each Object to JsonNode as we traverse it.
     *
     * This keeps WorkflowExecutionContext flexible while
     * allowing the expression engine to work consistently
     * with JSON values.
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
     * Converts arbitrary execution-context values
     * into JsonNode.
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

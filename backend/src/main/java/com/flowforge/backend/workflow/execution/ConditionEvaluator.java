package com.flowforge.backend.workflow.execution;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConditionEvaluator {

    private final ExpressionResolver expressionResolver;
    private final ObjectMapper objectMapper;

    public boolean evaluate(
        String condition,
        WorkflowExecutionContext context
    ) {

        /*
         * No condition means unconditional edge.
         */
        if (condition == null || condition.isBlank()) {
            return true;
        }

        ParsedCondition parsed =
            parse(condition);

        JsonNode left =
            resolveOperand(
                parsed.left(),
                context
            );

        JsonNode right =
            resolveOperand(
                parsed.right(),
                context
            );

        boolean equal =
            left == null
                ? right == null
                : left.equals(right);

        return switch (parsed.operator()) {

            case "==" -> equal;

            case "!=" -> !equal;

            default ->
                throw new IllegalArgumentException(
                    "Unsupported condition operator: "
                        + parsed.operator()
                );
        };
    }

    private JsonNode resolveOperand(
        String operand,
        WorkflowExecutionContext context
    ) {

        String value =
            operand.trim();

        /*
         * Expression:
         *
         * {{ input.userId }}
         *
         * or
         *
         * {{ nodes.get_user.id }}
         */
        if (value.startsWith("{{")
            && value.endsWith("}}")) {

            return expressionResolver.resolveJson(
                objectMapper.valueToTree(value),
                context
            );
        }

        /*
         * Quoted string.
         *
         * "Leanne Graham"
         */
        if (
            (value.startsWith("\"")
                && value.endsWith("\""))
                ||
                (value.startsWith("'")
                    && value.endsWith("'"))
        ) {

            return objectMapper.valueToTree(
                value.substring(
                    1,
                    value.length() - 1
                )
            );
        }

        /*
         * JSON literals:
         *
         * 1
         * true
         * false
         * null
         * 1.5
         */
        try {
            return objectMapper.readTree(value);

        } catch (Exception exception) {

            /*
             * Treat an unquoted value as a string.
             *
             * Example:
             *
             * status == SUCCESS
             */
            return objectMapper.valueToTree(value);
        }
    }

    private ParsedCondition parse(
        String condition
    ) {

        String trimmed =
            condition.trim();

        int operatorIndex =
            findOperator(
                trimmed,
                "=="
            );

        if (operatorIndex >= 0) {

            return createParsedCondition(
                trimmed,
                operatorIndex,
                "=="
            );
        }

        operatorIndex =
            findOperator(
                trimmed,
                "!="
            );

        if (operatorIndex >= 0) {

            return createParsedCondition(
                trimmed,
                operatorIndex,
                "!="
            );
        }

        throw new IllegalArgumentException(
            "Invalid condition. Supported operators: ==, !="
        );
    }

    private ParsedCondition createParsedCondition(
        String condition,
        int operatorIndex,
        String operator
    ) {

        String left =
            condition
                .substring(
                    0,
                    operatorIndex
                )
                .trim();

        String right =
            condition
                .substring(
                    operatorIndex
                        + operator.length()
                )
                .trim();

        if (left.isBlank() || right.isBlank()) {

            throw new IllegalArgumentException(
                "Invalid condition: "
                    + condition
            );
        }

        return new ParsedCondition(
            left,
            operator,
            right
        );
    }

    private int findOperator(
        String value,
        String operator
    ) {

        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        for (
            int index = 0;
            index <= value.length() - operator.length();
            index++
        ) {

            char current =
                value.charAt(index);

            if (current == '\''
                && !insideDoubleQuote) {

                insideSingleQuote =
                    !insideSingleQuote;
            }

            if (current == '"'
                && !insideSingleQuote) {

                insideDoubleQuote =
                    !insideDoubleQuote;
            }

            if (
                !insideSingleQuote
                    && !insideDoubleQuote
                    && value.startsWith(
                    operator,
                    index
                )
            ) {

                return index;
            }
        }

        return -1;
    }

    private record ParsedCondition(
        String left,
        String operator,
        String right
    ) {
    }
}

package com.flowforge.backend.workflow.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class WorkflowExecutionContext {

    private final ObjectMapper objectMapper;

    private final Map<String, JsonNode> input =
        new HashMap<>();

    private final Map<String, JsonNode> nodeOutputs =
        new HashMap<>();

    public void putInput(
        String key,
        Object value
    ) {
        input.put(
            key,
            objectMapper.valueToTree(value)
        );
    }

    public void putInputAll(
        Map<String, Object> values
    ) {

        if (values == null) {
            return;
        }

        values.forEach(
            (key, value) ->
                input.put(
                    key,
                    objectMapper.valueToTree(value)
                )
        );
    }

    public void putNodeOutput(
        String nodeKey,
        JsonNode output
    ) {

        nodeOutputs.put(
            nodeKey,
            output
        );
    }

    public JsonNode getInput(
        String key
    ) {
        return input.get(key);
    }

    public JsonNode getNodeOutput(
        String nodeKey
    ) {
        return nodeOutputs.get(nodeKey);
    }

    public Map<String, JsonNode> getInput() {
        return Collections.unmodifiableMap(input);
    }

    public Map<String, JsonNode> getNodeOutputs() {
        return Collections.unmodifiableMap(nodeOutputs);
    }

    public Map<String, Object> snapshot() {

        Map<String, Object> snapshot =
            new HashMap<>();

        snapshot.put(
            "input",
            new HashMap<>(input)
        );

        snapshot.put(
            "nodes",
            new HashMap<>(nodeOutputs)
        );

        return Collections.unmodifiableMap(
            snapshot
        );
    }
}

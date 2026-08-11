package com.flowforge.backend.workflow.execution;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class WorkflowExecutionContext {

    private final Map<String, Object> input =
        new HashMap<>();

    private final Map<String, Object> nodeOutputs =
        new HashMap<>();

    public void putInput(
        String key,
        Object value
    ) {
        input.put(key, value);
    }

    public void putInputAll(
        Map<String, Object> values
    ) {
        if (values != null) {
            input.putAll(values);
        }
    }

    public void putNodeOutput(
        String nodeKey,
        Object output
    ) {
        nodeOutputs.put(
            nodeKey,
            output
        );
    }

    public Object getInput(String key) {
        return input.get(key);
    }

    public Object getNodeOutput(String nodeKey) {
        return nodeOutputs.get(nodeKey);
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

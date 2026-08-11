package com.flowforge.backend.workflow.execution;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class WorkflowExecutionContext {

    private final Map<String, Object> variables =
        new HashMap<>();

    public void put(
        String key,
        Object value
    ) {
        variables.put(key, value);
    }

    public Object get(String key) {
        return variables.get(key);
    }

    public <T> T get(
        String key,
        Class<T> type
    ) {
        Object value = variables.get(key);

        if (value == null) {
            return null;
        }

        return type.cast(value);
    }

    public boolean contains(String key) {
        return variables.containsKey(key);
    }

    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(
            new HashMap<>(variables)
        );
    }
}

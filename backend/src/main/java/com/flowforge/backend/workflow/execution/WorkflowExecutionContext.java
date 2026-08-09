package com.flowforge.backend.workflow.execution;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class WorkflowExecutionContext {

    @Builder.Default
    private final Map<String, Object> variables = new HashMap<>();

    public void set(String key, Object value) {
        variables.put(key, value);
    }

    public Object get(String key) {
        return variables.get(key);
    }
}

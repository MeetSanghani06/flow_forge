package com.flowforge.backend.common.exception;

public class WorkflowRateLimitExceededException extends RuntimeException {
    public WorkflowRateLimitExceededException() {

        super(
            "Workflow execution rate limit exceeded. "
                + "Please try again later."
        );
    }
}

package com.flowforge.backend.workflow.execution.mapper;

import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionResponse;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowExecutionMapper {

    @Mapping(
        target = "workflowVersionId",
        source = "workflowVersion.id"
    )
    WorkflowExecutionResponse toResponse(
        WorkflowExecution execution
    );
}

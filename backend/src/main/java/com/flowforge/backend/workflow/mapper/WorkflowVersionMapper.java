package com.flowforge.backend.workflow.mapper;

import com.flowforge.backend.workflow.dto.WorkflowVersionResponse;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkflowVersionMapper {

    WorkflowVersionResponse toResponse(
        WorkflowVersion version
    );
}

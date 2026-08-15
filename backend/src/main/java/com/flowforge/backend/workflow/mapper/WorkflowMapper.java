package com.flowforge.backend.workflow.mapper;

import com.flowforge.backend.workflow.dto.WorkflowResponse;
import com.flowforge.backend.workflow.entity.Workflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkflowMapper {

    @Mapping(
        target = "workspaceId",
        source = "workspace.id"
    )
    @Mapping(
        target = "activeVersionId",
        source = "activeVersion.id"
    )
    WorkflowResponse toResponse(Workflow workflow);

    List<WorkflowResponse> toResponseList(List<Workflow> workflow);
}

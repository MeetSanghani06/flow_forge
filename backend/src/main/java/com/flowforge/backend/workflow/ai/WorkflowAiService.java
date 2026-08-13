package com.flowforge.backend.workflow.ai;

import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.ai.dto.GenerateWorkflowRequest;
import com.flowforge.backend.workflow.ai.dto.GenerateWorkflowResponse;
import com.flowforge.backend.workflow.ai.dto.ModifyWorkflowRequest;
import com.flowforge.backend.workflow.dto.SaveWorkflowGraphRequest;
import com.flowforge.backend.workflow.dto.WorkflowGraphResponse;
import com.flowforge.backend.workflow.service.WorkflowGraphService;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.repository.WorkflowVersionRepository;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import com.flowforge.backend.workflow.ai.prompt.WorkflowGenerationPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowAiService {

    private final ChatClient chatClient;
    private final WorkflowGraphService workflowGraphService;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkspaceContext workspaceContext;
    private final ObjectMapper objectMapper;

    @Transactional
    public GenerateWorkflowResponse generateWorkflow(
        UUID workspaceId,
        UUID workflowId,
        int versionNumber,
        UUID userId,
        GenerateWorkflowRequest request
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        WorkflowVersion version =
            workflowVersionRepository
                .findByWorkflowIdAndVersionNumber(
                    workflowId,
                    versionNumber
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow version not found"
                    )
                );

        if (version.isPublished()) {
            throw new IllegalStateException(
                "Cannot generate AI workflow into a published version"
            );
        }

        SaveWorkflowGraphRequest graph =
            chatClient
                .prompt()
                .system(
                    WorkflowGenerationPrompt.SYSTEM_PROMPT
                )
                .user(request.prompt())
                .call()
                .entity(
                    SaveWorkflowGraphRequest.class
                );

        if (graph == null) {
            throw new IllegalStateException(
                "AI failed to generate a workflow graph"
            );
        }

        WorkflowGraphResponse savedGraph =
            workflowGraphService.saveGraph(
                workspaceId,
                workflowId,
                versionNumber,
                userId,
                graph
            );

        return new GenerateWorkflowResponse(
            "Workflow generated successfully",
            savedGraph
        );
    }

    @Transactional
    public GenerateWorkflowResponse modifyWorkflow(
        UUID workspaceId,
        UUID workflowId,
        int versionNumber,
        UUID userId,
        ModifyWorkflowRequest request
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        WorkflowVersion version =
            workflowVersionRepository
                .findByWorkflowIdAndVersionNumber(
                    workflowId,
                    versionNumber
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Workflow version not found"
                    )
                );

        if (version.isPublished()) {
            throw new IllegalStateException(
                "Cannot modify a published workflow version"
            );
        }

        WorkflowGraphResponse currentGraph =
            workflowGraphService.getGraph(
                workspaceId,
                workflowId,
                versionNumber,
                userId
            );

        String currentGraphJson;

        try {
            currentGraphJson =
                objectMapper.writeValueAsString(currentGraph);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Failed to serialize current workflow graph",
                exception
            );
        }

        String userPrompt = """
        Here is the current FlowForge workflow:

        %s

        Modify this workflow according to the following instruction:

        %s

        Return ONLY the complete modified workflow JSON.

        Preserve existing nodes and edges unless the requested
        modification requires changing them.

        The returned JSON must match:

        {
          "nodes": [
            {
              "nodeKey": "string",
              "name": "string",
              "type": "TRIGGER | HTTP_REQUEST",
              "connectorId": null,
              "configuration": {}
            }
          ],
          "edges": [
            {
              "source": "string",
              "target": "string",
              "condition": null
            }
          ]
        }
        """.formatted(
            currentGraphJson,
            request.instruction()
        );

        SaveWorkflowGraphRequest modifiedGraph =
            chatClient
                .prompt()
                .system(
                    WorkflowGenerationPrompt.SYSTEM_PROMPT
                        + """

                    You are modifying an existing workflow.

                    IMPORTANT:
                    - Preserve the trigger unless explicitly asked to change it.
                    - Preserve existing node keys whenever possible.
                    - Do not introduce unsupported node types.
                    - Do not create cycles.
                    - Return only valid JSON.
                    """
                )
                .user(userPrompt)
                .call()
                .entity(SaveWorkflowGraphRequest.class);

        if (modifiedGraph == null) {
            throw new IllegalStateException(
                "AI failed to modify workflow"
            );
        }

        WorkflowGraphResponse savedGraph =
            workflowGraphService.saveGraph(
                workspaceId,
                workflowId,
                versionNumber,
                userId,
                modifiedGraph
            );

        return new GenerateWorkflowResponse(
            "Workflow modified successfully",
            savedGraph
        );
    }
}

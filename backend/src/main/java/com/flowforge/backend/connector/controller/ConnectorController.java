package com.flowforge.backend.connector.controller;

import com.flowforge.backend.common.response.ApiResponse;
import com.flowforge.backend.connector.dto.ConnectorResponse;
import com.flowforge.backend.connector.dto.CreateConnectorRequest;
import com.flowforge.backend.connector.service.ConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/connectors")
@RequiredArgsConstructor
public class ConnectorController {

    private final ConnectorService connectorService;

    @PostMapping
    public ApiResponse<ConnectorResponse> create(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @Valid @RequestBody CreateConnectorRequest request
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            connectorService.create(
                workspaceId,
                userId,
                request
            )
        );
    }

    @GetMapping
    public ApiResponse<List<ConnectorResponse>> getConnectors(
        Authentication authentication,
        @PathVariable UUID workspaceId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            connectorService.getConnectors(
                workspaceId,
                userId
            )
        );
    }

    @GetMapping("/{connectorId}")
    public ApiResponse<ConnectorResponse> getConnector(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @PathVariable UUID connectorId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            connectorService.getConnector(
                workspaceId,
                connectorId,
                userId
            )
        );
    }
}

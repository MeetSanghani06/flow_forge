package com.flowforge.backend.connector.service;

import com.flowforge.backend.common.exception.DuplicateResourceException;
import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.connector.dto.ConnectorResponse;
import com.flowforge.backend.connector.dto.CreateConnectorRequest;
import com.flowforge.backend.connector.entity.Connector;
import com.flowforge.backend.connector.repository.ConnectorRepository;
import com.flowforge.backend.workspace.entity.Workspace;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final WorkspaceContext workspaceContext;

    @Transactional
    public ConnectorResponse create(
        UUID workspaceId,
        UUID userId,
        CreateConnectorRequest request
    ) {

        var membership =
            workspaceContext.requireMembership(
                workspaceId,
                userId
            );

        if (connectorRepository.existsByWorkspaceIdAndName(
            workspaceId,
            request.name().trim()
        )) {
            throw new DuplicateResourceException(
                "A connector with this name already exists"
            );
        }

        Workspace workspace = membership.getWorkspace();

        Connector connector = new Connector();

        connector.setWorkspace(workspace);
        connector.setName(request.name().trim());
        connector.setType(request.type());
        connector.setActive(true);

        Connector saved =
            connectorRepository.save(connector);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ConnectorResponse> getConnectors(
        UUID workspaceId,
        UUID userId
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        return connectorRepository
            .findAllByWorkspaceIdAndActiveTrue(workspaceId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ConnectorResponse getConnector(
        UUID workspaceId,
        UUID connectorId,
        UUID userId
    ) {

        workspaceContext.requireMembership(
            workspaceId,
            userId
        );

        Connector connector =
            connectorRepository
                .findByIdAndWorkspaceId(
                    connectorId,
                    workspaceId
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Connector not found"
                    )
                );

        return toResponse(connector);
    }

    private ConnectorResponse toResponse(
        Connector connector
    ) {

        return new ConnectorResponse(
            connector.getId(),
            connector.getWorkspace().getId(),
            connector.getName(),
            connector.getType(),
            connector.isActive()
        );
    }
}

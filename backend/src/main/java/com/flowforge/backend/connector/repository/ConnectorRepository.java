package com.flowforge.backend.connector.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.connector.entity.Connector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectorRepository
    extends BaseRepository<Connector> {

    List<Connector> findAllByWorkspaceIdAndActiveTrue(
        UUID workspaceId
    );

    Optional<Connector> findByIdAndWorkspaceId(
        UUID connectorId,
        UUID workspaceId
    );

    boolean existsByWorkspaceIdAndName(
        UUID workspaceId,
        String name
    );
}

CREATE TABLE workspaces
(
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    slug VARCHAR(100) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_workspaces_slug
        UNIQUE (slug)
);

CREATE TABLE workspace_members
(
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,

    user_id UUID NOT NULL,

    role VARCHAR(20) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_workspace_member
        UNIQUE (workspace_id, user_id),

    CONSTRAINT fk_workspace_member_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workspace_member_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_workspace_member_role
        CHECK (
            role IN (
                     'OWNER',
                     'ADMIN',
                     'MEMBER',
                     'VIEWER'
                )
            )
);

CREATE INDEX idx_workspace_members_user_id
    ON workspace_members(user_id);

CREATE INDEX idx_workspace_members_workspace_id
    ON workspace_members(workspace_id);

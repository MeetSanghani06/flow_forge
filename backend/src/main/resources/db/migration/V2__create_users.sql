CREATE TABLE users
(
    id UUID PRIMARY KEY,

    email VARCHAR(320) NOT NULL,

    password_hash VARCHAR(100) NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    role VARCHAR(20) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_email
    ON users (email);

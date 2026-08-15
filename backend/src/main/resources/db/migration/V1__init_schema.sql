CREATE TABLE system_info
(
    id UUID PRIMARY KEY,

    application_name VARCHAR(100) NOT NULL,

    version VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

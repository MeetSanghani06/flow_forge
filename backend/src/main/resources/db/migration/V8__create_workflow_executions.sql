CREATE TABLE workflow_executions (
                                     id UUID PRIMARY KEY,
                                     workflow_version_id UUID NOT NULL,
                                     status VARCHAR(30) NOT NULL,
                                     started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     completed_at TIMESTAMP WITH TIME ZONE,
                                     input TEXT,
                                     output TEXT,
                                     error_message TEXT,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                     CONSTRAINT fk_workflow_execution_version
                                         FOREIGN KEY (workflow_version_id)
                                             REFERENCES workflow_versions(id)
);

CREATE INDEX idx_workflow_executions_version_id
    ON workflow_executions(workflow_version_id);

CREATE INDEX idx_workflow_executions_status
    ON workflow_executions(status);

CREATE INDEX idx_workflow_executions_started_at
    ON workflow_executions(started_at);


CREATE TABLE workflow_node_executions (
                                          id UUID PRIMARY KEY,
                                          workflow_execution_id UUID NOT NULL,
                                          workflow_node_id UUID NOT NULL,
                                          status VARCHAR(30) NOT NULL,
                                          started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          completed_at TIMESTAMP WITH TIME ZONE,
                                          input TEXT,
                                          output TEXT,
                                          error_message TEXT,
                                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                          CONSTRAINT fk_node_execution_execution
                                              FOREIGN KEY (workflow_execution_id)
                                                  REFERENCES workflow_executions(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_node_execution_node
                                              FOREIGN KEY (workflow_node_id)
                                                  REFERENCES workflow_nodes(id)
);

CREATE INDEX idx_node_executions_execution_id
    ON workflow_node_executions(workflow_execution_id);

CREATE INDEX idx_node_executions_node_id
    ON workflow_node_executions(workflow_node_id);

CREATE INDEX idx_node_executions_status
    ON workflow_node_executions(status);

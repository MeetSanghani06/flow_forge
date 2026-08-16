// Shared backend DTO types for the FlowForge API.

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  timestamp?: string;
}

export interface ApiErrorItem {
  code?: string;
  message?: string;
  field?: string;
}

export interface ApiErrorResponse {
  success: false;
  timestamp?: string;
  errors?: ApiErrorItem[];
  message?: string;
  error?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
}

/** POST /api/v1/auth/register — no tokens are returned. */
export interface RegisteredUser {
  id: string;
  email: string;
}

export interface Workspace {
  id: string;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export type WorkflowStatus = string;

export interface Workflow {
  id: string;
  name: string;
  description?: string | null;
  status?: WorkflowStatus;
  workspaceId?: string;
  currentVersionId?: string | null;
  latestVersionId?: string | null;
  currentVersion?: number | null;
  version?: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkflowVersion {
  id: string;
  workflowId?: string;
  version?: number;
  versionNumber?: number;
  status?: string;
  published?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkflowNodeDto {
  id: string;
  nodeKey: string;
  name: string;
  type: string;
  configuration: Record<string, unknown> | string | null;
  connectorId?: string | null;
}

export interface WorkflowEdgeDto {
  id: string;
  source: string;
  target: string;
  condition?: string | null;
}

export interface WorkflowGraphDto {
  workflowVersionId: string;
  versionNumber: number;
  nodes: WorkflowNodeDto[];
  edges: WorkflowEdgeDto[];
}

export type WorkflowExecutionStatus = "QUEUED" | "RUNNING" | "SUCCESS" | "FAILED" | string;

export interface WorkflowExecution {
  id: string;
  executionId?: string;
  workflowId?: string;
  workflowVersionId?: string;
  version?: number;
  status: WorkflowExecutionStatus;
  startedAt?: string | null;
  completedAt?: string | null;
  input?: unknown;
  output?: unknown;
  errorMessage?: string | null;
}

/** GET /api/v1/executions/{executionId}/nodes */
export interface NodeExecution {
  id?: string;
  executionId?: string;
  nodeId?: string;
  nodeKey?: string;
  nodeName?: string;
  name?: string;
  nodeType?: string;
  type?: string;
  status: WorkflowExecutionStatus;
  attempt?: number;
  sequence?: number;
  startedAt?: string | null;
  completedAt?: string | null;
  input?: unknown;
  output?: unknown;
  errorMessage?: string | null;
}

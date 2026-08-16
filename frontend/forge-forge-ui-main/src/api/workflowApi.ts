import { apiClient, unwrap, asArray } from "@/lib/apiClient";
import type {
  Workflow,
  WorkflowGraphDto,
} from "@/types/api";

export async function listWorkflows(
  workspaceId: string,
): Promise<Workflow[]> {
  const response = await apiClient.get(
    `/api/v1/workspaces/${workspaceId}/workflows`,
  );

  return asArray<Workflow>(unwrap<unknown>(response));
}

export async function getWorkflow(
  workspaceId: string,
  workflowId: string,
): Promise<Workflow> {
  const response = await apiClient.get(
    `/api/v1/workspaces/${workspaceId}/workflows/${workflowId}`,
  );

  return unwrap<Workflow>(response);
}

export interface CreateWorkflowRequest {
  name: string;
  description?: string;
}

export async function createWorkflow(
  workspaceId: string,
  payload: CreateWorkflowRequest,
): Promise<Workflow> {
  const response = await apiClient.post(
    `/api/v1/workspaces/${workspaceId}/workflows`,
    payload,
  );

  return unwrap<Workflow>(response);
}

export async function getGraph(
  workspaceId: string,
  workflowId: string,
  versionNumber: number,
): Promise<WorkflowGraphDto> {
  const response = await apiClient.get(
    `/api/v1/workspaces/${workspaceId}/workflows/${workflowId}/versions/${versionNumber}/graph`,
  );

  return unwrap<WorkflowGraphDto>(response);
}

export interface SaveGraphRequest {
  nodes: Array<{
    nodeKey: string;
    name: string;
    type: string;
    connectorId?: string | null;
    configuration: Record<string, unknown>;
  }>;
  edges: Array<{
    source: string;
    target: string;
    condition?: string | null;
  }>;
}

export async function saveGraph(
  workspaceId: string,
  workflowId: string,
  versionNumber: number,
  payload: SaveGraphRequest,
): Promise<WorkflowGraphDto> {
  const response = await apiClient.put(
    `/api/v1/workspaces/${workspaceId}/workflows/${workflowId}/versions/${versionNumber}/graph`,
    payload,
  );

  return unwrap<WorkflowGraphDto>(response);
}
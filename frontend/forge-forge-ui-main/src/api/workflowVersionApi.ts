// ============================================================================
// WORKFLOW VERSION + GRAPH CONTRACT — adjust endpoints here only.
// ============================================================================
import { apiClient, asArray, unwrap } from "@/lib/apiClient";
import type { WorkflowGraphDto, WorkflowVersion } from "@/types/api";
import { SaveGraphRequest } from "./workflowApi";

const base = (workspaceId: string, workflowId: string) =>
  `/api/v1/workspaces/${workspaceId}/workflows/${workflowId}/versions`;

export async function listVersions(
  workspaceId: string,
  workflowId: string,
): Promise<WorkflowVersion[]> {
  const response = await apiClient.get(base(workspaceId, workflowId));
  return asArray<WorkflowVersion>(unwrap<unknown>(response));
}

export async function createVersion(
  workspaceId: string,
  workflowId: string,
): Promise<WorkflowVersion> {
  const response = await apiClient.post(base(workspaceId, workflowId), {});
  return unwrap<WorkflowVersion>(response);
}

export async function cloneVersion(
  workspaceId: string,
  workflowId: string,
  versionNumber: number,
): Promise<WorkflowVersion> {
  const response = await apiClient.post(
    `${base(workspaceId, workflowId)}/${versionNumber}/clone`,
    {},
  );

  return unwrap<WorkflowVersion>(response);
}

export async function getGraph(
  workspaceId: string,
  workflowId: string,
  versionNumber: number,
): Promise<WorkflowGraphDto> {
  const response = await apiClient.get(
    `${base(workspaceId, workflowId)}/${versionNumber}/graph`,
  );
  return unwrap<WorkflowGraphDto>(response);
}

export async function saveGraph(
  workspaceId: string,
  workflowId: string,
  versionNumber: number,
  graph: SaveGraphRequest,
): Promise<WorkflowGraphDto> {
  const response = await apiClient.put(
    `${base(workspaceId, workflowId)}/${versionNumber}/graph`,
    graph,
  );
  return unwrap<WorkflowGraphDto>(response);
}

export async function publishVersion(
  workspaceId: string,
  workflowId: string,
  versionNumber: number,
): Promise<WorkflowVersion> {
  const response = await apiClient.post(
    `${base(workspaceId, workflowId)}/${versionNumber}/publish`,
    {},
  );
  return unwrap<WorkflowVersion>(response);
}

import { apiClient, asArray, unwrap } from "@/lib/apiClient";
import type { Workspace } from "@/types/api";

export async function listWorkspaces(): Promise<Workspace[]> {
  const response = await apiClient.get("/api/v1/workspaces");
  return asArray<Workspace>(unwrap<unknown>(response));
}

export async function getWorkspace(workspaceId: string): Promise<Workspace> {
  const response = await apiClient.get(`/api/v1/workspaces/${workspaceId}`);
  return unwrap<Workspace>(response);
}

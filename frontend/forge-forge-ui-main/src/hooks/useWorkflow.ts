import { useQuery } from "@tanstack/react-query";

import { getWorkflow } from "@/api/workflowApi";
import { listVersions } from "@/api/workflowVersionApi";
import type { Workflow, WorkflowVersion } from "@/types/api";

export function useWorkflow(workspaceId: string, workflowId: string) {
  return useQuery<Workflow>({
    queryKey: ["workflow", workspaceId, workflowId],
    queryFn: () => getWorkflow(workspaceId, workflowId),
    enabled: Boolean(workspaceId && workflowId),
    retry: false,
  });
}

export function useWorkflowVersions(workspaceId: string, workflowId: string) {
  return useQuery<WorkflowVersion[]>({
    queryKey: ["workflowVersions", workspaceId, workflowId],
    queryFn: () => listVersions(workspaceId, workflowId),
    enabled: Boolean(workspaceId && workflowId),
    retry: false,
  });
}

export function versionNumberOf(version: WorkflowVersion | undefined): number | undefined {
  return version?.versionNumber ?? version?.version ?? undefined;
}

/**
 * Resolves the version to work on: the workflow's current/latest version when the
 * backend exposes it, otherwise the highest-numbered version from the list.
 */
export function resolveActiveVersion(
  workflow: Workflow | undefined,
  versions: WorkflowVersion[] | undefined,
): WorkflowVersion | undefined {
  if (!versions || versions.length === 0) {
    const id = workflow?.currentVersionId ?? workflow?.latestVersionId;
    if (!id) return undefined;
    const num = workflow?.currentVersion ?? workflow?.version;
    return num == null ? { id } : { id, versionNumber: num };
  }
  const preferredId = workflow?.currentVersionId ?? workflow?.latestVersionId;
  const preferred = preferredId ? versions.find((v) => v.id === preferredId) : undefined;
  if (preferred) return preferred;
  return [...versions].sort(
    (a, b) => (versionNumberOf(b) ?? 0) - (versionNumberOf(a) ?? 0),
  )[0];
}

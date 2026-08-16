import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { createWorkflow, listWorkflows, type CreateWorkflowRequest } from "@/api/workflowApi";
import type { Workflow } from "@/types/api";

export function useWorkflows(workspaceId: string | null) {
  return useQuery<Workflow[]>({
    queryKey: ["workflows", workspaceId],
    queryFn: () => listWorkflows(workspaceId as string),
    enabled: Boolean(workspaceId),
    retry: false,
  });
}

export function useCreateWorkflow(workspaceId: string | null) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateWorkflowRequest) =>
      createWorkflow(workspaceId as string, payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["workflows", workspaceId] });
    },
  });
}

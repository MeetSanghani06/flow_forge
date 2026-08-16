import { useMutation, useQuery } from "@tanstack/react-query";

import {
  executeWorkflowVersion,
  getExecution,
  getExecutionNodes,
  type ExecuteWorkflowArgs,
} from "@/api/executionApi";
import type { NodeExecution, WorkflowExecution } from "@/types/api";

export const TERMINAL_STATUSES = ["SUCCESS", "FAILED", "CANCELLED", "TIMED_OUT"];

export function isTerminal(status: string | undefined): boolean {
  return Boolean(status && TERMINAL_STATUSES.includes(status.toUpperCase()));
}

export function useExecution(executionId: string) {
  return useQuery<WorkflowExecution>({
    queryKey: ["execution", executionId],
    queryFn: () => getExecution(executionId),
    enabled: Boolean(executionId),
    retry: false,
    // Poll only while QUEUED / RUNNING.
    refetchInterval: (query) => (isTerminal(query.state.data?.status) ? false : 2000),
  });
}

/** GET /api/v1/executions/{id}/nodes — polls alongside the execution itself. */
export function useExecutionNodes(executionId: string, executionStatus?: string) {
  return useQuery<NodeExecution[]>({
    queryKey: ["executionNodes", executionId],
    queryFn: () => getExecutionNodes(executionId),
    enabled: Boolean(executionId),
    retry: false,
    refetchInterval: isTerminal(executionStatus) ? false : 2000,
  });
}

export function useExecuteWorkflow() {
  return useMutation({
    mutationFn: (args: ExecuteWorkflowArgs) => executeWorkflowVersion(args),
  });
}

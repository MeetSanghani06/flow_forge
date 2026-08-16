// ============================================================================
// EXECUTION CONTRACT — adjust endpoints/headers here only.
//
// The backend exposes exactly two execution reads:
//   GET /api/v1/executions/{executionId}
//   GET /api/v1/executions/{executionId}/nodes
// There is NO list-executions endpoint — do not add one here.
// ============================================================================
import { apiClient, asArray, unwrap } from "@/lib/apiClient";
import type { NodeExecution, WorkflowExecution } from "@/types/api";

export interface ExecuteWorkflowArgs {
  workflowVersionId: string;
  input: unknown;
  /** Generated once per user-intent execution, reused across automatic retries. */
  idempotencyKey: string;
}

export async function executeWorkflowVersion({
  workflowVersionId,
  input,
  idempotencyKey,
}: ExecuteWorkflowArgs): Promise<WorkflowExecution> {
  const response = await apiClient.post(
    `/api/v1/workflows/${workflowVersionId}/execute`,
    { input },
    // { headers: { "Idempotency-Key": idempotencyKey } },
  );
  return unwrap<WorkflowExecution>(response);
}

export async function getExecution(executionId: string): Promise<WorkflowExecution> {
  const response = await apiClient.get(`/api/v1/workflow-executions/${executionId}`);
  return unwrap<WorkflowExecution>(response);
}

export async function getExecutionNodes(executionId: string): Promise<NodeExecution[]> {
  const response = await apiClient.get(`/api/v1/workflow-executions/${executionId}/nodes`);
  return asArray<NodeExecution>(unwrap<unknown>(response));
}

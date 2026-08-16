import { Outlet, createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute(
  "/_authenticated/workspaces/$workspaceId/workflows/$workflowId",
)({
  component: WorkflowLayout,
});

function WorkflowLayout() {
  return <Outlet />;
}
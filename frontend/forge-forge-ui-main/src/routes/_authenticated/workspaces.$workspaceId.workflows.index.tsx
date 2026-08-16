import { createFileRoute } from "@tanstack/react-router";

import { WorkflowDashboard } from "@/components/workflows/WorkflowDashboard";
import { useWorkspaceContext } from "@/context/WorkspaceContext";

export const Route = createFileRoute("/_authenticated/workspaces/$workspaceId/workflows/")({
  head: () => ({
    meta: [
      { title: "Workspace workflows — FlowForge" },
      { name: "description", content: "All workflows defined in this FlowForge workspace." },
      { property: "og:title", content: "Workspace workflows — FlowForge" },
      {
        property: "og:description",
        content: "All workflows defined in this FlowForge workspace.",
      },
    ],
  }),
  component: WorkspaceWorkflowsPage,
});

function WorkspaceWorkflowsPage() {
  const { workspaceId } = Route.useParams();
  const { workspaces } = useWorkspaceContext();
  const workspace = workspaces.find((item) => item.id === workspaceId);

  return (
    <div className="mx-auto max-w-7xl px-6 py-8">
      <WorkflowDashboard workspaceId={workspaceId} workspaceName={workspace?.name} />
    </div>
  );
}

import { createFileRoute } from "@tanstack/react-router";

import { EmptyState, ErrorState, LoadingState } from "@/components/common/StateBlocks";
import { WorkflowDashboard } from "@/components/workflows/WorkflowDashboard";
import { useWorkspaceContext } from "@/context/WorkspaceContext";

export const Route = createFileRoute("/_authenticated/dashboard")({
  head: () => ({
    meta: [
      { title: "Dashboard — FlowForge" },
      { name: "description", content: "Workflows in your selected FlowForge workspace." },
      { property: "og:title", content: "Dashboard — FlowForge" },
      {
        property: "og:description",
        content: "Workflows in your selected FlowForge workspace.",
      },
    ],
  }),
  component: DashboardPage,
});

function DashboardPage() {
  const { selectedWorkspaceId, selectedWorkspace, isLoading, error } = useWorkspaceContext();

  return (
    <div className="mx-auto max-w-7xl px-6 py-8">
      {isLoading ? <LoadingState label="Loading workspaces..." /> : null}
      {error ? <ErrorState error={error} /> : null}
      {!isLoading && !error && !selectedWorkspaceId ? (
        <EmptyState title="No workspaces found" />
      ) : null}
      {selectedWorkspaceId ? (
        <WorkflowDashboard
          workspaceId={selectedWorkspaceId}
          workspaceName={selectedWorkspace?.name}
        />
      ) : null}
    </div>
  );
}

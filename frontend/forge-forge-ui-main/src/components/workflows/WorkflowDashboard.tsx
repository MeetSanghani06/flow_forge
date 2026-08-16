import { Link } from "@tanstack/react-router";
import { History, Search, SquareArrowOutUpRight } from "lucide-react";
import { useMemo, useState } from "react";

import { EmptyState, ErrorState, LoadingState } from "@/components/common/StateBlocks";
import { StatusBadge } from "@/components/common/StatusBadge";
import { CreateWorkflowDialog } from "@/components/workflows/CreateWorkflowDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useWorkflows } from "@/hooks/useWorkflows";

function formatDate(value?: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : date.toLocaleString();
}

export function WorkflowDashboard({
  workspaceId,
  workspaceName,
}: {
  workspaceId: string;
  workspaceName?: string | undefined;
}) {
  const { data, isLoading, error } = useWorkflows(workspaceId);
  const [query, setQuery] = useState("");

  const workflows = useMemo(() => {
    const list = data ?? [];
    const q = query.trim().toLowerCase();
    if (!q) return list;
    return list.filter(
      (workflow) =>
        workflow.name?.toLowerCase().includes(q) ||
        (workflow.description ?? "").toLowerCase().includes(q),
    );
  }, [data, query]);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
            {workspaceName ?? "Workspace"}
          </p>
          <h1 className="text-2xl font-semibold tracking-tight">Workflows</h1>
        </div>
        <CreateWorkflowDialog workspaceId={workspaceId} />
      </div>

      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search workflows"
          className="pl-9"
        />
      </div>

      {isLoading ? <LoadingState label="Loading workflows..." /> : null}
      {error ? <ErrorState error={error} /> : null}

      {!isLoading && !error && workflows.length === 0 ? (
        <EmptyState title={query ? "No workflows match your search" : "No workflows yet"} />
      ) : null}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {workflows.map((workflow) => (
          <article
            key={workflow.id}
            className="flex flex-col justify-between rounded-lg border border-border bg-surface p-5 transition-colors hover:border-primary/50"
          >
            <div className="space-y-3">
              <div className="flex items-start justify-between gap-3">
                <h2 className="font-medium tracking-tight text-foreground">{workflow.name}</h2>
                <StatusBadge status={workflow.status ?? "DRAFT"} />
              </div>
              <p className="line-clamp-2 text-sm text-muted-foreground">
                {workflow.description || "No description"}
              </p>
              <div className="flex items-center gap-3 font-mono text-[11px] text-muted-foreground">
                <span className="rounded border border-border px-1.5 py-0.5">
                  v{workflow.currentVersion ?? workflow.version ?? "—"}
                </span>
                <span>Updated {formatDate(workflow.updatedAt)}</span>
              </div>
            </div>
            <div className="mt-5 flex gap-2">
              <Button asChild size="sm" className="gap-1.5">
                <Link
                  to="/workspaces/$workspaceId/workflows/$workflowId"
                  params={{ workspaceId, workflowId: workflow.id }}
                >
                  <SquareArrowOutUpRight className="size-3.5" />
                  Open
                </Link>
              </Button>
              <Button asChild size="sm" variant="outline" className="gap-1.5">
                <Link
                  to="/workspaces/$workspaceId/workflows/$workflowId/history"
                  params={{ workspaceId, workflowId: workflow.id }}
                >
                  <History className="size-3.5" />
                  History
                </Link>
              </Button>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

import { Link, createFileRoute } from "@tanstack/react-router";
import { ArrowLeft, CheckCircle2, Loader2, RefreshCw, XCircle } from "lucide-react";

import { JsonViewer } from "@/components/common/JsonViewer";
import { ErrorState, LoadingState } from "@/components/common/StateBlocks";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Button } from "@/components/ui/button";
import { isTerminal, useExecution } from "@/hooks/useExecutions";

export const Route = createFileRoute("/_authenticated/workspaces/$workspaceId/executions/$executionId")({
  head: () => ({
    meta: [
      { title: "Execution detail — FlowForge" },
      { name: "description", content: "Live status, input and output for a FlowForge run." },
      { property: "og:title", content: "Execution detail — FlowForge" },
      {
        property: "og:description",
        content: "Live status, input and output for a FlowForge run.",
      },
    ],
  }),
  component: ExecutionPage,
});

function formatDate(value?: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : date.toLocaleString();
}

const STATUS_COPY: Record<string, string> = {
  QUEUED: "Waiting for worker...",
  RUNNING: "Workflow is running...",
  SUCCESS: "Execution completed",
  FAILED: "Execution failed",
};

function ExecutionPage() {
  const { workspaceId, executionId } = Route.useParams();
  const { data, isLoading, error, isFetching, refetch } = useExecution(executionId);

  const status = (data?.status ?? "").toUpperCase();
  const done = isTerminal(status);

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-8">
      <nav className="flex items-center gap-2 font-mono text-[11px] text-muted-foreground">
        <Link to="/workspaces/$workspaceId/workflows" params={{ workspaceId }} className="hover:text-foreground">
          Workspace
        </Link>
        <span>/</span>
        <span className="text-foreground">Execution</span>
      </nav>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight">Execution</h1>
          <p className="font-mono text-xs text-muted-foreground">{executionId}</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" className="gap-1.5" onClick={() => void refetch()}>
            <RefreshCw className={isFetching ? "size-3.5 animate-spin" : "size-3.5"} />
            Refresh
          </Button>
          <Button asChild variant="outline" size="sm" className="gap-1.5">
            <Link to="/workspaces/$workspaceId/workflows" params={{ workspaceId }}>
              <ArrowLeft className="size-3.5" />
              Workflows
            </Link>
          </Button>
        </div>
      </div>

      {isLoading ? <LoadingState label="Loading execution..." /> : null}
      {error ? <ErrorState error={error} /> : null}

      {data ? (
        <>
          <div className="flex items-center gap-3 rounded-lg border border-border bg-surface px-4 py-4">
            {status === "SUCCESS" ? (
              <CheckCircle2 className="size-5 text-success" />
            ) : status === "FAILED" ? (
              <XCircle className="size-5 text-destructive" />
            ) : (
              <Loader2 className="size-5 animate-spin text-info" />
            )}
            <div>
              <div className="flex items-center gap-2">
                <StatusBadge status={data.status} />
                {!done ? (
                  <span className="font-mono text-[11px] text-muted-foreground">
                    polling every 2s
                  </span>
                ) : null}
              </div>
              <p className="mt-1 text-sm text-muted-foreground">
                {STATUS_COPY[status] ?? "Execution state unknown"}
              </p>
            </div>
          </div>

          <dl className="grid gap-4 rounded-lg border border-border bg-surface p-4 sm:grid-cols-2">
            <Meta label="Workflow version" value={data.workflowVersionId ?? "—"} mono />
            <Meta label="Version number" value={data.version != null ? `v${data.version}` : "—"} />
            <Meta label="Started at" value={formatDate(data.startedAt)} />
            <Meta label="Completed at" value={formatDate(data.completedAt)} />
          </dl>

          {data.errorMessage ? (
            <div className="rounded-lg border border-destructive/40 bg-destructive/10 p-4">
              <p className="text-sm font-medium text-destructive">Error</p>
              <p className="mt-1 font-mono text-xs text-destructive">{data.errorMessage}</p>
            </div>
          ) : null}

          <section className="space-y-2">
            <h2 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
              Input
            </h2>
            <JsonViewer value={data.input} />
          </section>

          <section className="space-y-2">
            <h2 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
              Output
            </h2>
            <JsonViewer value={data.output} empty={done ? "No output" : "Awaiting result..."} />
          </section>
        </>
      ) : null}
    </div>
  );
}

function Meta({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div>
      <dt className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
        {label}
      </dt>
      <dd className={mono ? "mt-1 font-mono text-xs break-all" : "mt-1 text-sm"}>{value}</dd>
    </div>
  );
}

import {
  Link,
  createFileRoute,
} from "@tanstack/react-router";
import {
  ArrowLeft,
  CheckCircle2,
  ChevronDown,
  Clock3,
  Loader2,
  RefreshCw,
  Search,
  XCircle,
} from "lucide-react";
import {
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";

import { JsonViewer } from "@/components/common/JsonViewer";
import {
  EmptyState,
  ErrorState,
  LoadingState,
} from "@/components/common/StateBlocks";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useWorkflow } from "@/hooks/useWorkflow";
import {
  isTerminal,
  useExecution,
  useExecutionNodes,
} from "@/hooks/useExecutions";
import type { NodeExecution } from "@/types/api";

export const Route = createFileRoute(
  "/_authenticated/workspaces/$workspaceId/workflows/$workflowId/history",
)({
  head: () => ({
    meta: [
      { title: "Execution history — FlowForge" },
      {
        name: "description",
        content:
          "Inspect FlowForge workflow executions and individual node runs.",
      },
      {
        property: "og:title",
        content: "Execution history — FlowForge",
      },
      {
        property: "og:description",
        content:
          "Inspect FlowForge workflow executions and individual node runs.",
      },
    ],
  }),
  component: HistoryPage,
});

/**
 * The backend currently exposes no list-executions endpoint,
 * so History keeps track of executions started from this browser.
 */
const STORAGE_PREFIX = "flowforge:recent-executions:";

interface RecentExecution {
  executionId: string;
  startedAt: string;
}

export function rememberExecution(
  workflowId: string,
  executionId: string,
) {
  if (
    typeof window === "undefined" ||
    !executionId
  ) {
    return;
  }

  const key =
    STORAGE_PREFIX + workflowId;

  try {
    const raw =
      window.localStorage.getItem(key);

    const parsed: RecentExecution[] =
      raw
        ? (JSON.parse(raw) as RecentExecution[])
        : [];

    const next = [
      {
        executionId,
        startedAt:
          new Date().toISOString(),
      },
      ...parsed.filter(
        (item) =>
          item.executionId !==
          executionId,
      ),
    ].slice(0, 20);

    window.localStorage.setItem(
      key,
      JSON.stringify(next),
    );
  } catch {
    /* best effort */
  }
}

function readRecent(
  workflowId: string,
): RecentExecution[] {
  if (typeof window === "undefined") {
    return [];
  }

  try {
    const raw =
      window.localStorage.getItem(
        STORAGE_PREFIX + workflowId,
      );

    return raw
      ? (JSON.parse(
          raw,
        ) as RecentExecution[])
      : [];
  } catch {
    return [];
  }
}

function formatDate(
  value?: string | null,
) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleString();
}

function formatTime(
  value?: string | null,
) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleTimeString();
}

function formatDuration(
  startedAt?: string | null,
  completedAt?: string | null,
) {
  if (!startedAt) {
    return "—";
  }

  const start = new Date(
    startedAt,
  ).getTime();

  const end = completedAt
    ? new Date(
        completedAt,
      ).getTime()
    : Date.now();

  if (
    Number.isNaN(start) ||
    Number.isNaN(end) ||
    end < start
  ) {
    return "—";
  }

  const durationMs = end - start;

  if (durationMs < 1000) {
    return `${durationMs} ms`;
  }

  if (durationMs < 60_000) {
    return `${(
      durationMs / 1000
    ).toFixed(2)} s`;
  }

  return `${(
    durationMs / 60_000
  ).toFixed(2)} min`;
}

function getNodeName(
  node: NodeExecution,
) {
  return (
    node.nodeName?.trim() ||
    node.name?.trim() ||
    node.nodeKey?.trim() ||
    node.nodeId?.trim() ||
    "Unnamed node"
  );
}

function getNodeType(
  node: NodeExecution,
) {
  return (
    node.nodeType?.trim() ||
    node.type?.trim() ||
    "NODE"
  );
}

function getStatusIcon(
  status: string,
) {
  const normalized =
    status.toUpperCase();

  if (normalized === "SUCCESS") {
    return (
      <CheckCircle2 className="size-5 text-success" />
    );
  }

  if (normalized === "FAILED") {
    return (
      <XCircle className="size-5 text-destructive" />
    );
  }

  if (
    normalized === "RUNNING" ||
    normalized === "QUEUED"
  ) {
    return (
      <Loader2 className="size-5 animate-spin text-info" />
    );
  }

  return (
    <Clock3 className="size-5 text-muted-foreground" />
  );
}

function getStatusOrder(
  status: string,
) {
  switch (status.toUpperCase()) {
    case "FAILED":
      return 0;
    case "RUNNING":
      return 1;
    case "QUEUED":
      return 2;
    case "SUCCESS":
      return 3;
    default:
      return 4;
  }
}

function HistoryPage() {
  const {
    workspaceId,
    workflowId,
  } = Route.useParams();

  const workflowQuery =
    useWorkflow(
      workspaceId,
      workflowId,
    );

  const [
    executionId,
    setExecutionId,
  ] = useState("");

  const [
    selectedExecutionId,
    setSelectedExecutionId,
  ] = useState<
    string | undefined
  >();

  const [
    recent,
    setRecent,
  ] = useState<
    RecentExecution[]
  >([]);

  const [
    expandedNodeId,
    setExpandedNodeId,
  ] = useState<
    string | null
  >(null);

  useEffect(() => {
    setRecent(
      readRecent(workflowId),
    );
  }, [workflowId]);

  const executionQuery =
    useExecution(
      selectedExecutionId ?? "",
    );

  const executionStatus =
    (
      executionQuery.data?.status ??
      ""
    ).toUpperCase();

  const nodesQuery =
    useExecutionNodes(
      selectedExecutionId ?? "",
      executionStatus,
    );

  const nodeExecutions =
    useMemo(() => {
      const nodes =
        nodesQuery.data ?? [];

      return [...nodes].sort(
        (a, b) => {
          if (
            a.sequence != null &&
            b.sequence != null
          ) {
            return (
              a.sequence - b.sequence
            );
          }

          if (
            a.startedAt &&
            b.startedAt
          ) {
            return (
              new Date(
                a.startedAt,
              ).getTime() -
              new Date(
                b.startedAt,
              ).getTime()
            );
          }

          return 0;
        },
      );
    }, [nodesQuery.data]);

  function openExecution(
    id: string,
  ) {
    const trimmed = id.trim();

    if (!trimmed) {
      return;
    }

    setExecutionId(trimmed);
    setSelectedExecutionId(
      trimmed,
    );
    setExpandedNodeId(null);
  }

  function handleSubmit(
    event: FormEvent,
  ) {
    event.preventDefault();

    openExecution(
      executionId,
    );
  }

  function handleRefresh() {
    void executionQuery.refetch();
    void nodesQuery.refetch();
  }

  const executionDone =
    isTerminal(
      executionStatus,
    );

  return (
    <div className="mx-auto max-w-5xl space-y-6 px-6 py-8">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 font-mono text-[11px] text-muted-foreground">
        <Link
          to="/workspaces/$workspaceId/workflows"
          params={{
            workspaceId,
          }}
          className="hover:text-foreground"
        >
          Workspace
        </Link>

        <span>/</span>

        <Link
          to="/workspaces/$workspaceId/workflows/$workflowId"
          params={{
            workspaceId,
            workflowId,
          }}
          className="hover:text-foreground"
        >
          {workflowQuery.data?.name ??
            "Workflow"}
        </Link>

        <span>/</span>

        <span className="text-foreground">
          History
        </span>
      </nav>

      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight">
            Execution history
          </h1>

          <p className="text-sm text-muted-foreground">
            Inspect workflow runs and
            individual node executions.
          </p>
        </div>

        <Button
          asChild
          variant="outline"
          size="sm"
          className="gap-1.5"
        >
          <Link
            to="/workspaces/$workspaceId/workflows/$workflowId"
            params={{
              workspaceId,
              workflowId,
            }}
          >
            <ArrowLeft className="size-3.5" />
            Back to builder
          </Link>
        </Button>
      </div>

      {/* Execution lookup */}
      <form
        onSubmit={handleSubmit}
        className="space-y-3 rounded-lg border border-border bg-surface p-4"
      >
        <Label htmlFor="execution-id">
          Execution ID
        </Label>

        <div className="flex gap-2">
          <Input
            id="execution-id"
            className="font-mono text-xs"
            value={executionId}
            onChange={(event) =>
              setExecutionId(
                event.target.value,
              )
            }
            placeholder="0f2c9a1e-7f3d-4c65-9d1c-2b0e5a7c9f11"
          />

          <Button
            type="submit"
            className="gap-1.5"
            disabled={
              !executionId.trim()
            }
          >
            <Search className="size-3.5" />
            Open
          </Button>
        </div>
      </form>

      {/* Recent executions */}
      <section className="space-y-3">
        <h2 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
          Recent executions
        </h2>

        {recent.length === 0 ? (
          <EmptyState title="No runs recorded on this device yet" />
        ) : (
          <ul className="divide-y divide-border rounded-lg border border-border bg-surface">
            {recent.map(
              (item) => {
                const active =
                  item.executionId ===
                  selectedExecutionId;

                return (
                  <li
                    key={
                      item.executionId
                    }
                  >
                    <button
                      type="button"
                      onClick={() =>
                        openExecution(
                          item.executionId,
                        )
                      }
                      className={`flex w-full items-center justify-between gap-3 px-4 py-3 text-left hover:bg-accent ${
                        active
                          ? "bg-accent"
                          : ""
                      }`}
                    >
                      <span className="truncate font-mono text-xs">
                        {
                          item.executionId
                        }
                      </span>

                      <span className="shrink-0 text-xs text-muted-foreground">
                        {formatDate(
                          item.startedAt,
                        )}
                      </span>
                    </button>
                  </li>
                );
              },
            )}
          </ul>
        )}
      </section>

      {/* Execution details */}
      {selectedExecutionId ? (
        <section className="space-y-6">
          {executionQuery.isLoading ? (
            <LoadingState label="Loading execution..." />
          ) : null}

          {executionQuery.error ? (
            <ErrorState
              error={
                executionQuery.error
              }
            />
          ) : null}

          {executionQuery.data ? (
            <>
              {/* Execution header */}
              <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-surface px-4 py-4">
                <div className="flex items-center gap-3">
                  {getStatusIcon(
                    executionStatus,
                  )}

                  <div>
                    <div className="flex items-center gap-2">
                      <StatusBadge
                        status={
                          executionQuery
                            .data
                            .status
                        }

                      />

                      {!executionDone ? (
                        <span className="font-mono text-[11px] text-muted-foreground">
                          polling every 2s
                        </span>
                      ) : null}
                    </div>

                    <p className="mt-1 font-mono text-xs text-muted-foreground">
                      {
                        selectedExecutionId
                      }
                    </p>
                  </div>
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  className="gap-1.5"
                  onClick={
                    handleRefresh
                  }
                  disabled={
                    executionQuery.isFetching ||
                    nodesQuery.isFetching
                  }
                >
                  <RefreshCw
                    className={
                      executionQuery.isFetching ||
                      nodesQuery.isFetching
                        ? "size-3.5 animate-spin"
                        : "size-3.5"
                    }
                  />
                  Refresh
                </Button>
              </div>

              {/* Metadata */}
              <dl className="grid gap-4 rounded-lg border border-border bg-surface p-4 sm:grid-cols-2 lg:grid-cols-4">
                <Meta
                  label="Workflow version"
                  value={
                    executionQuery.data
                      .workflowVersionId ??
                    "—"
                  }
                  mono
                />

                <Meta
                  label="Version number"
                  value={
                    executionQuery
                      .data
                      .version !=
                    null
                      ? `v${executionQuery.data.version}`
                      : "—"
                  }
                />

                <Meta
                  label="Started at"
                  value={formatDate(
                    executionQuery
                      .data
                      .startedAt,
                  )}
                />

                <Meta
                  label="Completed at"
                  value={formatDate(
                    executionQuery
                      .data
                      .completedAt,
                  )}
                />
              </dl>

              {/* Execution error */}
              {executionQuery.data
                .errorMessage ? (
                <div className="rounded-lg border border-destructive/40 bg-destructive/10 p-4">
                  <p className="text-sm font-medium text-destructive">
                    Execution error
                  </p>

                  <p className="mt-1 font-mono text-xs text-destructive">
                    {
                      executionQuery
                        .data
                        .errorMessage
                    }
                  </p>
                </div>
              ) : null}

              {/* Node executions */}
              <section className="space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
                      Node executions
                    </h2>

                    <p className="mt-1 text-sm text-muted-foreground">
                      Detailed execution state
                      for every workflow node.
                    </p>
                  </div>

                  {nodeExecutions.length >
                  0 ? (
                    <span className="font-mono text-xs text-muted-foreground">
                      {
                        nodeExecutions.length
                      }{" "}
                      node
                      {nodeExecutions.length !==
                      1
                        ? "s"
                        : ""}
                    </span>
                  ) : null}
                </div>

                {nodesQuery.isLoading ? (
                  <LoadingState label="Loading node executions..." />
                ) : null}

                {nodesQuery.error ? (
                  <ErrorState
                    error={
                      nodesQuery.error
                    }
                  />
                ) : null}

                {!nodesQuery.isLoading &&
                !nodesQuery.error &&
                nodeExecutions.length ===
                  0 ? (
                  <div className="rounded-lg border border-dashed border-border bg-surface px-4 py-8 text-center">
                    <p className="text-sm text-muted-foreground">
                      No node executions
                      recorded yet.
                    </p>

                    {!executionDone ? (
                      <p className="mt-1 font-mono text-[11px] text-muted-foreground">
                        Waiting for worker
                        execution data...
                      </p>
                    ) : null}
                  </div>
                ) : null}

                {nodeExecutions.length >
                0 ? (
                  <div className="overflow-hidden rounded-lg border border-border bg-surface">
                    {nodeExecutions.map(
                      (
                        node,
                        index,
                      ) => (
                        <NodeExecutionRow
                          key={
                            node.id ??
                            `${node.nodeKey ?? node.nodeId ?? "node"}-${index}`
                          }
                          node={node}
                          index={index}
                          expanded={
                            expandedNodeId ===
                            (node.id ??
                              `${node.nodeKey ?? node.nodeId ?? "node"}-${index}`)
                          }
                          onToggle={() => {
                            const id =
                              node.id ??
                              `${node.nodeKey ?? node.nodeId ?? "node"}-${index}`;

                            setExpandedNodeId(
                              (current) =>
                                current ===
                                id
                                  ? null
                                  : id,
                            );
                          }}
                        />
                      ),
                    )}
                  </div>
                ) : null}
              </section>

              {/* Workflow input */}
              <section className="space-y-2">
                <h2 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
                  Workflow input
                </h2>

                <JsonViewer
                  value={
                    executionQuery
                      .data.input
                  }
                />
              </section>

              {/* Workflow output */}
              <section className="space-y-2">
                <h2 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
                  Workflow output
                </h2>

                <JsonViewer
                  value={
                    executionQuery
                      .data.output
                  }
                  empty={
                    executionDone
                      ? "No output"
                      : "Awaiting result..."
                  }
                />
              </section>
            </>
          ) : null}
        </section>
      ) : null}
    </div>
  );
}

function NodeExecutionRow({
  node,
  index,
  expanded,
  onToggle,
}: {
  node: NodeExecution;
  index: number;
  expanded: boolean;
  onToggle: () => void;
}) {
  const status =
    String(
      node.status ?? "",
    ).toUpperCase();

  const name =
    getNodeName(node);

  const type =
    getNodeType(node);

  return (
    <div
      className={
        index > 0
          ? "border-t border-border"
          : ""
      }
    >
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full items-center gap-3 px-4 py-4 text-left hover:bg-accent"
      >
        <div className="shrink-0">
          {getStatusIcon(status)}
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-medium">
              {name}
            </span>

            <span className="rounded border border-border px-1.5 py-0.5 font-mono text-[10px] text-muted-foreground">
              {type}
            </span>

            {node.attempt !=
            null ? (
              <span className="font-mono text-[10px] text-muted-foreground">
                attempt {node.attempt}
              </span>
            ) : null}
          </div>

          <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted-foreground">
            {node.nodeKey ? (
              <span className="font-mono">
                {node.nodeKey}
              </span>
            ) : null}

            {node.startedAt ? (
              <span>
                {formatTime(
                  node.startedAt,
                )}
              </span>
            ) : null}

            <span>
              {formatDuration(
                node.startedAt,
                node.completedAt,
              )}
            </span>
          </div>
        </div>

        <StatusBadge
          status={node.status}
        />

        <ChevronDown
          className={`size-4 shrink-0 text-muted-foreground transition-transform ${
            expanded
              ? "rotate-180"
              : ""
          }`}
        />
      </button>

      {expanded ? (
        <div className="space-y-4 border-t border-border bg-muted/20 px-4 py-4">
          <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Meta
              label="Node key"
              value={
                node.nodeKey ??
                "—"
              }
              mono
            />

            <Meta
              label="Node ID"
              value={
                node.nodeId ??
                "—"
              }
              mono
            />

            <Meta
              label="Started"
              value={formatDate(
                node.startedAt,
              )}
            />

            <Meta
              label="Completed"
              value={formatDate(
                node.completedAt,
              )}
            />
          </dl>

          {node.errorMessage ? (
            <div className="rounded-lg border border-destructive/40 bg-destructive/10 p-4">
              <p className="text-sm font-medium text-destructive">
                Node error
              </p>

              <p className="mt-1 whitespace-pre-wrap font-mono text-xs text-destructive">
                {
                  node.errorMessage
                }
              </p>
            </div>
          ) : null}

          <section className="space-y-2">
            <h3 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
              Input
            </h3>

            <JsonViewer
              value={
                node.input
              }
              empty="No node input"
            />
          </section>

          <section className="space-y-2">
            <h3 className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
              Output
            </h3>

            <JsonViewer
              value={
                node.output
              }
              empty="No node output"
            />
          </section>
        </div>
      ) : null}
    </div>
  );
}

function Meta({
  label,
  value,
  mono,
}: {
  label: string;
  value: string;
  mono?: boolean;
}) {
  return (
    <div>
      <dt className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
        {label}
      </dt>

      <dd
        className={
          mono
            ? "mt-1 break-all font-mono text-xs"
            : "mt-1 text-sm"
        }
      >
        {value}
      </dd>
    </div>
  );
}
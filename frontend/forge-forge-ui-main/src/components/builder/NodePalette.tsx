import {
  Braces,
  Clock,
  GitBranch,
  Globe,
  Sparkles,
  Zap,
} from "lucide-react";
import type { ReactNode } from "react";
import {
  NODE_LABELS,
  NODE_TYPES,
  TRIGGER_NODE_TYPE,
} from "@/lib/mappers/workflowGraphMapper";
import { cn } from "@/lib/utils";

/**
 * Keep icons centralized here so FlowNodeCard can reuse them.
 *
 * NODE_TYPES are intentionally sourced from the workflow graph mapper,
 * which is the single source of truth for backend node types.
 */
export const NODE_ICONS: Record<string, ReactNode> = {
  TRIGGER: <Zap className="size-4" />,
  HTTP_REQUEST: <Globe className="size-4" />,
  HTTP: <Globe className="size-4" />,
  AI_PROMPT: <Sparkles className="size-4" />,
  CONDITION: <GitBranch className="size-4" />,
  TRANSFORM: <Braces className="size-4" />,
  DELAY: <Clock className="size-4" />,
};

interface NodePaletteProps {
  onAdd: (type: string) => void;
  hasTrigger?: boolean;
  disabled?: boolean;
}

export function NodePalette({
  onAdd,
  hasTrigger = false,
  disabled = false,
}: NodePaletteProps) {
  return (
    <aside className="w-56 shrink-0 overflow-y-auto border-r border-border bg-surface p-4">
      <div>
        <p className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
          Node Palette
        </p>

        <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
          Add a node to the canvas and configure it from the
          inspector.
        </p>
      </div>

      <div className="mt-4 space-y-2">
        {NODE_TYPES.map((type) => {
          const isTrigger = type === TRIGGER_NODE_TYPE;

          const blocked =
            disabled ||
            (isTrigger && hasTrigger);

          return (
            <button
              key={type}
              type="button"
              disabled={blocked}
              title={
                isTrigger && hasTrigger
                  ? "A workflow can have only one Trigger node."
                  : undefined
              }
              onClick={() => onAdd(type)}
              className={cn(
                "flex w-full items-center gap-2.5 rounded-md border px-3 py-2.5 text-left transition-colors",
                "font-mono text-xs",
                isTrigger
                  ? "border-warning/40 bg-warning/5 text-foreground"
                  : "border-border bg-background text-foreground",
                blocked
                  ? "cursor-not-allowed opacity-40"
                  : "hover:border-primary/60 hover:bg-accent",
              )}
            >
              <span
                className={cn(
                  "shrink-0",
                  isTrigger
                    ? "text-warning"
                    : "text-primary",
                )}
              >
                {NODE_ICONS[type] ??
                  null}
              </span>

              <span className="min-w-0 truncate">
                {NODE_LABELS[type] ?? type}
              </span>
            </button>
          );
        })}
      </div>

      <div className="mt-5 rounded-md border border-border bg-background/50 p-3">
        <p className="text-xs font-medium text-foreground">
          Workflow rule
        </p>

        <p className="mt-1 text-[11px] leading-relaxed text-muted-foreground">
          Every workflow must contain exactly one Trigger node.
          Other node types can be added as needed.
        </p>
      </div>
    </aside>
  );
}
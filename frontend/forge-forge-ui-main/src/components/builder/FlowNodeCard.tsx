import { Handle, Position, type NodeProps } from "@xyflow/react";

import { NODE_ICONS } from "@/components/builder/NodePalette";
import {
  NODE_LABELS,
  TRIGGER_NODE_TYPE,
} from "@/lib/mappers/workflowGraphMapper";
import { cn } from "@/lib/utils";

interface FlowNodeCardData {
  label: string;
  nodeType: string;
  nodeKey?: string;
}

export function FlowNodeCard({ data, selected }: NodeProps) {
  const nodeData = data as unknown as FlowNodeCardData;
  const isTrigger = nodeData.nodeType === TRIGGER_NODE_TYPE;

  return (
    <div
      className={cn(
        "relative min-w-52 border px-3 py-2.5 shadow-sm transition-all",
        isTrigger
          ? "rounded-full border-warning/60 bg-warning/10"
          : "rounded-md border-border bg-surface",
        selected &&
          (isTrigger
            ? "border-warning ring-1 ring-warning/30"
            : "border-primary ring-1 ring-primary/30"),
      )}
    >
      {/* Trigger has no incoming connection */}
      {!isTrigger && (
        <Handle
          type="target"
          position={Position.Left}
          className="!size-2.5 !border-0 !bg-primary"
        />
      )}

      <div className="flex items-center gap-2.5">
        <span
          className={cn(
            "flex shrink-0 items-center justify-center",
            isTrigger ? "text-warning" : "text-primary",
          )}
        >
          {NODE_ICONS[nodeData.nodeType] ?? null}
        </span>

        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-foreground">
            {nodeData.label}
          </p>

          <p className="font-mono text-[10px] uppercase tracking-wide text-muted-foreground">
            {NODE_LABELS[nodeData.nodeType] ?? nodeData.nodeType}
          </p>

          {nodeData.nodeKey && (
            <p className="mt-0.5 truncate font-mono text-[9px] text-muted-foreground/70">
              {nodeData.nodeKey}
            </p>
          )}
        </div>
      </div>

      {/* Every node can have outgoing edges */}
      <Handle
        type="source"
        position={Position.Right}
        className="!size-2.5 !border-0 !bg-primary"
      />
    </div>
  );
}
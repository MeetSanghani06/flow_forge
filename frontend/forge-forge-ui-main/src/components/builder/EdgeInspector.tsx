import { X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import type { FlowEdge } from "@/lib/mappers/workflowGraphMapper";

interface EdgeInspectorProps {
  edge?: FlowEdge;
  onChangeCondition: (condition: string | null) => void;
  onDelete: () => void;
}

export function EdgeInspector({
  edge,
  onChangeCondition,
  onDelete,
}: EdgeInspectorProps) {
  if (!edge) {
    return (
      <aside className="w-72 shrink-0 border-l border-border bg-surface">
        <div className="flex h-full items-center justify-center px-6">
          <p className="text-center text-sm text-muted-foreground">
            Select an edge to inspect its configuration.
          </p>
        </div>
      </aside>
    );
  }

  const condition =
    typeof edge.data?.condition === "string"
      ? edge.data.condition
      : "";

  return (
    <aside className="flex w-72 shrink-0 flex-col border-l border-border bg-surface">
      <div className="flex items-center justify-between border-b border-border px-4 py-3">
        <div>
          <h2 className="text-sm font-semibold">
            Edge Inspector
          </h2>

          <p className="mt-0.5 font-mono text-[10px] text-muted-foreground">
            {edge.source} → {edge.target}
          </p>
        </div>

        <Button
          variant="ghost"
          size="icon"
          className="size-8"
          onClick={onDelete}
          title="Delete edge"
        >
          <X className="size-4" />
        </Button>
      </div>

      <div className="flex-1 space-y-5 overflow-y-auto p-4">
        <div className="space-y-2">
          <Label htmlFor="edge-condition">
            Condition
          </Label>

          <Input
            id="edge-condition"
            value={condition}
            onChange={(event) => {
              const value = event.target.value;

              onChangeCondition(
                value.trim() === "" ? null : value,
              );
            }}
            placeholder="e.g. status == 'SUCCESS'"
            className="font-mono text-xs"
          />

          <p className="text-[11px] leading-relaxed text-muted-foreground">
            Optional condition evaluated before following this
            edge. Leave empty for an unconditional edge.
          </p>
        </div>

        <div className="space-y-2">
          <Label>Source</Label>

          <div className="rounded-md border border-border bg-muted/30 px-3 py-2 font-mono text-[11px] break-all">
            {edge.source}
          </div>
        </div>

        <div className="space-y-2">
          <Label>Target</Label>

          <div className="rounded-md border border-border bg-muted/30 px-3 py-2 font-mono text-[11px] break-all">
            {edge.target}
          </div>
        </div>

        {edge.data?.condition ? (
          <div className="rounded-md border border-border bg-accent/30 p-3">
            <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              Active condition
            </p>

            <p className="mt-1 font-mono text-xs break-words">
              {String(edge.data.condition)}
            </p>
          </div>
        ) : null}
      </div>

      <div className="border-t border-border p-4">
        <Button
          variant="destructive"
          className="w-full"
          onClick={onDelete}
        >
          Delete edge
        </Button>
      </div>
    </aside>
  );
}
import { useNavigate } from "@tanstack/react-router";
import { Loader2, Play } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useExecuteWorkflow } from "@/hooks/useExecutions";
import { getErrorMessage } from "@/lib/errorHandler";
import { rememberExecution } from "@/routes/_authenticated/workspaces.$workspaceId.workflows.$workflowId.history";

export function ExecuteDialog({
  workspaceId,
  workflowId,
  workflowVersionId,
  disabled,
}: {
  workspaceId: string;
  workflowId?: string;
  workflowVersionId: string | undefined;
  disabled?: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('{\n  "userId": 1\n}');
  const execute = useExecuteWorkflow();
  const navigate = useNavigate();

  async function handleExecute() {
    if (!workflowVersionId) return;
    let parsedInput: unknown;
    try {
      parsedInput = JSON.parse(input || "{}");
    } catch {
      toast.error("Input must be valid JSON.");
      return;
    }

    // ONE idempotency key per user-intent execution; axios retries reuse it.
    const idempotencyKey = crypto.randomUUID();

    try {
      const execution = await execute.mutateAsync({
        workflowVersionId,
        input: parsedInput,
        idempotencyKey,
      });
      const executionId = execution?.id ?? execution?.executionId;
      if (!executionId) throw new Error("Execution response did not contain an execution id.");
      if (workflowId) rememberExecution(workflowId, executionId);
      toast.success("Execution queued");
      setOpen(false);
      void navigate({
        to: "/workspaces/$workspaceId/executions/$executionId",
        params: { workspaceId, executionId },
      });
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="gap-2" disabled={disabled || !workflowVersionId}>
          <Play className="size-4" />
          Execute
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute workflow</DialogTitle>
          <DialogDescription>
            Execution is asynchronous — the run is queued and processed by a worker.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2 py-2">
          <Label htmlFor="execution-input">Input JSON</Label>
          <Textarea
            id="execution-input"
            rows={8}
            className="font-mono text-xs"
            value={input}
            onChange={(event) => setInput(event.target.value)}
          />
        </div>
        <DialogFooter>
          <Button onClick={() => void handleExecute()} disabled={execute.isPending}>
            {execute.isPending ? <Loader2 className="size-4 animate-spin" /> : null}
            {execute.isPending ? "Submitting..." : "Run execution"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

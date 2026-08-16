import { useNavigate } from "@tanstack/react-router";
import { Loader2, Plus } from "lucide-react";
import { useState, type FormEvent } from "react";
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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useCreateWorkflow } from "@/hooks/useWorkflows";
import { getErrorMessage } from "@/lib/errorHandler";

export function CreateWorkflowDialog({ workspaceId }: { workspaceId: string }) {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const createWorkflow = useCreateWorkflow(workspaceId);
  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      const workflow = await createWorkflow.mutateAsync({ name, description });
      toast.success("Workflow created");
      setOpen(false);
      setName("");
      setDescription("");
      if (workflow?.id) {
        void navigate({
          to: "/workspaces/$workspaceId/workflows/$workflowId",
          params: { workspaceId, workflowId: workflow.id },
        });
      }
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="gap-2">
          <Plus className="size-4" />
          New workflow
        </Button>
      </DialogTrigger>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Create workflow</DialogTitle>
            <DialogDescription>
              Workflows are created inside the selected workspace.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="workflow-name">Name</Label>
              <Input
                id="workflow-name"
                required
                value={name}
                onChange={(event) => setName(event.target.value)}
                placeholder="Order fulfilment"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="workflow-description">Description</Label>
              <Textarea
                id="workflow-description"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="What does this workflow do?"
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="submit" disabled={createWorkflow.isPending || !name.trim()}>
              {createWorkflow.isPending ? <Loader2 className="size-4 animate-spin" /> : null}
              Create workflow
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

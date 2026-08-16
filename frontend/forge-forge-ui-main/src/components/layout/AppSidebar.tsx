import { Link, useParams } from "@tanstack/react-router";
import { History, LayoutDashboard, LogOut, Loader2, Workflow } from "lucide-react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/context/AuthContext";
import { useWorkspaceContext } from "@/context/WorkspaceContext";

export function AppSidebar() {
  const { workspaces, selectedWorkspaceId, selectWorkspace, isLoading } = useWorkspaceContext();
  const { logout } = useAuth();
  const [signingOut, setSigningOut] = useState(false);
  const params = useParams({ strict: false }) as { workspaceId?: string };
  const workspaceId = params.workspaceId ?? selectedWorkspaceId ?? undefined;

  async function handleLogout() {
    setSigningOut(true);
    await logout();
    setSigningOut(false);
  }

  return (
    <aside className="sticky top-0 flex h-screen w-64 shrink-0 flex-col border-r border-border bg-surface">
      <div className="flex items-center gap-2 border-b border-border px-4 py-4">
        <Workflow className="size-5 text-primary" />
        <span className="font-mono text-sm font-semibold tracking-tight">FlowForge</span>
      </div>

      <div className="space-y-2 border-b border-border px-4 py-4">
        <p className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
          Workspace
        </p>
        {isLoading ? (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Loader2 className="size-3.5 animate-spin" /> Loading workspaces...
          </div>
        ) : workspaces.length === 0 ? (
          <p className="text-sm text-muted-foreground">No workspaces found</p>
        ) : (
          <Select value={selectedWorkspaceId ?? ""} onValueChange={selectWorkspace}>
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select workspace" />
            </SelectTrigger>
            <SelectContent>
              {workspaces.map((workspace) => (
                <SelectItem key={workspace.id} value={workspace.id}>
                  {workspace.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )}
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        <SidebarLink to="/dashboard" icon={<LayoutDashboard className="size-4" />}>
          Dashboard
        </SidebarLink>
        {workspaceId ? (
          <Link
            to="/workspaces/$workspaceId/workflows"
            params={{ workspaceId }}
            className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
            activeProps={{ className: "bg-accent text-foreground" }}
          >
            <Workflow className="size-4" />
            Workflows
          </Link>
        ) : null}
        <p className="flex items-center gap-2 px-3 py-2 text-xs text-muted-foreground/70">
          <History className="size-4" />
          Executions live on each workflow
        </p>
      </nav>

      <div className="border-t border-border p-3">
        <Button
          variant="ghost"
          className="w-full justify-start gap-2 text-muted-foreground hover:text-foreground"
          onClick={() => void handleLogout()}
          disabled={signingOut}
        >
          {signingOut ? <Loader2 className="size-4 animate-spin" /> : <LogOut className="size-4" />}
          Logout
        </Button>
      </div>
    </aside>
  );
}

function SidebarLink({
  to,
  icon,
  children,
}: {
  to: "/dashboard";
  icon: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <Link
      to={to}
      className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
      activeProps={{ className: "bg-accent text-foreground" }}
    >
      {icon}
      {children}
    </Link>
  );
}

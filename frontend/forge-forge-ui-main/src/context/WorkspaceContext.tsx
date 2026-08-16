import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

import { useWorkspaces } from "@/hooks/useWorkspaces";
import {
  getSelectedWorkspaceId,
  setSelectedWorkspaceId as persistWorkspaceId,
} from "@/lib/authStorage";
import type { Workspace } from "@/types/api";

interface WorkspaceContextValue {
  workspaces: Workspace[];
  selectedWorkspaceId: string | null;
  selectedWorkspace: Workspace | undefined;
  selectWorkspace: (id: string) => void;
  isLoading: boolean;
  error: unknown;
}

const WorkspaceContext = createContext<WorkspaceContextValue | null>(null);

export function WorkspaceProvider({ children }: { children: ReactNode }) {
  const { data, isLoading, error } = useWorkspaces();
  const workspaces = useMemo(() => data ?? [], [data]);
  const [selectedWorkspaceId, setSelected] = useState<string | null>(null);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // Restore persisted selection / auto-select first workspace.
  useEffect(() => {
    if (workspaces.length === 0) return;
    const persisted = selectedWorkspaceId ?? getSelectedWorkspaceId();
    const exists = persisted && workspaces.some((workspace) => workspace.id === persisted);
    const next = exists ? (persisted as string) : (workspaces[0]?.id ?? null);
    if (next && next !== selectedWorkspaceId) {
      setSelected(next);
      persistWorkspaceId(next);
    }
  }, [workspaces, selectedWorkspaceId]);

  const selectWorkspace = useCallback(
    (id: string) => {
      if (id === selectedWorkspaceId) return;
      setSelected(id);
      persistWorkspaceId(id);
      void queryClient.removeQueries({ queryKey: ["workflows"] });
      void navigate({ to: "/workspaces/$workspaceId/workflows", params: { workspaceId: id } });
    },
    [navigate, queryClient, selectedWorkspaceId],
  );

  const value = useMemo<WorkspaceContextValue>(
    () => ({
      workspaces,
      selectedWorkspaceId,
      selectedWorkspace: workspaces.find((workspace) => workspace.id === selectedWorkspaceId),
      selectWorkspace,
      isLoading,
      error,
    }),
    [workspaces, selectedWorkspaceId, selectWorkspace, isLoading, error],
  );

  return <WorkspaceContext.Provider value={value}>{children}</WorkspaceContext.Provider>;
}

export function useWorkspaceContext(): WorkspaceContextValue {
  const context = useContext(WorkspaceContext);
  if (!context) throw new Error("useWorkspaceContext must be used within WorkspaceProvider");
  return context;
}

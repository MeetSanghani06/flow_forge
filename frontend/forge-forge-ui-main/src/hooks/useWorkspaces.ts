import { useQuery } from "@tanstack/react-query";

import { listWorkspaces } from "@/api/workspaceApi";
import type { Workspace } from "@/types/api";

export function useWorkspaces(enabled = true) {
  return useQuery<Workspace[]>({
    queryKey: ["workspaces"],
    queryFn: listWorkspaces,
    enabled,
    retry: false,
  });
}

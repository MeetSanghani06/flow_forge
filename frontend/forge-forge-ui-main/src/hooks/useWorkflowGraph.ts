import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
  getGraph,
  publishVersion,
  saveGraph,
  createVersion,
  cloneVersion,
} from "@/api/workflowVersionApi";

import type { WorkflowGraphDto } from "@/types/api";
import { SaveGraphRequest } from "@/api/workflowApi";

/**
 * Fetch the graph for a specific workflow version.
 *
 * IMPORTANT:
 * - Graph GET uses version NUMBER.
 * - Example:
 *   GET /workspaces/{workspaceId}/workflows/{workflowId}/versions/6/graph
 */
export function useWorkflowGraph(
  workspaceId: string,
  workflowId: string,
  workflowVersionNumber: number | undefined,
) {
  const enabled =
    Boolean(workspaceId) &&
    Boolean(workflowId) &&
    workflowVersionNumber != null;

  return useQuery<WorkflowGraphDto>({
    queryKey: [
      "workflowGraph",
      workspaceId,
      workflowId,
      workflowVersionNumber,
    ],

    queryFn: async () => {
      if (workflowVersionNumber == null) {
        throw new Error("Workflow version number is required to fetch graph.");
      }

      return getGraph(
        workspaceId,
        workflowId,
        workflowVersionNumber,
      );
    },

    enabled,

    // Don't hammer the API for an invalid graph/version request.
    retry: false,
  });
}

/**
 * Save a workflow graph.
 *
 * IMPORTANT:
 * - Save uses workflow VERSION ID.
 * - This is intentionally different from GET graph.
 */
export function useSaveGraph(
  workspaceId: string,
  workflowId: string,
  versionNumber: number | undefined
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (graph: SaveGraphRequest) => {
      if (!workspaceId) {
        throw new Error("Workspace ID is required.");
      }

      if (!workflowId) {
        throw new Error("Workflow ID is required.");
      }

      if (!versionNumber) {
        throw new Error("Workflow version ID is required.");
      }

      return saveGraph(
        workspaceId,
        workflowId,
        versionNumber,
        graph,
      );
    },

    onSuccess: (savedGraph) => {
      /*
       * The GET graph query is keyed by VERSION NUMBER,
       * so invalidate it using version number.
       */
      if (versionNumber != null) {
        void queryClient.invalidateQueries({
          queryKey: [
            "workflowGraph",
            workspaceId,
            workflowId,
          ],
        });
      }

      /*
       * Refresh workflow metadata.
       */
      void queryClient.invalidateQueries({
        queryKey: [
          "workflow",
          workspaceId,
          workflowId,
        ],
      });

      /*
       * Refresh workflow versions because saving may have
       * changed the current/latest version state depending
       * on backend behavior.
       */
      void queryClient.invalidateQueries({
        queryKey: [
          "workflowVersions",
          workspaceId,
          workflowId,
        ],
      });

      /*
       * Optional:
       * If the backend returns workflowVersionId/versionNumber
       * from PUT, we can also use the response to update the
       * graph cache immediately.
       */
      if (
        savedGraph &&
        versionNumber != null
      ) {
        queryClient.setQueryData<WorkflowGraphDto>(
          [
            "workflowGraph",
            workspaceId,
            workflowId,
            versionNumber,
          ],
          savedGraph,
        );
      }
    },
  });
}

/**
 * Publish a workflow version.
 *
 * Publish uses VERSION ID, not version number.
 */
export function usePublishVersion(
  workspaceId: string,
  workflowId: string,
  versionNumber: number | undefined,
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      if (!workspaceId) {
        throw new Error("Workspace ID is required.");
      }

      if (!workflowId) {
        throw new Error("Workflow ID is required.");
      }

      if (!versionNumber) {
        throw new Error("Workflow version ID is required.");
      }

      return publishVersion(
        workspaceId,
        workflowId,
        versionNumber,
      );
    },

    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: [
          "workflow",
          workspaceId,
          workflowId,
        ],
      });

      void queryClient.invalidateQueries({
        queryKey: [
          "workflowVersions",
          workspaceId,
          workflowId,
        ],
      });

      /*
       * Don't invalidate workflowGraph using workflowVersionId.
       *
       * Graph queries are keyed by VERSION NUMBER.
       *
       * workflowVersions/workflow metadata invalidation above
       * will cause the selected version information to refresh.
       */
    },
  });
}

export function useCloneVersion(
  workspaceId: string,
  workflowId: string,
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (versionNumber: number) => {
      if (!workspaceId) {
        throw new Error("Workspace ID is required.");
      }

      if (!workflowId) {
        throw new Error("Workflow ID is required.");
      }

      if (!Number.isFinite(versionNumber)) {
        throw new Error("Version number is required.");
      }

      return cloneVersion(
        workspaceId,
        workflowId,
        versionNumber,
      );
    },

    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: [
          "workflowVersions",
          workspaceId,
          workflowId,
        ],
      });

      void queryClient.invalidateQueries({
        queryKey: [
          "workflow",
          workspaceId,
          workflowId,
        ],
      });
    },
  });
}

/**
 * Create a new workflow version.
 */
export function useCreateVersion(
  workspaceId: string,
  workflowId: string,
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      if (!workspaceId) {
        throw new Error("Workspace ID is required.");
      }

      if (!workflowId) {
        throw new Error("Workflow ID is required.");
      }

      return createVersion(
        workspaceId,
        workflowId,
      );
    },

    onSuccess: () => {
      /*
       * New version must immediately appear in the
       * version selector.
       */
      void queryClient.invalidateQueries({
        queryKey: [
          "workflowVersions",
          workspaceId,
          workflowId,
        ],
      });

      /*
       * Current/latest version metadata may also change.
       */
      void queryClient.invalidateQueries({
        queryKey: [
          "workflow",
          workspaceId,
          workflowId,
        ],
      });
    },
  });
}
import type { Edge, Node } from "@xyflow/react";

import type {
  WorkflowEdgeDto,
  WorkflowGraphDto,
  WorkflowNodeDto,
} from "@/types/api";

export interface FlowNodeData extends Record<string, unknown> {
  label: string;

  /** Stable backend node key. */
  nodeKey: string;

  /** Backend node type, e.g. TRIGGER, HTTP_REQUEST. */
  nodeType: string;

  /** Parsed configuration object used by the UI. */
  config: Record<string, unknown>;

  connectorId?: string | null;

  /** Backend UUID of the persisted node, if available. */
  backendId?: string;
}

export type FlowNode = Node<FlowNodeData>;

export interface FlowEdgeData extends Record<string, unknown> {
  condition?: string | null;
}

export type FlowEdge = Edge<FlowEdgeData>;

export const TRIGGER_NODE_TYPE = "TRIGGER";

export const NODE_TYPES = [
  TRIGGER_NODE_TYPE,
  "HTTP_REQUEST",
  "AI_PROMPT",
  "CONDITION",
  "TRANSFORM",
  "DELAY",
] as const;

export type KnownNodeType = (typeof NODE_TYPES)[number];

export const NODE_LABELS: Record<string, string> = {
  TRIGGER: "Trigger",
  HTTP_REQUEST: "HTTP Request",
  AI_PROMPT: "AI Prompt",
  CONDITION: "Condition",
  TRANSFORM: "Transform",
  DELAY: "Delay",
};

export const TRIGGER_TYPES = [
  "MANUAL",
  "SCHEDULE",
  "WEBHOOK",
  "EVENT",
] as const;

/**
 * Backend currently returns configuration as a JSON string:
 *
 * "{}"
 *
 * or:
 *
 * "{\"method\":\"GET\",\"url\":\"...\"}"
 *
 * The UI works with a normal object.
 */
function normalizeConfig(
  value: WorkflowNodeDto["configuration"],
): Record<string, unknown> {
  if (value == null) {
    return {};
  }

  if (typeof value === "object") {
    return { ...value };
  }

  if (typeof value === "string") {
    const trimmed = value.trim();

    if (!trimmed) {
      return {};
    }

    try {
      const parsed: unknown = JSON.parse(trimmed);

      if (
        parsed !== null &&
        typeof parsed === "object" &&
        !Array.isArray(parsed)
      ) {
        return parsed as Record<string, unknown>;
      }

      return {};
    } catch {
      console.warn(
        "Unable to parse workflow node configuration:",
        value,
      );

      return {};
    }
  }

  return {};
}

/**
 * Generates a stable-ish key for a newly-created UI node.
 *
 * Existing persisted nodes ALWAYS use the backend nodeKey.
 * This is only for nodes created locally before they are saved.
 */
export function makeNodeKey(type: string): string {
  const normalized = type.toLowerCase().replace(/[^a-z0-9]+/g, "_");

  return `${normalized}_${Date.now()}_${Math.random()
    .toString(36)
    .slice(2, 7)}`;
}

/**
 * React Flow IDs should be unique.
 *
 * For persisted nodes we prefer the backend UUID.
 * For newly-created nodes the UI's generated ID is used.
 */
function getFlowNodeId(
  dto: WorkflowNodeDto,
  index: number,
): string {
  return dto.id ?? `node-${index}`;
}

/**
 * Converts backend node type into the UI's known type.
 *
 * The backend currently returns HTTP_REQUEST.
 */
function normalizeNodeType(type: string | undefined): string {
  if (!type) {
    return "HTTP_REQUEST";
  }

  return type;
}

/**
 * Backend graph DTO -> React Flow graph.
 *
 * IMPORTANT:
 *
 * Backend edges reference nodeKey:
 *
 *   source: "trigger"
 *   target: "get_user"
 *
 * React Flow edges reference node IDs:
 *
 *   source: "backend-node-uuid"
 *   target: "backend-node-uuid"
 *
 * Therefore nodes MUST be mapped first and edges resolved afterwards.
 */
export function toFlowGraph(
  graph: WorkflowGraphDto | undefined,
): {
  nodes: FlowNode[];
  edges: FlowEdge[];
} {
  if (!graph) {
    return {
      nodes: [],
      edges: [],
    };
  }

  const nodeDtos = graph.nodes ?? [];
  const edgeDtos = graph.edges ?? [];

  /*
   * ------------------------------------------------------------
   * 1. Backend nodes -> React Flow nodes
   * ------------------------------------------------------------
   */
  const nodes: FlowNode[] = nodeDtos.map((dto, index) => {
    const id = getFlowNodeId(dto, index);

    const nodeKey =
      dto.nodeKey?.trim() ||
      `node_${index + 1}`;

    const nodeType = normalizeNodeType(dto.type);

    return {
      id,

      type: "flowforge",

      position: {
        // Backend graph currently doesn't persist positions,
        // therefore use deterministic fallback positioning.
        x: 120 + (index % 3) * 280,
        y: 80 + Math.floor(index / 3) * 160,
      },

      data: {
        label:
          dto.name?.trim() ||
          NODE_LABELS[nodeType] ||
          nodeKey,

        nodeKey,

        nodeType,

        config: normalizeConfig(dto.configuration),

        connectorId: dto.connectorId ?? null,

        backendId: dto.id,
      },
    };
  });

  /*
   * ------------------------------------------------------------
   * 2. Build node reference map
   * ------------------------------------------------------------
   *
   * Backend edges use nodeKey:
   *
   *   "trigger"
   *   "get_user"
   *
   * We need to resolve those to React Flow IDs.
   *
   * We support both nodeKey and backend UUID as references
   * because this makes the mapper resilient without changing
   * the backend contract.
   */
  const nodeIdByReference = new Map<string, string>();

  for (const node of nodes) {
    // React Flow ID
    nodeIdByReference.set(node.id, node.id);

    // Backend nodeKey
    if (node.data.nodeKey) {
      nodeIdByReference.set(
        node.data.nodeKey,
        node.id,
      );
    }

    // Backend UUID
    if (node.data.backendId) {
      nodeIdByReference.set(
        node.data.backendId,
        node.id,
      );
    }
  }

  /*
   * ------------------------------------------------------------
   * 3. Backend edges -> React Flow edges
   * ------------------------------------------------------------
   */
  const edges: FlowEdge[] = [];

  edgeDtos.forEach((dto, index) => {
    const sourceReference = dto.source;
    const targetReference = dto.target;

    if (!sourceReference || !targetReference) {
      console.warn(
        `Skipping invalid workflow edge ${dto.id ?? index}: missing source or target.`,
        dto,
      );

      return;
    }

    const source = nodeIdByReference.get(
      sourceReference,
    );

    const target = nodeIdByReference.get(
      targetReference,
    );

    if (!source || !target) {
      console.warn(
        `Skipping workflow edge ${dto.id ?? index}: unable to resolve source/target.`,
        {
          sourceReference,
          targetReference,
        },
      );

      return;
    }

    const condition = dto.condition ?? null;

    edges.push({
      id: dto.id ?? `edge-${index}`,

      source,

      target,

      animated: true,

      label: condition ?? undefined,

      data: {
        condition,
      },

      style: condition
        ? {
            strokeDasharray: "6 4",
            strokeWidth: 2,
          }
        : {
            strokeWidth: 2,
          },
    });
  });

  return {
    nodes,
    edges,
  };
}

/**
 * Shape expected by:
 *
 * PUT
 * /api/v1/workspaces/{workspaceId}/workflows/{workflowId}
 * /versions/{versionNumber}/graph
 *
 * IMPORTANT:
 *
 * This is NOT WorkflowGraphDto.
 *
 * The backend PUT contract does NOT expect:
 *
 * - id
 * - backendId
 * - nodeType
 * - config
 * - positionX
 * - positionY
 * - sourceNodeId
 * - targetNodeId
 *
 * It expects only:
 *
 * nodes:
 *   nodeKey
 *   name
 *   type
 *   configuration
 *   connectorId
 *
 * edges:
 *   source
 *   target
 *   condition
 */
export interface SaveGraphRequest {
  nodes: Array<{
    nodeKey: string;
    name: string;
    type: string;
    connectorId?: string | null;
    configuration: Record<string, unknown>;
  }>;

  edges: Array<{
    source: string;
    target: string;
    condition?: string | null;
  }>;
}

/**
 * React Flow graph -> backend PUT graph request.
 *
 * React Flow edge:
 *
 *   source = React Flow node UUID
 *   target = React Flow node UUID
 *
 * Backend expects:
 *
 *   source = nodeKey
 *   target = nodeKey
 */
export function toGraphDto(
  nodes: FlowNode[],
  edges: FlowEdge[],
): SaveGraphRequest {
  /*
   * Map React Flow node ID -> backend nodeKey.
   */
  const nodeKeyByFlowId = new Map<string, string>();

  for (const node of nodes) {
    const nodeKey =
      node.data.nodeKey?.trim();

    if (nodeKey) {
      nodeKeyByFlowId.set(
        node.id,
        nodeKey,
      );
    }
  }

  /*
   * ------------------------------------------------------------
   * Nodes
   * ------------------------------------------------------------
   *
   * configuration stays an OBJECT.
   *
   * DO NOT JSON.stringify() here.
   */
  const backendNodes = nodes.map((node) => ({
    nodeKey:
      node.data.nodeKey?.trim() ||
      node.id,

    name:
      node.data.label?.trim() ||
      node.data.nodeKey ||
      node.id,

    type:
      node.data.nodeType,

    connectorId:
      node.data.connectorId ?? null,

    configuration:
      node.data.config ?? {},
  }));

  /*
   * ------------------------------------------------------------
   * Edges
   * ------------------------------------------------------------
   *
   * Convert:
   *
   * React Flow ID -> nodeKey
   */
  const backendEdges = edges.map((edge) => {
    const source =
      nodeKeyByFlowId.get(edge.source);

    const target =
      nodeKeyByFlowId.get(edge.target);

    if (!source || !target) {
      throw new Error(
        `Unable to map edge "${edge.id}" to backend node keys.`,
      );
    }

    const condition =
      edge.data?.condition ??
      (typeof edge.label === "string"
        ? edge.label
        : null);

    return {
      source,
      target,
      condition,
    };
  });

  return {
    nodes: backendNodes,
    edges: backendEdges,
  };
}

/**
 * Default configuration for newly-created nodes.
 */
export function defaultConfigFor(
  type: string,
): Record<string, unknown> {
  switch (type) {
    case TRIGGER_NODE_TYPE:
      return {
        triggerType: "MANUAL",
      };

    case "HTTP_REQUEST":
      return {
        method: "GET",
        url: "",
        headers: {},
        queryParams: {},
        body: {},
      };

    case "AI_PROMPT":
      return {
        prompt: "",
      };

    case "CONDITION":
      return {
        expression: "",
      };

    case "TRANSFORM":
      return {
        expression: "",
      };

    case "DELAY":
      return {
        durationMs: 1000,
      };

    default:
      return {};
  }
}

export interface GraphValidationResult {
  ok: boolean;
  message?: string;
}

/**
 * Client-side graph validation.
 */
export function validateGraph(
  nodes: FlowNode[],
  edges: FlowEdge[],
): GraphValidationResult {
  /*
   * Exactly one trigger.
   */
  const triggers = nodes.filter(
    (node) =>
      node.data.nodeType ===
      TRIGGER_NODE_TYPE,
  );

  if (triggers.length === 0) {
    return {
      ok: false,
      message:
        "Workflow must contain a Trigger node.",
    };
  }

  if (triggers.length > 1) {
    return {
      ok: false,
      message:
        "A workflow can have only one Trigger node.",
    };
  }

  /*
   * Every node needs a unique nodeKey.
   */
  const keys = new Set<string>();

  for (const node of nodes) {
    const key =
      node.data.nodeKey?.trim();

    if (!key) {
      return {
        ok: false,
        message: `Node "${node.data.label}" is missing a node key.`,
      };
    }

    if (keys.has(key)) {
      return {
        ok: false,
        message: `Duplicate node key "${key}".`,
      };
    }

    keys.add(key);
  }

  /*
   * Every React Flow edge must reference an
   * existing React Flow node.
   */
  const nodeIds = new Set(
    nodes.map((node) => node.id),
  );

  for (const edge of edges) {
    if (
      !nodeIds.has(edge.source) ||
      !nodeIds.has(edge.target)
    ) {
      return {
        ok: false,
        message:
          "An edge references a node that no longer exists.",
      };
    }
  }

  return {
    ok: true,
  };
}
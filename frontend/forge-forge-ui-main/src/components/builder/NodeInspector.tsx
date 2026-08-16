import { Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";

import {
  TRIGGER_NODE_TYPE,
  TRIGGER_TYPES,
  type FlowNode,
} from "@/lib/mappers/workflowGraphMapper";

interface Props {
  node: FlowNode | undefined;
  onChangeLabel: (label: string) => void;
  onChangeConfig: (config: Record<string, unknown>) => void;
  onDelete: () => void;
}

const HTTP_METHODS = [
  "GET",
  "POST",
  "PUT",
  "PATCH",
  "DELETE",
] as const;

function stringifyJson(value: unknown): string {
  if (typeof value === "string") {
    return value;
  }

  try {
    return JSON.stringify(value ?? {}, null, 2);
  } catch {
    return "{}";
  }
}

function parseJsonObject(value: string): Record<string, unknown> | string {
  if (!value.trim()) {
    return {};
  }

  try {
    const parsed: unknown = JSON.parse(value);

    if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
      return parsed as Record<string, unknown>;
    }

    return value;
  } catch {
    return value;
  }
}

export function NodeInspector({
  node,
  onChangeLabel,
  onChangeConfig,
  onDelete,
}: Props) {
  if (!node) {
    return (
      <div className="w-80 shrink-0 border-l border-border bg-surface p-4">
        <p className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
          Inspector
        </p>

        <p className="mt-3 text-sm text-muted-foreground">
          Select a node on the canvas to configure it.
        </p>
      </div>
    );
  }

  const config = node.data.config ?? {};

  const set = (key: string, value: unknown) => {
    onChangeConfig({
      ...config,
      [key]: value,
    });
  };

  const str = (key: string): string => {
    const value = config[key];

    if (value === undefined || value === null) {
      return "";
    }

    if (typeof value === "string") {
      return value;
    }

    return String(value);
  };

  const nodeType = node.data.nodeType;

  const isTrigger = nodeType === TRIGGER_NODE_TYPE;

  const isHttp =
    nodeType === "HTTP_REQUEST" ||
    nodeType === "HTTP";

  return (
    <div className="w-80 shrink-0 overflow-y-auto border-l border-border bg-surface">
      <div className="space-y-5 p-4">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
              Node Inspector
            </p>

            <p className="mt-1 text-xs text-muted-foreground">
              {nodeType}
            </p>
          </div>

          <Button
            variant="ghost"
            size="sm"
            className="text-destructive hover:bg-destructive/10 hover:text-destructive"
            onClick={onDelete}
          >
            <Trash2 className="size-4" />
          </Button>
        </div>

        {/* Name */}
        <div className="space-y-2">
          <Label htmlFor="node-name">Name</Label>

          <Input
            id="node-name"
            value={node.data.label}
            onChange={(event) =>
              onChangeLabel(event.target.value)
            }
            placeholder="Node name"
          />
        </div>

        {/* Node key */}
        <div className="space-y-2">
          <Label htmlFor="node-key">Node Key</Label>

          <Input
            id="node-key"
            value={node.data.nodeKey}
            readOnly
            className="bg-secondary/30 font-mono text-xs"
          />

          <p className="text-[11px] leading-relaxed text-muted-foreground">
            Stable identifier used by workflow edges and runtime
            expressions. It cannot be changed after creation.
          </p>
        </div>

        {/* Trigger */}
        {isTrigger && (
          <div className="space-y-4 rounded-lg border border-warning/30 bg-warning/5 p-3">
            <div>
              <p className="text-sm font-medium text-foreground">
                Trigger Configuration
              </p>

              <p className="mt-1 text-xs text-muted-foreground">
                Defines how this workflow starts.
              </p>
            </div>

            <div className="space-y-2">
              <Label>Trigger Type</Label>

              <Select
                value={str("triggerType") || "MANUAL"}
                onValueChange={(value) =>
                  set("triggerType", value)
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select trigger type" />
                </SelectTrigger>

                <SelectContent>
                  {TRIGGER_TYPES.map((triggerType) => (
                    <SelectItem
                      key={triggerType}
                      value={triggerType}
                    >
                      {triggerType}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {str("triggerType") === "SCHEDULE" && (
              <div className="space-y-2">
                <Label htmlFor="trigger-cron">
                  Cron Expression
                </Label>

                <Input
                  id="trigger-cron"
                  value={str("cronExpression")}
                  onChange={(event) =>
                    set(
                      "cronExpression",
                      event.target.value,
                    )
                  }
                  placeholder="0 0 * * *"
                  className="font-mono text-xs"
                />
              </div>
            )}

            {str("triggerType") === "WEBHOOK" && (
              <div className="space-y-2">
                <Label htmlFor="trigger-path">
                  Webhook Path
                </Label>

                <Input
                  id="trigger-path"
                  value={str("webhookPath")}
                  onChange={(event) =>
                    set(
                      "webhookPath",
                      event.target.value,
                    )
                  }
                  placeholder="/webhooks/order-created"
                  className="font-mono text-xs"
                />
              </div>
            )}

            {str("triggerType") === "EVENT" && (
              <div className="space-y-2">
                <Label htmlFor="trigger-event">
                  Event Type
                </Label>

                <Input
                  id="trigger-event"
                  value={str("eventType")}
                  onChange={(event) =>
                    set("eventType", event.target.value)
                  }
                  placeholder="order.created"
                  className="font-mono text-xs"
                />
              </div>
            )}
          </div>
        )}

        {/* HTTP Request */}
        {isHttp && (
          <div className="space-y-4">
            <div>
              <p className="text-sm font-medium text-foreground">
                HTTP Request
              </p>

              <p className="mt-1 text-xs text-muted-foreground">
                Configure the outbound HTTP request.
              </p>
            </div>

            {/* Method */}
            <div className="space-y-2">
              <Label>Method</Label>

              <Select
                value={str("method") || "GET"}
                onValueChange={(value) =>
                  set("method", value)
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>

                <SelectContent>
                  {HTTP_METHODS.map((method) => (
                    <SelectItem
                      key={method}
                      value={method}
                    >
                      {method}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* URL */}
            <div className="space-y-2">
              <Label htmlFor="node-url">URL</Label>

              <Input
                id="node-url"
                value={str("url")}
                onChange={(event) =>
                  set("url", event.target.value)
                }
                placeholder="https://api.example.com/users/{{input.userId}}"
                className="font-mono text-xs"
              />
            </div>

            {/* Query Params */}
            <div className="space-y-2">
              <Label htmlFor="node-query-params">
                Query Parameters
              </Label>

              <Textarea
                id="node-query-params"
                rows={5}
                className="font-mono text-xs"
                value={stringifyJson(
                  config["queryParams"] ??
                    config["queryParameters"] ??
                    {},
                )}
                onChange={(event) =>
                  set(
                    "queryParams",
                    parseJsonObject(event.target.value),
                  )
                }
                placeholder={`{
  "userId": "{{input.userId}}"
}`}
              />

              <p className="text-[11px] leading-relaxed text-muted-foreground">
                JSON object. Runtime expressions such as
                {" "}
                <code className="font-mono">
                  {"{{input.userId}}"}
                </code>
                {" "}
                are resolved by the backend.
              </p>
            </div>

            {/* Headers */}
            <div className="space-y-2">
              <Label htmlFor="node-headers">
                Headers
              </Label>

              <Textarea
                id="node-headers"
                rows={6}
                className="font-mono text-xs"
                value={stringifyJson(
                  config["headers"] ?? {},
                )}
                onChange={(event) =>
                  set(
                    "headers",
                    parseJsonObject(event.target.value),
                  )
                }
                placeholder={`{
  "Content-Type": "application/json"
}`}
              />
            </div>

            {/* Body */}
            <div className="space-y-2">
              <Label htmlFor="node-body">
                Body
              </Label>

              <Textarea
                id="node-body"
                rows={8}
                className="font-mono text-xs"
                value={stringifyJson(
                  config["body"] ?? "",
                )}
                onChange={(event) => {
                  const value = event.target.value;

                  // Preserve JSON objects when valid.
                  if (
                    value.trim().startsWith("{") ||
                    value.trim().startsWith("[")
                  ) {
                    set("body", parseJsonObject(value));
                  } else {
                    set("body", value);
                  }
                }}
                placeholder={`{
  "userId": "{{input.userId}}"
}`}
              />

              <p className="text-[11px] leading-relaxed text-muted-foreground">
                Can be a JSON object or a plain string depending on
                the backend node configuration.
              </p>
            </div>
          </div>
        )}

        {/* AI Prompt */}
        {nodeType === "AI_PROMPT" && (
          <div className="space-y-3">
            <div>
              <p className="text-sm font-medium text-foreground">
                AI Prompt
              </p>

              <p className="mt-1 text-xs text-muted-foreground">
                Prompt sent to the configured AI model.
              </p>
            </div>

            <Textarea
              id="node-prompt"
              rows={10}
              className="font-mono text-xs"
              value={str("prompt")}
              onChange={(event) =>
                set("prompt", event.target.value)
              }
              placeholder="Summarize the user data: {{nodes.get_user}}"
            />

            <p className="text-[11px] leading-relaxed text-muted-foreground">
              Runtime expressions can reference workflow input and
              previous node output.
            </p>
          </div>
        )}

        {/* Condition */}
        {nodeType === "CONDITION" && (
          <div className="space-y-3">
            <div>
              <p className="text-sm font-medium text-foreground">
                Condition
              </p>

              <p className="mt-1 text-xs text-muted-foreground">
                Define the expression used to evaluate this node.
              </p>
            </div>

            <Textarea
              id="node-expression"
              rows={6}
              className="font-mono text-xs"
              value={str("expression")}
              onChange={(event) =>
                set("expression", event.target.value)
              }
              placeholder="{{nodes.get_user.id}} == 1"
            />
          </div>
        )}

        {/* Transform */}
        {nodeType === "TRANSFORM" && (
          <div className="space-y-3">
            <div>
              <p className="text-sm font-medium text-foreground">
                Transform
              </p>

              <p className="mt-1 text-xs text-muted-foreground">
                Define how the input data should be transformed.
              </p>
            </div>

            <Textarea
              id="node-transform-expression"
              rows={7}
              className="font-mono text-xs"
              value={str("expression")}
              onChange={(event) =>
                set("expression", event.target.value)
              }
              placeholder="..."
            />
          </div>
        )}

        {/* Delay */}
        {nodeType === "DELAY" && (
          <div className="space-y-3">
            <div>
              <p className="text-sm font-medium text-foreground">
                Delay
              </p>

              <p className="mt-1 text-xs text-muted-foreground">
                Pause workflow execution before continuing.
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="node-duration">
                Duration (ms)
              </Label>

              <Input
                id="node-duration"
                type="number"
                min={0}
                value={str("durationMs") || "1000"}
                onChange={(event) =>
                  set(
                    "durationMs",
                    Number(event.target.value),
                  )
                }
              />
            </div>
          </div>
        )}

        {/* Raw configuration */}
        <div className="border-t border-border pt-4">
          <div className="space-y-2">
            <Label htmlFor="node-raw">
              Raw Configuration
            </Label>

            <Textarea
              id="node-raw"
              rows={8}
              className="font-mono text-xs"
              value={JSON.stringify(config, null, 2)}
              onChange={(event) => {
                try {
                  const parsed: unknown = JSON.parse(
                    event.target.value || "{}",
                  );

                  if (
                    parsed &&
                    typeof parsed === "object" &&
                    !Array.isArray(parsed)
                  ) {
                    onChangeConfig(
                      parsed as Record<string, unknown>,
                    );
                  }
                } catch {
                  // Do not overwrite valid state while JSON is temporarily invalid.
                }
              }}
            />

            <p className="text-[11px] leading-relaxed text-muted-foreground">
              Advanced option. Changes are applied only when the
              JSON is valid.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
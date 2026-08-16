import { cn } from "@/lib/utils";

const STATUS_STYLES: Record<string, string> = {
  SUCCESS: "border-success/40 bg-success/10 text-success",
  COMPLETED: "border-success/40 bg-success/10 text-success",
  PUBLISHED: "border-success/40 bg-success/10 text-success",
  ACTIVE: "border-success/40 bg-success/10 text-success",
  RUNNING: "border-info/40 bg-info/10 text-info",
  QUEUED: "border-warning/40 bg-warning/10 text-warning",
  PENDING: "border-warning/40 bg-warning/10 text-warning",
  DRAFT: "border-border bg-muted text-muted-foreground",
  FAILED: "border-destructive/40 bg-destructive/10 text-destructive",
  ERROR: "border-destructive/40 bg-destructive/10 text-destructive",
};

export function StatusBadge({ status, className }: { status?: string | null; className?: string }) {
  const value = (status ?? "UNKNOWN").toUpperCase();
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2 py-0.5 font-mono text-[11px] uppercase tracking-wide",
        STATUS_STYLES[value] ?? "border-border bg-muted text-muted-foreground",
        className,
      )}
    >
      {value}
    </span>
  );
}

export function JsonViewer({ value, empty = "—" }: { value: unknown; empty?: string }) {
  if (value === null || value === undefined || value === "") {
    return <p className="text-sm text-muted-foreground">{empty}</p>;
  }
  let text: string;
  if (typeof value === "string") {
    try {
      text = JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      text = value;
    }
  } else {
    text = JSON.stringify(value, null, 2);
  }
  return (
    <pre className="max-h-80 overflow-auto rounded-md border border-border bg-code p-3 font-mono text-xs leading-relaxed text-foreground">
      {text}
    </pre>
  );
}

import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { Loader2, Workflow } from "lucide-react";
import { useEffect, useState, type FormEvent } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/context/AuthContext";
import { getErrorMessage } from "@/lib/errorHandler";

export const Route = createFileRoute("/login")({
  ssr: false,
  head: () => ({
    meta: [
      { title: "Sign in — FlowForge" },
      { name: "description", content: "Sign in to FlowForge to build, version and run workflows." },
      { property: "og:title", content: "Sign in — FlowForge" },
      {
        property: "og:description",
        content: "Sign in to FlowForge to build, version and run workflows.",
      },
    ],
  }),
  component: LoginPage,
});

function LoginPage() {
  const { login, isAuthenticated, isReady } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isReady && isAuthenticated) void navigate({ to: "/dashboard", replace: true });
  }, [isReady, isAuthenticated, navigate]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await login(email, password);
      toast.success("Signed in");
      void navigate({ to: "/dashboard", replace: true });
    } catch (loginError) {
      const message = getErrorMessage(loginError);
      setError(message);
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="grid min-h-screen bg-background lg:grid-cols-2">
      <div className="hidden flex-col justify-between border-r border-border bg-surface p-12 lg:flex">
        <div className="flex items-center gap-2 text-foreground">
          <Workflow className="size-5 text-primary" />
          <span className="font-mono text-sm font-semibold tracking-tight">FlowForge</span>
        </div>
        <div className="space-y-4">
          <h2 className="text-3xl font-semibold tracking-tight text-foreground">
            Orchestrate workflows that actually ship.
          </h2>
          <p className="max-w-md text-sm text-muted-foreground">
            Versioned graphs, durable execution backed by Kafka and an outbox, and full run history
            — in one technical control plane.
          </p>
        </div>
        <p className="font-mono text-xs text-muted-foreground">FlowForge control plane</p>
      </div>

      <div className="flex items-center justify-center p-6">
        <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-6">
          <div className="space-y-2">
            <div className="flex items-center gap-2 lg:hidden">
              <Workflow className="size-5 text-primary" />
              <span className="font-mono text-sm font-semibold">FlowForge</span>
            </div>
            <h1 className="text-2xl font-semibold tracking-tight">Sign in</h1>
            <p className="text-sm text-muted-foreground">Use your FlowForge account credentials.</p>
          </div>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="••••••••"
              />
            </div>
          </div>

          {error ? (
            <p className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {error}
            </p>
          ) : null}

          <Button type="submit" className="w-full" disabled={submitting}>
            {submitting ? <Loader2 className="size-4 animate-spin" /> : null}
            {submitting ? "Signing in..." : "Sign in"}
          </Button>
        </form>
      </div>
    </main>
  );
}

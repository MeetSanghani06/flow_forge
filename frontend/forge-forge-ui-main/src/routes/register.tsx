import { Link, createFileRoute, useNavigate } from "@tanstack/react-router";
import { Loader2, Workflow } from "lucide-react";
import { useState, type FormEvent } from "react";
import { toast } from "sonner";

import { register } from "@/api/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/errorHandler";

export const Route = createFileRoute("/register")({
  ssr: false,
  head: () => ({
    meta: [
      { title: "Create account — FlowForge" },
      {
        name: "description",
        content: "Create a FlowForge account to build, version and run workflows.",
      },
      { property: "og:title", content: "Create account — FlowForge" },
      {
        property: "og:description",
        content: "Create a FlowForge account to build, version and run workflows.",
      },
    ],
  }),
  component: RegisterPage,
});

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function validate(): string | null {
    if (!email.trim()) return "Email is required.";
    if (!EMAIL_PATTERN.test(email.trim())) return "Enter a valid email address.";
    if (!password) return "Password is required.";
    if (!confirmPassword) return "Please confirm your password.";
    if (password !== confirmPassword) return "Passwords do not match.";
    return null;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      // Registration never returns tokens — the user must sign in afterwards.
      await register({ email: email.trim(), password });
      toast.success("Account created successfully. Please sign in.");
      void navigate({ to: "/login", replace: true });
    } catch (registerError) {
      const message = getErrorMessage(registerError);
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
            Create your FlowForge account.
          </h2>
          <p className="max-w-md text-sm text-muted-foreground">
            Versioned graphs, durable execution and full run history — in one technical control
            plane.
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
            <h1 className="text-2xl font-semibold tracking-tight">Create account</h1>
            <p className="text-sm text-muted-foreground">
              Registration creates your account — you will sign in afterwards.
            </p>
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
                autoComplete="new-password"
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="••••••••"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirm-password">Confirm password</Label>
              <Input
                id="confirm-password"
                type="password"
                autoComplete="new-password"
                required
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
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
            {submitting ? "Creating account..." : "Create account"}
          </Button>

          <p className="text-center text-sm text-muted-foreground">
            Already have an account?{" "}
            <Link to="/login" className="font-medium text-primary hover:underline">
              Sign in
            </Link>
          </p>
        </form>
      </div>
    </main>
  );
}

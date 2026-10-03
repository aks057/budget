import { stepLabel } from "@/lib/agent/tools";
import type { ToolStatus } from "@/lib/api/types";
import { cn } from "@/lib/utils";
import { AlertTriangle, CheckCircle2, Hand, Loader2 } from "lucide-react";

export interface ToolStep {
  name: string;
  /** Undefined while the tool is still running. */
  status?: ToolStatus;
  latencyMs?: number;
}

/** What the agent did, step by step: makes every number in the answer traceable to a tool call. */
export function ToolTimeline({ steps }: { steps: ToolStep[] }) {
  if (steps.length === 0) return null;
  return (
    <ol className="space-y-1.5" aria-label="Agent steps">
      {steps.map((step, index) => (
        <li key={index} className="flex items-center gap-2 text-xs text-muted-foreground">
          <StepIcon status={step.status} />
          <span className={cn(step.status === "REJECTED" || step.status === "ERROR" ? "line-through opacity-70" : "")}>
            {stepLabel(step.name)}
          </span>
          {step.status === "PENDING_CONFIRMATION" && <span className="text-amber-500">· needs your OK</span>}
          {step.latencyMs !== undefined && step.status === "SUCCESS" && (
            <span className="tabular-nums opacity-60">{step.latencyMs} ms</span>
          )}
        </li>
      ))}
    </ol>
  );
}

function StepIcon({ status }: { status?: ToolStatus }) {
  switch (status) {
    case undefined:
      return <Loader2 className="h-3.5 w-3.5 animate-spin" aria-label="running" />;
    case "SUCCESS":
      return <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" aria-label="done" />;
    case "PENDING_CONFIRMATION":
      return <Hand className="h-3.5 w-3.5 text-amber-500" aria-label="awaiting confirmation" />;
    default:
      return <AlertTriangle className="h-3.5 w-3.5 text-red-400" aria-label="could not run" />;
  }
}

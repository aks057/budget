"use client";

import { stepLabel } from "@/lib/agent/tools";
import type { ToolStatus } from "@/lib/api/types";
import { cn } from "@/lib/utils";
import { AnimatePresence, motion } from "framer-motion";
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
        <motion.li
          key={index}
          initial={{ opacity: 0, x: -8 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.3 }}
          className="flex items-center gap-2 text-xs text-muted-foreground"
        >
          <span className="relative flex h-3.5 w-3.5 items-center justify-center">
            <AnimatePresence mode="wait" initial={false}>
              <motion.span
                key={step.status ?? "running"}
                initial={{ scale: 0.4, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.4, opacity: 0 }}
                transition={{ type: "spring", stiffness: 500, damping: 25 }}
                className="absolute inset-0 flex items-center justify-center"
              >
                <StepIcon status={step.status} />
              </motion.span>
            </AnimatePresence>
          </span>
          <span className={cn(step.status === "REJECTED" || step.status === "ERROR" ? "line-through opacity-70" : "")}>
            {stepLabel(step.name)}
          </span>
          {step.status === "PENDING_CONFIRMATION" && <span className="text-warning">· needs your OK</span>}
          {step.latencyMs !== undefined && step.status === "SUCCESS" && (
            <span className="tabular-nums opacity-60">{step.latencyMs} ms</span>
          )}
        </motion.li>
      ))}
    </ol>
  );
}

function StepIcon({ status }: { status?: ToolStatus }) {
  switch (status) {
    case undefined:
      return <Loader2 className="h-3.5 w-3.5 animate-spin text-primary" aria-label="running" />;
    case "SUCCESS":
      return <CheckCircle2 className="h-3.5 w-3.5 text-income" aria-label="done" />;
    case "PENDING_CONFIRMATION":
      return <Hand className="h-3.5 w-3.5 text-warning" aria-label="awaiting confirmation" />;
    default:
      return <AlertTriangle className="h-3.5 w-3.5 text-expense" aria-label="could not run" />;
  }
}

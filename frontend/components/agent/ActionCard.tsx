import { Button } from "@/components/ui/button";
import { actionTitle } from "@/lib/agent/tools";
import type { PendingActionDto } from "@/lib/api/types";
import { cn } from "@/lib/utils";
import { Check, CheckCircle2, Loader2, ShieldQuestion, X, XCircle } from "lucide-react";

export type ActionState =
  | { phase: "pending" }
  | { phase: "working"; intent: "confirm" | "reject" }
  | { phase: "executed"; message: string }
  | { phase: "cancelled" }
  | { phase: "failed"; message: string };

interface Props {
  action: PendingActionDto;
  state: ActionState;
  onConfirm: () => void;
  onReject: () => void;
}

/** Human in the loop: the agent only proposes writes; nothing changes until the user confirms here. */
export function ActionCard({ action, state, onConfirm, onReject }: Props) {
  const working = state.phase === "working";
  return (
    <div
      className={cn(
        "rounded-lg border p-3",
        state.phase === "pending" || working ? "border-amber-500/50 bg-amber-500/5" : "bg-muted/30"
      )}
    >
      <div className="flex items-start gap-3">
        <ShieldQuestion className="mt-0.5 h-5 w-5 shrink-0 text-amber-500" />
        <div className="min-w-0 flex-1">
          <p className="text-sm font-semibold">{actionTitle(action.toolName)}</p>
          <p className="text-sm text-muted-foreground">{action.summary}</p>

          {(state.phase === "pending" || working) && (
            <div className="mt-3 flex gap-2">
              <Button size="sm" onClick={onConfirm} disabled={working}>
                {working && state.intent === "confirm" ? (
                  <Loader2 className="mr-1 h-4 w-4 animate-spin" />
                ) : (
                  <Check className="mr-1 h-4 w-4" />
                )}
                Confirm
              </Button>
              <Button size="sm" variant="outline" onClick={onReject} disabled={working}>
                {working && state.intent === "reject" ? (
                  <Loader2 className="mr-1 h-4 w-4 animate-spin" />
                ) : (
                  <X className="mr-1 h-4 w-4" />
                )}
                Cancel
              </Button>
            </div>
          )}
          {state.phase === "executed" && (
            <p className="mt-2 flex items-center gap-1.5 text-sm text-emerald-500">
              <CheckCircle2 className="h-4 w-4" /> {state.message || "Done"}
            </p>
          )}
          {state.phase === "cancelled" && (
            <p className="mt-2 flex items-center gap-1.5 text-sm text-muted-foreground">
              <XCircle className="h-4 w-4" /> Cancelled. Nothing was changed.
            </p>
          )}
          {state.phase === "failed" && (
            <p className="mt-2 flex items-center gap-1.5 text-sm text-red-500">
              <XCircle className="h-4 w-4" /> {state.message}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

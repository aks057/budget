import { ActionCard, ActionState } from "@/components/agent/ActionCard";
import { FormattedText } from "@/components/agent/FormattedText";
import { ToolTimeline } from "@/components/agent/ToolTimeline";
import type { ChatItem } from "@/lib/agent/useAgentChat";
import { cn } from "@/lib/utils";
import { AlertCircle, Sparkles } from "lucide-react";

interface Props {
  item: ChatItem;
  actionState: (actionId: string) => ActionState;
  onConfirm: (actionId: string) => void;
  onReject: (actionId: string) => void;
}

export function ChatMessage({ item, actionState, onConfirm, onReject }: Props) {
  if (item.kind === "user") {
    return (
      <div className="flex justify-end">
        <div className="max-w-[85%] whitespace-pre-wrap rounded-2xl rounded-br-sm bg-primary px-4 py-2 text-sm text-primary-foreground">
          {item.content}
        </div>
      </div>
    );
  }

  const thinking = item.status === "streaming" && item.steps.length === 0;
  return (
    <div className="flex gap-3">
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-white">
        <Sparkles className="h-4 w-4" />
      </div>
      <div className="min-w-0 flex-1 space-y-3 pt-1">
        <ToolTimeline steps={item.steps} />
        {thinking && <p className="animate-pulse text-sm text-muted-foreground">Thinking…</p>}
        {item.content && <FormattedText text={item.content} />}
        {item.actions.map((action) => (
          <ActionCard
            key={action.actionId}
            action={action}
            state={actionState(action.actionId)}
            onConfirm={() => onConfirm(action.actionId)}
            onReject={() => onReject(action.actionId)}
          />
        ))}
        {item.status === "error" && (
          <p className={cn("flex items-start gap-2 text-sm text-red-500")} role="alert">
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" /> {item.error}
          </p>
        )}
      </div>
    </div>
  );
}

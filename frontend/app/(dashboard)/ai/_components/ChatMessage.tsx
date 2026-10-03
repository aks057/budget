import { ActionCard, ActionState } from "@/components/agent/ActionCard";
import { FormattedText } from "@/components/agent/FormattedText";
import { ToolTimeline } from "@/components/agent/ToolTimeline";
import { EASE_OUT } from "@/components/motion";
import type { ChatItem } from "@/lib/agent/useAgentChat";
import { motion } from "framer-motion";
import { AlertCircle, Sparkles } from "lucide-react";

interface Props {
  item: ChatItem;
  actionState: (actionId: string) => ActionState;
  onConfirm: (actionId: string) => void;
  onReject: (actionId: string) => void;
}

const enter = {
  initial: { opacity: 0, y: 12, scale: 0.98 },
  animate: { opacity: 1, y: 0, scale: 1 },
  transition: { duration: 0.4, ease: EASE_OUT },
};

export function ChatMessage({ item, actionState, onConfirm, onReject }: Props) {
  if (item.kind === "user") {
    return (
      <motion.div {...enter} className="flex justify-end">
        <div className="max-w-[85%] whitespace-pre-wrap rounded-2xl rounded-br-md bg-primary px-4 py-2.5 text-sm text-primary-foreground ">
          {item.content}
        </div>
      </motion.div>
    );
  }

  const thinking = item.status === "streaming" && item.steps.length === 0;
  return (
    <motion.div {...enter} className="flex gap-3">
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-gradient text-brand-foreground ">
        <Sparkles className="h-4 w-4" />
      </div>
      <div className="min-w-0 flex-1 space-y-3 rounded-2xl rounded-tl-md border bg-card px-4 py-3">
        <ToolTimeline steps={item.steps} />
        {thinking && <ThinkingDots />}
        {item.content && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.4 }}>
            <FormattedText text={item.content} />
          </motion.div>
        )}
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
          <p className="flex items-start gap-2 text-sm text-expense" role="alert">
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" /> {item.error}
          </p>
        )}
      </div>
    </motion.div>
  );
}

function ThinkingDots() {
  return (
    <span className="flex items-center gap-1 py-1" aria-label="Thinking">
      {[0, 1, 2].map((dot) => (
        <motion.span
          key={dot}
          className="h-2 w-2 rounded-full bg-primary"
          animate={{ y: [0, -5, 0], opacity: [0.4, 1, 0.4] }}
          transition={{ duration: 0.9, repeat: Infinity, delay: dot * 0.15, ease: "easeInOut" }}
        />
      ))}
    </span>
  );
}

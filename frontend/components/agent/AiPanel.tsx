"use client";

import { ChatComposer } from "@/app/(dashboard)/ai/_components/ChatComposer";
import { ChatMessage } from "@/app/(dashboard)/ai/_components/ChatMessage";
import { ActionCard } from "@/components/agent/ActionCard";
import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetDescription, SheetTitle } from "@/components/ui/sheet";
import { SUGGESTED_PROMPTS } from "@/lib/agent/tools";
import { useAgentChat } from "@/lib/agent/useAgentChat";
import { ArrowUpRight, Maximize2, Plus, Sparkles } from "lucide-react";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";

/**
 * Slide-in AI chat available on every app page. The chat hook lives here (outside the sheet content, which
 * unmounts when closed), so a conversation survives closing and reopening the panel.
 */
export function AiPanel() {
  const { open, setOpen, pendingQuestion, consumePendingQuestion } = useAiPanel();
  const [conversationId, setConversationId] = useState<string | undefined>(undefined);
  const chat = useAgentChat({ conversationId, onConversationCreated: setConversationId });
  const { send, busy } = chat;

  // A question handed over via ask(): send it once the panel is open and idle.
  useEffect(() => {
    if (!open || !pendingQuestion || busy) return;
    const question = consumePendingQuestion();
    if (question) send(question);
  }, [busy, consumePendingQuestion, open, pendingQuestion, send]);

  const endRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (open) endRef.current?.scrollIntoView({ block: "end", behavior: "smooth" });
  }, [chat.items, chat.restoredActions, open]);

  const empty = !chat.loadingHistory && chat.items.length === 0;

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetContent side="right" className="flex w-full flex-col gap-0 p-0 sm:max-w-[440px]">
        {/* Header (right padding leaves room for the sheet's close button) */}
        <div className="flex items-center justify-between gap-2 border-b px-4 py-3 pr-12">
          <div className="flex items-center gap-2.5">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-gradient">
              <Sparkles className="h-4 w-4 text-brand-foreground" />
            </span>
            <div>
              <SheetTitle className="text-base">Bud-Wiser AI</SheetTitle>
              <SheetDescription className="text-xs">Answers from your data · changes need your OK</SheetDescription>
            </div>
          </div>
          <div className="flex items-center gap-1">
            <Button variant="ghost" size="icon" className="h-8 w-8" aria-label="New chat" title="New chat" onClick={() => setConversationId(undefined)}>
              <Plus className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" className="h-8 w-8" aria-label="Open full chat" title="Open full chat" asChild>
              <Link href={conversationId ? `/ai?c=${conversationId}` : "/ai"} onClick={() => setOpen(false)}>
                <Maximize2 className="h-4 w-4" />
              </Link>
            </Button>
          </div>
        </div>

        {/* Conversation */}
        <div className="flex-1 space-y-5 overflow-y-auto px-4 py-5" aria-live="polite">
          {empty && (
            <div className="flex flex-col items-center gap-4 pt-8 text-center">
              <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-gradient">
                <Sparkles className="h-6 w-6 text-brand-foreground" />
              </span>
              <div>
                <p className="font-display text-lg font-semibold">How can I help?</p>
                <p className="text-sm text-muted-foreground">Ask about your spending, budgets or goals, or tell me what you spent.</p>
              </div>
              <div className="grid w-full gap-2">
                {SUGGESTED_PROMPTS.slice(0, 4).map((prompt) => (
                  <button
                    key={prompt}
                    type="button"
                    onClick={() => send(prompt)}
                    className="flex items-center justify-between gap-3 rounded-lg border bg-card px-3 py-2.5 text-left text-sm transition-colors hover:border-primary/40 hover:bg-accent"
                  >
                    {prompt}
                    <ArrowUpRight className="h-4 w-4 shrink-0 text-muted-foreground" />
                  </button>
                ))}
              </div>
            </div>
          )}
          {chat.historyError && <p className="text-sm text-expense">{chat.historyError}</p>}
          {chat.items.map((item) => (
            <ChatMessage key={item.id} item={item} actionState={chat.actionState} onConfirm={chat.confirm} onReject={chat.reject} />
          ))}
          {chat.restoredActions.map((action) => (
            <ActionCard
              key={action.actionId}
              action={action}
              state={chat.actionState(action.actionId)}
              onConfirm={() => chat.confirm(action.actionId)}
              onReject={() => chat.reject(action.actionId)}
            />
          ))}
          <div ref={endRef} />
        </div>

        {/* Composer */}
        <div className="border-t p-3">
          <ChatComposer busy={chat.busy} onSend={chat.send} onStop={chat.stop} />
        </div>
      </SheetContent>
    </Sheet>
  );
}

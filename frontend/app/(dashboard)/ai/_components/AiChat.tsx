"use client";

import { ActionCard } from "@/components/agent/ActionCard";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { SUGGESTED_PROMPTS } from "@/lib/agent/tools";
import { useAgentChat } from "@/lib/agent/useAgentChat";
import { Stagger, StaggerItem } from "@/components/motion";
import { ArrowUpRight, History, ShieldCheck, Sparkles } from "lucide-react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { ChatComposer } from "./ChatComposer";
import { ChatMessage } from "./ChatMessage";
import { ConversationList } from "./ConversationList";

export function AiChat() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const conversationId = searchParams.get("c") ?? undefined;
  const initialPrompt = searchParams.get("q");
  const [historyOpen, setHistoryOpen] = useState(false);

  const onConversationCreated = useCallback(
    (id: string) => router.replace(`${pathname}?c=${id}`, { scroll: false }),
    [pathname, router]
  );

  const chat = useAgentChat({ conversationId, onConversationCreated });
  const { send } = chat;

  // "/ai?q=…" (from the dashboard's ask box) sends the question once. The ref guards React's dev double-mount.
  const sentInitialPrompt = useRef(false);
  useEffect(() => {
    if (!initialPrompt || conversationId || sentInitialPrompt.current) return;
    sentInitialPrompt.current = true;
    router.replace(pathname, { scroll: false });
    send(initialPrompt);
  }, [conversationId, initialPrompt, pathname, router, send]);

  const endRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    endRef.current?.scrollIntoView({ block: "end", behavior: "smooth" });
  }, [chat.items, chat.restoredActions]);

  const empty = !chat.loadingHistory && chat.items.length === 0 && !chat.historyError;

  return (
    <div className="container grid gap-6 py-6 md:grid-cols-[240px_1fr]">
      <aside className="hidden md:block">
        <div className="sticky top-20">
          <ConversationList activeId={conversationId} />
        </div>
      </aside>

      <section className="flex min-h-[70vh] min-w-0 flex-col" aria-label="Chat with Bud-Wiser">
        <div className="mb-6 flex items-center justify-between gap-2">
          <div className="flex items-center gap-3">
            <span className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-brand-gradient">
              <Sparkles className="h-5 w-5 text-brand-foreground" />
              <span className="absolute -right-0.5 -top-0.5 h-3 w-3 rounded-full border-2 border-background bg-income">
                <span className="absolute inset-0 animate-ping rounded-full bg-income opacity-60" />
              </span>
            </span>
            <div>
              <h2 className="font-display text-xl font-bold tracking-tight">AI Assistant</h2>
              <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
                <ShieldCheck className="h-4 w-4 text-primary" /> Answers from your data · changes need your OK
              </p>
            </div>
          </div>
          <Sheet open={historyOpen} onOpenChange={setHistoryOpen}>
            <SheetTrigger asChild>
              <Button variant="outline" size="sm" className="md:hidden">
                <History className="mr-1 h-4 w-4" /> Chats
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-72">
              <SheetHeader>
                <SheetTitle>Conversations</SheetTitle>
              </SheetHeader>
              <div className="mt-4">
                <ConversationList activeId={conversationId} onNavigate={() => setHistoryOpen(false)} />
              </div>
            </SheetContent>
          </Sheet>
        </div>

        <div className="flex-1 space-y-6 pb-6" aria-live="polite">
          {chat.loadingHistory && (
            <div className="space-y-4">
              <Skeleton className="ml-auto h-10 w-2/3" />
              <Skeleton className="h-20 w-5/6" />
            </div>
          )}
          {chat.historyError && <p className="text-sm text-expense">{chat.historyError}</p>}

          {empty && (
            <div className="flex flex-col items-center gap-6 py-10 text-center">
              <span className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-gradient">
                <Sparkles className="h-7 w-7 text-brand-foreground" />
              </span>
              <div>
                <p className="font-display text-2xl font-bold tracking-tight">How can I help with your money today?</p>
                <p className="mt-1 text-sm text-muted-foreground">
                  I can analyse your spending, budgets and goals, and log things for you after you confirm.
                </p>
              </div>
              <Stagger onMount className="grid w-full max-w-2xl gap-2 sm:grid-cols-2" stagger={0.06} delay={0.15}>
                {SUGGESTED_PROMPTS.map((prompt) => (
                  <StaggerItem key={prompt}>
                    <button
                      type="button"
                      onClick={() => send(prompt)}
                      className="group flex w-full items-center justify-between gap-3 rounded-lg border bg-card px-4 py-3 text-left text-sm transition-colors hover:border-primary/40 hover:bg-accent"
                    >
                      {prompt}
                      <ArrowUpRight className="h-4 w-4 shrink-0 text-muted-foreground transition-colors group-hover:text-primary" />
                    </button>
                  </StaggerItem>
                ))}
              </Stagger>
            </div>
          )}

          {chat.items.map((item) => (
            <ChatMessage
              key={item.id}
              item={item}
              actionState={chat.actionState}
              onConfirm={chat.confirm}
              onReject={chat.reject}
            />
          ))}

          {chat.restoredActions.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-muted-foreground">Waiting for your confirmation</p>
              {chat.restoredActions.map((action) => (
                <ActionCard
                  key={action.actionId}
                  action={action}
                  state={chat.actionState(action.actionId)}
                  onConfirm={() => chat.confirm(action.actionId)}
                  onReject={() => chat.reject(action.actionId)}
                />
              ))}
            </div>
          )}
          <div ref={endRef} />
        </div>

        <div className="sticky bottom-0 bg-gradient-to-t from-background via-background/90 to-transparent pb-4 pt-6">
          <ChatComposer busy={chat.busy} onSend={chat.send} onStop={chat.stop} />
          <p className="mt-2 text-center text-xs text-muted-foreground">
            Bud-Wiser can make mistakes. Check important numbers in your dashboard.
          </p>
        </div>
      </section>
    </div>
  );
}

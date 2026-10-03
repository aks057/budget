"use client";

import { ActionCard } from "@/components/agent/ActionCard";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { SUGGESTED_PROMPTS } from "@/lib/agent/tools";
import { useAgentChat } from "@/lib/agent/useAgentChat";
import { History, ShieldCheck, Sparkles } from "lucide-react";
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
        <div className="sticky top-4">
          <ConversationList activeId={conversationId} />
        </div>
      </aside>

      <section className="flex min-h-[70vh] min-w-0 flex-col" aria-label="Chat with Bud-Wiser">
        <div className="mb-4 flex items-center justify-between gap-2">
          <div>
            <h1 className="text-2xl font-bold">AI Assistant</h1>
            <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
              <ShieldCheck className="h-4 w-4" /> Answers come from your data. Changes need your confirmation.
            </p>
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
          {chat.historyError && <p className="text-sm text-red-500">{chat.historyError}</p>}

          {empty && (
            <div className="flex flex-col items-center gap-4 py-12 text-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-white">
                <Sparkles className="h-6 w-6" />
              </div>
              <div>
                <p className="text-lg font-semibold">How can I help with your money today?</p>
                <p className="text-sm text-muted-foreground">
                  I can analyse your spending, budgets and goals, and log things for you.
                </p>
              </div>
              <div className="flex max-w-2xl flex-wrap justify-center gap-2">
                {SUGGESTED_PROMPTS.map((prompt) => (
                  <Button key={prompt} variant="outline" size="sm" className="rounded-full" onClick={() => send(prompt)}>
                    {prompt}
                  </Button>
                ))}
              </div>
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

        <div className="sticky bottom-0 bg-background/95 pb-4 pt-2 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <ChatComposer busy={chat.busy} onSend={chat.send} onStop={chat.stop} />
          <p className="mt-2 text-center text-xs text-muted-foreground">
            Bud-Wiser can make mistakes. Check important numbers in your dashboard.
          </p>
        </div>
      </section>
    </div>
  );
}

"use client";

import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { getConversations } from "@/lib/api/agent";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { formatDistanceToNow } from "date-fns";
import { LayoutGroup, motion } from "framer-motion";
import { MessageSquare, Plus } from "lucide-react";
import Link from "next/link";
import { useId } from "react";

interface Props {
  activeId?: string;
  onNavigate?: () => void;
}

export function ConversationList({ activeId, onNavigate }: Props) {
  const conversations = useQuery({
    queryKey: ["agent", "conversations"],
    queryFn: getConversations,
  });
  const groupId = useId();

  return (
    <div className="flex flex-col gap-2">
      <Button asChild variant="outline" className="justify-start gap-2">
        <Link href="/ai" onClick={onNavigate}>
          <Plus className="h-4 w-4" /> New chat
        </Link>
      </Button>

      <p className="mt-2 px-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">Recent</p>
      {conversations.isLoading && (
        <div className="space-y-2">
          {[0, 1, 2].map((index) => (
            <Skeleton key={index} className="h-10 w-full" />
          ))}
        </div>
      )}
      {conversations.isError && <p className="px-1 text-sm text-expense">Could not load conversations.</p>}
      {conversations.data?.length === 0 && (
        <p className="px-1 text-sm text-muted-foreground">No conversations yet.</p>
      )}
      {/* LayoutGroup scopes the sliding highlight: this list renders twice (sidebar + mobile sheet). */}
      <LayoutGroup id={groupId}>
        <nav className="flex flex-col gap-1" aria-label="Conversations">
          {conversations.data?.map((conversation, index) => {
            const active = conversation.id === activeId;
            return (
              <motion.div
                key={conversation.id}
                initial={{ opacity: 0, x: -8 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: Math.min(index, 10) * 0.03 }}
              >
                <Link
                  href={`/ai?c=${conversation.id}`}
                  onClick={onNavigate}
                  aria-current={active ? "page" : undefined}
                  className={cn(
                    "relative flex items-start gap-2 rounded-xl px-2.5 py-2 text-sm transition-colors",
                    active ? "font-medium" : "hover:bg-accent/60"
                  )}
                >
                  {active && (
                    <motion.span
                      layoutId="conversation-active"
                      className="absolute inset-0 rounded-xl border border-primary/30 bg-primary/10"
                      transition={{ type: "spring", stiffness: 380, damping: 32 }}
                    />
                  )}
                  <MessageSquare className={cn("relative mt-0.5 h-4 w-4 shrink-0", active ? "text-primary" : "text-muted-foreground")} />
                  <span className="relative min-w-0 flex-1">
                    <span className="block truncate">{conversation.title}</span>
                    <span className="block text-xs text-muted-foreground">
                      {formatDistanceToNow(conversation.lastMessageAt, { addSuffix: true })}
                    </span>
                  </span>
                </Link>
              </motion.div>
            );
          })}
        </nav>
      </LayoutGroup>
    </div>
  );
}

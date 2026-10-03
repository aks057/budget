"use client";

import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { getConversations } from "@/lib/api/agent";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { formatDistanceToNow } from "date-fns";
import { MessageSquare, Plus } from "lucide-react";
import Link from "next/link";

interface Props {
  activeId?: string;
  onNavigate?: () => void;
}

export function ConversationList({ activeId, onNavigate }: Props) {
  const conversations = useQuery({
    queryKey: ["agent", "conversations"],
    queryFn: getConversations,
  });

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
      {conversations.isError && <p className="px-1 text-sm text-red-500">Could not load conversations.</p>}
      {conversations.data?.length === 0 && (
        <p className="px-1 text-sm text-muted-foreground">No conversations yet.</p>
      )}
      <nav className="flex flex-col gap-1" aria-label="Conversations">
        {conversations.data?.map((conversation) => (
          <Link
            key={conversation.id}
            href={`/ai?c=${conversation.id}`}
            onClick={onNavigate}
            aria-current={conversation.id === activeId ? "page" : undefined}
            className={cn(
              "flex items-start gap-2 rounded-md px-2 py-2 text-sm transition-colors hover:bg-muted",
              conversation.id === activeId && "bg-muted font-medium"
            )}
          >
            <MessageSquare className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />
            <span className="min-w-0 flex-1">
              <span className="block truncate">{conversation.title}</span>
              <span className="block text-xs text-muted-foreground">
                {formatDistanceToNow(conversation.lastMessageAt, { addSuffix: true })}
              </span>
            </span>
          </Link>
        ))}
      </nav>
    </div>
  );
}

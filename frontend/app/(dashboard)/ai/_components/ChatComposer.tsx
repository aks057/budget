"use client";

import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { MAX_MESSAGE_LENGTH } from "@/lib/agent/useAgentChat";
import { cn } from "@/lib/utils";
import { SendHorizontal, Square } from "lucide-react";
import React, { useState } from "react";

interface Props {
  busy: boolean;
  onSend: (message: string) => void;
  onStop: () => void;
}

export function ChatComposer({ busy, onSend, onStop }: Props) {
  const [value, setValue] = useState("");
  const tooLong = value.length > MAX_MESSAGE_LENGTH;
  const canSend = !busy && value.trim().length > 0 && !tooLong;

  const submit = () => {
    if (!canSend) return;
    onSend(value);
    setValue("");
  };

  return (
    <form
      className="flex items-end gap-2 rounded-xl border bg-card p-2 transition-colors focus-within:border-primary/50"
      onSubmit={(e) => {
        e.preventDefault();
        submit();
      }}
    >
      <div className="flex-1">
        <label htmlFor="agent-message" className="sr-only">
          Message Bud-Wiser
        </label>
        <Textarea
          id="agent-message"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onKeyDown={(e: React.KeyboardEvent<HTMLTextAreaElement>) => {
            // Enter sends, Shift+Enter adds a line; ignore Enter while an IME composition is active.
            if (e.key === "Enter" && !e.shiftKey && !e.nativeEvent.isComposing) {
              e.preventDefault();
              submit();
            }
          }}
          placeholder="Ask about your spending, or tell me what you spent…"
          rows={1}
          className="max-h-40 min-h-[44px] resize-none border-0 bg-transparent text-base shadow-none focus-visible:ring-0 focus-visible:ring-offset-0"
        />
        {value.length > MAX_MESSAGE_LENGTH - 200 && (
          <p className={cn("px-3 text-xs", tooLong ? "text-expense" : "text-muted-foreground")}>
            {value.length}/{MAX_MESSAGE_LENGTH}
          </p>
        )}
      </div>
      {busy ? (
        <Button type="button" size="icon" variant="outline" className="h-11 w-11 rounded-xl" onClick={onStop} aria-label="Stop">
          <Square className="h-4 w-4" />
        </Button>
      ) : (
        <Button type="submit" size="icon" variant="gradient" className="h-11 w-11 rounded-xl" disabled={!canSend} aria-label="Send">
          <SendHorizontal className="h-4 w-4" />
        </Button>
      )}
    </form>
  );
}

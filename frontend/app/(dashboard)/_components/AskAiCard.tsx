"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { SUGGESTED_PROMPTS } from "@/lib/agent/tools";
import { SendHorizontal, Sparkles } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";

/** Entry point to the agent from the dashboard: the question is handed to /ai, which sends it immediately. */
export function AskAiCard() {
  const router = useRouter();
  const [question, setQuestion] = useState("");

  const ask = (text: string) => {
    const trimmed = text.trim();
    if (!trimmed) return;
    router.push(`/ai?q=${encodeURIComponent(trimmed.slice(0, 2000))}`);
  };

  return (
    <Card className="border-amber-500/30 bg-gradient-to-br from-amber-500/5 to-orange-500/5">
      <CardContent className="space-y-3 p-4 sm:p-6">
        <div className="flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-amber-500" />
          <p className="font-semibold">Ask Bud-Wiser</p>
        </div>
        <form
          className="flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            ask(question);
          }}
        >
          <label htmlFor="ask-ai" className="sr-only">
            Ask a question about your finances
          </label>
          <Input
            id="ask-ai"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="e.g. Why did I spend more this month?"
            maxLength={2000}
            className="bg-background"
          />
          <Button type="submit" size="icon" disabled={!question.trim()} aria-label="Ask">
            <SendHorizontal className="h-4 w-4" />
          </Button>
        </form>
        <div className="flex flex-wrap gap-2">
          {SUGGESTED_PROMPTS.slice(0, 4).map((prompt) => (
            <Button key={prompt} variant="outline" size="sm" className="rounded-full bg-background" onClick={() => ask(prompt)}>
              {prompt}
            </Button>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

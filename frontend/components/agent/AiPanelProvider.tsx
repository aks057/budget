"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

interface AiPanelContextValue {
  open: boolean;
  setOpen: (open: boolean) => void;
  toggle: () => void;
  /** Opens the panel and sends `question` as a new message. */
  ask: (question: string) => void;
  /** A question waiting to be sent by the panel; cleared once consumed. */
  pendingQuestion: string | null;
  consumePendingQuestion: () => string | null;
}

const AiPanelContext = createContext<AiPanelContextValue | null>(null);

/** App-wide state for the slide-in AI panel, plus the Ctrl/⌘+J shortcut. */
export function AiPanelProvider({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false);
  const [pendingQuestion, setPendingQuestion] = useState<string | null>(null);

  const toggle = useCallback(() => setOpen((value) => !value), []);
  const ask = useCallback((question: string) => {
    const trimmed = question.trim();
    if (trimmed) setPendingQuestion(trimmed.slice(0, 2000));
    setOpen(true);
  }, []);
  const consumePendingQuestion = useCallback(() => {
    const question = pendingQuestion;
    setPendingQuestion(null);
    return question;
  }, [pendingQuestion]);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "j") {
        event.preventDefault();
        toggle();
      }
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [toggle]);

  const value = useMemo(
    () => ({ open, setOpen, toggle, ask, pendingQuestion, consumePendingQuestion }),
    [ask, consumePendingQuestion, open, pendingQuestion, toggle]
  );
  return <AiPanelContext.Provider value={value}>{children}</AiPanelContext.Provider>;
}

export function useAiPanel(): AiPanelContextValue {
  const context = useContext(AiPanelContext);
  if (!context) throw new Error("useAiPanel must be used inside AiPanelProvider");
  return context;
}

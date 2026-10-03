"use client";

import type { ActionState } from "@/components/agent/ActionCard";
import type { ToolStep } from "@/components/agent/ToolTimeline";
import {
  AgentStreamEvent,
  confirmAction,
  getMessages,
  getPendingActions,
  rejectAction,
  streamChat,
} from "@/lib/api/agent";
import { ApiError } from "@/lib/api/client";
import type { PendingActionDto } from "@/lib/api/types";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useRef, useState } from "react";

export type ChatItem =
  | { kind: "user"; id: string; content: string }
  | {
      kind: "assistant";
      id: string;
      content: string;
      steps: ToolStep[];
      actions: PendingActionDto[];
      status: "streaming" | "done" | "error";
      error?: string;
    };

export const MAX_MESSAGE_LENGTH = 2000;

let localIds = 0;
const nextLocalId = () => `local-${++localIds}`;

// Every screen whose data an executed agent action can change.
const DATA_QUERY_KEYS = [["overview"], ["transactions"], ["analytics"], ["budgets"], ["goals"], ["categories"]];

const errorText = (error: unknown) =>
  error instanceof ApiError ? error.userMessage : "Something went wrong. Please try again.";

interface Options {
  /** Conversation from the URL; undefined for a new chat. */
  conversationId?: string;
  /** Called once the server assigns an id to a new conversation. */
  onConversationCreated: (conversationId: string) => void;
}

export function useAgentChat({ conversationId, onConversationCreated }: Options) {
  const queryClient = useQueryClient();
  const [items, setItems] = useState<ChatItem[]>([]);
  /** Pending actions restored from the server for a reloaded conversation (not tied to a rendered turn). */
  const [restoredActions, setRestoredActions] = useState<PendingActionDto[]>([]);
  const [actionStates, setActionStates] = useState<Record<string, ActionState>>({});
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  // The conversation this hook currently shows. A ref, so stream callbacks never read a stale value.
  const activeIdRef = useRef<string | undefined>(conversationId);
  const abortRef = useRef<AbortController | null>(null);
  const onCreatedRef = useRef(onConversationCreated);
  onCreatedRef.current = onConversationCreated;

  const updateAssistant = useCallback((id: string, update: (item: Extract<ChatItem, { kind: "assistant" }>) => ChatItem) => {
    setItems((prev) => prev.map((item) => (item.id === id && item.kind === "assistant" ? update(item) : item)));
  }, []);

  // Load history when the user navigates to a different conversation. When the id changes because *this* chat
  // just created the conversation, activeIdRef already matches and the live transcript is kept.
  useEffect(() => {
    if (conversationId === activeIdRef.current && (items.length > 0 || !conversationId)) return;
    abortRef.current?.abort();
    activeIdRef.current = conversationId;
    setItems([]);
    setRestoredActions([]);
    setActionStates({});
    setHistoryError(null);
    setBusy(false);
    if (!conversationId) return;

    let cancelled = false;
    setLoadingHistory(true);
    Promise.all([getMessages(conversationId), getPendingActions(conversationId)])
      .then(([messages, pending]) => {
        if (cancelled) return;
        setItems(
          messages.map((message): ChatItem =>
            message.role === "USER"
              ? { kind: "user", id: message.id, content: message.content }
              : { kind: "assistant", id: message.id, content: message.content, steps: [], actions: [], status: "done" }
          )
        );
        setRestoredActions(pending);
      })
      .catch((error) => !cancelled && setHistoryError(errorText(error)))
      .finally(() => !cancelled && setLoadingHistory(false));
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- only navigation should trigger a reload
  }, [conversationId]);

  // Leaving the page stops reading the stream (the server still finishes and stores the turn).
  useEffect(() => () => abortRef.current?.abort(), []);

  const applyEvent = useCallback(
    (assistantId: string, event: AgentStreamEvent) => {
      switch (event.type) {
        case "conversation":
          if (!activeIdRef.current) {
            activeIdRef.current = event.conversationId;
            onCreatedRef.current(event.conversationId);
          }
          break;
        case "tool_call":
          updateAssistant(assistantId, (item) => ({ ...item, steps: [...item.steps, { name: event.name }] }));
          break;
        case "tool_result":
          updateAssistant(assistantId, (item) => {
            const steps = [...item.steps];
            const index = steps.findLastIndex((step) => step.name === event.name && step.status === undefined);
            const resolved = { name: event.name, status: event.status, latencyMs: event.latencyMs };
            if (index === -1) steps.push(resolved);
            else steps[index] = resolved;
            return { ...item, steps };
          });
          break;
        case "action_pending":
          updateAssistant(assistantId, (item) =>
            item.actions.some((action) => action.actionId === event.action.actionId)
              ? item
              : { ...item, actions: [...item.actions, event.action] }
          );
          break;
        case "done":
          updateAssistant(assistantId, (item) => ({
            ...item,
            content: event.response.reply,
            status: "done",
            // The final response is authoritative for pending actions (covers any missed stream event).
            actions: event.response.pendingActions.length > 0 ? event.response.pendingActions : item.actions,
          }));
          break;
        case "error":
          updateAssistant(assistantId, (item) => ({ ...item, status: "error", error: event.message }));
          break;
      }
    },
    [updateAssistant]
  );

  const send = useCallback(
    async (rawMessage: string) => {
      const message = rawMessage.trim();
      if (!message || busy || message.length > MAX_MESSAGE_LENGTH) return;

      const assistantId = nextLocalId();
      setItems((prev) => [
        ...prev,
        { kind: "user", id: nextLocalId(), content: message },
        { kind: "assistant", id: assistantId, content: "", steps: [], actions: [], status: "streaming" },
      ]);
      const controller = new AbortController();
      abortRef.current = controller;
      setBusy(true);
      try {
        await streamChat(
          { conversationId: activeIdRef.current, message },
          (event) => applyEvent(assistantId, event),
          controller.signal
        );
      } catch (error) {
        if (controller.signal.aborted) {
          updateAssistant(assistantId, (item) =>
            item.status === "streaming"
              ? { ...item, status: "error", error: "Stopped. The answer will still be saved to this conversation." }
              : item
          );
        } else {
          updateAssistant(assistantId, (item) => ({ ...item, status: "error", error: errorText(error) }));
        }
      } finally {
        if (abortRef.current === controller) {
          abortRef.current = null;
          setBusy(false);
        }
        queryClient.invalidateQueries({ queryKey: ["agent", "conversations"] });
      }
    },
    [applyEvent, busy, queryClient, updateAssistant]
  );

  const stop = useCallback(() => abortRef.current?.abort(), []);

  const decide = useCallback(
    async (actionId: string, intent: "confirm" | "reject") => {
      setActionStates((prev) => ({ ...prev, [actionId]: { phase: "working", intent } }));
      try {
        const result = intent === "confirm" ? await confirmAction(actionId) : await rejectAction(actionId);
        let next: ActionState;
        if (result.status === "EXECUTED") next = { phase: "executed", message: result.message };
        else if (result.status === "CANCELLED") next = { phase: "cancelled" };
        else next = { phase: "failed", message: result.message || "The action could not be completed." };
        setActionStates((prev) => ({ ...prev, [actionId]: next }));
        if (result.status === "EXECUTED") {
          DATA_QUERY_KEYS.forEach((queryKey) => queryClient.invalidateQueries({ queryKey }));
        }
      } catch (error) {
        setActionStates((prev) => ({ ...prev, [actionId]: { phase: "failed", message: errorText(error) } }));
      }
    },
    [queryClient]
  );

  const actionState = useCallback(
    (actionId: string): ActionState => actionStates[actionId] ?? { phase: "pending" },
    [actionStates]
  );

  return {
    items,
    restoredActions,
    actionState,
    confirm: (actionId: string) => decide(actionId, "confirm"),
    reject: (actionId: string) => decide(actionId, "reject"),
    send,
    stop,
    busy,
    loadingHistory,
    historyError,
  };
}

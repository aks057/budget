import { apiFetch, ApiError, authorizedFetch, toApiError } from "@/lib/api/client";
import type {
  ActionResultDto,
  AgentChatResponse,
  ConversationDto,
  MessageDto,
  PendingActionDto,
  ToolStatus,
} from "@/lib/api/types";

/**
 * Agent API. The chat stream is `POST /api/v1/agent/chat/stream` (Server-Sent Events). EventSource only supports
 * GET without headers, so the stream is read from fetch() and parsed here; that also lets it reuse the bearer
 * token and the refresh-on-401 logic of the API client.
 */

export type AgentStreamEvent =
  | { type: "conversation"; conversationId: string }
  | { type: "tool_call"; name: string }
  | { type: "tool_result"; name: string; status: ToolStatus; latencyMs: number }
  | { type: "action_pending"; action: PendingActionDto }
  | { type: "done"; response: AgentChatResponse }
  | { type: "error"; code: string; message: string };

interface RawSseEvent {
  event: string;
  data: string;
}

/** Splits an SSE byte stream into events (spec: fields per line, events separated by a blank line). */
async function* readSse(body: ReadableStream<Uint8Array>): AsyncGenerator<RawSseEvent> {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true }).replace(/\r\n?/g, "\n");
      let boundary = buffer.indexOf("\n\n");
      while (boundary !== -1) {
        const parsed = parseBlock(buffer.slice(0, boundary));
        buffer = buffer.slice(boundary + 2);
        if (parsed) yield parsed;
        boundary = buffer.indexOf("\n\n");
      }
    }
    const tail = parseBlock(buffer);
    if (tail) yield tail;
  } finally {
    reader.releaseLock();
  }
}

function parseBlock(block: string): RawSseEvent | null {
  let event = "message";
  const data: string[] = [];
  for (const line of block.split("\n")) {
    if (!line || line.startsWith(":")) continue;
    const colon = line.indexOf(":");
    const field = colon === -1 ? line : line.slice(0, colon);
    let value = colon === -1 ? "" : line.slice(colon + 1);
    if (value.startsWith(" ")) value = value.slice(1);
    if (field === "event") event = value;
    else if (field === "data") data.push(value);
  }
  return data.length > 0 ? { event, data: data.join("\n") } : null;
}

function toStreamEvent(raw: RawSseEvent): AgentStreamEvent | null {
  const data = JSON.parse(raw.data);
  switch (raw.event) {
    case "conversation":
      return { type: "conversation", conversationId: String(data.conversationId) };
    case "tool_call":
      return { type: "tool_call", name: data.name };
    case "tool_result":
      return { type: "tool_result", name: data.name, status: data.status, latencyMs: data.latencyMs };
    case "action_pending":
      return { type: "action_pending", action: data as PendingActionDto };
    case "done":
      return { type: "done", response: data as AgentChatResponse };
    case "error":
      return { type: "error", code: data.code, message: data.message };
    default:
      return null; // Unknown events are ignored so the server can add new ones safely.
  }
}

/**
 * Streams one agent turn. Rejects with {@link ApiError} when the request itself fails (validation, 429, 401);
 * failures during the turn arrive as an `error` event instead.
 */
export async function streamChat(
  request: { conversationId?: string; message: string },
  onEvent: (event: AgentStreamEvent) => void,
  signal?: AbortSignal,
): Promise<void> {
  const response = await authorizedFetch("/api/v1/agent/chat/stream", {
    method: "POST",
    json: { conversationId: request.conversationId, message: request.message },
    headers: { Accept: "text/event-stream" },
    signal,
  });
  if (!response.ok) {
    throw await toApiError(response);
  }
  if (!response.body) {
    throw new ApiError(response.status, undefined, "Streaming is not supported by this browser");
  }
  let finished = false;
  for await (const raw of readSse(response.body)) {
    const event = toStreamEvent(raw);
    if (!event) continue;
    if (event.type === "done" || event.type === "error") finished = true;
    onEvent(event);
  }
  if (!finished) {
    // The connection dropped mid-turn. The turn still completes server-side, so the history can be reloaded.
    onEvent({ type: "error", code: "STREAM_CLOSED", message: "The connection was interrupted. Reload to see the answer." });
  }
}

export const getConversations = () => apiFetch<ConversationDto[]>("/api/v1/agent/conversations");

export const getMessages = (conversationId: string) =>
  apiFetch<MessageDto[]>(`/api/v1/agent/conversations/${conversationId}/messages`);

export const getPendingActions = (conversationId: string) =>
  apiFetch<PendingActionDto[]>(`/api/v1/agent/conversations/${conversationId}/pending-actions`);

export const confirmAction = (actionId: string) =>
  apiFetch<ActionResultDto>(`/api/v1/agent/actions/${actionId}/confirm`, { method: "POST" });

export const rejectAction = (actionId: string) =>
  apiFetch<ActionResultDto>(`/api/v1/agent/actions/${actionId}/reject`, { method: "POST" });

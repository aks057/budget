import type { ApiEnvelope, ApiErrorDetail } from "@/lib/api/types";

/**
 * Browser API client.
 *
 * - The access token lives only in memory (never localStorage): an XSS bug cannot exfiltrate a long-lived credential.
 * - The refresh token is an httpOnly cookie scoped to /api/v1/auth; it is invisible to JavaScript.
 * - On 401 the client refreshes once and retries. Refreshes are single-flight within a tab and serialized across
 *   tabs with the Web Locks API: the backend rotates refresh tokens and treats reuse as theft, so two tabs
 *   refreshing at once would otherwise log the user out. New tokens and logouts are shared via BroadcastChannel.
 */

const AUTH_PREFIX = "/api/v1/auth/";
const REFRESH_PATH = "/api/v1/auth/refresh";
const LOCK_NAME = "bw-auth-refresh";
const CHANNEL_NAME = "bw-auth";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string | undefined,
    message: string,
    public readonly details: ApiErrorDetail[] = [],
  ) {
    super(message);
    this.name = "ApiError";
  }

  /** Human-readable message: field errors when present, otherwise the server message. */
  get userMessage(): string {
    const messages = this.details.map((detail) => detail.message).filter(Boolean);
    return messages.length > 0 ? messages.join(". ") : this.message;
  }
}

type AuthEvent = { type: "token"; token: string } | { type: "logout" } | { type: "expired" };
type AuthListener = (event: AuthEvent) => void;

let accessToken: string | null = null;
let refreshInFlight: Promise<boolean> | null = null;
const listeners = new Set<AuthListener>();

const channel: BroadcastChannel | null =
  typeof window !== "undefined" && "BroadcastChannel" in window ? new BroadcastChannel(CHANNEL_NAME) : null;

channel?.addEventListener("message", (message: MessageEvent<AuthEvent>) => {
  const event = message.data;
  if (event.type === "token") {
    accessToken = event.token;
  } else if (event.type === "logout") {
    accessToken = null;
  }
  emit(event);
});

function emit(event: AuthEvent) {
  listeners.forEach((listener) => listener(event));
}

export function onAuthEvent(listener: AuthListener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function setAccessToken(token: string) {
  accessToken = token;
  channel?.postMessage({ type: "token", token } satisfies AuthEvent);
}

/** Local + all tabs. */
export function clearSession() {
  accessToken = null;
  channel?.postMessage({ type: "logout" } satisfies AuthEvent);
  emit({ type: "logout" });
}

export function hasAccessToken(): boolean {
  return accessToken !== null;
}

/** Exchanges the refresh cookie for a new access token. Resolves false when there is no valid session. */
export function refreshAccessToken(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = runRefresh().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

async function runRefresh(): Promise<boolean> {
  const tokenBefore = accessToken;
  const refresh = async (): Promise<boolean> => {
    // Another tab refreshed while we waited for the lock: its token already reached us.
    if (accessToken !== null && accessToken !== tokenBefore) {
      return true;
    }
    const response = await fetch(REFRESH_PATH, { method: "POST", credentials: "same-origin" });
    if (!response.ok) {
      return false;
    }
    const envelope = (await response.json()) as ApiEnvelope<{ accessToken: string }>;
    if (!envelope.data?.accessToken) {
      return false;
    }
    setAccessToken(envelope.data.accessToken);
    return true;
  };
  if (typeof navigator !== "undefined" && navigator.locks) {
    return navigator.locks.request(LOCK_NAME, refresh);
  }
  return refresh();
}

export interface RequestOptions extends Omit<RequestInit, "body"> {
  json?: unknown;
  query?: Record<string, string | number | undefined | null>;
}

/**
 * Raw authenticated fetch: adds the bearer token and, on 401, refreshes once and retries.
 * Used directly for streaming responses; JSON calls go through {@link apiRequest}.
 */
export async function authorizedFetch(path: string, options: RequestOptions = {}, retry = true): Promise<Response> {
  const { json, query, headers, ...init } = options;
  const requestHeaders = new Headers(headers);
  if (json !== undefined) {
    requestHeaders.set("Content-Type", "application/json");
  }
  if (accessToken) {
    requestHeaders.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(withQuery(path, query), {
    ...init,
    headers: requestHeaders,
    body: json !== undefined ? JSON.stringify(json) : undefined,
    credentials: "same-origin",
  });

  if (response.status === 401 && retry && !path.startsWith(AUTH_PREFIX)) {
    if (await refreshAccessToken()) {
      return authorizedFetch(path, options, false);
    }
    accessToken = null;
    emit({ type: "expired" });
  }
  return response;
}

/** Turns a non-2xx response carrying the `Response<T>` error envelope into an {@link ApiError}. */
export async function toApiError(response: Response): Promise<ApiError> {
  const envelope = (await response.json().catch(() => null)) as ApiEnvelope<unknown> | null;
  return new ApiError(
    response.status,
    envelope?.errors?.[0]?.code,
    envelope?.message ?? `Request failed (${response.status})`,
    envelope?.errors ?? [],
  );
}

/** Full envelope (needed for paginated lists). */
export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<ApiEnvelope<T>> {
  const response = await authorizedFetch(path, options);
  if (!response.ok) {
    throw await toApiError(response);
  }
  const envelope = (await response.json().catch(() => null)) as ApiEnvelope<T> | null;
  if (!envelope) {
    throw new ApiError(response.status, undefined, `Request failed (${response.status})`);
  }
  return envelope;
}

/** Just the `data` payload. */
export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const envelope = await apiRequest<T>(path, options);
  return envelope.data as T;
}

function withQuery(path: string, query?: RequestOptions["query"]): string {
  if (!query) {
    return path;
  }
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });
  const queryString = params.toString();
  return queryString ? `${path}?${queryString}` : path;
}

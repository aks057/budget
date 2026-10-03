"use client";

import { useAuth } from "@/components/providers/AuthProvider";
import { Loader2 } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import React, { useEffect, useRef } from "react";

/**
 * Client-side route guard. The refresh cookie is httpOnly and path-scoped to /api/v1/auth, so Next middleware can't
 * see a session; the API still authorizes every request, this only decides what to render.
 */
export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { status } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (status === "anonymous") {
      router.replace(`/sign-in?next=${encodeURIComponent(pathname)}`);
    }
  }, [status, router, pathname]);

  if (status !== "authenticated") {
    return <FullPageLoader />;
  }
  return <>{children}</>;
}

/**
 * Inverse guard for sign-in / sign-up: visitors who arrive already signed in go to the dashboard. A login that
 * happens on this page is left alone — the form decides where to go next (e.g. the wizard after sign-up).
 */
export function GuestOnly({ children }: { children: React.ReactNode }) {
  const { status } = useAuth();
  const router = useRouter();
  const wasAnonymous = useRef(false);

  useEffect(() => {
    if (status === "anonymous") {
      wasAnonymous.current = true;
    } else if (status === "authenticated" && !wasAnonymous.current) {
      router.replace("/dashboard");
    }
  }, [status, router]);

  if (status === "loading") {
    return <FullPageLoader />;
  }
  return <>{children}</>;
}

/** Only same-site relative paths are honoured as post-login redirects (no open redirect via ?next=). */
export function safeNextPath(next: string | null, fallback: string): string {
  return next && next.startsWith("/") && !next.startsWith("//") ? next : fallback;
}

export function FullPageLoader() {
  return (
    <div className="flex min-h-[60vh] w-full items-center justify-center" aria-busy="true" aria-live="polite">
      <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      <span className="sr-only">Loading</span>
    </div>
  );
}

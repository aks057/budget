"use client";

import { FullPageLoader } from "@/components/auth/RequireAuth";
import { useAuth } from "@/components/providers/AuthProvider";
import { useRouter } from "next/navigation";
import { useEffect, useRef } from "react";
import { toast } from "sonner";

/**
 * Landing page after Google sign-in. The backend already set the httpOnly refresh cookie on its OAuth redirect;
 * here we exchange it for an access token. No token ever travels in a URL.
 */
export default function OAuthCallbackPage() {
  const { reloadSession } = useAuth();
  const router = useRouter();
  const started = useRef(false);

  useEffect(() => {
    if (started.current) {
      return;
    }
    started.current = true;
    void reloadSession().then((signedIn) => {
      if (signedIn) {
        router.replace("/dashboard");
      } else {
        toast.error("Google sign-in didn't complete. Please try again.");
        router.replace("/sign-in");
      }
    });
  }, [reloadSession, router]);

  return <FullPageLoader />;
}

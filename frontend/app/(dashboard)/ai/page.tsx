"use client";

import { FullPageLoader } from "@/components/auth/RequireAuth";
import { Suspense } from "react";
import { AiChat } from "./_components/AiChat";

// useSearchParams needs a Suspense boundary so the route can still be statically prerendered.
export default function AiPage() {
  return (
    <Suspense fallback={<FullPageLoader />}>
      <AiChat />
    </Suspense>
  );
}

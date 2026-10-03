import { RequireAuth } from "@/components/auth/RequireAuth";
import { Aurora } from "@/components/motion";
import React, { ReactNode } from "react";

function layout({ children }: { children: ReactNode }) {
  return (
    <RequireAuth>
      <div className="relative flex min-h-screen w-full flex-col items-center justify-center overflow-hidden px-4 py-12">
        <Aurora intensity="strong" />
        <div className="relative w-full">{children}</div>
      </div>
    </RequireAuth>
  );
}

export default layout;

import { RequireAuth } from "@/components/auth/RequireAuth";
import React, { ReactNode } from "react";

function layout({ children }: { children: ReactNode }) {
  return (
    <RequireAuth>
      <div className="relative flex h-screen w-full flex-col items-center justify-center">
        {children}
      </div>
    </RequireAuth>
  );
}

export default layout;

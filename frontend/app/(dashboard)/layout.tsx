import { AiFab } from "@/components/agent/AiFab";
import { AiPanel } from "@/components/agent/AiPanel";
import { AiPanelProvider } from "@/components/agent/AiPanelProvider";
import { RequireAuth } from "@/components/auth/RequireAuth";
import { AppSidebar } from "@/components/layout/AppSidebar";
import { AppTopbar } from "@/components/layout/AppTopbar";
import React, { ReactNode } from "react";

/** App shell: sidebar on the left; top bar + page on the right; AI panel and launcher available everywhere. */
function layout({ children }: { children: ReactNode }) {
  return (
    <RequireAuth>
      <AiPanelProvider>
        <div className="flex min-h-screen w-full">
          <AppSidebar />
          <div className="flex min-w-0 flex-1 flex-col">
            <AppTopbar />
            <main id="main-content" className="flex-1">
              {children}
            </main>
          </div>
        </div>
        <AiPanel />
        <AiFab />
      </AiPanelProvider>
    </RequireAuth>
  );
}

export default layout;

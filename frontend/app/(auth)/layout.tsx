import Logo from "@/components/Logo";
import { AuthShowcase } from "@/components/auth/AuthShowcase";
import { GuestOnly } from "@/components/auth/RequireAuth";
import { Aurora, Reveal } from "@/components/motion";
import React, { ReactNode } from "react";

function layout({ children }: { children: ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <AuthShowcase />

      <div className="relative flex items-center justify-center overflow-hidden px-4 py-12 sm:px-8">
        {/* Mobile has no showcase panel, so give the form its own ambient background. */}
        <Aurora intensity="subtle" className="lg:hidden" />
        <Reveal onMount className="relative w-full max-w-[400px]">
          <div className="mb-8 flex justify-center lg:hidden">
            <Logo />
          </div>
          <div className="glass-card space-y-6 p-6 sm:p-8">
            <GuestOnly>{children}</GuestOnly>
          </div>
        </Reveal>
      </div>
    </div>
  );
}

export default layout;

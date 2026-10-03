import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import { Breadcrumbs } from "@/components/Breadcrumbs";
import { RequireAuth } from "@/components/auth/RequireAuth";
import React, { ReactNode } from "react";

function layout({ children }: { children: ReactNode }) {
  return (
    <RequireAuth>
      <div className="relative flex min-h-screen w-full flex-col">
        <Navbar />
        <Breadcrumbs />
        <main id="main-content" className="flex-1">
          {children}
        </main>
        <Footer />
      </div>
    </RequireAuth>
  );
}

export default layout;

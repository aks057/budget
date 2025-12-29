import Logo from "@/components/Logo";
import { PiggyBank } from "lucide-react";
import Image from "next/image";
import React, { ReactNode } from "react";

function layout({ children }: { children: ReactNode }) {
  return (
    <div className="container relative min-h-screen flex-col items-center justify-center grid lg:max-w-none lg:grid-cols-2 lg:px-0">
      {/* Left side - Image */}
      <div className="relative hidden h-full flex-col bg-muted p-10 text-white dark:border-r lg:flex">
        <Image
          src="/login.jpg"
          alt="Authentication"
          fill
          className="object-cover"
          priority
        />
        <div className="absolute inset-0 bg-black/40" />
        <div className="relative z-20 flex items-center text-lg font-medium text-amber-500">
          <PiggyBank className="mr-2 h-6 w-6 stroke-amber-500" />
          <span className="font-semibold">BudWiser</span>
        </div>
      </div>

      {/* Right side - Form */}
      <div className="lg:p-8">
        <div className="mx-auto flex w-full flex-col justify-center space-y-6 sm:w-[350px]">
          {/* Mobile logo */}
          <div className="flex flex-col items-center lg:hidden mb-4">
            <Logo />
          </div>
          {children}
        </div>
      </div>
    </div>
  );
}

export default layout;

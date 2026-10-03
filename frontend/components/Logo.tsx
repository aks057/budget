import { PiggyBank } from "lucide-react";
import React from "react";

function Logo() {
  return (
    <a href="/" className="flex items-center gap-2">
      <PiggyBank className="stroke h-11 w-11 stroke-amber-500 stroke-[1.5]" />
      <p className="bg-gradient-to-r from-amber-400 to-orange-500 bg-clip-text text-3xl font-bold leading-tight tracking-tighter text-transparent">
        BudWiser
      </p>
    </a>
  );
}

export function LogoMobile() {
  return (
    <a href="/" className="flex items-center gap-1.5">
      <PiggyBank className="h-7 w-7 stroke-brand stroke-[1.5]" />
      <span className="bg-gradient-to-r from-amber-400 to-orange-500 bg-clip-text text-lg font-bold tracking-tight text-transparent">
        BudWiser
      </span>
    </a>
  );
}

export default Logo;

import { cn } from "@/lib/utils";
import Link from "next/link";

/** Brand mark: a rising "W" trend line in a glowing gradient tile. */
export function LogoMark({ className }: { className?: string }) {
  return (
    <span
      className={cn(
        "relative inline-flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient shadow-glow",
        className
      )}
    >
      <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" aria-hidden>
        <path
          d="M3 7l4.5 10L12 9l4.5 8L21 5"
          stroke="hsl(var(--brand-foreground))"
          strokeWidth="2.4"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
      <span className="absolute inset-0 rounded-xl ring-1 ring-inset ring-white/20" />
    </span>
  );
}

function Logo({ href = "/", className }: { href?: string; className?: string }) {
  return (
    <Link href={href} className={cn("flex items-center gap-2.5", className)} aria-label="Bud-Wiser home">
      <LogoMark />
      <span className="font-display text-xl font-bold tracking-tight">
        Bud<span className="gradient-text">-Wiser</span>
      </span>
    </Link>
  );
}

export function LogoMobile({ href = "/" }: { href?: string }) {
  return (
    <Link href={href} className="flex items-center gap-2" aria-label="Bud-Wiser home">
      <LogoMark className="h-8 w-8" />
      <span className="font-display text-lg font-bold tracking-tight">
        Bud<span className="gradient-text">-Wiser</span>
      </span>
    </Link>
  );
}

export default Logo;

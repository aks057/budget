import { cn } from "@/lib/utils";
import type { HTMLAttributes } from "react";

/**
 * Standard in-app surface: a flat card with a subtle border (the earlier cursor spotlight/glow was removed to keep
 * the app calm). Kept as a component so every surface stays consistent in one place.
 */
export function SpotlightCard({ className, children, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={cn("rounded-xl border bg-card text-card-foreground shadow-sm transition-colors hover:border-border/100", className)} {...props}>
      {children}
    </div>
  );
}

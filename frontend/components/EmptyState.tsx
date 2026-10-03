import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";

interface Props {
  icon: LucideIcon;
  title: string;
  description?: ReactNode;
  action?: ReactNode;
  className?: string;
  /** Compact variant for use inside cards/charts. */
  compact?: boolean;
}

/** Consistent empty state: icon tile, title, hint and an optional action. */
export function EmptyState({ icon: Icon, title, description, action, className, compact = false }: Props) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center rounded-xl border border-dashed text-center",
        compact ? "gap-2 px-4 py-8" : "gap-3 px-6 py-14",
        className
      )}
    >
      <span className={cn("flex items-center justify-center rounded-xl bg-primary/10", compact ? "h-10 w-10" : "h-12 w-12")}>
        <Icon className={cn("text-primary", compact ? "h-5 w-5" : "h-6 w-6")} />
      </span>
      <p className={cn("font-display font-semibold", compact ? "text-base" : "text-lg")}>{title}</p>
      {description && <p className="max-w-sm text-sm text-muted-foreground">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}

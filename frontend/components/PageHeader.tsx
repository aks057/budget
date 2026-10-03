import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";

interface Props {
  title: ReactNode;
  subtitle?: ReactNode;
  icon?: LucideIcon;
  actions?: ReactNode;
  className?: string;
}

/** Shared app page header: title, subtitle and an actions slot. (The page itself fades in via the route template.) */
export function PageHeader({ title, subtitle, icon: Icon, actions, className }: Props) {
  return (
    <div className={cn("container flex flex-wrap items-end justify-between gap-4 pb-6 pt-6 md:pt-8", className)}>
      <div className="flex items-start gap-3">
        {Icon && (
          <span className="mt-0.5 hidden h-10 w-10 shrink-0 items-center justify-center rounded-lg border bg-card sm:flex">
            <Icon className="h-5 w-5 text-primary" />
          </span>
        )}
        <div>
          <h2 className="font-display text-2xl font-bold tracking-tight">{title}</h2>
          {subtitle && <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>}
        </div>
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
}

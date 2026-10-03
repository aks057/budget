import { Reveal } from "@/components/motion";
import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";

interface Props {
  eyebrow: string;
  icon?: LucideIcon;
  title: ReactNode;
  subtitle?: ReactNode;
  className?: string;
}

/** Consistent section header: glowing eyebrow pill, display title, muted subtitle. */
export function SectionHeading({ eyebrow, icon: Icon, title, subtitle, className }: Props) {
  return (
    <div className={cn("mx-auto mb-14 max-w-3xl text-center md:mb-20", className)}>
      <Reveal>
        <span className="mb-5 inline-flex items-center gap-2 rounded-full border border-primary/25 bg-primary/10 px-3.5 py-1 text-xs font-semibold uppercase tracking-widest text-primary">
          {Icon && <Icon className="h-3.5 w-3.5" />}
          {eyebrow}
        </span>
      </Reveal>
      <Reveal delay={0.08}>
        <h2 className="text-balance font-display text-3xl font-bold tracking-tight sm:text-4xl md:text-5xl">{title}</h2>
      </Reveal>
      {subtitle && (
        <Reveal delay={0.16}>
          <p className="mx-auto mt-5 max-w-2xl text-pretty text-lg text-muted-foreground">{subtitle}</p>
        </Reveal>
      )}
    </div>
  );
}

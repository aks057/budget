import { cn } from "@/lib/utils";

/**
 * Ambient background: slowly drifting brand-coloured blobs over a faded grid. Pure CSS (GPU transforms only),
 * so it costs nothing on the main thread; the global reduced-motion rule freezes it.
 */
export function Aurora({ className, intensity = "normal" }: { className?: string; intensity?: "subtle" | "normal" | "strong" }) {
  const opacity = intensity === "subtle" ? "opacity-40" : intensity === "strong" ? "opacity-90" : "opacity-60";
  return (
    <div aria-hidden className={cn("pointer-events-none absolute inset-0 overflow-hidden", className)}>
      <div className="absolute inset-0 bg-grid mask-radial opacity-70" />
      <div className={cn("absolute inset-0 dark:mix-blend-screen", opacity)}>
        <div className="absolute -left-[10%] -top-[20%] h-[55vh] w-[55vw] animate-aurora rounded-full bg-brand/30 blur-[120px]" />
        <div className="absolute -right-[10%] top-[5%] h-[50vh] w-[45vw] animate-aurora-slow rounded-full bg-brand-2/25 blur-[120px]" />
        <div className="absolute bottom-[-20%] left-[25%] h-[45vh] w-[50vw] animate-aurora rounded-full bg-violet-500/15 blur-[140px] [animation-delay:-6s]" />
      </div>
      <div className="absolute inset-x-0 bottom-0 h-40 bg-gradient-to-t from-background to-transparent" />
    </div>
  );
}

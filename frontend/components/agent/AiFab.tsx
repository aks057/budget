"use client";

import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { cn } from "@/lib/utils";
import { AnimatePresence, motion } from "framer-motion";
import { Sparkles } from "lucide-react";
import { usePathname } from "next/navigation";

/** Floating AI launcher (bottom-right). Hidden on the full chat page and while the panel is open. */
export function AiFab() {
  const { open, setOpen } = useAiPanel();
  const pathname = usePathname();
  const visible = !open && !pathname.startsWith("/ai");

  return (
    <AnimatePresence>
      {visible && (
        <motion.button
          type="button"
          onClick={() => setOpen(true)}
          aria-label="Ask Bud-Wiser AI (Ctrl+J)"
          title="Ask Bud-Wiser AI (Ctrl+J)"
          initial={{ opacity: 0, scale: 0.6, y: 12 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.6, y: 12 }}
          whileHover={{ scale: 1.06 }}
          whileTap={{ scale: 0.94 }}
          transition={{ type: "spring", stiffness: 400, damping: 26 }}
          className={cn(
            "fixed bottom-5 right-5 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-brand-gradient text-brand-foreground",
            "shadow-[0_10px_30px_-8px_rgba(0,0,0,0.6)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          )}
        >
          {/* One soft pulse when it first appears, then still. */}
          <motion.span
            aria-hidden
            className="absolute inset-0 rounded-full border-2 border-primary"
            initial={{ scale: 1, opacity: 0.6 }}
            animate={{ scale: 1.6, opacity: 0 }}
            transition={{ duration: 1.4, ease: "easeOut", delay: 0.3 }}
          />
          <Sparkles className="h-6 w-6" />
        </motion.button>
      )}
    </AnimatePresence>
  );
}

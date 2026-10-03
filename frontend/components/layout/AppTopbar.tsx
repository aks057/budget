"use client";

import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { CommandMenu } from "@/components/CommandMenu";
import { SidebarContent } from "@/components/layout/AppSidebar";
import { pageTitle } from "@/components/layout/navItems";
import { Notifications } from "@/components/Notifications";
import { ThemeSwitcherBtn } from "@/components/ThemeSwitcherBtn";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { AnimatePresence, motion } from "framer-motion";
import { Menu, Sparkles } from "lucide-react";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

const EXAMPLES = ["Why did I spend more this month?", "Am I on track for my goals?", "I spent 450 on lunch today", "Can I afford a ₹40k trip?"];

/** Slim sticky top bar: page title, the "Ask Bud-Wiser" launcher, and global actions. */
export function AppTopbar() {
  const pathname = usePathname();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  return (
    <header className="sticky top-0 z-30 flex h-14 items-center gap-3 border-b bg-background/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/80 md:px-6">
      {/* Mobile: sidebar as a drawer */}
      <Sheet open={mobileNavOpen} onOpenChange={setMobileNavOpen}>
        <SheetTrigger asChild>
          <Button variant="ghost" size="icon" className="h-9 w-9 md:hidden" aria-label="Open navigation">
            <Menu className="h-5 w-5" />
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="w-72 p-0">
          <SheetTitle className="sr-only">Navigation</SheetTitle>
          <SidebarContent onNavigate={() => setMobileNavOpen(false)} />
        </SheetContent>
      </Sheet>

      <h1 className="truncate font-display text-base font-semibold">{pageTitle(pathname)}</h1>

      <div className="flex flex-1 justify-center px-2">
        <AskPill hidden={pathname.startsWith("/ai")} />
      </div>

      <div className="flex items-center gap-1 sm:gap-2">
        <CommandMenu />
        <Notifications />
        <ThemeSwitcherBtn />
      </div>
    </header>
  );
}

function AskPill({ hidden }: { hidden: boolean }) {
  const { setOpen } = useAiPanel();
  const [index, setIndex] = useState(0);
  useEffect(() => {
    const timer = setInterval(() => setIndex((i) => (i + 1) % EXAMPLES.length), 3500);
    return () => clearInterval(timer);
  }, []);
  if (hidden) return null;

  return (
    <button
      type="button"
      onClick={() => setOpen(true)}
      aria-label="Ask Bud-Wiser AI"
      className="group hidden h-9 w-full max-w-md items-center gap-2.5 rounded-full border bg-card px-3.5 text-sm text-muted-foreground transition-colors hover:border-primary/40 hover:text-foreground sm:flex"
    >
      <Sparkles className="h-4 w-4 shrink-0 text-primary" />
      <span className="relative h-5 flex-1 overflow-hidden text-left">
        <AnimatePresence mode="wait" initial={false}>
          <motion.span
            key={index}
            initial={{ y: 12, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: -12, opacity: 0 }}
            transition={{ duration: 0.25 }}
            className="absolute inset-0 truncate"
          >
            Ask Bud-Wiser… “{EXAMPLES[index]}”
          </motion.span>
        </AnimatePresence>
      </span>
      <kbd className="rounded border bg-muted px-1.5 py-0.5 font-sans text-[10px] font-medium">Ctrl J</kbd>
    </button>
  );
}

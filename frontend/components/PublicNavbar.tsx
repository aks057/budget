"use client";

import Logo, { LogoMobile } from "@/components/Logo";
import { ThemeSwitcherBtn } from "@/components/ThemeSwitcherBtn";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { useAuth } from "@/components/providers/AuthProvider";
import { cn } from "@/lib/utils";
import { motion, useMotionValueEvent, useScroll } from "framer-motion";
import { ArrowRight, LayoutDashboard, Menu } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

const navItems = [
  { label: "AI demo", href: "/#ai-demo" },
  { label: "Features", href: "/#features" },
  { label: "Security", href: "/#security" },
  { label: "How it works", href: "/#how" },
  { label: "FAQ", href: "/#faq" },
];

export default function PublicNavbar() {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const { scrollY } = useScroll();
  useMotionValueEvent(scrollY, "change", (y) => setScrolled(y > 12));
  const { status, user } = useAuth();
  const ready = status !== "loading";

  const primaryCta = user ? (
    <Button asChild variant="gradient" size="sm" className="gap-1.5">
      <Link href="/dashboard">
        <LayoutDashboard className="h-4 w-4" /> Dashboard
      </Link>
    </Button>
  ) : (
    <Button asChild variant="gradient" size="sm" className="gap-1.5">
      <Link href="/sign-up">
        Start free <ArrowRight className="h-4 w-4" />
      </Link>
    </Button>
  );

  return (
    <motion.header
      initial={{ y: -20, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5 }}
      className="sticky top-0 z-40 w-full px-3 pt-3"
    >
      <nav
        aria-label="Main navigation"
        className={cn(
          "container flex h-14 items-center justify-between rounded-2xl border transition-all duration-300",
          scrolled ? "border-border/60 bg-background/70 shadow-glass backdrop-blur-xl" : "border-transparent bg-transparent"
        )}
      >
        <div className="flex items-center gap-8">
          <div className="hidden md:block">
            <Logo />
          </div>
          <div className="md:hidden">
            <LogoMobile />
          </div>
          <div className="hidden items-center gap-1 lg:flex">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="rounded-lg px-3 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-accent/60 hover:text-foreground"
              >
                {item.label}
              </Link>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-2">
          <ThemeSwitcherBtn />
          {ready && !user && (
            <Button asChild variant="ghost" size="sm" className="hidden sm:inline-flex">
              <Link href="/sign-in">Sign in</Link>
            </Button>
          )}
          {ready && primaryCta}
          <Sheet open={isOpen} onOpenChange={setIsOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="h-9 w-9 lg:hidden" aria-label="Open menu">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-[85vw] max-w-sm border-border/60 bg-background/90 backdrop-blur-xl">
              <SheetHeader className="mb-6">
                <SheetTitle asChild>
                  <div>
                    <Logo />
                  </div>
                </SheetTitle>
              </SheetHeader>
              <div className="flex flex-col gap-1">
                {navItems.map((item, index) => (
                  <motion.div key={item.href} initial={{ opacity: 0, x: 12 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: index * 0.05 }}>
                    <Link
                      href={item.href}
                      onClick={() => setIsOpen(false)}
                      className="block rounded-xl px-3 py-2.5 text-base text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                    >
                      {item.label}
                    </Link>
                  </motion.div>
                ))}
                {!user && (
                  <Link
                    href="/sign-in"
                    onClick={() => setIsOpen(false)}
                    className="mt-2 block rounded-xl px-3 py-2.5 text-base text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                  >
                    Sign in
                  </Link>
                )}
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </nav>
    </motion.header>
  );
}

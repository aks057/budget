"use client";

import Logo, { LogoMobile } from "@/components/Logo";
import { ThemeSwitcherBtn } from "@/components/ThemeSwitcherBtn";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Separator } from "@/components/ui/separator";
import { Menu, LayoutDashboard } from "lucide-react";
import Link from "next/link";
import { useState, useEffect } from "react";
import { createClient } from "@/lib/supabase/client";
import { User } from "@supabase/supabase-js";

const navItems = [
  { label: "About", href: "/about" },
  { label: "Privacy", href: "/privacy" },
  { label: "Terms", href: "/terms" },
];

export default function PublicNavbar() {
  const [isOpen, setIsOpen] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const supabase = createClient();

    // Check current session
    supabase.auth.getUser().then(({ data: { user } }) => {
      setUser(user);
      setLoading(false);
    });

    // Listen for auth changes
    const { data: { subscription } } = supabase.auth.onAuthStateChange(
      (_event, session) => {
        setUser(session?.user ?? null);
      }
    );

    return () => subscription.unsubscribe();
  }, []);

  return (
    <>
      {/* Desktop Navbar */}
      <div className="hidden border-separate border-b bg-background md:block">
        <nav className="container flex items-center justify-between px-8">
          <div className="flex h-16 items-center gap-x-8">
            <Logo />
            <div className="flex items-center gap-6">
              {navItems.map((item) => (
                <Link
                  key={item.label}
                  href={item.href}
                  className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-3">
            <ThemeSwitcherBtn />
            {!loading && (
              <>
                {user ? (
                  <Button asChild className="gap-2 bg-amber-500 hover:bg-amber-600">
                    <Link href="/dashboard">
                      <LayoutDashboard className="h-4 w-4" />
                      Dashboard
                    </Link>
                  </Button>
                ) : (
                  <>
                    <Button asChild variant="ghost">
                      <Link href="/sign-in">Sign In</Link>
                    </Button>
                    <Button asChild className="bg-amber-500 hover:bg-amber-600">
                      <Link href="/sign-up">Get Started</Link>
                    </Button>
                  </>
                )}
              </>
            )}
          </div>
        </nav>
      </div>

      {/* Mobile Navbar */}
      <div className="block border-separate bg-background md:hidden">
        <nav className="container flex h-14 items-center justify-between px-4">
          <div className="flex items-center gap-2">
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="h-9 w-9">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent className="w-[85vw] max-w-sm" side="left">
                <SheetHeader className="mb-4">
                  <SheetTitle asChild>
                    <Logo />
                  </SheetTitle>
                </SheetHeader>
                <Separator className="mb-4" />
                <div className="flex flex-col gap-2">
                  {navItems.map((item) => (
                    <Link
                      key={item.label}
                      href={item.href}
                      onClick={() => setIsOpen(false)}
                      className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                    >
                      {item.label}
                    </Link>
                  ))}
                  <Separator className="my-2" />
                  {user ? (
                    <Button asChild className="gap-2 bg-amber-500 hover:bg-amber-600">
                      <Link href="/dashboard" onClick={() => setIsOpen(false)}>
                        <LayoutDashboard className="h-4 w-4" />
                        Go to Dashboard
                      </Link>
                    </Button>
                  ) : (
                    <>
                      <Link
                        href="/sign-in"
                        onClick={() => setIsOpen(false)}
                        className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                      >
                        Sign In
                      </Link>
                      <Button asChild className="mt-2 bg-amber-500 hover:bg-amber-600">
                        <Link href="/sign-up" onClick={() => setIsOpen(false)}>
                          Get Started
                        </Link>
                      </Button>
                    </>
                  )}
                </div>
              </SheetContent>
            </Sheet>
            <LogoMobile />
          </div>
          <div className="flex items-center gap-2">
            <ThemeSwitcherBtn />
            {!loading && (
              user ? (
                <Button asChild size="sm" className="gap-2 bg-amber-500 hover:bg-amber-600">
                  <Link href="/dashboard">
                    <LayoutDashboard className="h-4 w-4" />
                    Dashboard
                  </Link>
                </Button>
              ) : (
                <Button asChild size="sm" className="bg-amber-500 hover:bg-amber-600">
                  <Link href="/sign-up">Get Started</Link>
                </Button>
              )
            )}
          </div>
        </nav>
      </div>
    </>
  );
}

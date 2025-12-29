"use client";
import Logo, { LogoMobile } from "@/components/Logo";
import { ThemeSwitcherBtn } from "@/components/ThemeSwitcherBtn";
import { CommandMenu } from "@/components/CommandMenu";
import { Notifications } from "@/components/Notifications";
import { Button, buttonVariants } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Separator } from "@/components/ui/separator";
import { cn } from "@/lib/utils";
import { UserButton } from "@/components/auth/UserButton";
import {
  Menu,
  LayoutDashboard,
  ArrowLeftRight,
  Settings,
  BarChart3,
  LucideIcon,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import React, { useState } from "react";

function Navbar() {
  return (
    <>
      {/* Skip to main content link for accessibility */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-brand focus:px-4 focus:py-2 focus:text-brand-foreground focus:outline-none"
      >
        Skip to main content
      </a>
      <DesktopNavbar />
      <MobileNavbar />
    </>
  );
}

const items: { label: string; link: string; icon: LucideIcon }[] = [
  { label: "Dashboard", link: "/dashboard", icon: LayoutDashboard },
  { label: "Transactions", link: "/transactions", icon: ArrowLeftRight },
  { label: "Analytics", link: "/analytics", icon: BarChart3 },
  { label: "Manage", link: "/manage", icon: Settings },
];

function MobileNavbar() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="block border-separate bg-background md:hidden">
      <nav className="container flex h-14 items-center justify-between px-2 sm:px-4" aria-label="Mobile navigation">
        <div className="flex items-center gap-2">
          <Sheet open={isOpen} onOpenChange={setIsOpen}>
            <SheetTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-9 w-9 shrink-0 transition-transform active:scale-95"
                aria-label="Open navigation menu"
                aria-expanded={isOpen}
              >
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
              <div className="flex flex-col gap-1">
                {items.map((item) => (
                  <NavbarItem
                    key={item.label}
                    link={item.link}
                    label={item.label}
                    icon={item.icon}
                    clickCallback={() => setIsOpen(false)}
                  />
                ))}
              </div>
            </SheetContent>
          </Sheet>
          <LogoMobile />
        </div>
        <div className="flex items-center gap-1 sm:gap-2">
          <CommandMenu />
          <Notifications />
          <ThemeSwitcherBtn />
          <UserButton />
        </div>
      </nav>
    </div>
  );
}

function DesktopNavbar() {
  return (
    <div className="hidden border-separate border-b bg-background md:block">
      <nav className="container flex items-center justify-between px-8" aria-label="Main navigation">
        <div className="flex h-16 items-center gap-x-4">
          <Logo />
          <div className="flex h-full">
            {items.map((item) => (
              <NavbarItem
                key={item.label}
                link={item.link}
                label={item.label}
                icon={item.icon}
              />
            ))}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <CommandMenu />
          <Notifications />
          <ThemeSwitcherBtn />
          <UserButton />
        </div>
      </nav>
    </div>
  );
}

function NavbarItem({
  link,
  label,
  icon: Icon,
  clickCallback,
}: {
  link: string;
  label: string;
  icon: LucideIcon;
  clickCallback?: () => void;
}) {
  const pathname = usePathname();
  const isActive = pathname === link;

  return (
    <div className="relative flex items-center">
      <Link
        href={link}
        className={cn(
          buttonVariants({ variant: "ghost" }),
          "w-full justify-start gap-2 text-base text-muted-foreground transition-all duration-200",
          "hover:bg-accent/50 hover:text-foreground",
          "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand",
          "active:scale-95",
          isActive && "bg-accent/30 text-brand hover:bg-accent/50 hover:text-brand"
        )}
        onClick={() => {
          if (clickCallback) clickCallback();
        }}
        aria-current={isActive ? "page" : undefined}
      >
        <Icon className="h-4 w-4" />
        {label}
      </Link>
      {isActive && (
        <div className="absolute -bottom-[2px] left-1/2 hidden h-1 w-[80%] -translate-x-1/2 rounded-xl bg-brand md:block" />
      )}
    </div>
  );
}

export default Navbar;

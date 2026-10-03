"use client";

import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { NAV_SECTIONS, SETTINGS_ITEM, isActivePath, type NavItem } from "@/components/layout/navItems";
import { LogoMark } from "@/components/Logo";
import { useAuth } from "@/components/providers/AuthProvider";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { getUnreadInsightCount } from "@/lib/api/insights";
import { INSIGHT_KEYS } from "@/lib/insights";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { ChevronsLeft, ChevronsRight, ChevronsUpDown, LogOut, Sparkles, UserRound } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";

const COLLAPSED_KEY = "bw-sidebar-collapsed";

/** Desktop sidebar: full width or an icon rail; the choice is remembered per browser. */
export function AppSidebar() {
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    try {
      setCollapsed(localStorage.getItem(COLLAPSED_KEY) === "1");
    } catch {
      // Storage unavailable (private mode): default to expanded.
    }
  }, []);

  const toggle = () => {
    setCollapsed((value) => {
      try {
        localStorage.setItem(COLLAPSED_KEY, value ? "0" : "1");
      } catch {
        // Ignore: the preference just won't persist.
      }
      return !value;
    });
  };

  return (
    <aside
      className={cn(
        "sticky top-0 hidden h-screen shrink-0 flex-col border-r bg-card/40 transition-[width] duration-200 md:flex",
        collapsed ? "w-[68px]" : "w-64"
      )}
    >
      <SidebarContent collapsed={collapsed} />
      <button
        type="button"
        onClick={toggle}
        aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        className="absolute -right-3 top-20 flex h-6 w-6 items-center justify-center rounded-full border bg-background text-muted-foreground shadow-sm transition-colors hover:text-foreground"
      >
        {collapsed ? <ChevronsRight className="h-3.5 w-3.5" /> : <ChevronsLeft className="h-3.5 w-3.5" />}
      </button>
    </aside>
  );
}

/** Sidebar body, shared by the desktop sidebar and the mobile drawer. */
export function SidebarContent({ collapsed = false, onNavigate }: { collapsed?: boolean; onNavigate?: () => void }) {
  const pathname = usePathname();
  const { setOpen: openAiPanel } = useAiPanel();
  const unread = useQuery({ queryKey: INSIGHT_KEYS.unread, queryFn: getUnreadInsightCount, refetchInterval: 5 * 60 * 1000 });

  return (
    <TooltipProvider delayDuration={100}>
      <div className="flex h-full flex-col">
        {/* Brand */}
        <div className={cn("flex h-14 items-center border-b", collapsed ? "justify-center" : "gap-2.5 px-4")}>
          <Link href="/dashboard" onClick={onNavigate} className="flex items-center gap-2.5" aria-label="Bud-Wiser dashboard">
            <LogoMark className="h-8 w-8" />
            {!collapsed && <span className="font-display text-lg font-bold tracking-tight">Bud-Wiser</span>}
          </Link>
        </div>

        {/* Ask AI */}
        <div className={cn("pt-4", collapsed ? "px-3" : "px-3")}>
          <WithTooltip show={collapsed} label="Ask AI (Ctrl+J)">
            <button
              type="button"
              onClick={() => {
                onNavigate?.();
                openAiPanel(true);
              }}
              className={cn(
                "flex w-full items-center rounded-lg bg-brand-gradient text-sm font-semibold text-brand-foreground transition-[filter] hover:brightness-110",
                collapsed ? "h-10 justify-center" : "h-10 gap-2 px-3"
              )}
            >
              <Sparkles className="h-4 w-4 shrink-0" />
              {!collapsed && (
                <>
                  Ask AI
                  <kbd className="ml-auto rounded bg-black/15 px-1.5 py-0.5 font-sans text-[10px] font-medium">Ctrl J</kbd>
                </>
              )}
            </button>
          </WithTooltip>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto px-3 py-4" aria-label="Main navigation">
          {NAV_SECTIONS.map((section) => (
            <div key={section.title} className="mb-5">
              {!collapsed ? (
                <p className="mb-1.5 px-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted-foreground/70">{section.title}</p>
              ) : (
                <div className="mx-auto mb-2 h-px w-6 bg-border" />
              )}
              <ul className="space-y-0.5">
                {section.items.map((item) => (
                  <li key={item.href}>
                    <SidebarLink
                      item={item}
                      active={isActivePath(pathname, item.href)}
                      collapsed={collapsed}
                      onNavigate={onNavigate}
                      badge={item.href === "/insights" ? unread.data : undefined}
                    />
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>

        {/* Footer: settings + account */}
        <div className="space-y-1 border-t p-3">
          <SidebarLink item={SETTINGS_ITEM} active={isActivePath(pathname, SETTINGS_ITEM.href)} collapsed={collapsed} onNavigate={onNavigate} />
          <UserCard collapsed={collapsed} onNavigate={onNavigate} />
        </div>
      </div>
    </TooltipProvider>
  );
}

function SidebarLink({
  item,
  active,
  collapsed,
  onNavigate,
  badge,
}: {
  item: NavItem;
  active: boolean;
  collapsed: boolean;
  onNavigate?: () => void;
  badge?: number;
}) {
  const showBadge = badge !== undefined && badge > 0;
  return (
    <WithTooltip show={collapsed} label={item.label}>
      <Link
        href={item.href}
        onClick={onNavigate}
        aria-current={active ? "page" : undefined}
        aria-label={collapsed ? item.label : undefined}
        className={cn(
          "relative flex h-9 items-center rounded-lg text-sm transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
          collapsed ? "justify-center" : "gap-3 px-2.5",
          active ? "bg-primary/10 font-medium text-primary" : "text-muted-foreground hover:bg-accent hover:text-foreground"
        )}
      >
        {active && <span className="absolute -left-3 top-1.5 h-6 w-1 rounded-r-full bg-primary" aria-hidden />}
        <item.icon className="h-[18px] w-[18px] shrink-0" />
        {!collapsed && <span className="flex-1 truncate">{item.label}</span>}
        {showBadge &&
          (collapsed ? (
            <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-primary" aria-label={`${badge} unread`} />
          ) : (
            <span className="rounded-full bg-primary px-1.5 text-[10px] font-semibold leading-4 text-primary-foreground">
              {badge > 9 ? "9+" : badge}
            </span>
          ))}
      </Link>
    </WithTooltip>
  );
}

function UserCard({ collapsed, onNavigate }: { collapsed: boolean; onNavigate?: () => void }) {
  const { user, logout } = useAuth();
  const router = useRouter();
  if (!user) return null;
  const initials = user.fullName.trim().split(/\s+/).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("") || "U";

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button
          type="button"
          aria-label="Account menu"
          className={cn(
            "flex w-full items-center rounded-lg text-left transition-colors hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
            collapsed ? "justify-center p-1.5" : "gap-2.5 p-2"
          )}
        >
          <Avatar className="h-8 w-8">
            <AvatarImage src={user.avatarUrl ?? undefined} alt="" />
            <AvatarFallback className="bg-primary/15 text-xs font-semibold text-primary">{initials}</AvatarFallback>
          </Avatar>
          {!collapsed && (
            <>
              <span className="min-w-0 flex-1">
                <span className="block truncate text-sm font-medium">{user.fullName}</span>
                <span className="block truncate text-xs text-muted-foreground">{user.email}</span>
              </span>
              <ChevronsUpDown className="h-4 w-4 shrink-0 text-muted-foreground" />
            </>
          )}
        </button>
      </DropdownMenuTrigger>
      <DropdownMenuContent side={collapsed ? "right" : "top"} align="start" className="w-56">
        <DropdownMenuLabel className="font-normal">
          <p className="truncate text-sm font-medium">{user.fullName}</p>
          <p className="truncate text-xs text-muted-foreground">{user.email}</p>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={() => {
            onNavigate?.();
            router.push("/profile");
          }}
        >
          <UserRound className="mr-2 h-4 w-4" /> Profile
        </DropdownMenuItem>
        <DropdownMenuItem
          className="text-expense focus:text-expense"
          onClick={async () => {
            await logout();
            router.replace("/sign-in");
          }}
        >
          <LogOut className="mr-2 h-4 w-4" /> Sign out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

function WithTooltip({ show, label, children }: { show: boolean; label: string; children: ReactNode }) {
  if (!show) return <>{children}</>;
  return (
    <Tooltip>
      <TooltipTrigger asChild>{children}</TooltipTrigger>
      <TooltipContent side="right">{label}</TooltipContent>
    </Tooltip>
  );
}

"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import {
  LayoutDashboard,
  ArrowLeftRight,
  Settings,
  User,
  Search,
  Moon,
  Sun,
  Monitor,
  Receipt,
  Tags,
  Clock,
} from "lucide-react";
import { useTheme } from "next-themes";

import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from "@/components/ui/command";
import { Button } from "@/components/ui/button";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";

// Recent pages storage key
const RECENT_PAGES_KEY = "budwiser-recent-pages";
const MAX_RECENT_PAGES = 3;

interface RecentPage {
  label: string;
  href: string;
  icon: string;
}

function getRecentPages(): RecentPage[] {
  if (typeof window === "undefined") return [];
  try {
    const stored = localStorage.getItem(RECENT_PAGES_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}

function addRecentPage(page: RecentPage) {
  if (typeof window === "undefined") return;
  try {
    const pages = getRecentPages().filter((p) => p.href !== page.href);
    pages.unshift(page);
    localStorage.setItem(
      RECENT_PAGES_KEY,
      JSON.stringify(pages.slice(0, MAX_RECENT_PAGES))
    );
  } catch {
    // Ignore storage errors
  }
}

export function CommandMenu() {
  const [open, setOpen] = React.useState(false);
  const [recentPages, setRecentPages] = React.useState<RecentPage[]>([]);
  const router = useRouter();
  const { setTheme } = useTheme();

  // Load recent pages on mount
  React.useEffect(() => {
    setRecentPages(getRecentPages());
  }, [open]);

  React.useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === "k" && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setOpen((open) => !open);
      }
    };

    document.addEventListener("keydown", down);
    return () => document.removeEventListener("keydown", down);
  }, []);

  const runCommand = React.useCallback((command: () => void) => {
    setOpen(false);
    command();
  }, []);

  const navigateTo = React.useCallback(
    (href: string, label: string, icon: string) => {
      addRecentPage({ href, label, icon });
      runCommand(() => router.push(href));
    },
    [router, runCommand]
  );

  return (
    <>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              variant="outline"
              className="relative h-8 w-8 shrink-0 p-0 transition-transform active:scale-95 sm:h-9 sm:w-9 xl:h-9 xl:w-60 xl:justify-start xl:px-3 xl:py-2"
              onClick={() => setOpen(true)}
              aria-label="Search commands (⌘K)"
            >
              <Search className="h-4 w-4 xl:mr-2" />
              <span className="hidden xl:inline-flex">Search...</span>
              <kbd className="pointer-events-none absolute right-1.5 top-1.5 hidden h-6 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium opacity-100 xl:flex">
                <span className="text-xs">⌘</span>K
              </kbd>
            </Button>
          </TooltipTrigger>
          <TooltipContent className="xl:hidden">
            <p>Search (⌘K)</p>
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <CommandDialog open={open} onOpenChange={setOpen}>
        <CommandInput placeholder="Type a command or search..." />
        <CommandList className="max-h-[60vh]">
          <CommandEmpty>No results found.</CommandEmpty>

          {/* Recent Pages */}
          {recentPages.length > 0 && (
            <>
              <CommandGroup heading="Recent">
                {recentPages.map((page) => (
                  <CommandItem
                    key={page.href}
                    onSelect={() => navigateTo(page.href, page.label, page.icon)}
                  >
                    <Clock className="mr-2 h-4 w-4 text-muted-foreground" />
                    {page.label}
                  </CommandItem>
                ))}
              </CommandGroup>
              <CommandSeparator />
            </>
          )}

          {/* Quick Actions */}
          <CommandGroup heading="Quick Actions">
            <CommandItem
              onSelect={() => navigateTo("/transactions", "Search Transactions", "receipt")}
            >
              <Receipt className="mr-2 h-4 w-4" />
              Search Transactions
            </CommandItem>
            <CommandItem
              onSelect={() => navigateTo("/manage", "Manage Categories", "tags")}
            >
              <Tags className="mr-2 h-4 w-4" />
              Manage Categories
            </CommandItem>
          </CommandGroup>
          <CommandSeparator />

          {/* Navigation */}
          <CommandGroup heading="Navigation">
            <CommandItem
              onSelect={() => navigateTo("/", "Dashboard", "dashboard")}
            >
              <LayoutDashboard className="mr-2 h-4 w-4" />
              Dashboard
            </CommandItem>
            <CommandItem
              onSelect={() => navigateTo("/transactions", "Transactions", "transactions")}
            >
              <ArrowLeftRight className="mr-2 h-4 w-4" />
              Transactions
            </CommandItem>
            <CommandItem
              onSelect={() => navigateTo("/manage", "Manage", "manage")}
            >
              <Settings className="mr-2 h-4 w-4" />
              Manage
            </CommandItem>
            <CommandItem
              onSelect={() => navigateTo("/profile", "Profile", "profile")}
            >
              <User className="mr-2 h-4 w-4" />
              Profile
            </CommandItem>
          </CommandGroup>
          <CommandSeparator />

          {/* Theme */}
          <CommandGroup heading="Theme">
            <CommandItem onSelect={() => runCommand(() => setTheme("light"))}>
              <Sun className="mr-2 h-4 w-4" />
              Light Mode
            </CommandItem>
            <CommandItem onSelect={() => runCommand(() => setTheme("dark"))}>
              <Moon className="mr-2 h-4 w-4" />
              Dark Mode
            </CommandItem>
            <CommandItem onSelect={() => runCommand(() => setTheme("system"))}>
              <Monitor className="mr-2 h-4 w-4" />
              System Theme
            </CommandItem>
          </CommandGroup>
        </CommandList>
      </CommandDialog>
    </>
  );
}

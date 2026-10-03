import {
  ArrowLeftRight,
  BarChart3,
  BellRing,
  LayoutDashboard,
  MessageSquareText,
  PiggyBank,
  Settings,
  Target,
  UserRound,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
}

export interface NavSection {
  title: string;
  items: NavItem[];
}

/** Single source of truth for the app's routes: sidebar sections, top-bar titles and the command menu. */
export const NAV_SECTIONS: NavSection[] = [
  {
    title: "Overview",
    items: [
      { label: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
      { label: "Analytics", href: "/analytics", icon: BarChart3 },
    ],
  },
  {
    title: "Money",
    items: [
      { label: "Transactions", href: "/transactions", icon: ArrowLeftRight },
      { label: "Budgets", href: "/budgets", icon: PiggyBank },
      { label: "Goals", href: "/goals", icon: Target },
    ],
  },
  {
    title: "Assistant",
    items: [
      { label: "AI Chat", href: "/ai", icon: MessageSquareText },
      { label: "Insights", href: "/insights", icon: BellRing },
    ],
  },
];

export const SETTINGS_ITEM: NavItem = { label: "Settings", href: "/manage", icon: Settings };
const PROFILE_ITEM: NavItem = { label: "Profile", href: "/profile", icon: UserRound };

export const ALL_NAV_ITEMS: NavItem[] = [...NAV_SECTIONS.flatMap((section) => section.items), SETTINGS_ITEM, PROFILE_ITEM];

export const isActivePath = (pathname: string, href: string) => pathname === href || pathname.startsWith(`${href}/`);

export function pageTitle(pathname: string): string {
  return ALL_NAV_ITEMS.find((item) => isActivePath(pathname, item.href))?.label ?? "Bud-Wiser";
}

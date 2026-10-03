"use client";

import { PageHeader } from "@/components/PageHeader";
import { useCurrentUser } from "@/components/providers/AuthProvider";
import { BarChart3 } from "lucide-react";
import AnalyticsDashboard from "./_components/AnalyticsDashboard";

export default function AnalyticsPage() {
  const user = useCurrentUser();

  return (
    <div>
      <PageHeader icon={BarChart3} title="Analytics" subtitle="Trends, category breakdowns and where your money goes" />
      <AnalyticsDashboard currency={user.currency} />
    </div>
  );
}

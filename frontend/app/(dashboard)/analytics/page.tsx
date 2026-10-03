"use client";

import { useCurrentUser } from "@/components/providers/AuthProvider";
import AnalyticsDashboard from "./_components/AnalyticsDashboard";

export default function AnalyticsPage() {
  const user = useCurrentUser();

  return (
    <div className="h-full bg-background">
      <div className="border-b bg-card">
        <div className="container py-8">
          <h1 className="text-3xl font-bold">Analytics</h1>
          <p className="mt-1 text-muted-foreground">
            Detailed insights into your financial activity
          </p>
        </div>
      </div>
      <AnalyticsDashboard currency={user.currency} />
    </div>
  );
}

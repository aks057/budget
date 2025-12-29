import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import { redirect } from "next/navigation";
import AnalyticsDashboard from "./_components/AnalyticsDashboard";

export default async function AnalyticsPage() {
  const user = await requireUser();
  const supabase = await createClient();

  const { data: userSettings } = await supabase
    .from("user_settings")
    .select("*")
    .eq("user_id", user.id)
    .single();

  if (!userSettings) {
    redirect("/wizard");
  }

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
      <AnalyticsDashboard userSettings={userSettings} />
    </div>
  );
}

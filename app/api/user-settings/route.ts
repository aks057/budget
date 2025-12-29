import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import { revalidatePath } from "next/cache";

export async function GET(request: Request) {
  const user = await requireUser();
  const supabase = await createClient();

  let { data: userSettings, error } = await supabase
    .from("user_settings")
    .select("*")
    .eq("user_id", user.id)
    .single();

  if (error && error.code === "PGRST116") {
    // No rows found, create default settings
    const { data: newSettings, error: createError } = await supabase
      .from("user_settings")
      .insert({
        user_id: user.id,
        currency: "INR",
      })
      .select()
      .single();

    if (createError) {
      return Response.json({ error: createError.message }, { status: 500 });
    }

    userSettings = newSettings;
  } else if (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }

  // Revalidate the home page that uses the user currency
  revalidatePath("/");
  return Response.json(userSettings);
}

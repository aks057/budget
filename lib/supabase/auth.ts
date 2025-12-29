import { createClient } from "./server";
import { redirect } from "next/navigation";

export async function getUser() {
  const supabase = await createClient();
  const {
    data: { user },
    error,
  } = await supabase.auth.getUser();

  if (error || !user) {
    return null;
  }

  return user;
}

export async function requireUser() {
  const user = await getUser();

  if (!user) {
    redirect("/sign-in");
  }

  return user;
}

// Get user metadata (name, avatar, etc.)
export async function getUserMetadata() {
  const user = await getUser();

  if (!user) return null;

  return {
    id: user.id,
    email: user.email,
    firstName:
      user.user_metadata?.first_name || user.email?.split("@")[0] || "User",
    lastName: user.user_metadata?.last_name || "",
    avatarUrl: user.user_metadata?.avatar_url || null,
  };
}

"use client";

import { GoogleIcon } from "@/components/auth/GoogleIcon";
import { safeNextPath } from "@/components/auth/RequireAuth";
import { useAuth } from "@/components/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiError } from "@/lib/api/client";
import { Loader2, PiggyBank } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { toast } from "sonner";

export function SignInForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const { login, loginWithGoogle } = useAuth();
  const router = useRouter();

  // The backend redirects here with ?error=oauth_failed when Google sign-in is rejected.
  useEffect(() => {
    if (new URLSearchParams(window.location.search).get("error")) {
      toast.error("Google sign-in failed. Please try again or use email and password.");
    }
  }, []);

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      toast.success("Signed in successfully!");
      router.replace(safeNextPath(new URLSearchParams(window.location.search).get("next"), "/dashboard"));
    } catch (error) {
      toast.error(error instanceof ApiError ? error.userMessage : "Could not sign in. Please try again.");
      setLoading(false);
    }
  };

  const handleGoogleSignIn = () => {
    setGoogleLoading(true);
    loginWithGoogle();
  };

  const isLoading = loading || googleLoading;

  return (
    <div className="grid gap-6">
      {/* Loading Overlay with Logo */}
      {isLoading && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
          <div className="flex flex-col items-center gap-4">
            <PiggyBank className="h-16 w-16 animate-bounce stroke-amber-500 stroke-[1.5]" />
            <p className="animate-pulse bg-gradient-to-r from-amber-400 to-orange-500 bg-clip-text text-xl font-semibold text-transparent">
              {googleLoading ? "Connecting to Google..." : "Signing you in..."}
            </p>
          </div>
        </div>
      )}

      <form onSubmit={handleSignIn}>
        <div className="grid gap-4">
          <div className="grid gap-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              autoComplete="email"
              placeholder="m@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              autoComplete="current-password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <Button disabled={isLoading}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Sign In
          </Button>
        </div>
      </form>

      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <span className="w-full border-t" />
        </div>
        <div className="relative flex justify-center text-xs uppercase">
          <span className="bg-background px-2 text-muted-foreground">Or continue with</span>
        </div>
      </div>

      <Button variant="outline" type="button" disabled={isLoading} onClick={handleGoogleSignIn}>
        {googleLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <GoogleIcon className="mr-2 h-4 w-4" />}
        Google
      </Button>
    </div>
  );
}

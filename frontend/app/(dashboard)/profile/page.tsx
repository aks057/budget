"use client";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import { useAuth, useCurrentUser } from "@/components/providers/AuthProvider";
import { ApiError } from "@/lib/api/client";
import { updateMe } from "@/lib/api/endpoints";
import { useMutation } from "@tanstack/react-query";
import { Reveal } from "@/components/motion";
import { PageHeader } from "@/components/PageHeader";
import { Loader2, UserRound } from "lucide-react";
import React, { useEffect, useState } from "react";
import { toast } from "sonner";

const MAX_NAME_LENGTH = 120;

function initialsOf(fullName: string, email: string): string {
  const parts = fullName.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return email[0]?.toUpperCase() ?? "U";
  return parts
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("");
}

function ProfilePage() {
  const user = useCurrentUser();
  const { setUser } = useAuth();
  const [fullName, setFullName] = useState(user.fullName);

  // Keep the field in sync if the session user changes elsewhere (e.g. another tab).
  useEffect(() => {
    setFullName(user.fullName);
  }, [user.fullName]);

  const updateProfileMutation = useMutation({
    mutationFn: (name: string) => updateMe({ fullName: name, currency: user.currency }),
    onSuccess: (updated) => {
      setUser(updated);
      toast.success("Profile updated successfully!");
    },
    onError: (error) => {
      toast.error(error instanceof ApiError ? error.userMessage : "Failed to update profile");
    },
  });

  const trimmedName = fullName.trim();
  const isValid = trimmedName.length > 0 && trimmedName.length <= MAX_NAME_LENGTH;
  const isDirty = trimmedName !== user.fullName;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isValid) {
      toast.error("Name is required");
      return;
    }
    updateProfileMutation.mutate(trimmedName);
  };

  const isPending = updateProfileMutation.isPending;

  return (
    <div className="pb-16">
      <PageHeader icon={UserRound} title="Profile" subtitle="Your personal information" />

      <Reveal onMount delay={0.05} className="container flex max-w-3xl flex-col gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
            <CardDescription>Update your profile details</CardDescription>
          </CardHeader>
          <Separator />
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Avatar: Google profile photo when available, initials otherwise */}
              <div className="flex flex-col items-center gap-4 sm:flex-row sm:items-start">
                <Avatar className="h-24 w-24 ring-2 ring-primary/40 ring-offset-4 ring-offset-background">
                  <AvatarImage src={user.avatarUrl || undefined} alt="Profile" />
                  <AvatarFallback className="text-2xl bg-brand-gradient text-brand-foreground">
                    {initialsOf(user.fullName, user.email)}
                  </AvatarFallback>
                </Avatar>
                <div className="text-center sm:text-left">
                  <p className="font-medium">Profile Picture</p>
                  <p className="text-sm text-muted-foreground">
                    {user.avatarUrl
                      ? "Synced from your Google account."
                      : "Sign in with Google to use your Google profile photo."}
                  </p>
                </div>
              </div>

              <Separator />

              <div className="space-y-2">
                <Label htmlFor="fullName">Full Name</Label>
                <Input
                  id="fullName"
                  value={fullName}
                  maxLength={MAX_NAME_LENGTH}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Enter your full name"
                  disabled={isPending}
                />
              </div>

              {/* Email (Read-only) */}
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" value={user.email} disabled className="bg-muted" />
                <p className="text-xs text-muted-foreground">Email cannot be changed</p>
              </div>

              <div className="flex justify-end">
                <Button type="submit" disabled={isPending || !isDirty || !isValid}>
                  {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Save Changes
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </Reveal>
    </div>
  );
}

export default ProfilePage;

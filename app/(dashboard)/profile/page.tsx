"use client";

import { createClient } from "@/lib/supabase/client";
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
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Camera, Loader2, User } from "lucide-react";
import React, { useRef, useState, useEffect } from "react";
import { toast } from "sonner";
import type { User as SupabaseUser } from "@supabase/supabase-js";

function ProfilePage() {
  const supabase = createClient();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [avatarUrl, setAvatarUrl] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  // Fetch user data
  const userQuery = useQuery({
    queryKey: ["user-profile"],
    queryFn: async () => {
      const { data: { user }, error } = await supabase.auth.getUser();
      if (error || !user) throw new Error("Not authenticated");
      return user;
    },
  });

  // Set form values when user data loads
  useEffect(() => {
    if (userQuery.data) {
      setFirstName(userQuery.data.user_metadata?.first_name || "");
      setLastName(userQuery.data.user_metadata?.last_name || "");
      setAvatarUrl(userQuery.data.user_metadata?.avatar_url || null);
    }
  }, [userQuery.data]);

  // Update profile mutation
  const updateProfileMutation = useMutation({
    mutationFn: async ({ firstName, lastName, avatarUrl }: { firstName: string; lastName: string; avatarUrl?: string | null }) => {
      const { data, error } = await supabase.auth.updateUser({
        data: {
          first_name: firstName,
          last_name: lastName,
          ...(avatarUrl !== undefined && { avatar_url: avatarUrl }),
        },
      });
      if (error) throw error;
      return data;
    },
    onSuccess: () => {
      toast.success("Profile updated successfully!");
      queryClient.invalidateQueries({ queryKey: ["user-profile"] });
    },
    onError: (error: Error) => {
      toast.error(error.message || "Failed to update profile");
    },
  });

  // Handle avatar upload
  const handleAvatarUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith("image/")) {
      toast.error("Please select an image file");
      return;
    }

    // Validate file size (max 2MB)
    if (file.size > 2 * 1024 * 1024) {
      toast.error("Image must be less than 2MB");
      return;
    }

    setUploading(true);

    try {
      const user = userQuery.data;
      if (!user) throw new Error("Not authenticated");

      const fileExt = file.name.split(".").pop();
      const fileName = `${user.id}/avatar.${fileExt}`;

      // Upload to Supabase Storage
      const { error: uploadError } = await supabase.storage
        .from("avatars")
        .upload(fileName, file, { upsert: true });

      if (uploadError) throw uploadError;

      // Get public URL
      const { data: { publicUrl } } = supabase.storage
        .from("avatars")
        .getPublicUrl(fileName);

      // Add timestamp to bust cache
      const avatarUrlWithTimestamp = `${publicUrl}?t=${Date.now()}`;

      // Update user metadata with new avatar URL
      await updateProfileMutation.mutateAsync({
        firstName,
        lastName,
        avatarUrl: avatarUrlWithTimestamp,
      });

      setAvatarUrl(avatarUrlWithTimestamp);
    } catch (error: any) {
      toast.error(error.message || "Failed to upload avatar");
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate({ firstName, lastName });
  };

  const user = userQuery.data;
  const initials = firstName?.[0]?.toUpperCase() || user?.email?.[0]?.toUpperCase() || "U";
  const isLoading = updateProfileMutation.isPending || uploading;

  return (
    <>
      {/* HEADER */}
      <div className="border-b bg-card">
        <div className="container flex flex-wrap items-center justify-between gap-6 py-8">
          <div>
            <p className="text-3xl font-bold">Profile</p>
            <p className="text-muted-foreground">
              Manage your personal information
            </p>
          </div>
        </div>
      </div>

      {/* CONTENT */}
      <div className="container flex flex-col gap-4 p-4">
        <SkeletonWrapper isLoading={userQuery.isLoading}>
          <Card>
            <CardHeader>
              <CardTitle>Personal Information</CardTitle>
              <CardDescription>
                Update your profile details and avatar
              </CardDescription>
            </CardHeader>
            <Separator />
            <CardContent className="pt-6">
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Avatar Section */}
                <div className="flex flex-col items-center gap-4 sm:flex-row sm:items-start">
                  <div className="relative group">
                    <Avatar className="h-24 w-24 cursor-pointer" onClick={() => fileInputRef.current?.click()}>
                      <AvatarImage src={avatarUrl || undefined} alt="Profile" />
                      <AvatarFallback className="text-2xl bg-gradient-to-br from-amber-400 to-orange-500 text-white">
                        {initials}
                      </AvatarFallback>
                    </Avatar>
                    <div
                      className="absolute inset-0 flex items-center justify-center rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      {uploading ? (
                        <Loader2 className="h-6 w-6 text-white animate-spin" />
                      ) : (
                        <Camera className="h-6 w-6 text-white" />
                      )}
                    </div>
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={handleAvatarUpload}
                      disabled={uploading}
                    />
                  </div>
                  <div className="text-center sm:text-left">
                    <p className="font-medium">Profile Picture</p>
                    <p className="text-sm text-muted-foreground">
                      Click to upload a new avatar. Max size 2MB.
                    </p>
                  </div>
                </div>

                <Separator />

                {/* Name Fields */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">First Name</Label>
                    <Input
                      id="firstName"
                      value={firstName}
                      onChange={(e) => setFirstName(e.target.value)}
                      placeholder="Enter your first name"
                      disabled={isLoading}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName">Last Name</Label>
                    <Input
                      id="lastName"
                      value={lastName}
                      onChange={(e) => setLastName(e.target.value)}
                      placeholder="Enter your last name"
                      disabled={isLoading}
                    />
                  </div>
                </div>

                {/* Email (Read-only) */}
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    value={user?.email || ""}
                    disabled
                    className="bg-muted"
                  />
                  <p className="text-xs text-muted-foreground">
                    Email cannot be changed
                  </p>
                </div>

                {/* Submit Button */}
                <div className="flex justify-end">
                  <Button type="submit" disabled={isLoading}>
                    {updateProfileMutation.isPending && (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    )}
                    Save Changes
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </SkeletonWrapper>
      </div>
    </>
  );
}

export default ProfilePage;

"use server";

import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import {
  CreateCategorySchema,
  CreateCategorySchemaType,
  DeleteCategorySchema,
  DeleteCategorySchemaType,
} from "@/schema/categories";

export async function CreateCategory(form: CreateCategorySchemaType) {
  const parsedBody = CreateCategorySchema.safeParse(form);
  if (!parsedBody.success) {
    throw new Error("bad request");
  }

  const user = await requireUser();
  const supabase = await createClient();

  const { name, icon, type } = parsedBody.data;

  const { data, error } = await supabase
    .from("categories")
    .insert({
      user_id: user.id,
      name,
      icon,
      type,
    })
    .select()
    .single();

  if (error) {
    throw new Error(error.message);
  }

  return data;
}

export async function DeleteCategory(form: DeleteCategorySchemaType) {
  const parsedBody = DeleteCategorySchema.safeParse(form);
  if (!parsedBody.success) {
    throw new Error("bad request");
  }

  const user = await requireUser();
  const supabase = await createClient();

  const { data, error } = await supabase
    .from("categories")
    .delete()
    .eq("user_id", user.id)
    .eq("name", parsedBody.data.name)
    .eq("type", parsedBody.data.type)
    .select()
    .single();

  if (error) {
    throw new Error(error.message);
  }

  return data;
}

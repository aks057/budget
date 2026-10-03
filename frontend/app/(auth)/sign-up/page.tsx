import { SignUpForm } from "@/components/auth/SignUpForm";
import Link from "next/link";

export default function Page() {
  return (
    <>
      <div className="flex flex-col space-y-2 text-center">
        <h1 className="font-display text-3xl font-bold tracking-tight">Create an account</h1>
        <p className="text-sm text-muted-foreground">
          Enter your details below to create your account
        </p>
      </div>
      <SignUpForm />
      <p className="px-8 text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link
          href="/sign-in"
          className="font-medium text-primary underline-offset-4 hover:underline"
        >
          Sign in
        </Link>
      </p>
    </>
  );
}

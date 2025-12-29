import { createServerClient } from "@supabase/ssr";
import { NextResponse, type NextRequest } from "next/server";

export async function middleware(request: NextRequest) {
  let response = NextResponse.next({
    request: {
      headers: request.headers,
    },
  });

  const supabase = createServerClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      cookies: {
        getAll() {
          return request.cookies.getAll();
        },
        setAll(cookiesToSet) {
          cookiesToSet.forEach(({ name, value }) =>
            request.cookies.set(name, value)
          );
          response = NextResponse.next({
            request: {
              headers: request.headers,
            },
          });
          cookiesToSet.forEach(({ name, value, options }) =>
            response.cookies.set(name, value, options)
          );
        },
      },
    }
  );

  // Refresh session if expired
  const {
    data: { user },
  } = await supabase.auth.getUser();

  // Public routes - accessible without authentication
  const publicPaths = [
    "/",
    "/about",
    "/blog",
    "/careers",
    "/contact",
    "/privacy",
    "/terms",
    "/cookies",
  ];
  const isPublicPath = publicPaths.some(
    (path) =>
      request.nextUrl.pathname === path ||
      (path !== "/" && request.nextUrl.pathname.startsWith(path + "/"))
  );

  // Exclude auth callback and public paths from protection
  if (
    request.nextUrl.pathname.startsWith("/auth/callback") ||
    isPublicPath
  ) {
    return response;
  }

  // Protected routes - redirect to sign-in if not authenticated
  const protectedPaths = ["/dashboard", "/wizard", "/transactions", "/manage", "/analytics", "/profile"];
  const isProtectedPath =
    protectedPaths.some(
      (path) =>
        request.nextUrl.pathname === path ||
        request.nextUrl.pathname.startsWith(path + "/")
    ) || request.nextUrl.pathname.startsWith("/api/");

  // Auth pages - redirect to dashboard if already authenticated
  const authPaths = ["/sign-in", "/sign-up"];
  const isAuthPath = authPaths.some((path) =>
    request.nextUrl.pathname.startsWith(path)
  );

  if (isProtectedPath && !user) {
    return NextResponse.redirect(new URL("/sign-in", request.url));
  }

  if (isAuthPath && user) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  return response;
}


export const config = {
  matcher: [
    "/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)",
  ],
};

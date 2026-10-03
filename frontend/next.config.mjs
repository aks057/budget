/** @type {import('next').NextConfig} */
const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig = {
  // Same-origin API: the browser only talks to this app; Next proxies to Spring Boot (in production Caddy does the
  // same routing). This keeps the httpOnly, SameSite=Strict refresh cookie working with no CORS.
  async rewrites() {
    return [
      { source: "/api/v1/:path*", destination: `${backendUrl}/api/v1/:path*` },
      { source: "/oauth2/:path*", destination: `${backendUrl}/oauth2/:path*` },
      { source: "/login/oauth2/:path*", destination: `${backendUrl}/login/oauth2/:path*` },
    ];
  },
  images: {
    remotePatterns: [{ protocol: "https", hostname: "lh3.googleusercontent.com" }],
  },
};

export default nextConfig;

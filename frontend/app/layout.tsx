import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import RootProviders from "@/components/providers/RootProviders";
import { Toaster } from "@/components/ui/sonner";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "BudWiser",
  description: "Bud-Wiser: an AI financial agent that tracks spending, budgets and goals.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className="dark"
      style={{
        colorScheme: "dark",
      }}
    >
      <body className={inter.className}>
        <Toaster richColors position="bottom-right" />
        <RootProviders>{children}</RootProviders>
      </body>
    </html>
  );
}

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  PiggyBank,
  TrendingUp,
  Shield,
  Zap,
  BarChart3,
  Wallet,
} from "lucide-react";
import Link from "next/link";

const features = [
  {
    icon: Wallet,
    title: "Track Expenses",
    description:
      "Monitor every transaction and categorize your spending with ease.",
  },
  {
    icon: BarChart3,
    title: "Visual Analytics",
    description:
      "Understand your finances with beautiful charts and insights.",
  },
  {
    icon: TrendingUp,
    title: "Set Goals",
    description:
      "Create budgets and track your progress towards financial goals.",
  },
  {
    icon: Shield,
    title: "Secure & Private",
    description:
      "Your data is encrypted and protected with industry-standard security.",
  },
  {
    icon: Zap,
    title: "Fast & Intuitive",
    description:
      "A clean, modern interface designed for speed and ease of use.",
  },
  {
    icon: PiggyBank,
    title: "Smart Budgeting",
    description:
      "Intelligent suggestions to help you save more and spend wisely.",
  },
];

export default function AboutPage() {
  return (
    <div className="container py-12 md:py-20">
      {/* Hero Section */}
      <div className="mx-auto max-w-3xl text-center">
        <div className="mb-6 inline-flex items-center gap-2 rounded-full bg-amber-500/10 px-4 py-2">
          <PiggyBank className="h-5 w-5 text-amber-500" />
          <span className="text-sm font-medium text-amber-500">
            About BudWiser
          </span>
        </div>
        <h1 className="mb-6 text-4xl font-bold tracking-tight md:text-5xl">
          Take Control of Your{" "}
          <span className="bg-gradient-to-r from-amber-400 to-orange-500 bg-clip-text text-transparent">
            Financial Future
          </span>
        </h1>
        <p className="mb-8 text-lg text-muted-foreground">
          BudWiser is a modern budget tracking app designed to help you
          understand your spending habits, manage your money effectively, and
          achieve your financial goals.
        </p>
        <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
          <Button asChild size="lg" className="bg-amber-500 hover:bg-amber-600">
            <Link href="/sign-up">Get Started Free</Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link href="/">View Dashboard</Link>
          </Button>
        </div>
      </div>

      {/* Features Section */}
      <div className="mt-20">
        <h2 className="mb-12 text-center text-3xl font-bold">
          Everything You Need to Manage Your Money
        </h2>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {features.map((feature) => (
            <Card key={feature.title} className="border-dashed">
              <CardContent className="p-6">
                <div className="mb-4 inline-flex rounded-lg bg-amber-500/10 p-3">
                  <feature.icon className="h-6 w-6 text-amber-500" />
                </div>
                <h3 className="mb-2 text-lg font-semibold">{feature.title}</h3>
                <p className="text-sm text-muted-foreground">
                  {feature.description}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Mission Section */}
      <div className="mt-20">
        <Card className="bg-gradient-to-br from-amber-500/10 to-orange-500/10">
          <CardContent className="p-8 md:p-12">
            <div className="mx-auto max-w-2xl text-center">
              <h2 className="mb-4 text-2xl font-bold md:text-3xl">
                Our Mission
              </h2>
              <p className="text-muted-foreground">
                We believe everyone deserves financial clarity. Our mission is
                to provide simple, powerful tools that help people understand
                where their money goes, make informed decisions, and build a
                secure financial future. No complexity, no jargon — just smart
                budgeting made easy.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* CTA Section */}
      <div className="mt-20 text-center">
        <h2 className="mb-4 text-2xl font-bold">Ready to Start?</h2>
        <p className="mb-8 text-muted-foreground">
          Join thousands of users who are already managing their finances smarter.
        </p>
        <Button asChild size="lg" className="bg-amber-500 hover:bg-amber-600">
          <Link href="/sign-up">Create Free Account</Link>
        </Button>
      </div>
    </div>
  );
}

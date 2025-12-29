"use client";

import { Badge } from "@/components/ui/badge";
import { Users, IndianRupee, Clock, Star } from "lucide-react";
import { useEffect, useRef, useState } from "react";

const stats = [
  {
    icon: Users,
    value: 10000,
    suffix: "+",
    label: "Active Users",
    description: "Trust BudWiser for their finances",
  },
  {
    icon: IndianRupee,
    value: 50,
    suffix: "L+",
    label: "Tracked",
    description: "In transactions managed",
  },
  {
    icon: Clock,
    value: 99.9,
    suffix: "%",
    label: "Uptime",
    description: "Reliable and always available",
  },
  {
    icon: Star,
    value: 4.9,
    suffix: "",
    label: "Rating",
    description: "From satisfied users",
  },
];

function AnimatedNumber({ value, suffix }: { value: number; suffix: string }) {
  const [count, setCount] = useState(0);
  const [isVisible, setIsVisible] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
        }
      },
      { threshold: 0.1 }
    );

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!isVisible) return;

    const duration = 2000;
    const steps = 60;
    const increment = value / steps;
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if (current >= value) {
        setCount(value);
        clearInterval(timer);
      } else {
        setCount(current);
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [isVisible, value]);

  const displayValue = value >= 1000
    ? `${Math.floor(count / 1000)}K`
    : value % 1 !== 0
    ? count.toFixed(1)
    : Math.floor(count).toString();

  return (
    <div ref={ref} className="text-4xl md:text-5xl font-bold tracking-tight gradient-text">
      {displayValue}{suffix}
    </div>
  );
}

export function StatsSection() {
  return (
    <section className="relative py-20 md:py-28 overflow-hidden">
      {/* Background */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-gradient-to-b from-background via-muted/50 to-background" />
        <div className="absolute inset-0 bg-grid opacity-30" />
        <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 h-[600px] w-[600px] rounded-full bg-amber-500/10 blur-3xl" />
      </div>

      <div className="container px-4 md:px-6">
        {/* Header */}
        <div className="mx-auto mb-16 max-w-3xl text-center">
          <Badge
            variant="outline"
            className="mb-4 border-amber-500/30 bg-amber-500/5"
          >
            Trusted Platform
          </Badge>
          <h2 className="mb-4 text-3xl font-bold tracking-tight sm:text-4xl md:text-5xl">
            Trusted by thousands of{" "}
            <span className="gradient-text">smart budgeters</span>
          </h2>
          <p className="text-lg text-muted-foreground md:text-xl">
            Join a growing community of people who have taken control of their
            finances.
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat, index) => (
            <div
              key={stat.label}
              className="group relative overflow-hidden rounded-2xl border border-border/50 bg-gradient-to-br from-background to-muted/30 p-6 backdrop-blur-sm transition-all duration-300 hover:border-amber-500/30 hover:shadow-lg hover:-translate-y-1"
              style={{ animationDelay: `${index * 100}ms` }}
            >
              {/* Glow on hover */}
              <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 bg-gradient-to-br from-amber-500/5 to-orange-500/5" />

              <div className="relative text-center">
                {/* Icon */}
                <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-amber-500/10 text-amber-500 transition-all duration-300 group-hover:bg-amber-500 group-hover:text-white group-hover:scale-110">
                  <stat.icon className="h-7 w-7" />
                </div>

                {/* Animated Value */}
                <AnimatedNumber value={stat.value} suffix={stat.suffix} />

                {/* Label */}
                <div className="mt-2 font-semibold text-foreground">
                  {stat.label}
                </div>
                <p className="mt-1 text-sm text-muted-foreground">
                  {stat.description}
                </p>
              </div>

              {/* Corner decoration */}
              <div className="absolute -right-6 -top-6 h-20 w-20 rounded-full bg-gradient-to-br from-amber-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

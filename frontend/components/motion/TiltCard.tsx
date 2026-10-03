"use client";

import { cn } from "@/lib/utils";
import { motion, useMotionTemplate, useMotionValue, useReducedMotion, useSpring, useTransform } from "framer-motion";
import type { ReactNode } from "react";

interface Props {
  children: ReactNode;
  className?: string;
  /** Maximum tilt in degrees. */
  maxTilt?: number;
  /** Adds a moving light reflection that follows the pointer. */
  glare?: boolean;
}

/** 3D tilt that follows the pointer (desktop); flat and static on touch and with reduced motion. */
export function TiltCard({ children, className, maxTilt = 10, glare = true }: Props) {
  const reduceMotion = useReducedMotion();
  const px = useMotionValue(0.5);
  const py = useMotionValue(0.5);
  const springConfig = { stiffness: 150, damping: 18, mass: 0.6 };
  const rotateX = useSpring(useTransform(py, [0, 1], [maxTilt, -maxTilt]), springConfig);
  const rotateY = useSpring(useTransform(px, [0, 1], [-maxTilt, maxTilt]), springConfig);
  const glareX = useTransform(px, (v) => `${v * 100}%`);
  const glareY = useTransform(py, (v) => `${v * 100}%`);
  const glareBackground = useMotionTemplate`radial-gradient(500px circle at ${glareX} ${glareY}, rgba(255,255,255,0.12), transparent 45%)`;

  if (reduceMotion) {
    return <div className={className}>{children}</div>;
  }

  return (
    <div style={{ perspective: 1200 }}>
      <motion.div
        className={cn("relative [transform-style:preserve-3d]", className)}
        style={{ rotateX, rotateY }}
        onPointerMove={(event) => {
          if (event.pointerType !== "mouse") return;
          const rect = event.currentTarget.getBoundingClientRect();
          px.set((event.clientX - rect.left) / rect.width);
          py.set((event.clientY - rect.top) / rect.height);
        }}
        onPointerLeave={() => {
          px.set(0.5);
          py.set(0.5);
        }}
      >
        {children}
        {glare && (
          <motion.div
            aria-hidden
            className="pointer-events-none absolute inset-0 rounded-[inherit]"
            style={{ background: glareBackground }}
          />
        )}
      </motion.div>
    </div>
  );
}

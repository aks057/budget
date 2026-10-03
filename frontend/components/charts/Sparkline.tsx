"use client";

import { useId } from "react";
import { Area, AreaChart, ResponsiveContainer, YAxis } from "recharts";

interface Props {
  values: number[];
  /** Hex colour (SVG attributes cannot resolve CSS variables). */
  color: string;
  height?: number;
}

/** Minimal trend line with a fading fill; draws itself on mount. */
export function Sparkline({ values, color, height = 44 }: Props) {
  const gradientId = useId().replace(/:/g, "");
  const data = values.map((value, index) => ({ index, value }));
  if (data.length < 2) return <div style={{ height }} />;

  return (
    <div style={{ height }} aria-hidden>
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={data} margin={{ top: 4, right: 0, bottom: 0, left: 0 }}>
          <defs>
            <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity={0.35} />
              <stop offset="100%" stopColor={color} stopOpacity={0} />
            </linearGradient>
          </defs>
          <YAxis hide domain={["dataMin", "dataMax"]} />
          <Area
            type="monotone"
            dataKey="value"
            stroke={color}
            strokeWidth={2}
            fill={`url(#${gradientId})`}
            animationDuration={1200}
            animationEasing="ease-out"
            dot={false}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

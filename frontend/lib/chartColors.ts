/**
 * Chart palette shared by every chart: cool, brand-adjacent hues only (no orange/yellow), ordered so neighbouring
 * slices stay distinguishable. Hex values (not CSS variables) because SVG presentation attributes such as
 * stopColor/stroke/fill do not resolve var(); they match the --income / --expense tokens in globals.css.
 */
export const CATEGORY_COLORS = [
  "#34d399", // emerald-400
  "#22d3ee", // cyan-400
  "#818cf8", // indigo-400
  "#a78bfa", // violet-400
  "#38bdf8", // sky-400
  "#2dd4bf", // teal-400
  "#f472b6", // pink-400
  "#a3e635", // lime-400
] as const;

export const INCOME_COLOR = "#34d399"; // --income (dark)
export const EXPENSE_COLOR = "#fb7185"; // --expense (dark)

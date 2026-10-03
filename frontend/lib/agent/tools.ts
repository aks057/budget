/** UI copy for the agent's tools (names match the backend ToolRegistry). */

const STEP_LABELS: Record<string, string> = {
  get_monthly_summary: "Reviewing your monthly summary",
  get_category_spending: "Breaking down spending by category",
  compare_spending: "Comparing with last month",
  get_spending_trends: "Looking at your spending trends",
  detect_anomalies: "Scanning for unusual spending",
  get_recurring_expenses: "Finding recurring expenses",
  search_transactions: "Searching your transactions",
  list_categories: "Checking your categories",
  get_budgets: "Checking your budgets",
  get_goals: "Checking your goals",
  add_transaction: "Preparing a transaction",
  create_budget: "Preparing a budget",
  update_budget: "Preparing a budget change",
  create_goal: "Preparing a goal",
};

const ACTION_TITLES: Record<string, string> = {
  add_transaction: "Add transaction",
  create_budget: "Create budget",
  update_budget: "Update budget",
  create_goal: "Create goal",
};

export const stepLabel = (toolName: string): string => STEP_LABELS[toolName] ?? `Running ${toolName.replace(/_/g, " ")}`;

export const actionTitle = (toolName: string): string => ACTION_TITLES[toolName] ?? "Confirm action";

export const SUGGESTED_PROMPTS = [
  "How much did I spend this month?",
  "Where am I spending more than last month?",
  "Any unusual transactions this month?",
  "Am I on track for my goals?",
  "Set a 5000 budget for Food",
  "I spent 450 on lunch today",
];

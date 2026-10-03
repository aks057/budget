// Mirrors of the Spring Boot DTOs (ids are strings, money is a JSON number, dates are ISO strings).

export type ApiTransactionType = "INCOME" | "EXPENSE";

export interface ApiErrorDetail {
  type?: string;
  code?: string;
  message?: string;
  errorInfo?: unknown;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiEnvelope<T> {
  timestamp: number;
  status: "SUCCESS" | "ERROR";
  message?: string;
  data?: T;
  errors?: ApiErrorDetail[];
  page?: PageMeta;
}

export interface UserDto {
  id: string;
  email: string;
  fullName: string;
  avatarUrl: string | null;
  currency: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserDto;
}

export interface CategoryDto {
  id: string;
  name: string;
  icon: string;
  type: ApiTransactionType;
}

export interface TransactionDto {
  id: string;
  amount: number;
  type: ApiTransactionType;
  description: string;
  transactionDate: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
}

export interface OverviewDto {
  from: string;
  to: string;
  income: number;
  expense: number;
  balance: number;
}

export interface CategorySpendingDto {
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  total: number;
  percentOfTotal: number;
}

export interface MonthlyTrendDto {
  month: string; // yyyy-MM
  income: number;
  expense: number;
  savings: number;
  savingsRate: number | null;
}

export interface DailyCashflowDto {
  date: string; // yyyy-MM-dd
  income: number;
  expense: number;
  net: number;
  runningBalance: number;
}

export interface MonthForecastDto {
  spentSoFar: number;
  dailyRunRate: number;
  projectedTotal: number;
  daysElapsed: number;
  daysInMonth: number;
}

export interface MonthlySummaryDto {
  month: string; // yyyy-MM
  income: number;
  expense: number;
  savings: number;
  savingsRate: number | null;
  previousMonthExpense: number;
  expenseChangePercent: number | null;
  forecast: MonthForecastDto | null;
}

export type AnomalyType = "SPENDING_SPIKE" | "BUDGET_THRESHOLD" | "LARGE_TRANSACTION";
export type AnomalySeverity = "INFO" | "WARNING" | "CRITICAL";

export interface AnomalyDto {
  type: AnomalyType;
  severity: AnomalySeverity;
  categoryId: string | null;
  categoryName: string | null;
  transactionId: string | null;
  actual: number;
  baseline: number;
  deviationPercent: number | null;
}

// ---------- Budgets & goals ----------

export type BudgetLevel = "ON_TRACK" | "WARNING" | "EXCEEDED";

export interface BudgetDto {
  id: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  amount: number;
  month: string; // yyyy-MM
  spent: number;
  remaining: number; // negative when overspent
  percentUsed: number;
  level: BudgetLevel;
}

export interface GoalDto {
  id: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: string; // yyyy-MM-dd
  remaining: number;
  percentComplete: number;
  monthsRemaining: number;
  requiredMonthlyContribution: number;
  achieved: boolean;
  overdue: boolean;
}

// ---------- Agent ----------

export type ToolStatus = "SUCCESS" | "REJECTED" | "ERROR" | "PENDING_CONFIRMATION";
export type ActionStatus =
  | "SUCCESS"
  | "REJECTED"
  | "ERROR"
  | "PENDING_CONFIRMATION"
  | "EXECUTING"
  | "EXECUTED"
  | "FAILED"
  | "CANCELLED";

export interface ToolCallDto {
  name: string;
  status: ToolStatus;
  latencyMs: number;
}

export interface PendingActionDto {
  actionId: string;
  toolName: string;
  summary: string;
}

export interface AgentChatResponse {
  conversationId: string;
  reply: string;
  toolCalls: ToolCallDto[];
  pendingActions: PendingActionDto[];
}

export interface ConversationDto {
  id: string;
  title: string;
  lastMessageAt: number; // epoch millis
}

export interface MessageDto {
  id: string;
  role: "USER" | "ASSISTANT";
  content: string;
  createdAt: number; // epoch millis
}

export interface ActionResultDto {
  actionId: string;
  status: ActionStatus;
  message: string;
  data: unknown;
}

import { apiFetch, apiRequest } from "@/lib/api/client";
import type {
  AnomalyDto,
  ApiTransactionType,
  AuthResponse,
  BudgetDto,
  CategoryDto,
  GoalDto,
  MonthlySummaryDto,
  CategorySpendingDto,
  DailyCashflowDto,
  MonthlyTrendDto,
  OverviewDto,
  PageMeta,
  TransactionDto,
  UserDto,
} from "@/lib/api/types";
import type { TransactionType } from "@/lib/types";
import { format } from "date-fns";

// The UI keeps its lower-case "income"/"expense" type; the API speaks INCOME/EXPENSE. Conversion lives only here.
const toApiType = (type: TransactionType): ApiTransactionType => (type === "income" ? "INCOME" : "EXPENSE");
const fromApiType = (type: ApiTransactionType): TransactionType => (type === "INCOME" ? "income" : "expense");

/** Every date sent to the API is a plain calendar date (the backend owns the business timezone). */
export const toApiDate = (date: Date): string => format(date, "yyyy-MM-dd");

// ---------- UI-facing shapes ----------

export interface Category {
  id: string;
  name: string;
  icon: string;
  type: TransactionType;
}

export interface Transaction {
  id: string;
  amount: number;
  type: TransactionType;
  description: string;
  date: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
}

export interface CategoryStat {
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  type: TransactionType;
  total: number;
  percentOfTotal: number;
}

/** History chart point. `month` is 0-based to match JS Date and the existing `Period` type. */
export interface HistoryPoint {
  year: number;
  month: number;
  day?: number;
  income: number;
  expense: number;
}

const toCategory = (dto: CategoryDto): Category => ({ ...dto, type: fromApiType(dto.type) });

const toTransaction = (dto: TransactionDto): Transaction => ({
  id: dto.id,
  amount: dto.amount,
  type: fromApiType(dto.type),
  description: dto.description,
  date: dto.transactionDate,
  categoryId: dto.categoryId,
  categoryName: dto.categoryName,
  categoryIcon: dto.categoryIcon,
});

// ---------- Auth & profile ----------

export const login = (email: string, password: string) =>
  apiFetch<AuthResponse>("/api/v1/auth/login", { method: "POST", json: { email, password } });

export const register = (fullName: string, email: string, password: string) =>
  apiFetch<AuthResponse>("/api/v1/auth/register", { method: "POST", json: { fullName, email, password } });

export const logout = () => apiFetch<void>("/api/v1/auth/logout", { method: "POST" });

export const getMe = () => apiFetch<UserDto>("/api/v1/users/me");

export const updateMe = (request: { fullName: string; currency: string }) =>
  apiFetch<UserDto>("/api/v1/users/me", { method: "PUT", json: request });

// ---------- Categories ----------

export const getCategories = async (type?: TransactionType): Promise<Category[]> =>
  (await apiFetch<CategoryDto[]>("/api/v1/categories", { query: { type: type && toApiType(type) } })).map(toCategory);

export const createCategory = async (request: { name: string; icon: string; type: TransactionType }) =>
  toCategory(
    await apiFetch<CategoryDto>("/api/v1/categories", {
      method: "POST",
      json: { ...request, type: toApiType(request.type) },
    }),
  );

export const deleteCategory = (id: string) => apiFetch<void>(`/api/v1/categories/${id}`, { method: "DELETE" });

// ---------- Transactions ----------

export const createTransaction = async (request: {
  amount: number;
  categoryId: string;
  description?: string;
  date: Date;
}) =>
  toTransaction(
    await apiFetch<TransactionDto>("/api/v1/transactions", {
      method: "POST",
      json: {
        amount: request.amount,
        categoryId: request.categoryId,
        description: request.description,
        transactionDate: toApiDate(request.date),
      },
    }),
  );

export const getTransactions = async (params: {
  from: Date;
  to: Date;
  page: number;
  size: number;
  type?: TransactionType;
  categoryId?: string;
}): Promise<{ items: Transaction[]; page: PageMeta }> => {
  const envelope = await apiRequest<TransactionDto[]>("/api/v1/transactions", {
    query: {
      from: toApiDate(params.from),
      to: toApiDate(params.to),
      page: params.page,
      size: params.size,
      type: params.type && toApiType(params.type),
      categoryId: params.categoryId,
    },
  });
  return {
    items: (envelope.data ?? []).map(toTransaction),
    page: envelope.page ?? { page: 0, size: params.size, totalElements: 0, totalPages: 0 },
  };
};

export const deleteTransaction = (id: string) => apiFetch<void>(`/api/v1/transactions/${id}`, { method: "DELETE" });

// ---------- Analytics ----------

export const getOverview = (from: Date, to: Date) =>
  apiFetch<OverviewDto>("/api/v1/analytics/overview", { query: { from: toApiDate(from), to: toApiDate(to) } });

export const getCategoryStats = async (from: Date, to: Date, type: TransactionType): Promise<CategoryStat[]> =>
  (
    await apiFetch<CategorySpendingDto[]>("/api/v1/analytics/categories", {
      query: { from: toApiDate(from), to: toApiDate(to), type: toApiType(type) },
    })
  ).map((dto) => ({ ...dto, type }));

export const getPeriods = () => apiFetch<number[]>("/api/v1/analytics/periods");

export const getYearlyHistory = async (year: number): Promise<HistoryPoint[]> =>
  (await apiFetch<MonthlyTrendDto[]>("/api/v1/analytics/yearly", { query: { year } })).map((row) => ({
    year,
    month: Number(row.month.slice(5, 7)) - 1,
    income: row.income,
    expense: row.expense,
  }));

/** `month` is 0-based (UI convention); converted to yyyy-MM for the API. */
export const getMonthlyHistory = async (year: number, month: number): Promise<HistoryPoint[]> =>
  (
    await apiFetch<DailyCashflowDto[]>("/api/v1/analytics/cashflow", {
      query: { month: `${year}-${String(month + 1).padStart(2, "0")}` },
    })
  ).map((row) => ({
    year,
    month,
    day: Number(row.date.slice(8, 10)),
    income: row.income,
    expense: row.expense,
  }));

export const getTrends = (months: number) =>
  apiFetch<MonthlyTrendDto[]>("/api/v1/analytics/trends", { query: { months } });

/** yyyy-MM; omit for the current month (in the backend's business timezone). */
export const getMonthlySummary = (month?: string) =>
  apiFetch<MonthlySummaryDto>("/api/v1/analytics/summary", { query: { month } });

export const getAnomalies = (month?: string) =>
  apiFetch<AnomalyDto[]>("/api/v1/analytics/anomalies", { query: { month } });

// ---------- Budgets ----------

/** Budgets with their status for `month` (yyyy-MM, default current month). */
export const getBudgets = (month?: string) => apiFetch<BudgetDto[]>("/api/v1/budgets", { query: { month } });

export const createBudget = (request: { categoryId: string; amount: number }) =>
  apiFetch<BudgetDto>("/api/v1/budgets", { method: "POST", json: request });

export const updateBudget = (id: string, amount: number) =>
  apiFetch<BudgetDto>(`/api/v1/budgets/${id}`, { method: "PUT", json: { amount } });

export const deleteBudget = (id: string) => apiFetch<void>(`/api/v1/budgets/${id}`, { method: "DELETE" });

// ---------- Goals ----------

export interface GoalInput {
  name: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: Date;
}

const toGoalRequest = (input: GoalInput) => ({ ...input, targetDate: toApiDate(input.targetDate) });

export const getGoals = () => apiFetch<GoalDto[]>("/api/v1/goals");

export const createGoal = (input: GoalInput) =>
  apiFetch<GoalDto>("/api/v1/goals", { method: "POST", json: toGoalRequest(input) });

export const updateGoal = (id: string, input: GoalInput) =>
  apiFetch<GoalDto>(`/api/v1/goals/${id}`, { method: "PUT", json: toGoalRequest(input) });

export const deleteGoal = (id: string) => apiFetch<void>(`/api/v1/goals/${id}`, { method: "DELETE" });

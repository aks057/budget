export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[];

export interface Database {
  public: {
    Tables: {
      user_settings: {
        Row: {
          user_id: string;
          currency: string;
          created_at: string;
          updated_at: string;
        };
        Insert: {
          user_id: string;
          currency?: string;
          created_at?: string;
          updated_at?: string;
        };
        Update: {
          user_id?: string;
          currency?: string;
          created_at?: string;
          updated_at?: string;
        };
        Relationships: [];
      };
      categories: {
        Row: {
          id: string;
          created_at: string;
          name: string;
          user_id: string;
          icon: string;
          type: "income" | "expense";
        };
        Insert: {
          id?: string;
          created_at?: string;
          name: string;
          user_id: string;
          icon: string;
          type?: "income" | "expense";
        };
        Update: {
          id?: string;
          created_at?: string;
          name?: string;
          user_id?: string;
          icon?: string;
          type?: "income" | "expense";
        };
        Relationships: [];
      };
      transactions: {
        Row: {
          id: string;
          created_at: string;
          updated_at: string;
          amount: number;
          description: string;
          date: string;
          user_id: string;
          type: "income" | "expense";
          category: string;
          category_icon: string;
        };
        Insert: {
          id?: string;
          created_at?: string;
          updated_at?: string;
          amount: number;
          description?: string;
          date: string;
          user_id: string;
          type?: "income" | "expense";
          category: string;
          category_icon: string;
        };
        Update: {
          id?: string;
          created_at?: string;
          updated_at?: string;
          amount?: number;
          description?: string;
          date?: string;
          user_id?: string;
          type?: "income" | "expense";
          category?: string;
          category_icon?: string;
        };
        Relationships: [];
      };
      month_history: {
        Row: {
          user_id: string;
          day: number;
          month: number;
          year: number;
          income: number;
          expense: number;
        };
        Insert: {
          user_id: string;
          day: number;
          month: number;
          year: number;
          income?: number;
          expense?: number;
        };
        Update: {
          user_id?: string;
          day?: number;
          month?: number;
          year?: number;
          income?: number;
          expense?: number;
        };
        Relationships: [];
      };
      year_history: {
        Row: {
          user_id: string;
          month: number;
          year: number;
          income: number;
          expense: number;
        };
        Insert: {
          user_id: string;
          month: number;
          year: number;
          income?: number;
          expense?: number;
        };
        Update: {
          user_id?: string;
          month?: number;
          year?: number;
          income?: number;
          expense?: number;
        };
        Relationships: [];
      };
    };
    Views: Record<string, never>;
    Functions: {
      create_transaction_with_history: {
        Args: {
          p_user_id: string;
          p_amount: number;
          p_description: string;
          p_date: string;
          p_type: string;
          p_category: string;
          p_category_icon: string;
        };
        Returns: string;
      };
      delete_transaction_with_history: {
        Args: {
          p_transaction_id: string;
          p_user_id: string;
        };
        Returns: boolean;
      };
    };
    Enums: Record<string, never>;
    CompositeTypes: Record<string, never>;
  };
}

// Helper types for easier use
export type UserSettings = Database["public"]["Tables"]["user_settings"]["Row"];
export type Category = Database["public"]["Tables"]["categories"]["Row"];
export type Transaction = Database["public"]["Tables"]["transactions"]["Row"];
export type MonthHistory = Database["public"]["Tables"]["month_history"]["Row"];
export type YearHistory = Database["public"]["Tables"]["year_history"]["Row"];

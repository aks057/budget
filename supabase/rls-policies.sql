-- ===========================================
-- Row Level Security (RLS) Policies
-- Run this in your Supabase SQL Editor
-- ===========================================

-- Step 1: Enable RLS on all tables
ALTER TABLE user_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE month_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE year_history ENABLE ROW LEVEL SECURITY;

-- ===========================================
-- user_settings policies
-- ===========================================
CREATE POLICY "Users can view own settings"
ON user_settings FOR SELECT
USING (auth.uid() = user_id::uuid);

CREATE POLICY "Users can insert own settings"
ON user_settings FOR INSERT
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can update own settings"
ON user_settings FOR UPDATE
USING (auth.uid() = user_id::uuid)
WITH CHECK (auth.uid() = user_id::uuid);

-- ===========================================
-- categories policies
-- ===========================================
CREATE POLICY "Users can view own categories"
ON categories FOR SELECT
USING (auth.uid() = user_id::uuid);

CREATE POLICY "Users can insert own categories"
ON categories FOR INSERT
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can update own categories"
ON categories FOR UPDATE
USING (auth.uid() = user_id::uuid)
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can delete own categories"
ON categories FOR DELETE
USING (auth.uid() = user_id::uuid);

-- ===========================================
-- transactions policies
-- ===========================================
CREATE POLICY "Users can view own transactions"
ON transactions FOR SELECT
USING (auth.uid() = user_id::uuid);

CREATE POLICY "Users can insert own transactions"
ON transactions FOR INSERT
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can update own transactions"
ON transactions FOR UPDATE
USING (auth.uid() = user_id::uuid)
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can delete own transactions"
ON transactions FOR DELETE
USING (auth.uid() = user_id::uuid);

-- ===========================================
-- month_history policies
-- ===========================================
CREATE POLICY "Users can view own month history"
ON month_history FOR SELECT
USING (auth.uid() = user_id::uuid);

CREATE POLICY "Users can insert own month history"
ON month_history FOR INSERT
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can update own month history"
ON month_history FOR UPDATE
USING (auth.uid() = user_id::uuid)
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can delete own month history"
ON month_history FOR DELETE
USING (auth.uid() = user_id::uuid);

-- ===========================================
-- year_history policies
-- ===========================================
CREATE POLICY "Users can view own year history"
ON year_history FOR SELECT
USING (auth.uid() = user_id::uuid);

CREATE POLICY "Users can insert own year history"
ON year_history FOR INSERT
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can update own year history"
ON year_history FOR UPDATE
USING (auth.uid() = user_id::uuid)
WITH CHECK (auth.uid() = user_id::uuid);

CREATE POLICY "Users can delete own year history"
ON year_history FOR DELETE
USING (auth.uid() = user_id::uuid);

-- ===========================================
-- Update RPC functions to use SECURITY INVOKER
-- This ensures they respect RLS policies
-- ===========================================
-- Note: If your functions were created with SECURITY DEFINER,
-- you may need to recreate them with SECURITY INVOKER:
--
-- ALTER FUNCTION create_transaction_with_history SECURITY INVOKER;
-- ALTER FUNCTION delete_transaction_with_history SECURITY INVOKER;

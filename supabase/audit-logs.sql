-- ===========================================
-- Audit Logging Setup
-- Run this in your Supabase SQL Editor
-- ===========================================

-- Step 1: Create Audit Log Table
CREATE TABLE audit_logs (
  id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id uuid,
  action text NOT NULL,
  table_name text NOT NULL,
  record_id text,
  old_data jsonb,
  new_data jsonb,
  ip_address text,
  user_agent text,
  created_at timestamptz DEFAULT now()
);

-- Index for faster queries
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_table_name ON audit_logs(table_name);

-- RLS: Only admins can view audit logs (users cannot see)
-- No policies = no user access (only service role can read/write)
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- ===========================================
-- Step 2: Create Trigger Function
-- ===========================================

CREATE OR REPLACE FUNCTION log_audit_event()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, new_data)
    VALUES (auth.uid(), 'INSERT', TG_TABLE_NAME, NEW.id::text, to_jsonb(NEW));
    RETURN NEW;
  ELSIF TG_OP = 'UPDATE' THEN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, old_data, new_data)
    VALUES (auth.uid(), 'UPDATE', TG_TABLE_NAME, NEW.id::text, to_jsonb(OLD), to_jsonb(NEW));
    RETURN NEW;
  ELSIF TG_OP = 'DELETE' THEN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, old_data)
    VALUES (auth.uid(), 'DELETE', TG_TABLE_NAME, OLD.id::text, to_jsonb(OLD));
    RETURN OLD;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ===========================================
-- Step 3: Attach Triggers to Tables
-- ===========================================

-- Transactions audit
CREATE TRIGGER audit_transactions
AFTER INSERT OR UPDATE OR DELETE ON transactions
FOR EACH ROW EXECUTE FUNCTION log_audit_event();

-- Categories audit
CREATE TRIGGER audit_categories
AFTER INSERT OR UPDATE OR DELETE ON categories
FOR EACH ROW EXECUTE FUNCTION log_audit_event();

-- User settings audit
CREATE TRIGGER audit_user_settings
AFTER INSERT OR UPDATE OR DELETE ON user_settings
FOR EACH ROW EXECUTE FUNCTION log_audit_event();

-- ===========================================
-- Optional: Add triggers for history tables
-- ===========================================

-- Month history audit
CREATE TRIGGER audit_month_history
AFTER INSERT OR UPDATE OR DELETE ON month_history
FOR EACH ROW EXECUTE FUNCTION log_audit_event();

-- Year history audit
CREATE TRIGGER audit_year_history
AFTER INSERT OR UPDATE OR DELETE ON year_history
FOR EACH ROW EXECUTE FUNCTION log_audit_event();

-- ===========================================
-- Useful Queries for Viewing Audit Logs
-- ===========================================

-- View recent activity
-- SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 50;

-- View activity for specific user
-- SELECT * FROM audit_logs WHERE user_id = 'user-uuid-here' ORDER BY created_at DESC;

-- View all transaction changes
-- SELECT * FROM audit_logs WHERE table_name = 'transactions' ORDER BY created_at DESC;

-- View deletions only
-- SELECT * FROM audit_logs WHERE action = 'DELETE' ORDER BY created_at DESC;

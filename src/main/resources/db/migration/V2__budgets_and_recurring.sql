CREATE TABLE monthly_budgets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  budget_month VARCHAR(7) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  warned_80 BOOLEAN NOT NULL DEFAULT FALSE,
  warned_100 BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uk_monthly_budgets_month UNIQUE (budget_month)
);

CREATE TABLE category_budgets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  budget_month VARCHAR(7) NOT NULL,
  category VARCHAR(64) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  warned_80 BOOLEAN NOT NULL DEFAULT FALSE,
  warned_100 BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uk_category_budgets_month_category UNIQUE (budget_month, category)
);

CREATE TABLE recurring_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  amount DECIMAL(12,2) NOT NULL,
  category VARCHAR(64) NOT NULL,
  note VARCHAR(255) NULL,
  schedule_type VARCHAR(16) NOT NULL,
  day_of_month INT NULL,
  next_run_date DATE NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE recurring_occurrences (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_id BIGINT NOT NULL,
  occurrence_date DATE NOT NULL,
  expense_id BIGINT NOT NULL,
  CONSTRAINT uk_recurring_occurrence UNIQUE (template_id, occurrence_date)
);

CREATE INDEX idx_recurring_templates_next_run ON recurring_templates(next_run_date);

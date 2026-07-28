CREATE TABLE expenses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  amount DECIMAL(12,2) NOT NULL,
  expense_date DATE NOT NULL,
  category VARCHAR(64) NOT NULL,
  note VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE deleted_expenses (
  id BIGINT PRIMARY KEY,
  amount DECIMAL(12,2) NOT NULL,
  expense_date DATE NOT NULL,
  category VARCHAR(64) NOT NULL,
  note VARCHAR(255) NULL,
  deleted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_deleted_expenses_deleted_at ON deleted_expenses(deleted_at);

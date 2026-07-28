CREATE TABLE expenses (
  id BIGINT NOT NULL AUTO_INCREMENT,
  amount DECIMAL(12,2) NOT NULL,
  expense_date DATE NOT NULL,
  category VARCHAR(64) NOT NULL,
  note VARCHAR(255) NULL,
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_expenses_date (expense_date),
  INDEX idx_expenses_category (category),
  INDEX idx_expenses_deleted_at (deleted_at)
);

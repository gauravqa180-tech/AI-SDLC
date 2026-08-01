CREATE TABLE expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(13,2) NOT NULL,
    expense_date DATE NOT NULL,
    category VARCHAR(100) NOT NULL,
    note VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_expenses_date (expense_date),
    INDEX idx_expenses_category (category)
);

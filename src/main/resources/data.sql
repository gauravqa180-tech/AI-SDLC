INSERT INTO categories (id, name, normalized_name, active, created_at, updated_at)
VALUES
  (1, 'Food', 'food', true, NOW(), NOW()),
  (2, 'Transport', 'transport', true, NOW(), NOW()),
  (3, 'Utilities', 'utilities', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

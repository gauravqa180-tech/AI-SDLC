# Expense Tracker v1 – Enhancements

This README documents how to set up and use the **Expense Tracker v1 (enhanced)** application.

## Prerequisites

- **Java 17**
- **Maven 3.9+**
- **MySQL 8+**
- (Recommended) `curl` for API testing

Verify:

```bash
java -version
mvn -version
mysql --version
```

## Database Setup (MySQL)

1. Create a database:

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Configure application database settings.

The default `application.yml` typically uses these credentials (change as needed):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password
```

> If your project uses Flyway/Liquibase, ensure migrations are enabled and run automatically on startup.

## Build & Run

### Build

```bash
mvn clean package
```

### Run (Maven)

```bash
mvn spring-boot:run
```

### Run (JAR)

```bash
java -jar target/*.jar
```

The API will typically be available at:

- `http://localhost:8080`

## API Usage

### Conventions

- `Content-Type: application/json` for JSON requests
- Examples below assume base URL:

```bash
BASE_URL=http://localhost:8080
```

---

## Categories API

### List Categories

```bash
curl -sS "$BASE_URL/api/categories"
```

### Create Category

```bash
curl -sS -X POST "$BASE_URL/api/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food"
  }'
```

### Update Category

```bash
curl -sS -X PUT "$BASE_URL/api/categories/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Groceries"
  }'
```

### Delete Category

```bash
curl -sS -X DELETE "$BASE_URL/api/categories/1"
```

### Merge Categories

```bash
curl -sS -X POST "$BASE_URL/api/categories/merge" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCategoryId": 1,
    "targetCategoryId": 2
  }'
```

---

## Expenses API

### List Expenses (filters via query params)

```bash
curl -sS "$BASE_URL/api/expenses?from=2026-07-01&to=2026-07-31&categoryId=1"
```

### Create Expense

Request fields: `amount`, `date`, `categoryId`, `note`

```bash
curl -sS -X POST "$BASE_URL/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 12.50,
    "date": "2026-07-28",
    "categoryId": 1,
    "note": "Lunch"
  }'
```

### Update Expense

```bash
curl -sS -X PUT "$BASE_URL/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15.00,
    "date": "2026-07-28",
    "categoryId": 1,
    "note": "Lunch + drink"
  }'
```

### Delete Expense

```bash
curl -sS -X DELETE "$BASE_URL/api/expenses/1"
```

### Undo Expense Change

```bash
curl -sS -X POST "$BASE_URL/api/expenses/1/undo"
```

---

## Reports

### Monthly Total

```bash
curl -sS "$BASE_URL/api/expenses/monthly-total?year=2026&month=7"
```

### Monthly Insights by Category

```bash
curl -sS "$BASE_URL/api/insights/monthly-by-category?year=2026&month=7"
```

---

## Export CSV

```bash
curl -sS -L "$BASE_URL/api/expenses/export?from=2026-07-01&to=2026-07-31" \
  -H "Accept: text/csv" \
  -o expenses.csv
```

---

## Budgets

### Create/Set Budget

```bash
curl -sS -X POST "$BASE_URL/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "year": 2026,
    "month": 7,
    "limit": 300.00
  }'
```

### List Budgets (by month)

```bash
curl -sS "$BASE_URL/api/budgets?year=2026&month=7"
```

### Delete Budget

```bash
curl -sS -X DELETE "$BASE_URL/api/budgets/1"
```

### Budget Status

```bash
curl -sS "$BASE_URL/api/budgets/status?year=2026&month=7"
```

---

## Notes / Troubleshooting

- If the app fails to start due to DB connectivity, confirm:
  - MySQL is running and reachable
  - credentials match your configuration
  - DB schema exists (`expense_tracker`)

---

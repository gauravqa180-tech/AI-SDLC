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

1. Create a database and user (adjust names/passwords as desired):

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'expense_user'@'%' IDENTIFIED BY 'expense_pass';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'%';
FLUSH PRIVILEGES;
```

2. Configure application database settings.

Update your `application.properties` / `application.yml` to point to MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=UTC
spring.datasource.username=expense_user
spring.datasource.password=expense_pass

# Typical Hibernate settings (adjust to your project conventions)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
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
- IDs are typically returned by create endpoints
- Examples below assume base URL:

```bash
BASE_URL=http://localhost:8080
```

---

## Categories API

### Create Category

```bash
curl -sS -X POST "$BASE_URL/api/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food"
  }'
```

### List Categories

```bash
curl -sS "$BASE_URL/api/categories"
```

### Get Category by ID

```bash
curl -sS "$BASE_URL/api/categories/1"
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

---

## Expenses API (CRUD)

### Create Expense

```bash
curl -sS -X POST "$BASE_URL/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 12.50,
    "date": "2026-07-28",
    "description": "Lunch",
    "categoryId": 1
  }'
```

### List Expenses

```bash
curl -sS "$BASE_URL/api/expenses"
```

### Get Expense by ID

```bash
curl -sS "$BASE_URL/api/expenses/1"
```

### Update Expense

```bash
curl -sS -X PUT "$BASE_URL/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15.00,
    "date": "2026-07-28",
    "description": "Lunch + drink",
    "categoryId": 1
  }'
```

### Delete Expense

```bash
curl -sS -X DELETE "$BASE_URL/api/expenses/1"
```

---

## Expense Filters

> Common filters include date range, category, min/max amount, and text search. Adjust parameter names to your API implementation.

### Filter by Date Range

```bash
curl -sS "$BASE_URL/api/expenses?from=2026-07-01&to=2026-07-31"
```

### Filter by Category

```bash
curl -sS "$BASE_URL/api/expenses?categoryId=1"
```

### Filter by Amount Range

```bash
curl -sS "$BASE_URL/api/expenses?minAmount=10&maxAmount=100"
```

### Filter by Description Search

```bash
curl -sS "$BASE_URL/api/expenses?query=lunch"
```

---

## Undo Last Operation

> If supported, this endpoint reverts the most recent state-changing operation.

```bash
curl -sS -X POST "$BASE_URL/api/undo"
```

---

## Monthly Total

> Returns total spent for a given month (e.g., `YYYY-MM`).

```bash
curl -sS "$BASE_URL/api/reports/monthly-total?month=2026-07"
```

---

## Insights

> Provides analytics such as top categories, spending trends, averages, etc.

```bash
curl -sS "$BASE_URL/api/insights?from=2026-07-01&to=2026-07-31"
```

---

## Export CSV

> Exports expenses to CSV. The response is typically `text/csv`.

```bash
curl -sS -L "$BASE_URL/api/expenses/export.csv?from=2026-07-01&to=2026-07-31" \
  -H "Accept: text/csv" \
  -o expenses.csv
```

---

## Budgets

> Budget endpoints commonly support setting a budget for a category and/or a month.

### Create/Set Budget

```bash
curl -sS -X POST "$BASE_URL/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "month": "2026-07",
    "limit": 300.00
  }'
```

### List Budgets

```bash
curl -sS "$BASE_URL/api/budgets"
```

### Get Budget by ID

```bash
curl -sS "$BASE_URL/api/budgets/1"
```

### Update Budget

```bash
curl -sS -X PUT "$BASE_URL/api/budgets/1" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "month": "2026-07",
    "limit": 350.00
  }'
```

### Delete Budget

```bash
curl -sS -X DELETE "$BASE_URL/api/budgets/1"
```

### Budget Status (Optional)

> If available, returns current spend vs limit for a month/category.

```bash
curl -sS "$BASE_URL/api/budgets/status?month=2026-07&categoryId=1"
```

---

## Notes / Troubleshooting

- If the app fails to start due to DB connectivity, confirm:
  - MySQL is running and reachable
  - credentials match your configuration
  - DB schema exists (`expense_tracker`)
- If using `ddl-auto=update`, tables will be created/updated on startup. In production, prefer migrations.

---

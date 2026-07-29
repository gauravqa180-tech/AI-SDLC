# Expense Tracker (Spring Boot)

Simple Expense Tracker REST API with budgets, reports, CSV export, and Swagger documentation.

## Prerequisites
- Java 17
- Maven 3.8+
- MySQL 8+

## Database setup
Create a database and a user (adjust names/passwords as needed):

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'expense_pass';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
```

Configure your app (e.g., `src/main/resources/application.yml` or `application.properties`) with:
- URL: `jdbc:mysql://localhost:3306/expense_tracker`
- Username: `expense_user`
- Password: `expense_pass`

## Run the service
```bash
mvn clean spring-boot:run
```

Default base URL:
- `http://localhost:8080`

## Swagger / OpenAPI
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API usage (curl)

> Replace URLs/fields to match your actual DTOs if they differ.

### Create expense
```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "date":"2026-07-29",
    "amount":24.50,
    "currency":"USD",
    "category":"FOOD",
    "merchant":"Cafe",
    "description":"Lunch"
  }'
```

### List expenses (filters + sort + search)
Example: filter by date range + category, search text, sort newest first, paginate.
```bash
curl "http://localhost:8080/api/expenses?from=2026-07-01&to=2026-07-31&category=FOOD&search=lunch&sort=date,desc&page=0&size=20"
```

### Update expense
```bash
curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "date":"2026-07-29",
    "amount":26.00,
    "currency":"USD",
    "category":"FOOD",
    "merchant":"Cafe",
    "description":"Lunch + drink"
  }'
```

### Delete expense
```bash
curl -X DELETE "http://localhost:8080/api/expenses/1"
```

### Monthly total
```bash
curl "http://localhost:8080/api/analytics/monthly-total?year=2026&month=7"
```

### Report (summary by category, etc.)
```bash
curl "http://localhost:8080/api/reports?from=2026-07-01&to=2026-07-31&groupBy=category"
```

### Export CSV
```bash
curl -L "http://localhost:8080/api/exports/expenses.csv?from=2026-07-01&to=2026-07-31" \
  -o expenses-2026-07.csv
```

### Create budget
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "month":"2026-07",
    "category":"FOOD",
    "limit":300.00,
    "currency":"USD"
  }'
```

### Budget progress
```bash
curl "http://localhost:8080/api/budgets/progress?month=2026-07"
```

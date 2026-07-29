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

Configure your app datasource in `src/main/resources/application.yml` (or change the example below to match your current defaults):
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

### Create expense
`ExpenseCreateRequest` fields: `amount`, `date`, `category`, `note`
```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 24.50,
    "date": "2026-07-29",
    "category": "FOOD",
    "note": "Lunch"
  }'
```

### List expenses (filters + sort + search)
Query params: `from`, `to`, `category`, `q`, `page`, `size`, `sortBy`, `direction`
```bash
curl "http://localhost:8080/api/expenses?from=2026-07-01&to=2026-07-31&category=FOOD&q=lunch&sortBy=date&direction=desc&page=0&size=20"
```

### Update expense
```bash
curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 26.00,
    "date": "2026-07-29",
    "category": "FOOD",
    "note": "Lunch + drink"
  }'
```

### Delete expense
```bash
curl -X DELETE "http://localhost:8080/api/expenses/1"
```

### Monthly total
```bash
curl "http://localhost:8080/api/expenses/monthly-total?month=2026-07"
```

### Report
```bash
curl "http://localhost:8080/api/expenses/report?from=2026-07-01&to=2026-07-31"
```

### Export CSV
```bash
curl -L "http://localhost:8080/api/expenses/export?from=2026-07-01&to=2026-07-31" \
  -o expenses-2026-07.csv
```

### Upsert budget
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "month": "2026-07",
    "category": "FOOD",
    "amount": 300.00
  }'
```

### Budget progress
`category` is optional.
```bash
curl "http://localhost:8080/api/budgets/progress?month=2026-07&category=FOOD"
```

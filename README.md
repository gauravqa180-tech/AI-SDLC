# Expense Tracker v1

Spring Boot REST API for tracking expenses with CRUD operations, filtering, sorting, and monthly totals.

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8+

## Setup

1. Create a MySQL database (example):
   - `expense_tracker`

2. Configure DB connection in `src/main/resources/application.properties` (or `application.yml`):
   - `spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker`
   - `spring.datasource.username=YOUR_USER`
   - `spring.datasource.password=YOUR_PASS`

3. Build:
   - `mvn clean package`

4. Run:
   - `mvn spring-boot:run`
   - or `java -jar target/*.jar`

API base URL (default): `http://localhost:8080`

## API

### Expense Model (typical JSON)
```json
{
  "title": "Groceries",
  "amount": 42.50,
  "category": "FOOD",
  "date": "2026-07-28",
  "note": "Weekly shopping"
}
```

### Create expense
- `POST /api/v1/expenses`
```bash
curl -X POST "http://localhost:8080/api/v1/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Groceries",
    "amount":42.50,
    "category":"FOOD",
    "date":"2026-07-28",
    "note":"Weekly shopping"
  }'
```

### List expenses (filters + sort)
- `GET /api/v1/expenses`

Query params (optional):
- `category` (e.g., `FOOD`, `TRAVEL`, etc.)
- `minAmount` (number)
- `maxAmount` (number)
- `startDate` (ISO date `YYYY-MM-DD`)
- `endDate` (ISO date `YYYY-MM-DD`)
- `q` (free-text search; name may vary by implementation)
- `sort` (see below)
- `page`, `size` (if pagination is enabled)

Sort param values:
- `date,asc` / `date,desc`
- `amount,asc` / `amount,desc`
- `createdAt,asc` / `createdAt,desc` (if supported)
- `id,asc` / `id,desc`

Example:
```bash
curl "http://localhost:8080/api/v1/expenses?category=FOOD&minAmount=10&maxAmount=100&startDate=2026-07-01&endDate=2026-07-31&sort=date,desc"
```

### Get expense by id
- `GET /api/v1/expenses/{id}`
```bash
curl "http://localhost:8080/api/v1/expenses/1"
```

### Update expense
- `PUT /api/v1/expenses/{id}`
```bash
curl -X PUT "http://localhost:8080/api/v1/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Groceries (updated)",
    "amount":45.00,
    "category":"FOOD",
    "date":"2026-07-28",
    "note":"Added snacks"
  }'
```

### Delete expense
- `DELETE /api/v1/expenses/{id}`
```bash
curl -X DELETE "http://localhost:8080/api/v1/expenses/1"
```

### Monthly total
- `GET /api/v1/expenses/summary/monthly`

Query params:
- `year` (e.g., `2026`)
- `month` (1-12)

Example:
```bash
curl "http://localhost:8080/api/v1/expenses/summary/monthly?year=2026&month=7"
```
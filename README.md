# AI-SDLC

Spring Boot backend for personal finance: expenses, budgets, recurring rules, insights, and export.

## Tech stack

- Java 17
- Spring Boot (Maven)
- MySQL

## Prerequisites

- Java 17 installed (`java -version`)
- Maven installed (`mvn -v`)
- MySQL 8+ running and accessible

## Database setup (MySQL)

Create a database and user (adjust as desired):

```sql
CREATE DATABASE ai_sdlc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'ai_sdlc_user'@'%' IDENTIFIED BY 'ai_sdlc_password';
GRANT ALL PRIVILEGES ON ai_sdlc.* TO 'ai_sdlc_user'@'%';
FLUSH PRIVILEGES;
```

## Configure application

Set DB connection in `src/main/resources/application.properties` (or `application.yml`) as applicable:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_sdlc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=ai_sdlc_user
spring.datasource.password=ai_sdlc_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

server.port=8080
```

If the project uses environment variables, you can also export:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/ai_sdlc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="ai_sdlc_user"
export SPRING_DATASOURCE_PASSWORD="ai_sdlc_password"
```

## Run locally

```bash
mvn clean spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package
java -jar target/*.jar
```

App should start on:

- http://localhost:8080

## API overview

Base URL: `http://localhost:8080`

Typical content type:
- `Content-Type: application/json`

> Note: Exact DTO shapes and query parameter names may vary slightly depending on implementation. The examples below show common, expected usage patterns for the implemented endpoints.

---

## Expenses endpoints

### Create expense
`POST /api/expenses`

```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 42.50,
    "currency": "USD",
    "category": "GROCERIES",
    "description": "Weekly groceries",
    "date": "2026-07-01"
  }'
```

### List expenses (optionally filter by date range/category)
`GET /api/expenses?from=YYYY-MM-DD&to=YYYY-MM-DD&category=...`

```bash
curl "http://localhost:8080/api/expenses?from=2026-07-01&to=2026-07-31"
```

### Get expense by id
`GET /api/expenses/{id}`

```bash
curl "http://localhost:8080/api/expenses/1"
```

### Update expense
`PUT /api/expenses/{id}`

```bash
curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 45.00,
    "currency": "USD",
    "category": "GROCERIES",
    "description": "Groceries (updated)",
    "date": "2026-07-01"
  }'
```

### Delete expense
`DELETE /api/expenses/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/expenses/1"
```

---

## Budgets endpoints

### Create budget (typically per category and time period)
`POST /api/budgets`

```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "GROCERIES",
    "limitAmount": 300.00,
    "currency": "USD",
    "period": "MONTHLY",
    "startDate": "2026-07-01"
  }'
```

### List budgets
`GET /api/budgets`

```bash
curl "http://localhost:8080/api/budgets"
```

### Get budget by id
`GET /api/budgets/{id}`

```bash
curl "http://localhost:8080/api/budgets/1"
```

### Update budget
`PUT /api/budgets/{id}`

```bash
curl -X PUT "http://localhost:8080/api/budgets/1" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "GROCERIES",
    "limitAmount": 350.00,
    "currency": "USD",
    "period": "MONTHLY",
    "startDate": "2026-07-01"
  }'
```

### Delete budget
`DELETE /api/budgets/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/budgets/1"
```

---

## Recurring rules endpoints

Recurring rules define automatically generated expenses (e.g., rent monthly).

### Create recurring rule
`POST /api/recurring-rules`

```bash
curl -X POST "http://localhost:8080/api/recurring-rules" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rent",
    "amount": 1200.00,
    "currency": "USD",
    "category": "HOUSING",
    "description": "Monthly rent",
    "frequency": "MONTHLY",
    "startDate": "2026-07-01",
    "dayOfMonth": 1
  }'
```

### List recurring rules
`GET /api/recurring-rules`

```bash
curl "http://localhost:8080/api/recurring-rules"
```

### Get recurring rule by id
`GET /api/recurring-rules/{id}`

```bash
curl "http://localhost:8080/api/recurring-rules/1"
```

### Update recurring rule
`PUT /api/recurring-rules/{id}`

```bash
curl -X PUT "http://localhost:8080/api/recurring-rules/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rent",
    "amount": 1250.00,
    "currency": "USD",
    "category": "HOUSING",
    "description": "Monthly rent (updated)",
    "frequency": "MONTHLY",
    "startDate": "2026-07-01",
    "dayOfMonth": 1
  }'
```

### Delete recurring rule
`DELETE /api/recurring-rules/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/recurring-rules/1"
```

### Trigger generation/run (if exposed)
Some deployments expose an endpoint to run rule processing manually.

`POST /api/recurring-rules/run` (or similar)

```bash
curl -X POST "http://localhost:8080/api/recurring-rules/run"
```

---

## Insights endpoints

Insights provide aggregated views like totals per period/category, budget vs actual, trends.

### Summary insights (date range)
`GET /api/insights/summary?from=YYYY-MM-DD&to=YYYY-MM-DD`

```bash
curl "http://localhost:8080/api/insights/summary?from=2026-07-01&to=2026-07-31"
```

### Category breakdown
`GET /api/insights/categories?from=YYYY-MM-DD&to=YYYY-MM-DD`

```bash
curl "http://localhost:8080/api/insights/categories?from=2026-07-01&to=2026-07-31"
```

### Budget vs actual
`GET /api/insights/budgets?from=YYYY-MM-DD&to=YYYY-MM-DD`

```bash
curl "http://localhost:8080/api/insights/budgets?from=2026-07-01&to=2026-07-31"
```

---

## Export endpoints

Export allows downloading expenses/insights in a file format (commonly CSV).

### Export expenses (CSV)
`GET /api/export/expenses?from=YYYY-MM-DD&to=YYYY-MM-DD&format=csv`

```bash
curl -L "http://localhost:8080/api/export/expenses?from=2026-07-01&to=2026-07-31&format=csv" \
  -o expenses-july-2026.csv
```

### Export insights/summary (CSV/JSON depending on implementation)
`GET /api/export/insights?from=YYYY-MM-DD&to=YYYY-MM-DD&format=csv`

```bash
curl -L "http://localhost:8080/api/export/insights?from=2026-07-01&to=2026-07-31&format=csv" \
  -o insights-july-2026.csv
```

---

## Troubleshooting

- **Cannot connect to MySQL**: verify host/port, credentials, and that MySQL is accepting TCP connections.
- **Timezone/SSL warnings**: keep `serverTimezone=UTC` and `useSSL=false` (or configure proper SSL in production).
- **Schema issues**: if using `spring.jpa.hibernate.ddl-auto=update`, the schema will be created/updated automatically on startup; for production prefer migrations (Flyway/Liquibase) if available.
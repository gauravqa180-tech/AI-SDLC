# AI-SDLC — Expense Tracker API (Spring Boot)

REST API for tracking expenses and managing monthly budgets.

## Tech Stack

- Java 17
- Maven
- Spring Boot
- MySQL

---

## Prerequisites

- Java 17 installed (`java -version`)
- Maven installed (`mvn -version`)
- MySQL 8.x running
- (Optional) curl/Postman for API calls

---

## MySQL Setup

Create a database and user (example):

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'expense_user'@'%' IDENTIFIED BY 'expense_pass';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'%';
FLUSH PRIVILEGES;
```

---

## Configuration (application.yml)

Configure these keys in `src/main/resources/application.yml` (names may vary slightly depending on the project structure, but the standard Spring keys apply):

```yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: expense_user
    password: expense_pass
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

# If present in the project:
# app:
#   timezone: UTC
#   export:
#     csv:
#       delimiter: ","
```

Common `ddl-auto` values:
- `update` (dev-friendly)
- `validate` / `none` (recommended for production with migrations)

---

## Build & Run

From the repository root:

```bash
mvn clean test
mvn spring-boot:run
```

Or build a jar:

```bash
mvn clean package
java -jar target/*.jar
```

API will be available at:

- `http://localhost:8080`

---

## API Endpoints

> Base URL: `/api`
>
> Note: Exact request/response fields depend on the DTOs in this project; the examples below show typical payload shapes.

### Expenses

#### Create expense
- `POST /api/expenses`

Example:

```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "date":"2026-07-28",
    "amount":25.50,
    "category":"FOOD",
    "description":"Lunch"
  }'
```

#### Get expense by id
- `GET /api/expenses/{id}`

```bash
curl "http://localhost:8080/api/expenses/1"
```

#### List expenses (paged/filterable if supported)
- `GET /api/expenses`

Common optional query params (if implemented):
- `page`, `size`, `sort`
- `from`, `to`
- `category`
- `q` (free-text)

```bash
curl "http://localhost:8080/api/expenses?page=0&size=20&sort=date,desc"
```

#### Update expense
- `PUT /api/expenses/{id}`

```bash
curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "date":"2026-07-28",
    "amount":30.00,
    "category":"FOOD",
    "description":"Lunch (updated)"
  }'
```

#### Delete expense (soft delete if supported)
- `DELETE /api/expenses/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/expenses/1"
```

#### Restore deleted expense
- `POST /api/expenses/{id}/restore`

```bash
curl -X POST "http://localhost:8080/api/expenses/1/restore"
```

#### Search expenses
- `GET /api/expenses/search`

Typical query params (if implemented):
- `q` (search text)
- `from`, `to`
- `minAmount`, `maxAmount`
- `category`
- `includeDeleted` (true/false)

```bash
curl "http://localhost:8080/api/expenses/search?q=lunch&from=2026-07-01&to=2026-07-31"
```

#### Monthly total
- `GET /api/expenses/monthly-total`

Typical query params:
- `year`
- `month` (1-12)

```bash
curl "http://localhost:8080/api/expenses/monthly-total?year=2026&month=7"
```

#### Monthly report
- `GET /api/expenses/report`

Typical query params:
- `year`
- `month`

```bash
curl "http://localhost:8080/api/expenses/report?year=2026&month=7"
```

#### Export CSV
- `GET /api/expenses/export/csv`

Typical query params:
- `from`, `to`
- `year`, `month`
- `category`
- `includeDeleted`

```bash
curl -L "http://localhost:8080/api/expenses/export/csv?year=2026&month=7" \
  -o expenses-2026-07.csv
```

---

### Budgets

Budgets are typically defined per month (and optionally per category), then used to calculate progress vs. actual expenses.

#### Upsert budget (create or update)
- `PUT /api/budgets`

Example:

```bash
curl -X PUT "http://localhost:8080/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2026,
    "month": 7,
    "amount": 600.00,
    "category": null
  }'
```

If category budgets are supported:

```bash
curl -X PUT "http://localhost:8080/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2026,
    "month": 7,
    "amount": 250.00,
    "category": "FOOD"
  }'
```

#### Budget progress (spent vs budget)
- `GET /api/budgets/progress`

Typical query params:
- `year`
- `month`
- `category` (optional)

```bash
curl "http://localhost:8080/api/budgets/progress?year=2026&month=7"
```

```bash
curl "http://localhost:8080/api/budgets/progress?year=2026&month=7&category=FOOD"
```

#### Delete budget
- `DELETE /api/budgets`

Typical query params:
- `year`
- `month`
- `category` (optional)

```bash
curl -X DELETE "http://localhost:8080/api/budgets?year=2026&month=7"
```

```bash
curl -X DELETE "http://localhost:8080/api/budgets?year=2026&month=7&category=FOOD"
```

---

## Notes

- Make sure MySQL is reachable and the `spring.datasource.*` properties are correct.
- If the application uses Flyway/Liquibase, keep `ddl-auto` set to `validate` or `none`.
- If security is enabled in this project, add required auth headers/tokens to the curl examples.
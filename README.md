# Spring Boot Expense Tracker v1

Expense Tracker v1 is a Spring Boot service for managing expenses and budgets, reporting monthly totals and category breakdowns, and exporting expenses to CSV. The service exposes a REST API and includes interactive Swagger/OpenAPI documentation.

---

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.x
- (Optional) cURL for API testing

---

## Setup

### 1) Create MySQL database

Log into MySQL and create a database:

CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

Create a user (optional but recommended):

CREATE USER 'expense_user'@'%' IDENTIFIED BY 'expense_password';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'%';
FLUSH PRIVILEGES;

### 2) Configure application properties

Set your DB connection in `src/main/resources/application.yml` or `application.properties`.

Example (application.yml):

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: expense_user
    password: expense_password
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080

If this project uses Flyway/Liquibase, keep `ddl-auto` disabled and ensure migrations are configured.

---

## Build & Run

### Run locally with Maven

mvn clean spring-boot:run

### Build a jar

mvn clean package
java -jar target/*.jar

Service runs at:

http://localhost:8080

---

## Swagger / OpenAPI

Swagger UI:

http://localhost:8080/swagger-ui/index.html

OpenAPI JSON:

http://localhost:8080/v3/api-docs

---

## API Overview (v1)

Base URL: http://localhost:8080/api/v1

Common conventions:

- Dates use ISO-8601 format: `YYYY-MM-DD`
- Money values are typically decimal numbers (e.g., `12.50`)
- Category is a string (e.g., `FOOD`, `TRANSPORT`, `UTILITIES`)
- Most listing endpoints support query parameters for filtering, sorting, and searching

### Query Parameters (Filters / Sort / Search)

These are commonly supported on list endpoints (exact support may vary by endpoint):

- Filters:
  - `from` (YYYY-MM-DD): start date inclusive
  - `to` (YYYY-MM-DD): end date inclusive
  - `category`: filter by category
  - `minAmount`: minimum amount
  - `maxAmount`: maximum amount
- Search:
  - `q`: free text search (e.g., by description/merchant/notes)
- Pagination:
  - `page` (0-based), `size`
- Sorting:
  - `sort`: e.g. `date,desc` or `amount,asc` (Spring-style), sometimes repeatable

Example:

GET /expenses?from=2026-07-01&to=2026-07-31&category=FOOD&q=coffee&page=0&size=20&sort=date,desc

---

## Sample cURL Commands

Set a base URL for convenience:

BASE_URL=http://localhost:8080/api/v1

### Expenses

#### Create an expense

curl -X POST "$BASE_URL/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-07-28",
    "amount": 12.50,
    "currency": "USD",
    "category": "FOOD",
    "description": "Lunch",
    "paymentMethod": "CARD"
  }'

#### List expenses (with filters/search/sort)

curl -X GET "$BASE_URL/expenses?from=2026-07-01&to=2026-07-31&category=FOOD&q=lunch&page=0&size=25&sort=date,desc"

#### Update an expense

Replace `{expenseId}` with the actual id:

curl -X PUT "$BASE_URL/expenses/{expenseId}" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-07-28",
    "amount": 14.00,
    "currency": "USD",
    "category": "FOOD",
    "description": "Lunch (updated)",
    "paymentMethod": "CARD"
  }'

If your API uses PATCH for partial updates:

curl -X PATCH "$BASE_URL/expenses/{expenseId}" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 14.00,
    "description": "Lunch (updated)"
  }'

#### Delete an expense

curl -X DELETE "$BASE_URL/expenses/{expenseId}"

#### Monthly total

Example: total for July 2026:

curl -X GET "$BASE_URL/reports/monthly-total?year=2026&month=7"

Some deployments may also support date-based month selection:

curl -X GET "$BASE_URL/reports/monthly-total?month=2026-07"

#### Category breakdown

Example: breakdown for a month, optionally filtered:

curl -X GET "$BASE_URL/reports/category-breakdown?year=2026&month=7"

With filters (if supported):

curl -X GET "$BASE_URL/reports/category-breakdown?from=2026-07-01&to=2026-07-31&q=lunch&sort=total,desc"

#### Export expenses as CSV

Exports typically respect filters/search/sort the same as listing:

curl -X GET "$BASE_URL/expenses/export.csv?from=2026-07-01&to=2026-07-31&category=FOOD&q=lunch&sort=date,desc" \
  -H "Accept: text/csv" \
  -o expenses-july.csv

If the endpoint is a generic export route:

curl -X GET "$BASE_URL/expenses/export?format=csv&from=2026-07-01&to=2026-07-31" \
  -H "Accept: text/csv" \
  -o expenses.csv

---

## Budgets

### Create a budget

curl -X POST "$BASE_URL/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "July Food Budget",
    "category": "FOOD",
    "amount": 300.00,
    "currency": "USD",
    "periodStart": "2026-07-01",
    "periodEnd": "2026-07-31"
  }'

### List budgets

curl -X GET "$BASE_URL/budgets?page=0&size=20&sort=periodStart,desc"

With filters/search (if supported):

curl -X GET "$BASE_URL/budgets?category=FOOD&from=2026-07-01&to=2026-07-31&q=July&sort=amount,desc"

### Budget progress

Shows spent vs budget for a given budget id:

curl -X GET "$BASE_URL/budgets/{budgetId}/progress"

If progress supports calculating for a specific month/date range:

curl -X GET "$BASE_URL/budgets/{budgetId}/progress?from=2026-07-01&to=2026-07-31"

### Delete a budget

curl -X DELETE "$BASE_URL/budgets/{budgetId}"

---

## Notes

- Use Swagger UI to confirm exact request/response schemas and the available query parameters in your running version.
- If you change the server port or context path, update the URLs accordingly.
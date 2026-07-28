# Expense Tracker v1

A simple REST API to track expenses with CRUD endpoints and a monthly total summary.

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.x

## Setup

### 1) Create database

Create a MySQL database (and user if needed):

- Database: `expense_tracker` (or any name you configure)

### 2) Configure `application.yml`

Update your `src/main/resources/application.yml` with your MySQL connection and server settings.

Example:

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=UTC
    username: your_mysql_user
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080

Notes:
- `ddl-auto: update` is convenient for local dev. Consider `validate` or migrations (Flyway/Liquibase) for production.
- Ensure MySQL timezone settings match your environment.

### 3) Build and run

Build:
mvn clean package

Run:
mvn spring-boot:run

Or run the built jar:
java -jar target/*.jar

API base URL (default):
http://localhost:8080

## API Usage (Examples)

Assumptions:
- Endpoints below follow conventional REST naming: `/api/v1/expenses`
- Replace IDs, dates, and fields as appropriate for your model.

### Create expense

curl -X POST "http://localhost:8080/api/v1/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Groceries",
    "amount": 42.50,
    "category": "FOOD",
    "date": "2026-07-28",
    "notes": "Weekly shopping"
  }'

### List expenses

curl "http://localhost:8080/api/v1/expenses"

Optional common filters (if supported):
curl "http://localhost:8080/api/v1/expenses?from=2026-07-01&to=2026-07-31&category=FOOD"

### Get expense by id

curl "http://localhost:8080/api/v1/expenses/1"

### Update expense

curl -X PUT "http://localhost:8080/api/v1/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Groceries (updated)",
    "amount": 45.00,
    "category": "FOOD",
    "date": "2026-07-28",
    "notes": "Added household items"
  }'

Partial update (if supported):
curl -X PATCH "http://localhost:8080/api/v1/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 44.25
  }'

### Delete expense

curl -X DELETE "http://localhost:8080/api/v1/expenses/1"

### Monthly total

curl "http://localhost:8080/api/v1/expenses/monthly-total?year=2026&month=7"

If your API uses a path format instead:
curl "http://localhost:8080/api/v1/expenses/monthly-total/2026/7"

## Validation

Requests are validated (e.g., required fields, non-empty title, positive amount, valid date, etc.). If validation fails, the API responds with HTTP 400.

## Error Response Shape

Errors are returned as JSON with a consistent structure. Typical shapes:

Validation error (400):
{
  "timestamp": "2026-07-28T12:34:56Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/expenses",
  "details": [
    { "field": "amount", "message": "must be greater than 0" },
    { "field": "title", "message": "must not be blank" }
  ]
}

Not found (404):
{
  "timestamp": "2026-07-28T12:34:56Z",
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found",
  "path": "/api/v1/expenses/999"
}
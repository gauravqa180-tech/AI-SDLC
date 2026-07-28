# Expense Tracker API (Spring Boot)

A simple RESTful expense tracker implemented with Spring Boot. It supports managing expenses, listing with filters, and basic analytics (totals) suitable for personal budgeting or as a starter backend.

## Tech Stack

- Java 17+
- Spring Boot (Web, Validation)
- Spring Data JPA
- H2 (in-memory) by default (can be swapped for Postgres/MySQL)
- Maven

## Features

- CRUD for expenses
- Filter expenses by date range and/or category
- Compute totals over a date range and optionally by category
- Input validation
- Pageable listing (if enabled in the implementation)

---

## Getting Started

### Prerequisites

- Java 17 or newer
- Maven 3.8+

### Run Locally

#### 1) Build
mvn clean package

#### 2) Run
mvn spring-boot:run

Or run the built JAR:
java -jar target/*.jar

### Default App URL

- API base: http://localhost:8080

### H2 Console (if enabled)

- http://localhost:8080/h2-console  
Use the JDBC URL and credentials from `application.properties` (commonly `jdbc:h2:mem:testdb` with user `sa` and empty password, depending on your config).

---

## API Endpoints

Base path: `/api`

### Health

- `GET /actuator/health` (if Spring Boot Actuator is included/enabled)

### Expenses

#### Create an expense
- `POST /api/expenses`
- Request body (example):
  {
    "title": "Lunch",
    "amount": 12.50,
    "category": "FOOD",
    "date": "2026-07-28",
    "notes": "Salad and drink"
  }

- Response: `201 Created` with the created expense.

#### Get an expense by id
- `GET /api/expenses/{id}`
- Response: `200 OK` or `404 Not Found`

#### List expenses (with optional filters)
- `GET /api/expenses`
- Optional query params:
  - `from` (ISO date, e.g. `2026-07-01`)
  - `to` (ISO date, e.g. `2026-07-31`)
  - `category` (e.g. `FOOD`)
  - `minAmount`
  - `maxAmount`
  - `page`, `size`, `sort` (if pagination is supported by your controller/service)

Examples:
- `GET /api/expenses`
- `GET /api/expenses?from=2026-07-01&to=2026-07-31`
- `GET /api/expenses?category=FOOD`
- `GET /api/expenses?from=2026-07-01&to=2026-07-31&category=FOOD`

#### Update an expense
- `PUT /api/expenses/{id}`
- Request body (example):
  {
    "title": "Lunch (updated)",
    "amount": 13.00,
    "category": "FOOD",
    "date": "2026-07-28",
    "notes": "Added dessert"
  }
- Response: `200 OK` or `404 Not Found`

#### Delete an expense
- `DELETE /api/expenses/{id}`
- Response: `204 No Content` or `404 Not Found`

---

## Analytics / Summary

#### Get total spend (optionally filtered)
- `GET /api/expenses/total`
- Optional query params:
  - `from`
  - `to`
  - `category`

Examples:
- `GET /api/expenses/total`
- `GET /api/expenses/total?from=2026-07-01&to=2026-07-31`
- `GET /api/expenses/total?from=2026-07-01&to=2026-07-31&category=FOOD`

Response (example):
{
  "total": 123.45,
  "currency": "USD"
}

(Actual response fields may vary depending on implementation.)

---

## Data Model

Typical expense fields:

- `id` (Long)
- `title` (String)
- `amount` (BigDecimal)
- `category` (String or enum)
- `date` (LocalDate)
- `notes` (String, optional)
- `createdAt` / `updatedAt` (optional, if auditing enabled)

---

## Validation & Error Handling

Common validations:
- `title` required
- `amount` must be positive
- `date` required and must be valid ISO date

Errors are returned as structured JSON (format depends on the implementation), typically with fields like:
- `timestamp`
- `status`
- `error`
- `message`
- `path`

---

## Configuration

Edit `src/main/resources/application.properties` (or `.yml`) to:
- change server port
- switch DB from H2 to a persistent database
- enable/disable H2 console or Actuator endpoints

---

## Example cURL Commands

Create:
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Coffee","amount":3.75,"category":"FOOD","date":"2026-07-28","notes":"Latte"}'

List:
curl "http://localhost:8080/api/expenses?from=2026-07-01&to=2026-07-31"

Get by id:
curl http://localhost:8080/api/expenses/1

Update:
curl -X PUT http://localhost:8080/api/expenses/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Coffee (large)","amount":4.50,"category":"FOOD","date":"2026-07-28","notes":"Large latte"}'

Delete:
curl -X DELETE http://localhost:8080/api/expenses/1

Total:
curl "http://localhost:8080/api/expenses/total?from=2026-07-01&to=2026-07-31"

---

## License

MIT (or your preferred license).
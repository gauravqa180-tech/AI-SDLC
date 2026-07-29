# Expense Tracker v1 — Spring Boot Backend

Backend service for Expense Tracker v1. Provides REST APIs to manage users, categories, expenses, and reporting.

## Tech Stack

- Java 17+
- Spring Boot (Web, Validation)
- Spring Data JPA
- Database: H2 (dev) / PostgreSQL (prod-ready)
- Build: Maven

## Prerequisites

- Java 17 (or compatible)
- Maven 3.8+
- (Optional) Docker + Docker Compose for running a database

## Run Instructions

### 1) Configure Environment

The application can run with default settings (H2 in-memory) or with an external DB.

Common configuration options (via `application.properties` / `application.yml` or environment variables):

- `SPRING_PROFILES_ACTIVE` (e.g., `dev`, `prod`)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT` (default `8080`)

### 2) Run with Maven

```bash
mvn clean spring-boot:run
```

### 3) Run Packaged JAR

```bash
mvn clean package
java -jar target/*.jar
```

### 4) Verify Health

Once running, the API is available at:

- Base URL: `http://localhost:8080`

If Actuator is enabled in the project, you may also have:

- `GET /actuator/health`

## API Documentation

All endpoints are prefixed with:

- `/api/v1`

### Authentication / Users (v1)

> Note: If your implementation uses session/token-based auth, include the required headers accordingly. If auth is not implemented yet, these endpoints operate directly on the user resource.

#### Create User
- `POST /api/v1/users`
- Request body:
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com"
}
```
- Response: `201 Created` with created user.

#### Get User by ID
- `GET /api/v1/users/{userId}`
- Response: `200 OK`

#### List Users
- `GET /api/v1/users`
- Response: `200 OK`

#### Update User
- `PUT /api/v1/users/{userId}`
- Request body:
```json
{
  "name": "Jane D",
  "email": "jane.d@example.com"
}
```
- Response: `200 OK`

#### Delete User
- `DELETE /api/v1/users/{userId}`
- Response: `204 No Content`

---

## User Story Endpoints (Implemented)

### 1) Manage Categories

#### Create Category
- `POST /api/v1/users/{userId}/categories`
- Request body:
```json
{
  "name": "Groceries"
}
```
- Response: `201 Created`

#### List Categories
- `GET /api/v1/users/{userId}/categories`
- Response: `200 OK`

#### Get Category by ID
- `GET /api/v1/users/{userId}/categories/{categoryId}`
- Response: `200 OK`

#### Update Category
- `PUT /api/v1/users/{userId}/categories/{categoryId}`
- Request body:
```json
{
  "name": "Food"
}
```
- Response: `200 OK`

#### Delete Category
- `DELETE /api/v1/users/{userId}/categories/{categoryId}`
- Response: `204 No Content`

---

### 2) Manage Expenses

#### Create Expense
- `POST /api/v1/users/{userId}/expenses`
- Request body:
```json
{
  "amount": 25.75,
  "currency": "USD",
  "date": "2026-07-01",
  "description": "Weekly groceries",
  "categoryId": 1
}
```
- Response: `201 Created`

#### List Expenses (optionally filterable)
- `GET /api/v1/users/{userId}/expenses`
- Optional query params (if implemented):
  - `from` (e.g., `2026-07-01`)
  - `to` (e.g., `2026-07-31`)
  - `categoryId`
  - `minAmount`
  - `maxAmount`
- Response: `200 OK`

Example:
- `GET /api/v1/users/1/expenses?from=2026-07-01&to=2026-07-31&categoryId=2`

#### Get Expense by ID
- `GET /api/v1/users/{userId}/expenses/{expenseId}`
- Response: `200 OK`

#### Update Expense
- `PUT /api/v1/users/{userId}/expenses/{expenseId}`
- Request body:
```json
{
  "amount": 30.00,
  "currency": "USD",
  "date": "2026-07-02",
  "description": "Groceries (updated)",
  "categoryId": 1
}
```
- Response: `200 OK`

#### Delete Expense
- `DELETE /api/v1/users/{userId}/expenses/{expenseId}`
- Response: `204 No Content`

---

### 3) Reports / Summary

#### Get Monthly Summary
Returns totals for a given month, optionally grouped by category if implemented.

- `GET /api/v1/users/{userId}/reports/monthly`
- Query params:
  - `year` (e.g., `2026`)
  - `month` (1-12)
- Response: `200 OK`

Example:
- `GET /api/v1/users/1/reports/monthly?year=2026&month=7`

Example response:
```json
{
  "userId": 1,
  "year": 2026,
  "month": 7,
  "total": 523.10,
  "currency": "USD",
  "byCategory": [
    { "categoryId": 1, "categoryName": "Groceries", "total": 210.25 },
    { "categoryId": 2, "categoryName": "Transport", "total": 80.00 }
  ]
}
```

---

## Validation & Error Handling

Typical error responses:

- `400 Bad Request` for validation errors (missing/invalid fields)
- `404 Not Found` for missing resources (user/category/expense not found)
- `409 Conflict` for duplicates (e.g., category name already exists for user) if implemented

Example error payload shape (may vary by implementation):
```json
{
  "timestamp": "2026-07-29T12:34:56Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users/1/expenses"
}
```

## Local Development Notes

- If using H2:
  - Data resets on restart unless file-based configuration is used.
- For PostgreSQL:
  - Configure datasource properties and ensure DB is reachable.
- CORS:
  - Configure allowed origins if a frontend is consuming the API from a different domain.

## Versioning

- This README describes **Expense Tracker v1** backend APIs under `/api/v1`.

## License

Internal / unspecified.
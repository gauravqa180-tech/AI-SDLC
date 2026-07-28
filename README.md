# Expense Tracker v1 — Setup & API Usage (User Story 1: Edit Expense)

This repository contains an **Expense Tracker** API. User Story 1 focuses on **editing an expense** with **optimistic locking** to prevent lost updates.

---

## Requirements

- **Java 17+**
- **Maven 3.9+** (or Gradle if your project uses it; commands below assume Maven)
- (Optional) **Docker** if you run the database in a container
- A supported relational database (commonly **PostgreSQL**). Check `application.yml/properties` for the configured datasource.

---

## Running Locally

### 1) Configure environment

Update your application config (e.g., `src/main/resources/application.yml` or `.properties`) for your local database:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

If the project uses Flyway/Liquibase migrations, ensure the database exists and the user has privileges.

### 2) Start the application

Using Maven:

- Run tests:
  - `mvn test`

- Run the API:
  - `mvn spring-boot:run`

Or build and run the jar:

- `mvn clean package`
- `java -jar target/*.jar`

### 3) Verify it’s up

By default, the API will be on:

- `http://localhost:8080`

(If your project config changes the port, use that port.)

---

## API Overview

Base URL (local):

- `http://localhost:8080`

Core endpoints for expenses:

- `POST   /api/expenses`
- `GET    /api/expenses`
- `GET    /api/expenses/{id}`
- `PUT    /api/expenses/{id}` (Edit expense — user story 1)
- `DELETE /api/expenses/{id}`

> Notes:
> - Payload fields and exact response may vary slightly by implementation, but the examples below show the expected structure for **Expense Tracker v1**.
> - Editing uses **optimistic locking** via a `version` field.

---

## Data Model (Conceptual)

An `Expense` typically includes:

- `id` (server-generated)
- `description` (string, required)
- `amount` (number/decimal, required, must be > 0)
- `currency` (string, e.g., `USD`, optional depending on your implementation)
- `date` (ISO-8601 date, required or optional depending on your implementation)
- `category` (string, optional)
- `version` (integer/long, required for updates; used for optimistic locking)
- `createdAt`, `updatedAt` (timestamps; often server-managed)

---

## Validation & Error Format

### Validation errors (HTTP 400)

When request validation fails, the API returns `400 Bad Request` with a structured body.

Expected format:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "amount", "message": "must be greater than 0" },
    { "field": "description", "message": "must not be blank" }
  ]
}
```

### Not found (HTTP 404)

If an expense ID does not exist:

```json
{
  "code": "NOT_FOUND",
  "message": "Expense not found"
}
```

### Conflict / optimistic locking (HTTP 409)

If you try to update an expense using an **outdated `version`**, the API returns `409 Conflict`:

```json
{
  "code": "OPTIMISTIC_LOCK_CONFLICT",
  "message": "Expense was modified by another request. Please refresh and retry.",
  "currentVersion": 3
}
```

---

## Optimistic Locking (Version) — How Editing Works

To prevent overwriting another user’s changes:

1. **Client fetches the current expense** (includes `version`).
2. **Client edits** and sends an update with the same `version`.
3. Server updates only if the `version` matches the latest stored value.
4. Server increments `version` on successful update.
5. If the stored version is different, server returns **409 Conflict**.

This is the workflow for **User Story 1: Edit Expense**.

---

## Sample cURL Commands

Set a base URL:

- Linux/macOS:
  - `BASE_URL=http://localhost:8080`

- Windows PowerShell:
  - `$BASE_URL="http://localhost:8080"`

### 1) Create an expense

**Request:**

```bash
curl -sS -X POST "$BASE_URL/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Lunch",
    "amount": 12.50,
    "currency": "USD",
    "date": "2026-07-28",
    "category": "Food"
  }'
```

**Response (example):**

```json
{
  "id": "2f4b3b2a-5e5d-4d5c-9e77-0f62f6a5bb8a",
  "description": "Lunch",
  "amount": 12.5,
  "currency": "USD",
  "date": "2026-07-28",
  "category": "Food",
  "version": 0
}
```

### 2) List expenses

```bash
curl -sS "$BASE_URL/api/expenses"
```

Response example:

```json
[
  {
    "id": "2f4b3b2a-5e5d-4d5c-9e77-0f62f6a5bb8a",
    "description": "Lunch",
    "amount": 12.5,
    "currency": "USD",
    "date": "2026-07-28",
    "category": "Food",
    "version": 0
  }
]
```

### 3) Get expense by ID

```bash
EXPENSE_ID="2f4b3b2a-5e5d-4d5c-9e77-0f62f6a5bb8a"

curl -sS "$BASE_URL/api/expenses/$EXPENSE_ID"
```

### 4) Update (edit) an expense — with optimistic locking

Fetch the expense first (or use the latest response you have) to obtain the current `version`.

**Update request (example):**

```bash
EXPENSE_ID="2f4b3b2a-5e5d-4d5c-9e77-0f62f6a5bb8a"

curl -sS -X PUT "$BASE_URL/api/expenses/$EXPENSE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Lunch with colleagues",
    "amount": 14.00,
    "currency": "USD",
    "date": "2026-07-28",
    "category": "Food",
    "version": 0
  }'
```

**Successful response (example):** note `version` increments.

```json
{
  "id": "2f4b3b2a-5e5d-4d5c-9e77-0f62f6a5bb8a",
  "description": "Lunch with colleagues",
  "amount": 14.0,
  "currency": "USD",
  "date": "2026-07-28",
  "category": "Food",
  "version": 1
}
```

#### Conflict example (409)

If another update happened and the current server version is now `1`, but you send `version: 0`, you’ll get:

```bash
curl -sS -i -X PUT "$BASE_URL/api/expenses/$EXPENSE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Lunch (stale update)",
    "amount": 15.00,
    "currency": "USD",
    "date": "2026-07-28",
    "category": "Food",
    "version": 0
  }'
```

Response (example):

- Status: `HTTP/1.1 409 Conflict`

```json
{
  "code": "OPTIMISTIC_LOCK_CONFLICT",
  "message": "Expense was modified by another request. Please refresh and retry.",
  "currentVersion": 1
}
```

To resolve:
1) `GET /api/expenses/{id}` to obtain the latest version.
2) Re-apply changes and `PUT` again with the updated `version`.

### 5) Delete an expense

```bash
EXPENSE_ID="2f4b3b2a-5e5d-4d5c-9e77-0f62f6a5bb8a"

curl -sS -X DELETE "$BASE_URL/api/expenses/$EXPENSE_ID"
```

Common responses:
- `204 No Content` on success
- `404 Not Found` if the ID does not exist

---

## Tips for Local Development

- If you see database connection errors, verify:
  - DB is running
  - Credentials match config
  - DB schema/migrations executed
- If you get `409 Conflict` on update:
  - Refresh the expense, and retry with the latest `version`.
- If you get `400 Bad Request`:
  - Inspect `fieldErrors` to fix the payload.
# Expense Tracker API

REST API for tracking personal expenses. This project exposes versioned endpoints under `/api/v1` and provides interactive documentation via Swagger/OpenAPI.

---

## Requirements

- **Java**: 17+
- **Build Tool**: Maven 3.8+ (or Gradle if your project uses it)
- **Database**: PostgreSQL (recommended) or any configured datasource supported by the app
- **Git**
- **Optional**:
  - Docker + Docker Compose (for running database/services locally)
  - IDE (IntelliJ IDEA / VS Code)

---

## Run Locally

### 1) Clone repository
```bash
git clone <your-repo-url>
cd <your-repo-folder>
```

### 2) Configure environment
Set the application configuration (choose one approach):

#### Option A: `application.yml` / `application.properties`
Update datasource and server settings as needed.

Example (PostgreSQL):
- URL: `jdbc:postgresql://localhost:5432/expense_tracker`
- Username: `postgres`
- Password: `postgres`

#### Option B: Environment variables
Set equivalents for your framework (e.g., Spring Boot):
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### 3) Start database (optional via Docker)
If you use Docker for PostgreSQL:
```bash
docker run --name expense-tracker-db \
  -e POSTGRES_DB=expense_tracker \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

### 4) Build & run the API

#### Maven
```bash
mvn clean package
mvn spring-boot:run
```

#### Or run the packaged jar
```bash
java -jar target/*.jar
```

API default base URL (typical):  
- `http://localhost:8080`

---

## Swagger / OpenAPI

Interactive API docs (typical Springdoc endpoints):

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

If your server runs on a different port or has a context path, adjust URLs accordingly.

---

## API Conventions

- Base path: `/api/v1`
- Content type: `application/json`
- Standard response codes:
  - `200 OK` for successful retrieval/update (unless otherwise specified)
  - `201 Created` for new resource creation
  - `400 Bad Request` for validation errors
  - `401 Unauthorized` / `403 Forbidden` if authentication/authorization is enabled
  - `404 Not Found` when a resource does not exist
  - `409 Conflict` for conflicting requests (if applicable)
  - `500 Internal Server Error` for unexpected failures

---

## User Story 1 (KAN-11): Edit Expense

### Endpoint
**PUT** `/api/v1/expenses/{id}`

Updates an existing expense by ID.

### Path Parameters
- `id` (required, number/integer): Expense identifier

### Request Body (example)
```json
{
  "title": "Groceries",
  "amount": 54.25,
  "currency": "USD",
  "category": "FOOD",
  "expenseDate": "2026-07-28",
  "notes": "Weekly groceries at local market"
}
```

> Field names and enums may vary depending on your implementation. Use Swagger UI as the source of truth for the exact schema.

### Successful Response

#### 200 OK (example)
```json
{
  "id": 123,
  "title": "Groceries",
  "amount": 54.25,
  "currency": "USD",
  "category": "FOOD",
  "expenseDate": "2026-07-28",
  "notes": "Weekly groceries at local market",
  "updatedAt": "2026-07-28T10:22:41Z"
}
```

### Error Responses

#### 400 Bad Request (validation error example)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "amount must be greater than 0",
  "path": "/api/v1/expenses/123",
  "timestamp": "2026-07-28T10:23:01Z"
}
```

#### 404 Not Found (expense does not exist)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found: 123",
  "path": "/api/v1/expenses/123",
  "timestamp": "2026-07-28T10:23:20Z"
}
```

#### 401 Unauthorized / 403 Forbidden (if security enabled)
- `401` when not authenticated
- `403` when authenticated but not permitted to edit the expense

---

## Quick Test (cURL)

```bash
curl -X PUT "http://localhost:8080/api/v1/expenses/123" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Groceries",
    "amount": 54.25,
    "currency": "USD",
    "category": "FOOD",
    "expenseDate": "2026-07-28",
    "notes": "Weekly groceries at local market"
  }'
```

---

## Notes

- Refer to Swagger UI for the authoritative contract, including required fields, enum values, authentication, and error formats.
- If running behind a reverse proxy or with a custom context path, update the base URLs accordingly.
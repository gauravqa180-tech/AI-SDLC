# Expense Tracker v1 — Enhanced Spring Boot Service

Expense Tracker v1 is an enhanced Spring Boot REST service for tracking personal expenses with MySQL persistence, validation, and a clean API surface suitable for local development and CI.

---

## Requirements

- Java 17+
- Maven 3.9+
- Docker (optional, recommended for local MySQL and Testcontainers tests)
- MySQL 8.x (local install or Docker)

---

## Run Locally (MySQL)

### 1) Create a MySQL database and user

Run the following SQL in your MySQL instance:

```sql
CREATE DATABASE IF NOT EXISTS expense_tracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'expense_user'@'%' IDENTIFIED BY 'expense_pass';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'%';
FLUSH PRIVILEGES;
```

### 2) Configure Spring Boot

Set the following in `application.yml` or `application.properties` (adjust as needed):

**application.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: expense_user
    password: expense_pass
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080
```

### 3) Run the application

```bash
mvn clean spring-boot:run
```

Service will start at:

- http://localhost:8080

---

## API Endpoints

Base URL:
- `http://localhost:8080/api/v1`

### Expense Model (example)
```json
{
  "id": 1,
  "title": "Groceries",
  "amount": 45.90,
  "category": "FOOD",
  "expenseDate": "2026-07-28",
  "notes": "Weekly grocery run",
  "createdAt": "2026-07-28T10:15:30Z",
  "updatedAt": "2026-07-28T10:15:30Z"
}
```

> Fields may vary slightly based on implementation (e.g., timestamps), but the examples below reflect typical request/response formats.

---

### 1) Create Expense

**POST** `/expenses`

Request:
```bash
curl -X POST "http://localhost:8080/api/v1/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Coffee",
    "amount": 4.75,
    "category": "FOOD",
    "expenseDate": "2026-07-28",
    "notes": "Morning coffee"
  }'
```

Response (201):
```json
{
  "id": 101,
  "title": "Coffee",
  "amount": 4.75,
  "category": "FOOD",
  "expenseDate": "2026-07-28",
  "notes": "Morning coffee",
  "createdAt": "2026-07-28T10:20:00Z",
  "updatedAt": "2026-07-28T10:20:00Z"
}
```

---

### 2) List Expenses (optionally filter/paginate)

**GET** `/expenses`

Example:
```bash
curl "http://localhost:8080/api/v1/expenses"
```

Response (200):
```json
[
  {
    "id": 101,
    "title": "Coffee",
    "amount": 4.75,
    "category": "FOOD",
    "expenseDate": "2026-07-28",
    "notes": "Morning coffee",
    "createdAt": "2026-07-28T10:20:00Z",
    "updatedAt": "2026-07-28T10:20:00Z"
  }
]
```

---

### 3) Get Expense by ID

**GET** `/expenses/{id}`

Example:
```bash
curl "http://localhost:8080/api/v1/expenses/101"
```

Response (200):
```json
{
  "id": 101,
  "title": "Coffee",
  "amount": 4.75,
  "category": "FOOD",
  "expenseDate": "2026-07-28",
  "notes": "Morning coffee",
  "createdAt": "2026-07-28T10:20:00Z",
  "updatedAt": "2026-07-28T10:20:00Z"
}
```

---

### 4) Update Expense

**PUT** `/expenses/{id}`

Request:
```bash
curl -X PUT "http://localhost:8080/api/v1/expenses/101" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Coffee (Large)",
    "amount": 5.50,
    "category": "FOOD",
    "expenseDate": "2026-07-28",
    "notes": "Upgraded size"
  }'
```

Response (200):
```json
{
  "id": 101,
  "title": "Coffee (Large)",
  "amount": 5.50,
  "category": "FOOD",
  "expenseDate": "2026-07-28",
  "notes": "Upgraded size",
  "createdAt": "2026-07-28T10:20:00Z",
  "updatedAt": "2026-07-28T10:30:00Z"
}
```

---

### 5) Delete Expense

**DELETE** `/expenses/{id}`

Example:
```bash
curl -X DELETE "http://localhost:8080/api/v1/expenses/101"
```

Response (204): no content

---

## Testcontainers Tests

This project includes integration tests that use **Testcontainers** to run MySQL in a Docker container during tests.

### Notes
- Docker must be installed and running for these tests to work.
- If Docker is not available, Testcontainers-based integration tests will fail.

Run tests:
```bash
mvn test
```
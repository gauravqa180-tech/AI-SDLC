# Expense Tracker v1 — Spring Boot + MySQL REST API

Expense Tracker v1 is a RESTful API for tracking personal expenses with CRUD operations, monthly category breakdown analytics, and CSV export support. It includes interactive API documentation via Swagger/OpenAPI.

---

## Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
  - Spring Web (REST)
  - Spring Data JPA (Persistence)
  - Validation (Jakarta Validation)
- **MySQL 8.x** (Primary database)
- **Hibernate** (JPA provider)
- **Maven** (Build tool)
- **Springdoc OpenAPI** (Swagger UI)

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x running locally (or accessible remotely)

---

## Local Run Instructions

### 1) Create MySQL Database

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2) Configure Application Properties

Update your `application.yml` or `application.properties` with your MySQL connection details.

Example (`application.yml`):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    open-in-view: false

server:
  port: 8080
```

Notes:
- `ddl-auto: update` is convenient for local development. For production, prefer managed migrations (e.g., Flyway/Liquibase).
- `open-in-view: false` is recommended for REST APIs.

### 3) Build & Run

```bash
mvn clean package
mvn spring-boot:run
```

Or run the packaged jar:

```bash
java -jar target/*.jar
```

The API will be available at:

- `http://localhost:8080`

---

## Swagger / OpenAPI

Once the application is running:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## API Endpoints (v1)

Base URL (typical): `http://localhost:8080/api/v1`

### Expenses — CRUD

#### Create an Expense
- **POST** `/expenses`
- Body (example):
  ```json
  {
    "title": "Lunch",
    "amount": 12.50,
    "category": "FOOD",
    "expenseDate": "2026-07-28",
    "notes": "Business lunch"
  }
  ```

#### Edit/Update an Expense
- **PUT** `/expenses/{id}`
- Body (example):
  ```json
  {
    "title": "Lunch (updated)",
    "amount": 13.00,
    "category": "FOOD",
    "expenseDate": "2026-07-28",
    "notes": "Updated notes"
  }
  ```

#### List Expenses
- **GET** `/expenses`
- Common optional query parameters (if supported by your implementation):
  - `from` (date, e.g. `2026-07-01`)
  - `to` (date, e.g. `2026-07-31`)
  - `category` (e.g. `FOOD`)
  - `page`, `size`, `sort`

#### Delete an Expense
- **DELETE** `/expenses/{id}`

---

### Analytics — Monthly Category Breakdown

#### Monthly Breakdown by Category
- **GET** `/reports/monthly-category-breakdown`
- Typical query parameters:
  - `year` (e.g. `2026`)
  - `month` (1-12)

Returns totals aggregated per category for the specified month (exact response shape may vary by implementation).

Example (illustrative):
```json
{
  "year": 2026,
  "month": 7,
  "totalsByCategory": {
    "FOOD": 240.75,
    "TRANSPORT": 89.00,
    "UTILITIES": 120.10
  }
}
```

---

### Export — CSV

#### Export Expenses as CSV
- **GET** `/exports/expenses.csv`
- Optional filters (commonly supported):
  - `from`, `to`
  - `category`

Response:
- `Content-Type: text/csv`
- Includes a CSV file of matching expenses.

---

## Notes

- Use Swagger UI to explore request/response schemas and test endpoints interactively.
- If you encounter connection errors, verify MySQL is running and credentials/DB name match your configuration.
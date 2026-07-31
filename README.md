# AI-SDLC

A Spring Boot REST API for managing expenses with MySQL persistence, including CRUD operations, update support, and a monthly summary endpoint.

---

## Requirements

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- (Optional) Docker / Docker Compose

---

## Local Setup (MySQL)

### 1) Create Database

Log into MySQL and create a database:

```sql
CREATE DATABASE ai_sdlc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

(Optional) Create a dedicated user:

```sql
CREATE USER 'ai_sdlc_user'@'%' IDENTIFIED BY 'ai_sdlc_password';
GRANT ALL PRIVILEGES ON ai_sdlc.* TO 'ai_sdlc_user'@'%';
FLUSH PRIVILEGES;
```

### 2) Configure Application Properties

Update `src/main/resources/application.properties` (or `application.yml`) to point to your MySQL instance. Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_sdlc?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=ai_sdlc_user
spring.datasource.password=ai_sdlc_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

server.port=8080
```

### 3) Build and Run

```bash
mvn clean package
mvn spring-boot:run
```

Application will start on:

- http://localhost:8080

---

## API Endpoints

Base URL: `http://localhost:8080`

### Health / Info

- `GET /actuator/health` (if Spring Actuator is enabled)

---

## Expense API

> All request/response bodies are JSON unless otherwise specified.

### 1) Create Expense

- `POST /api/expenses`

Request body example:
```json
{
  "description": "Groceries",
  "amount": 54.25,
  "category": "FOOD",
  "date": "2026-07-31"
}
```

Response: `201 Created` with created expense.

---

### 2) List Expenses

- `GET /api/expenses`

Optional query params (if supported by implementation):
- `from=YYYY-MM-DD`
- `to=YYYY-MM-DD`
- `category=...`

Response: `200 OK` list of expenses.

---

### 3) Get Expense by ID

- `GET /api/expenses/{id}`

Response: `200 OK` expense, or `404 Not Found`.

---

### 4) Update Expense (PUT)

- `PUT /api/expenses/{id}`

Request body example:
```json
{
  "description": "Groceries (updated)",
  "amount": 60.00,
  "category": "FOOD",
  "date": "2026-07-31"
}
```

Response: `200 OK` updated expense, or `404 Not Found`.

---

### 5) Delete Expense

- `DELETE /api/expenses/{id}`

Response: `204 No Content`, or `404 Not Found`.

---

## Monthly Summary Endpoint

### Get Monthly Summary

- `GET /api/expenses/summary/monthly?year=YYYY&month=MM`

Example:
- `GET /api/expenses/summary/monthly?year=2026&month=07`

Response example:
```json
{
  "year": 2026,
  "month": 7,
  "total": 523.40,
  "byCategory": {
    "FOOD": 210.25,
    "TRANSPORT": 45.00,
    "UTILITIES": 120.00,
    "OTHER": 148.15
  },
  "count": 18
}
```

Response: `200 OK` monthly summary for the given year/month.

---

## Notes

- Dates are expected in ISO format: `YYYY-MM-DD`.
- Ensure your MySQL instance is running before starting the application.
- If you change database credentials, update `application.properties` accordingly.
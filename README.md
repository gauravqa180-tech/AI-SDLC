# AI-SDLC

## Overview
This repository contains a Spring Boot application that manages expenses with support for:

- Create, list, update, delete, and undo delete for expenses
- Monthly total calculation
- MySQL persistence

---

## Prerequisites

- Java 17+ (recommended)
- Maven 3.8+
- MySQL 8+
- (Optional) Postman or curl for API testing

---

## Run the Application

### 1) Configure MySQL

Create a database (and optionally a dedicated user).

#### Option A: Use root (quick start)
```sql
CREATE DATABASE aisdlc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Option B: Create a user (recommended)
```sql
CREATE DATABASE aisdlc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'aisdlc_user'@'%' IDENTIFIED BY 'aisdlc_password';
GRANT ALL PRIVILEGES ON aisdlc.* TO 'aisdlc_user'@'%';
FLUSH PRIVILEGES;
```

---

### 2) Configure Spring Boot Datasource

Update your `src/main/resources/application.properties` (or `application.yml`) with your MySQL connection details.

Example `application.properties`:
```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/aisdlc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=aisdlc_user
spring.datasource.password=aisdlc_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

If your project uses `application.yml`, equivalent:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aisdlc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: aisdlc_user
    password: aisdlc_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

---

### 3) Build and Run

From the project root:

```bash
mvn clean package
mvn spring-boot:run
```

Or run the built jar (name may differ depending on your artifact):
```bash
java -jar target/*.jar
```

The application should be available at:
- http://localhost:8080

---

## API Examples (Expenses)

Base URL:
- `http://localhost:8080`

> Note: Endpoint paths may vary depending on your controller mappings. The examples below assume a REST style under `/api/expenses`.

### Expense Model (example JSON)
```json
{
  "amount": 12.50,
  "currency": "USD",
  "category": "FOOD",
  "description": "Lunch",
  "expenseDate": "2026-07-31"
}
```

Typical fields:
- `amount` (number)
- `currency` (string like `USD`)
- `category` (string like `FOOD`, `TRANSPORT`, etc.)
- `description` (string)
- `expenseDate` (ISO date `YYYY-MM-DD`)

---

## 1) Create an Expense

### curl
```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 12.50,
    "currency": "USD",
    "category": "FOOD",
    "description": "Lunch",
    "expenseDate": "2026-07-31"
  }'
```

### Example Response
```json
{
  "id": 1,
  "amount": 12.50,
  "currency": "USD",
  "category": "FOOD",
  "description": "Lunch",
  "expenseDate": "2026-07-31",
  "deleted": false,
  "createdAt": "2026-07-31T10:15:30",
  "updatedAt": "2026-07-31T10:15:30"
}
```

---

## 2) List Expenses

### List all (optionally paged/filtered depending on implementation)
```bash
curl "http://localhost:8080/api/expenses"
```

### Filter by month (common pattern)
```bash
curl "http://localhost:8080/api/expenses?year=2026&month=7"
```

---

## 3) Update an Expense

Assuming `PUT /api/expenses/{id}`:

```bash
curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15.00,
    "currency": "USD",
    "category": "FOOD",
    "description": "Lunch + coffee",
    "expenseDate": "2026-07-31"
  }'
```

---

## 4) Delete an Expense

Assuming soft delete via `DELETE /api/expenses/{id}`:

```bash
curl -X DELETE "http://localhost:8080/api/expenses/1"
```

---

## 5) Undo Delete (Restore) an Expense

Assuming `POST /api/expenses/{id}/undo`:

```bash
curl -X POST "http://localhost:8080/api/expenses/1/undo"
```

---

## 6) Get Monthly Total

Assuming `GET /api/expenses/monthly-total?year=YYYY&month=MM`:

```bash
curl "http://localhost:8080/api/expenses/monthly-total?year=2026&month=7"
```

### Example Response
```json
{
  "year": 2026,
  "month": 7,
  "currency": "USD",
  "total": 245.75
}
```

---

## Troubleshooting

### MySQL connection issues
- Verify MySQL is running and accessible.
- Confirm hostname/port (`localhost:3306`) and credentials.
- If using MySQL 8 with newer auth, keep `allowPublicKeyRetrieval=true`.

### Database schema not created
- Ensure `spring.jpa.hibernate.ddl-auto=update` (or `create` for a fresh dev database).
- Check application logs for Hibernate errors.

---

## Notes
- If your project uses different API paths, search for `@RequestMapping` and `@RestController` in the source and adjust the URLs accordingly.
- Consider using environment variables or a Spring profile for production credentials.
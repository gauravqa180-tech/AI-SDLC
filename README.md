# Expense Tracker API (Spring Boot)

REST API for tracking expenses with soft-delete and restore support.

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- MySQL

## Run Locally

### Prerequisites
- Java 17
- Maven
- MySQL running locally (or accessible remotely)

### Configure `application.yml`
Set your database connection details (example):

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: your_mysql_user
    password: your_mysql_password
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

### Start the Application

```bash
mvn spring-boot:run
```

API will be available at:

```text
http://localhost:8080
```

## API Endpoints (Expenses)

Base path:

```text
/expenses
```

### Create Expense
```http
POST /expenses
Content-Type: application/json
```

Example body:
```json
{
  "title": "Groceries",
  "amount": 45.90,
  "currency": "USD",
  "category": "FOOD",
  "date": "2026-07-31",
  "notes": "Weekly shopping"
}
```

### List Expenses
```http
GET /expenses
```

Optional query params (if supported by your implementation):
```text
?page=0&size=20&sort=date,desc
```

### Get Expense by ID
```http
GET /expenses/{id}
```

### Update Expense
```http
PUT /expenses/{id}
Content-Type: application/json
```

Example body:
```json
{
  "title": "Groceries (updated)",
  "amount": 50.10,
  "currency": "USD",
  "category": "FOOD",
  "date": "2026-07-31",
  "notes": "Added snacks"
}
```

### Delete Expense (Soft Delete)
```http
DELETE /expenses/{id}
```

### Restore Expense
```http
POST /expenses/{id}/restore
```

## Notes

- Delete is expected to be a soft delete (records can be restored).
- Adjust endpoint paths to match your controllers if they differ.
# Expense Tracker API (Spring Boot + MySQL)

This project is a Spring Boot REST API for managing expenses backed by a MySQL database. It also provides interactive API documentation via Swagger/OpenAPI.

---

## Prerequisites

- Java 17+ (recommended: Temurin/Adoptium)
- Maven 3.8+
- MySQL 8.x
- (Optional) Docker / Docker Compose

---

## Setup

### 1) Create MySQL database and user

Log into MySQL and run:

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'expense_user'@'%' IDENTIFIED BY 'expense_pass';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'%';
FLUSH PRIVILEGES;
```

You can adjust names/passwords to your environment.

---

### 2) Configure Spring Boot datasource

Update your application configuration (typically `src/main/resources/application.yml`) with your MySQL connection info.

Example `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: expense_user
    password: expense_pass

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: 8080
```

Notes:
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development. For production, prefer migrations (Flyway/Liquibase).
- If your MySQL is not local, replace `localhost` and adjust firewall/network settings.

---

## Build & Run

### Run with Maven

```bash
mvn clean spring-boot:run
```

### Package and run jar

```bash
mvn clean package
java -jar target/*.jar
```

API will be available at:

- Base URL: `http://localhost:8080`

---

## Swagger / OpenAPI

Swagger/OpenAPI paths are configured in `application.yml`.

Interactive Swagger UI is available at:

- `http://localhost:8080/swagger-ui`

OpenAPI JSON is available at:

- `http://localhost:8080/api-docs`

---

## API: Expenses

All endpoints below assume the base path:

- `/api/expenses`

### Expense Fields

Expenses include:

- `id` (number) — server-generated
- `amount` (number) — must be positive
- `category` (string) — e.g., `FOOD`, `TRANSPORT`, etc.
- `date` (string) — ISO-8601 date (e.g., `2026-08-01`)
- `note` (string, optional)

---

### 1) Create Expense

**POST** `/api/expenses`

Request body example:

```json
{
  "amount": 12.50,
  "category": "FOOD",
  "date": "2026-08-01",
  "note": "Team lunch"
}
```

Responses:
- `201 Created` with the created expense
- `400 Bad Request` if validation fails

---

### 2) List Expenses

**GET** `/api/expenses`

Returns a paginated list of expenses using Spring Data `Pageable`.

Query parameters:
- `page` (0-based) — page index (e.g., `?page=0`)
- `size` — page size (e.g., `?size=20`)
- `sort` — sorting criteria (repeatable), e.g. `?sort=date,desc` or `?sort=amount,asc`

Responses:
- `200 OK` with a page of expenses

(See Swagger UI for the authoritative list and response schema.)

---

### 3) Get Expense by ID

**GET** `/api/expenses/{id}`

Responses:
- `200 OK` with the expense
- `404 Not Found` if the expense does not exist

---

### 4) Update Expense

**PUT** `/api/expenses/{id}`

Request body example:

```json
{
  "amount": 13.00,
  "category": "FOOD",
  "date": "2026-08-01",
  "note": "Updated note"
}
```

Responses:
- `200 OK` with updated expense (or `204 No Content` depending on implementation)
- `400 Bad Request` if validation fails
- `404 Not Found` if the expense does not exist

---

### 5) Delete Expense

**DELETE** `/api/expenses/{id}`

Responses:
- `204 No Content` on success
- `404 Not Found` if the expense does not exist

---

## Validation Behavior

When request validation fails (e.g., missing required fields, invalid formats), the API responds with:

- `400 Bad Request`

Typical validation rules include:
- `amount`: required, must be greater than 0
- `date`: required, must be a valid ISO date
- `category`: required, must be one of the supported values (if an enum is used)
- `note`: optional (may be limited in length depending on implementation)

The error response body format may vary by implementation (for example, a message plus a list/map of field errors). Refer to Swagger UI and actual responses to confirm the exact error schema.

---

## Troubleshooting

### MySQL connection issues
- Ensure MySQL is running and reachable.
- Verify `spring.datasource.url`, username, and password.
- If using MySQL 8 and seeing authentication errors, ensure `allowPublicKeyRetrieval=true` is set in the JDBC URL (for local/dev).

### Port already in use
- Change `server.port` in your Spring config or stop the process using that port.

---

## License

Add license information here if applicable.

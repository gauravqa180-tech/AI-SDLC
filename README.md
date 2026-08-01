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

Update your application configuration (typically `src/main/resources/application.properties` or `application.yml`) with your MySQL connection info.

Example `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=expense_user
spring.datasource.password=expense_pass

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

server.port=8080
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

Interactive Swagger UI is available at:

- `http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON is typically available at:

- `http://localhost:8080/v3/api-docs`

(Exact paths may vary if the project customizes springdoc settings.)

---

## API: Expenses

All endpoints below assume the base path:

- `/api/expenses`

### Expense Fields (typical)

The exact schema may vary slightly depending on implementation, but expenses generally include:

- `id` (number) — server-generated
- `title` (string) — short description/name
- `amount` (number) — must be positive
- `category` (string) — e.g., `FOOD`, `TRANSPORT`, etc.
- `date` (string) — ISO-8601 date (e.g., `2026-08-01`)
- `notes` (string, optional)

---

### 1) Create Expense

**POST** `/api/expenses`

Request body example:

```json
{
  "title": "Lunch",
  "amount": 12.50,
  "category": "FOOD",
  "date": "2026-08-01",
  "notes": "Team lunch"
}
```

Responses:
- `201 Created` with the created expense
- `400 Bad Request` if validation fails

---

### 2) List Expenses

**GET** `/api/expenses`

Returns a list of expenses.

Responses:
- `200 OK` with an array of expenses

(Optional query parameters may exist in your implementation, such as pagination or date ranges. See Swagger UI for the authoritative list.)

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
  "title": "Lunch (updated)",
  "amount": 13.00,
  "category": "FOOD",
  "date": "2026-08-01",
  "notes": "Updated note"
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
- `title`: required, non-blank
- `amount`: required, must be greater than 0
- `date`: required, must be a valid ISO date
- `category`: required, must be one of the supported values (if an enum is used)

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

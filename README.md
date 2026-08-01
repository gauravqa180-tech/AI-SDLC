# Expense Tracker (Spring Boot) v1.1

A simple Expense Tracker REST API built with Spring Boot. It provides CRUD operations for expenses and categories, reporting endpoints, and API documentation via Swagger/OpenAPI. Version v1.1 includes soft-delete support with an undo window to restore recently deleted records.

---

## Features

- Manage **Expenses**
  - Create, update, list, search/filter, and delete
  - Soft-delete with undo window (v1.1)
- Manage **Categories**
  - Create, update, list, and delete
- Reporting
  - Summaries by date range and category (as supported by the API)
- API Documentation
  - Swagger UI / OpenAPI

---

## Requirements

- Java 17+ (recommended)
- Maven 3.8+ (or Gradle if the project is configured for it)
- Database:
  - H2 (for local/dev) or a supported RDBMS such as PostgreSQL/MySQL (depending on configuration)
- (Optional) Docker / Docker Compose if your environment uses containerized services

---

## Run Steps

### 1) Configure application properties
Set your database configuration and any environment-specific properties in:
- `src/main/resources/application.properties` or `application.yml`

If using PostgreSQL/MySQL, ensure:
- JDBC URL, username, password
- Hibernate DDL settings (dev vs prod)
- Server port if you want a non-default value

### 2) Build
Using Maven:
- `mvn clean package`

### 3) Run
Using Maven:
- `mvn spring-boot:run`

Or run the generated jar:
- `java -jar target/*.jar`

The service will start on:
- `http://localhost:8080` (unless configured otherwise)

---

## Swagger / OpenAPI

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`

OpenAPI spec:
- `http://localhost:8080/v3/api-docs`

---

## Soft-Delete & Undo Window (v1.1)

- Deleting an entity (e.g., expense/category) performs a **soft-delete** rather than a hard delete.
- Soft-deleted records are excluded from normal list/search endpoints.
- An **undo window** is available for a limited time after deletion:
  - Within the configured undo window, the record can be restored via the restore/undo endpoint (if exposed by the API).
  - After the undo window expires, the record may be permanently removed by cleanup logic (if implemented) or treated as non-restorable.
- Notes:
  - If you need to include deleted items (e.g., for admin/audit purposes), use the dedicated endpoint/flag (if available).
  - Time window duration is controlled by configuration in the application settings (if present).

---

## Notes

- Ensure the database is reachable and credentials are correct before starting the application.
- For production usage, avoid using `ddl-auto=create` and prefer managed migrations (Flyway/Liquibase) if available in the project.
- If authentication/authorization is configured, include required headers/tokens when calling secured endpoints.
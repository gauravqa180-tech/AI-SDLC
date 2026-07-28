# Spring Boot Expense Tracker v1 — Enhancements (KAN-73..77)

This backend provides APIs for managing expenses and categories, including recent enhancements covering soft delete behavior, improved endpoints, and API documentation.

---

## Requirements

- Java 17 (recommended) or Java 21
- Maven 3.9+
- Spring Boot 3.x
- A supported database:
  - PostgreSQL (recommended for production)
  - H2 (optional for local/dev if configured)
- Optional tooling:
  - Docker + Docker Compose (if you run the database in containers)
  - IntelliJ IDEA / VS Code

---

## Configuration

Update `application.yml` / `application.properties` as needed:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.jpa.hibernate.ddl-auto` (use `validate`/`update` for dev; `validate` for prod)
- `server.port` (default: `8080`)

If you use PostgreSQL locally, a typical configuration looks like:

- JDBC URL: `jdbc:postgresql://localhost:5432/expense_tracker`
- Username/password: as configured in your local DB

---

## Run (Local)

### Option A — Run with Maven
1. Build:
   - `mvn clean package`
2. Start:
   - `mvn spring-boot:run`

### Option B — Run the packaged JAR
1. Build:
   - `mvn clean package`
2. Run:
   - `java -jar target/*.jar`

The service starts at:
- `http://localhost:8080`

---

## API Documentation (Swagger / OpenAPI)

Once the application is running:

- Swagger UI:
  - `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON:
  - `http://localhost:8080/v3/api-docs`

---

## Core Endpoints

> Exact request/response models and query parameters are documented in Swagger UI.

### Health / Status
- `GET /actuator/health` (if Actuator is enabled)

### Expenses
- `GET /api/v1/expenses`
  - List expenses (may support pagination/sorting via query parameters; see Swagger)
- `GET /api/v1/expenses/{id}`
  - Retrieve an expense by id
- `POST /api/v1/expenses`
  - Create a new expense
- `PUT /api/v1/expenses/{id}`
  - Update an existing expense
- `DELETE /api/v1/expenses/{id}`
  - Soft-delete an expense (see notes below)

### Categories
- `GET /api/v1/categories`
  - List categories
- `GET /api/v1/categories/{id}`
  - Retrieve a category by id
- `POST /api/v1/categories`
  - Create a new category
- `PUT /api/v1/categories/{id}`
  - Update an existing category
- `DELETE /api/v1/categories/{id}`
  - Soft-delete a category (see notes below)

---

## Soft Delete Notes (KAN-73..77)

This project uses **soft delete** for selected resources (e.g., expenses and categories).

### What soft delete means
- `DELETE` does **not** physically remove rows from the database.
- Instead, the record is marked as deleted (commonly via fields like `deleted`, `deletedAt`, or similar).

### Expected behavior
- Soft-deleted records are excluded from standard list and get operations.
- Attempting to fetch a soft-deleted resource by ID should behave as “not found” (or similar) depending on implementation.
- Re-creating resources with the same natural keys may be possible depending on uniqueness constraints and how soft delete is implemented.

### Database considerations
- If unique constraints exist on soft-deleted fields, ensure the implementation accounts for soft-deleted rows (e.g., partial indexes in PostgreSQL or application-level checks).
- Review migration scripts and entity annotations to confirm soft delete filtering is applied consistently.

---

## Common Troubleshooting

- **Port already in use**: change `server.port` or stop the conflicting service.
- **DB connection failures**: verify datasource URL, credentials, and that the DB is running.
- **Swagger not available**: ensure OpenAPI/Swagger dependency is included and enabled by configuration.

---

## Version / Scope

This README reflects backend guidance for **Expense Tracker v1 enhancements**, including work tracked under **KAN-73..77**.
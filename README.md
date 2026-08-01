# AI-SDLC

## Project Overview

AI-SDLC is a backend service for managing expenses. It exposes REST APIs for creating, listing, and editing expenses, backed by a MySQL database. The project includes automated tests that can run against ephemeral MySQL instances using Testcontainers.

Key features:
- RESTful API for expense management
- MySQL persistence
- Integration tests powered by Testcontainers (no local DB required for tests)

---

## Prerequisites

- Java 17+ (or the Java version configured by the project)
- Maven 3.8+ (or the build tool used by the project)
- Docker (required for Testcontainers tests)
- MySQL 8+ (required to run the application locally with MySQL)

---

## How to Run (with MySQL)

### 1) Start MySQL

You can use an existing MySQL instance or start one locally. Example using Docker:

- Start MySQL:
  - Image: `mysql:8`
  - Port: `3306`
  - Create a database (example): `ai_sdlc`
  - Create a user/password (example): `ai_user` / `ai_password`

Example (illustrative) Docker command:

- `docker run --name ai-sdlc-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=ai_sdlc -e MYSQL_USER=ai_user -e MYSQL_PASSWORD=ai_password -p 3306:3306 -d mysql:8`

### 2) Configure Application

Set your application’s DB connection properties to point to MySQL. Typical properties include:

- JDBC URL (example): `jdbc:mysql://localhost:3306/ai_sdlc`
- Username (example): `ai_user`
- Password (example): `ai_password`

Depending on the framework, these are commonly configured via:
- `application.properties` / `application.yml`
- Environment variables (e.g., `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)

### 3) Build and Run

Using Maven (common setup):

- Build:
  - `mvn clean package`
- Run:
  - `mvn spring-boot:run`

If the project uses a different runtime entry point (e.g., Gradle, Quarkus, Micronaut), run it using the corresponding command defined in the project.

---

## How to Run Tests (with Testcontainers)

Tests use Testcontainers to provision a MySQL container automatically at test time. This requires Docker to be installed and running.

Run tests:

- `mvn test`

Notes:
- Ensure Docker daemon is running before executing tests.
- The first run may take longer while Docker images are pulled.
- If your environment restricts Docker (CI runners, corporate laptops), configure Docker access accordingly.

---

## Implemented User Story #1: Edit Expense

### Endpoints

User Story #1 implements editing an existing expense.

- Update/Edit an Expense  
  - Method: `PUT` (or `PATCH`, depending on implementation)  
  - Path: `/expenses/{id}`  
  - Description: Updates fields of an existing expense by its identifier.

If your project exposes a versioned API (e.g., `/api/v1`), the endpoint will be:

- `PUT /api/v1/expenses/{id}` (or `PATCH /api/v1/expenses/{id}`)

### Expected Request/Response (High Level)

- Request typically includes one or more editable fields such as:
  - amount
  - description
  - date
  - category (if applicable)

- Response typically returns the updated expense representation.

Refer to the API controller or OpenAPI/Swagger documentation (if included) for the exact schema and validations.
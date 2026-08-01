# AI-SDLC

## Project Overview
AI-SDLC is a Spring Boot REST API that demonstrates a typical SDLC-ready backend service with:
- CRUD operations + update endpoint for a domain resource
- Input validation with clear validation rules
- Optimistic concurrency control via a `version` field
- API documentation via Swagger UI (OpenAPI)

The service uses MySQL as the primary datastore and is configured through `application.yml`.

---

## Tech Stack
- Java + Spring Boot
- Spring Web (REST)
- Spring Data JPA (persistence)
- Bean Validation (Jakarta Validation / Hibernate Validator)
- MySQL
- OpenAPI/Swagger UI

---

## How to Run

### Prerequisites
- Java (matching the project’s configured version)
- Maven or Gradle (depending on the build setup)
- MySQL server running and accessible

### Configure MySQL (application.yml)
Update the MySQL connection properties in `src/main/resources/application.yml` (or the environment-specific YAML if applicable). Typical configuration includes:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.jpa.hibernate.ddl-auto` (e.g., `update`, `validate`, etc.)

Example (illustrative—use the project’s actual keys/structure):
- URL: `jdbc:mysql://localhost:3306/<db_name>`
- Username/password for your local environment

### Run the Application
Using Maven:
- `mvn spring-boot:run`

Or build and run:
- `mvn clean package`
- `java -jar target/<artifact-name>.jar`

The application will start on the configured server port (commonly `8080` unless overridden).

---

## API Documentation (Swagger UI)
Swagger UI is available at:

- **Swagger UI**: `/swagger-ui/index.html`
- **OpenAPI spec (JSON)**: `/v3/api-docs`

(If your app runs on port 8080 locally: `http://localhost:8080/swagger-ui/index.html`)

---

## API Endpoints
The API exposes standard RESTful operations including create, read, update, and delete. Endpoints follow typical conventions:

### Create
- **POST** `/<resource>`
  - Creates a new resource.
  - Returns the created entity (often including generated `id` and initial `version`).

### Read (List)
- **GET** `/<resource>`
  - Returns a list (optionally pageable/filterable depending on implementation).

### Read (By ID)
- **GET** `/<resource>/{id}`
  - Returns a single entity by its identifier.
  - Returns `404 Not Found` if the entity does not exist.

### Update
- **PUT** `/<resource>/{id}`
  - Updates an existing entity.
  - Typically requires the current `version` to support optimistic locking (see “Concurrency Control”).
  - Returns the updated entity with an incremented `version` on success.

### Delete
- **DELETE** `/<resource>/{id}`
  - Deletes an entity by its identifier.
  - Returns `204 No Content` on success (or equivalent behavior depending on implementation).

> Note: Replace `/<resource>` with the actual resource path used by the application (e.g., `/items`, `/users`, etc.).

---

## Concurrency Control (Optimistic Locking via Version)
This project uses optimistic locking to prevent lost updates when multiple clients update the same record concurrently.

### How it works
- Each entity includes a **`version`** field.
- On update, the client provides the current `version` value.
- If the stored record’s `version` differs from the client’s `version`, the update is rejected (indicating a concurrent modification).
- On successful update, the `version` increments automatically (JPA-managed).

### Expected behavior
- If two users load the same record and both attempt to update:
  - The first update succeeds and increments `version`.
  - The second update fails due to a version mismatch (commonly resulting in `409 Conflict` or an optimistic locking exception mapped to an HTTP error).

---

## Validation Rules
Request payloads are validated using Bean Validation annotations. Common rules enforced by the API typically include:

- **Required fields**: must be non-null / non-empty (e.g., `@NotNull`, `@NotBlank`)
- **String length constraints**: minimum/maximum sizes (e.g., `@Size`)
- **Numeric constraints**: min/max ranges (e.g., `@Min`, `@Max`, `@Positive`)
- **Format constraints**: e.g., email format (e.g., `@Email`), patterns (e.g., `@Pattern`)
- **ID and version constraints**:
  - `id` is usually path-driven and must be a valid positive number when applicable
  - `version` is required for update operations (optimistic locking)

### Validation error responses
Invalid requests typically return:
- **400 Bad Request**
- A structured error response describing the invalid fields and messages (implementation-dependent)

---

## Common HTTP Status Codes
- `200 OK` / `201 Created` for successful reads/creates
- `204 No Content` for successful deletes (if configured)
- `400 Bad Request` for validation failures
- `404 Not Found` when the resource does not exist
- `409 Conflict` (or equivalent) for optimistic locking / version conflicts

---

## Notes
- Ensure MySQL is running and the database exists (unless auto-create is enabled).
- If running in different environments, confirm the active Spring profile and the corresponding YAML configuration.
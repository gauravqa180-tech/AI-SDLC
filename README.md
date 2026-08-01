# Spring Boot + MySQL — Setup, Run Instructions, API Endpoints, Validation, and CSV Injection Mitigation

This project is a Spring Boot REST API backed by MySQL. It includes request validation, clear run instructions, endpoint documentation, and guidance to mitigate CSV injection risks when exporting data.

---

## Tech Stack

- Java 17+
- Spring Boot (Web, Validation, Data JPA)
- MySQL 8+
- Maven (or Gradle)
- Flyway/Liquibase (if configured)
- Lombok (optional)

---

## Prerequisites

- Java 17 installed and `JAVA_HOME` configured
- Maven 3.9+ (or Gradle)
- MySQL 8+ running locally or accessible remotely

---

## Database Setup (MySQL)

1. Create a database and a dedicated user (recommended):

```sql
CREATE DATABASE app_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'app_user'@'%' IDENTIFIED BY 'app_password';
GRANT ALL PRIVILEGES ON app_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;
```

2. Configure Spring Boot to connect to MySQL.

### `application.yml` (recommended)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: app_user
    password: app_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

  jackson:
    serialization:
      write-dates-as-timestamps: false

logging:
  level:
    org.hibernate.SQL: INFO
    org.hibernate.type.descriptor.sql: INFO
```

### `application.properties` (alternative)

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=app_user
spring.datasource.password=app_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
```

> Production note: prefer `ddl-auto=validate` (with migrations via Flyway/Liquibase), use secrets management, and enable TLS.

---

## Build and Run

### Run with Maven

```bash
mvn clean test
mvn spring-boot:run
```

### Run as a packaged JAR

```bash
mvn clean package
java -jar target/*.jar
```

### Run with Gradle (if applicable)

```bash
./gradlew clean test
./gradlew bootRun
```

---

## Environment Variables (Optional)

You can externalize configuration:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`

Example:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="app_user"
export SPRING_DATASOURCE_PASSWORD="app_password"
export SERVER_PORT=8080
mvn spring-boot:run
```

---

## API Documentation

If Swagger/OpenAPI is enabled in your project, typical locations are:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

If not configured, add `springdoc-openapi-starter-webmvc-ui` and expose docs as above.

---

## Endpoints

> Actual paths may vary based on your controllers. The following are standard REST patterns to align with typical Spring Boot APIs.

### Health Check

- `GET /actuator/health` (if Spring Actuator enabled)
- `GET /health` (if custom)

Response (example):
```json
{ "status": "UP" }
```

### CRUD Pattern (Example)

Assuming a resource `items`:

- `GET /api/items` — list items
- `GET /api/items/{id}` — get item by id
- `POST /api/items` — create item
- `PUT /api/items/{id}` — update item
- `DELETE /api/items/{id}` — delete item

Typical `POST` request body (example):
```json
{
  "name": "Example",
  "description": "A sample item"
}
```

Typical error response shape (recommended):
```json
{
  "timestamp": "2026-08-01T10:20:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/items"
}
```

> If your API uses different resources, update this section to mirror your controllers.

---

## Validation

This project uses Spring Validation (`jakarta.validation`) to ensure incoming requests are well-formed and safe.

### Recommended Validation Practices

- Use `@Valid` on controller method parameters
- Use constraint annotations in DTOs:
  - `@NotNull`, `@NotBlank`, `@Size`
  - `@Email`, `@Pattern`
  - `@Min`, `@Max`, `@Positive`
- Validate path variables and request params where applicable
- Return consistent validation errors via `@ControllerAdvice`

Example DTO:

```java
public class CreateItemRequest {
  @NotBlank
  @Size(max = 100)
  private String name;

  @Size(max = 500)
  private String description;
}
```

Example controller usage:

```java
@PostMapping("/api/items")
public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
  ...
}
```

---

## Story Coverage

This repository aims to cover the following user stories (high-level):

1. **As a user, I can run the service locally with MySQL**  
   - Clear database configuration and run instructions included.

2. **As a user, I can perform CRUD operations via REST endpoints**  
   - Standard REST endpoints documented and tested (where applicable).

3. **As a user, I receive helpful validation errors for invalid input**  
   - Requests are validated with Bean Validation; errors are returned consistently.

4. **As a user, I can export data safely to CSV without spreadsheet formula execution risks**  
   - CSV injection mitigations are defined and enforced where CSV export exists.

---

## CSV Injection Mitigation Notes

If your application exports user-provided fields to CSV, spreadsheet software (Excel, Google Sheets) may interpret certain values as formulas. This can lead to **CSV injection** (a.k.a. formula injection) if values start with:

- `=`
- `+`
- `-`
- `@`

### Recommended Mitigation

1. **Prefix dangerous values with a single quote `'`**  
   Example:
   - Input: `=HYPERLINK("http://evil.com","Click")`
   - Output: `'=HYPERLINK("http://evil.com","Click")`

2. **Apply mitigation to all exported string cells** that originate from untrusted sources (users, external systems).

3. **Do not rely on CSV quoting alone** (`"..."`) — it may not prevent evaluation in some spreadsheet apps.

4. **Document and test**:
   - Unit test that values beginning with `= + - @` are sanitized.
   - Ensure already-safe values are not altered incorrectly.

### Sample Sanitization Logic (Reference)

- If value starts with `[=+\-@]` after trimming leading whitespace, prefix `'`.
- Preserve null/empty values.

Pseudo-rule:
- `sanitized = value`
- `if trimmed(value) startsWith one of (= + - @): sanitized = "'" + value`

> If your project includes CSV export endpoints or batch jobs, ensure they use the sanitization step before writing values.

---

## Troubleshooting

### Common MySQL connection issues

- **`Communications link failure`**  
  Ensure MySQL is running, host/port are correct, and firewall allows connection.

- **Authentication plugin error**  
  Use MySQL 8 compatible driver (`com.mysql.cj.jdbc.Driver`) and ensure user auth plugin is supported.

- **Timezone / SSL warnings**  
  Include `serverTimezone=UTC` and configure SSL appropriately for your environment.

---

## License

Add your license information here (MIT/Apache-2.0/etc.) if applicable.
# Expense Tracker API v2 (Spring Boot)

A RESTful Expense Tracker API built with Spring Boot. Version 2 includes enhancements such as improved configuration via `application.yml`, MySQL persistence, and interactive API documentation via Swagger/OpenAPI.

---

## Prerequisites

- **Java 17**
- **Maven 3.8+**
- **MySQL 8+**

Verify installed versions:

- `java -version`
- `mvn -version`

---

## Configuration (`application.yml`)

Update the application configuration in:

- `src/main/resources/application.yml`

Example configuration (adjust values to your environment):

```yaml
server:
  port: 8080

spring:
  application:
    name: expense-tracker-api

  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect

# Optional: tune logging as needed
logging:
  level:
    org.springframework: INFO
    org.hibernate.SQL: WARN
```

### MySQL Setup

Create the database (if not already created):

```sql
CREATE DATABASE expense_tracker;
```

Ensure the configured MySQL user has permissions to read/write to this database.

---

## Running the Application

From the project root:

```bash
mvn spring-boot:run
```

The API will start on:

- `http://localhost:8080`

---

## Swagger / OpenAPI Documentation

Once the application is running, access:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## Notes

- If you change the server port in `application.yml`, update the Swagger URLs accordingly.
- For production deployments, store secrets (like DB passwords) securely (e.g., environment variables, vaults) instead of hardcoding them in configuration files.
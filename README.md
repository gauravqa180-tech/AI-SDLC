# Spring Boot API

RESTful API built with Spring Boot. This document describes prerequisites, database setup, how to run the application, Swagger/OpenAPI access, and available endpoints.

---

## Prerequisites

- **Java**: 17+
- **Build tool**: Maven 3.8+ (or use Maven Wrapper if included)
- **Database**: PostgreSQL 13+ (recommended)  
- **Git**: for cloning the repository

Optional:
- **Docker** / **Docker Compose** (if you prefer running PostgreSQL in a container)

---

## Database Setup

### Option A: Local PostgreSQL

1. Create a database and user:

```sql
CREATE DATABASE app_db;
CREATE USER app_user WITH ENCRYPTED PASSWORD 'app_password';
GRANT ALL PRIVILEGES ON DATABASE app_db TO app_user;
```

2. Configure the application database connection in `src/main/resources/application.yml` (or `application.properties`), for example:

**application.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/app_db
    username: app_user
    password: app_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
```

> If your project uses Flyway/Liquibase, ensure migrations are enabled and the schema is created automatically on startup.

### Option B: Docker (PostgreSQL)

Create a `docker-compose.yml` (if one isn’t already included) and start PostgreSQL:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: app-postgres
    environment:
      POSTGRES_DB: app_db
      POSTGRES_USER: app_user
      POSTGRES_PASSWORD: app_password
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```

Run:

```bash
docker compose up -d
```

---

## Build & Run

### 1) Build

```bash
mvn clean package
```

### 2) Run

Run via Maven:

```bash
mvn spring-boot:run
```

Or run the built JAR:

```bash
java -jar target/*.jar
```

### Configuration

You can override configuration via environment variables, for example:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/app_db
export SPRING_DATASOURCE_USERNAME=app_user
export SPRING_DATASOURCE_PASSWORD=app_password
```

---

## Swagger / OpenAPI

Once the application is running, access the API documentation:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

> If your server runs on a different port, replace `8080` accordingly.

---

## Endpoints

Base URL (default): `http://localhost:8080`

### Health / Info
- `GET /actuator/health` (if Spring Actuator is enabled)
- `GET /actuator/info` (if Spring Actuator is enabled)

### API Endpoints (as described in this PR)
- `GET /api/...`
- `POST /api/...`
- `PUT /api/...`
- `PATCH /api/...`
- `DELETE /api/...`

> The authoritative list of endpoints and request/response schemas is available in Swagger UI.

---

## Notes

- Ensure the database is reachable and credentials are correct before starting the app.
- If you use profiles (e.g., `dev`, `test`, `prod`), run with:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=dev
  ```
- If CORS/security is enabled, consult the security configuration for authentication requirements.
# AI-SDLC

## Overview
This repository contains a **Spring Boot + MySQL** application that provides a backend API (with **Swagger/OpenAPI UI**) and a web UI for managing personal finance data (transactions, categories, budgets), including dashboards and import/export features.

---

## Prerequisites
- **Java 17+** (recommended: 17)
- **Maven 3.8+**
- **MySQL 8.x**
- (Optional) **Docker** + **Docker Compose** (if you prefer containerized MySQL)

---

## Setup (MySQL)
### 1) Create database & user (recommended)
Login to MySQL and run:

```sql
CREATE DATABASE aisdlc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'aisdlc_user'@'%' IDENTIFIED BY 'aisdlc_password';
GRANT ALL PRIVILEGES ON aisdlc.* TO 'aisdlc_user'@'%';
FLUSH PRIVILEGES;
```

> You can name the database/user differently—just ensure your Spring configuration matches.

---

## Configure Application
Update your Spring Boot configuration (typically `src/main/resources/application.properties` or `application.yml`) to point to MySQL.

### Example (application.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/aisdlc?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=aisdlc_user
spring.datasource.password=aisdlc_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

server.port=8080
```

If the project uses `application.yml`, the same values apply in YAML format.

---

## Build & Run
### Run locally with Maven
```bash
mvn clean install
mvn spring-boot:run
```

### Run the packaged jar
```bash
mvn clean package
java -jar target/*.jar
```

App will start at:
- Backend/API base: `http://localhost:8080`

---

## Database Migration / Schema
Depending on the project configuration:
- If `spring.jpa.hibernate.ddl-auto=update`, tables are created/updated automatically on startup.
- If migrations are used (Flyway/Liquibase), ensure the migration tool is enabled and configured in `application.*`.

---

## Swagger / OpenAPI (API Documentation)
Once the app is running, open:
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- (If enabled) OpenAPI spec: `http://localhost:8080/v3/api-docs`

Use Swagger UI to:
- Explore all endpoints
- Run requests directly (GET/POST/PUT/DELETE)
- Validate request/response payloads

---

## Web UI Usage
If the project includes a UI served by Spring Boot (e.g., Thymeleaf) or a frontend packaged in the same server:
- Open the UI at: `http://localhost:8080/`

Typical UI capabilities implemented:
- View a dashboard with summaries/insights
- Create/edit/delete transactions
- Filter/sort/search transaction lists
- Manage categories
- Manage budgets and view alerts
- Import/export transactions (CSV)

> If this repository uses a separate frontend (React/Angular/Vue), refer to the frontend folder README (if present) and ensure CORS/backend URL are configured.

---

## Main Features / User Stories Implemented
The application includes the following user stories and capabilities:

1. **Edit transactions**
   - Users can create and update transaction records (amount, date, category, description, etc.).

2. **Filter / Sort / Search**
   - Users can filter by date ranges, categories, types, amounts.
   - Users can sort lists (e.g., by date, amount).
   - Users can search transactions by keywords (e.g., description/merchant).

3. **Dashboard**
   - Users can view summary metrics (e.g., totals by period, by category).
   - Charts/aggregations may be available depending on UI.

4. **CSV Import / Export**
   - Users can export transactions to CSV for reporting/backups.
   - Users can import CSV to bulk-load transactions.

5. **Budgets & Alerts**
   - Users can create budgets (e.g., monthly per category).
   - System provides alerts/indicators when spending approaches/exceeds budgets.

---

## API Endpoints (High-Level)
Exact routes may vary; the authoritative list is always in **Swagger UI**.

Commonly available endpoint groups:
- **Transactions**
  - Create transaction
  - Update transaction
  - Delete transaction
  - Get transaction by id
  - List transactions (with filtering/sorting/search query params)
- **Categories**
  - CRUD categories
  - List categories
- **Budgets**
  - CRUD budgets
  - Budget status/alerts (e.g., usage vs limit)
- **Dashboard**
  - Summary endpoints (totals, breakdowns, trends)
- **Import/Export**
  - Export transactions CSV
  - Import transactions CSV

### How to find exact endpoint paths
1. Start the app.
2. Open Swagger UI: `http://localhost:8080/swagger-ui/index.html`
3. Expand each controller/group to see:
   - Path
   - Method (GET/POST/PUT/DELETE)
   - Request params/body
   - Response schemas

---

## CSV Import/Export Notes
- Ensure CSV headers match the expected format shown in UI or Swagger documentation.
- If import supports validation, invalid rows may be rejected or reported.
- For large files, consider server limits (multipart size) and adjust Spring settings if needed (e.g., `spring.servlet.multipart.max-file-size`).

---

## Troubleshooting
### MySQL connection errors
- Verify MySQL is running and reachable.
- Confirm username/password and database exist.
- Ensure the JDBC URL uses correct host/port (`localhost:3306` by default).

### Port already in use
- Change `server.port` in `application.properties`:
  ```properties
  server.port=8081
  ```

### Swagger not loading
- Confirm the dependency for springdoc-openapi (or Swagger library) is included.
- Check security config (if Spring Security is enabled) allows access to `/swagger-ui/**` and `/v3/api-docs/**`.

---

## Quick Start
1. Create MySQL database/user.
2. Configure `application.properties` with DB credentials.
3. Run:
   ```bash
   mvn spring-boot:run
   ```
4. Open:
   - UI: `http://localhost:8080/`
   - Swagger: `http://localhost:8080/swagger-ui/index.html`
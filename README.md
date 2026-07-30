# AI-SDLC

Expense Tracker API (Spring Boot + MySQL)

## Tech Stack
- Java 17
- Spring Boot
- MySQL
- Swagger/OpenAPI (swagger-ui)

---

## Prerequisites
- Java 17 installed (`java -version`)
- Maven installed (`mvn -v`) or use Maven Wrapper (`./mvnw`)
- MySQL 8+ running
- (Optional) Docker + Docker Compose

---

## Setup

### 1) Create MySQL Database
Create a database (and user if desired):

```sql
CREATE DATABASE expense_tracker;
-- Optional:
-- CREATE USER 'expense_user'@'%' IDENTIFIED BY 'expense_pass';
-- GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'%';
-- FLUSH PRIVILEGES;
```

### 2) Configure Application Properties
Set your DB connection in `application.properties` or `application.yml`.

Example (application.properties):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

server.port=8080
```

If your project uses environment variables, set:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

---

## Run

### Run with Maven
```bash
mvn clean spring-boot:run
```

### Run with Maven Wrapper
```bash
./mvnw clean spring-boot:run
```

### Build and run jar
```bash
mvn clean package
java -jar target/*.jar
```

App default:
- Base URL: `http://localhost:8080`

---

## API Documentation (Swagger UI)
Swagger UI is available at:
- `http://localhost:8080/swagger-ui/index.html`

(OpenAPI JSON is typically at `/v3/api-docs`.)

---

## API Examples

Notes:
- Replace `http://localhost:8080` with your host/port.
- Payload fields may vary slightly depending on your entity model (e.g., `notes` vs `description`). Use swagger-ui to confirm.

### 1) Create an Expense
```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Groceries",
    "amount": 54.23,
    "currency": "USD",
    "category": "FOOD",
    "date": "2026-07-01",
    "paymentMethod": "CARD",
    "notes": "Weekly grocery run"
  }'
```

### 2) List Expenses (Filters + Sort + Search)
Typical query params:
- `from` / `to` (date range)
- `minAmount` / `maxAmount`
- `category`
- `q` (search in title/notes)
- `sort` (e.g., `date,desc` or `amount,asc`)
- `page` / `size` (pagination)

Examples:

**Filter by date range + category, sort newest first**
```bash
curl "http://localhost:8080/api/expenses?from=2026-07-01&to=2026-07-31&category=FOOD&sort=date,desc"
```

**Search by keyword**
```bash
curl "http://localhost:8080/api/expenses?q=grocery&sort=date,desc"
```

**Filter by amount range**
```bash
curl "http://localhost:8080/api/expenses?minAmount=10&maxAmount=100&sort=amount,asc"
```

**Paginated**
```bash
curl "http://localhost:8080/api/expenses?page=0&size=20&sort=date,desc"
```

### 3) Get Expense by ID
```bash
curl "http://localhost:8080/api/expenses/123"
```

### 4) Edit/Update an Expense
If your API uses PUT for full update:
```bash
curl -X PUT "http://localhost:8080/api/expenses/123" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Groceries (updated)",
    "amount": 60.00,
    "currency": "USD",
    "category": "FOOD",
    "date": "2026-07-01",
    "paymentMethod": "CARD",
    "notes": "Added a few items"
  }'
```

If your API supports PATCH for partial update:
```bash
curl -X PATCH "http://localhost:8080/api/expenses/123" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 60.00,
    "notes": "Adjusted total"
  }'
```

### 5) Delete an Expense
```bash
curl -X DELETE "http://localhost:8080/api/expenses/123"
```

---

## CSV Export

### Export Expenses to CSV
Common patterns:
- `GET /api/expenses/export` (returns `text/csv`)
- Or `GET /api/expenses.csv`

Example:
```bash
curl -L "http://localhost:8080/api/expenses/export?from=2026-07-01&to=2026-07-31&category=FOOD" \
  -H "Accept: text/csv" \
  -o expenses_july.csv
```

If your endpoint is:
```text
GET /api/expenses/export/csv
```
then:
```bash
curl -L "http://localhost:8080/api/expenses/export/csv?sort=date,desc" -o expenses.csv
```

Use swagger-ui to confirm the exact route.

---

## Category Summary (Aggregation)

Typical endpoint examples:
- `GET /api/reports/categories/summary`
- `GET /api/expenses/summary/by-category`

Example: totals by category for a date range
```bash
curl "http://localhost:8080/api/reports/categories/summary?from=2026-07-01&to=2026-07-31"
```

Example response shape (illustrative):
```json
[
  { "category": "FOOD", "total": 342.10 },
  { "category": "TRANSPORT", "total": 120.00 }
]
```

---

## Budgets & Alerts

Typical capabilities:
- Define a monthly budget per category (or overall).
- Alert when spending reaches a threshold (e.g., 80% of budget) or exceeds budget.

### Create/Update a Budget
Common patterns:
- `POST /api/budgets`
- `PUT /api/budgets/{id}`

Example (monthly category budget):
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Content-Type: application/json" \
  -d '{
    "period": "MONTHLY",
    "month": "2026-07",
    "category": "FOOD",
    "limit": 500.00,
    "currency": "USD",
    "alertThresholdPercent": 80
  }'
```

### List Budgets
```bash
curl "http://localhost:8080/api/budgets?month=2026-07"
```

### Check Budget Status / Alerts
Common patterns:
- `GET /api/budgets/status?month=YYYY-MM`
- `GET /api/alerts`
- `GET /api/budgets/{id}/status`

Example:
```bash
curl "http://localhost:8080/api/budgets/status?month=2026-07"
```

Example response shape (illustrative):
```json
{
  "month": "2026-07",
  "category": "FOOD",
  "limit": 500.00,
  "spent": 412.10,
  "remaining": 87.90,
  "alertThresholdPercent": 80,
  "alertTriggered": true,
  "overBudget": false
}
```

---

## Troubleshooting

### MySQL connection issues
- Ensure MySQL is running and reachable.
- Verify `spring.datasource.url`, username, password.
- Check timezone params and `allowPublicKeyRetrieval=true` for MySQL 8.

### Port already in use
- Change `server.port` in application properties or stop the conflicting service.

---

## Quick Links
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
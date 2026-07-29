# ExpenseTracker v1 — Spring Boot Implementation

ExpenseTracker v1 is a Spring Boot REST API for tracking personal expenses. It provides endpoints to create, view, update, and delete expenses, along with basic categorization and summary reporting.

## Features

- Create, read, update, and delete expenses (CRUD)
- Categorize expenses (e.g., Food, Travel, Utilities, Shopping)
- Filter and search expenses (by date range, category, amount range)
- Summary reporting (total spent, totals by category, totals by period)
- Input validation and consistent API error responses
- Swagger/OpenAPI documentation (if enabled in the project)
- Environment-based configuration via `application.yml` / `application.properties`

## Tech Stack

- Java (recommended: 17+)
- Spring Boot (Web, Validation)
- Spring Data (JPA) with an SQL database (H2 for local or Postgres/MySQL)
- Maven or Gradle build tool (as configured in the project)

## Running Locally

### Prerequisites

- Java 17+ installed
- Maven or Gradle installed (depending on the project setup)
- (Optional) Docker, if using a containerized database like Postgres

### 1) Configure Application

Update configuration in:

- `src/main/resources/application.properties` or
- `src/main/resources/application.yml`

Typical local configuration examples:

**H2 (in-memory)**
- URL: `jdbc:h2:mem:expensetracker`
- Console: `/h2-console` (if enabled)

**Postgres**
- URL: `jdbc:postgresql://localhost:5432/expensetracker`
- Username/password set via environment variables or config file

### 2) Run the Application

**Maven**
- `mvn spring-boot:run`

**Gradle**
- `./gradlew bootRun`

The API will be available at:

- `http://localhost:8080`

### 3) (Optional) Run Tests

**Maven**
- `mvn test`

**Gradle**
- `./gradlew test`

## API Documentation

If Swagger/OpenAPI is enabled, check one of the following (depends on configuration):

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API Endpoints

Base URL: `/api/v1`

### Health

- `GET /actuator/health` (if Spring Actuator is enabled)

### Expenses

#### Create an expense
- `POST /api/v1/expenses`

**Request body**
```json
{
  "title": "Lunch",
  "amount": 12.50,
  "currency": "USD",
  "category": "FOOD",
  "date": "2026-07-29",
  "notes": "Team lunch"
}
```

**Response (201)**
```json
{
  "id": "b1f6f1b0-1f2f-4a2f-9b7a-8f4b2d3d0a11",
  "title": "Lunch",
  "amount": 12.5,
  "currency": "USD",
  "category": "FOOD",
  "date": "2026-07-29",
  "notes": "Team lunch",
  "createdAt": "2026-07-29T12:00:00Z",
  "updatedAt": "2026-07-29T12:00:00Z"
}
```

#### Get expense by ID
- `GET /api/v1/expenses/{id}`

#### List expenses (with optional filters)
- `GET /api/v1/expenses`

**Optional query params**
- `fromDate` (e.g., `2026-07-01`)
- `toDate` (e.g., `2026-07-31`)
- `category` (e.g., `FOOD`)
- `minAmount` (e.g., `10`)
- `maxAmount` (e.g., `100`)
- `q` (search text in title/notes)
- `page`, `size`, `sort`

Example:
- `GET /api/v1/expenses?fromDate=2026-07-01&toDate=2026-07-31&category=FOOD&page=0&size=20&sort=date,desc`

#### Update an expense
- `PUT /api/v1/expenses/{id}`

**Request body**
```json
{
  "title": "Lunch (updated)",
  "amount": 13.00,
  "currency": "USD",
  "category": "FOOD",
  "date": "2026-07-29",
  "notes": "Included tip"
}
```

#### Partial update an expense
- `PATCH /api/v1/expenses/{id}`

Use this to update only specific fields.

#### Delete an expense
- `DELETE /api/v1/expenses/{id}`

### Categories (if implemented as a separate resource)

- `GET /api/v1/categories` — list supported categories

### Reports / Summaries

#### Get summary totals
- `GET /api/v1/reports/summary`

**Optional query params**
- `fromDate`
- `toDate`
- `groupBy` (e.g., `category`, `day`, `month`)

Example:
- `GET /api/v1/reports/summary?fromDate=2026-07-01&toDate=2026-07-31&groupBy=category`

**Response**
```json
{
  "currency": "USD",
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "total": 245.75,
  "breakdown": [
    { "key": "FOOD", "total": 120.25 },
    { "key": "TRAVEL", "total": 80.00 },
    { "key": "UTILITIES", "total": 45.50 }
  ]
}
```

## Error Handling

Common HTTP status codes:
- `400 Bad Request` — validation errors
- `404 Not Found` — resource not found
- `409 Conflict` — conflicts (if applicable)
- `500 Internal Server Error` — unexpected errors

Validation error response example:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "One or more fields are invalid.",
  "details": [
    { "field": "amount", "message": "must be greater than 0" }
  ]
}
```

## License

Internal/educational use unless otherwise specified.
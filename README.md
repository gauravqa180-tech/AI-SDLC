# Expense Tracker API (Spring Boot)

A RESTful Expense Tracker API built with Spring Boot that helps users record expenses, categorize them, and view summaries.

## Features

- Create, read, update, and delete expenses
- Categorize expenses
- Filter and list expenses
- Basic validation and error handling
- API-first design suitable for web/mobile clients

---

## Tech Stack

- Java 17+
- Spring Boot (Web, Validation)
- Maven
- H2 (in-memory) or any supported relational database (via Spring Data/JPA, if configured)
- Swagger/OpenAPI (if enabled in the project)

---

## Requirements

- Java 17 (or newer)
- Maven 3.8+
- Git

Optional:
- Docker (if you containerize the service)
- Postman / Insomnia / curl for testing endpoints

---

## Getting Started (Local)

### 1) Clone the repository

git clone <YOUR_REPO_URL>
cd <YOUR_PROJECT_FOLDER>

### 2) Configure environment (if applicable)

If the project uses an `application.yml` / `application.properties`, ensure database and server settings are correct.

Common defaults:
- Server port: 8080
- H2 console (if enabled): http://localhost:8080/h2-console

### 3) Build the application

mvn clean package

### 4) Run the application

Option A: Run via Maven
mvn spring-boot:run

Option B: Run the packaged JAR
java -jar target/*.jar

### 5) Verify the API is running

- Health check (if configured): http://localhost:8080/actuator/health
- API base URL: http://localhost:8080

---

## API Documentation

If OpenAPI/Swagger is enabled:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

---

## Implemented User Stories

1. As a user, I can create an expense with amount, description, date, and category.
2. As a user, I can view a list of all my expenses.
3. As a user, I can view details of a single expense by its ID.
4. As a user, I can update an existing expense.
5. As a user, I can delete an expense.
6. As a user, I can filter expenses by date range.
7. As a user, I can filter expenses by category.
8. As a user, I can view total expenses for a given period (if implemented in endpoints/service).

---

## Example Requests

### Create an Expense

POST /api/expenses
Content-Type: application/json

{
  "amount": 25.50,
  "description": "Lunch",
  "date": "2026-07-29",
  "category": "FOOD"
}

### List Expenses

GET /api/expenses

### Filter by Category

GET /api/expenses?category=FOOD

### Filter by Date Range

GET /api/expenses?from=2026-07-01&to=2026-07-31

---

## Contributing

- Create a feature branch
- Commit with clear messages
- Open a pull request with details of the change

---

## License

Add your license information here (e.g., MIT, Apache-2.0), or remove this section if not applicable.
# Expense Tracker API

A RESTful API for tracking personal expenses with secure authentication, categorization, and reporting capabilities.

## Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Maven / Gradle (project dependent)
- **Database:** PostgreSQL (recommended) / MySQL (optional)
- **ORM:** Spring Data JPA / Hibernate
- **Auth:** Spring Security + JWT
- **API Docs:** Swagger / OpenAPI

## Running Locally

### Prerequisites
- Java 17+ (or the version specified by the project)
- Maven or Gradle
- A running database instance (PostgreSQL recommended)

### Steps

1. **Clone the repository**
   - `git clone <repo-url>`
   - `cd <repo-folder>`

2. **Configure environment**
   Update `application.yml` / `application.properties` with your database credentials and JWT settings, for example:

   - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/expense_tracker`
   - `SPRING_DATASOURCE_USERNAME=postgres`
   - `SPRING_DATASOURCE_PASSWORD=postgres`
   - `JWT_SECRET=<your-secret>`
   - `JWT_EXPIRATION_MS=86400000`

3. **Run database migrations (if applicable)**
   - If Flyway/Liquibase is used, migrations will run automatically on startup.

4. **Start the application**
   - Maven: `mvn spring-boot:run`
   - Gradle: `./gradlew bootRun`

5. **Verify the server**
   - API base URL: `http://localhost:8080`

## Swagger / OpenAPI

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

## Key Endpoints

### Authentication
- `POST /api/auth/register` — Register a new user
- `POST /api/auth/login` — Authenticate and receive JWT

### Expenses
- `GET /api/expenses` — List expenses (supports filtering/pagination if implemented)
- `POST /api/expenses` — Create a new expense
- `GET /api/expenses/{id}` — Get expense details
- `PUT /api/expenses/{id}` — Update an expense
- `DELETE /api/expenses/{id}` — Delete an expense

### Categories
- `GET /api/categories` — List categories
- `POST /api/categories` — Create a category
- `PUT /api/categories/{id}` — Update a category
- `DELETE /api/categories/{id}` — Delete a category

### Reports (if implemented)
- `GET /api/reports/summary` — Summary totals by date range/category
- `GET /api/reports/monthly` — Monthly breakdown

## Notes

- Most endpoints require an `Authorization: Bearer <token>` header after login.
- Use Swagger UI to explore request/response models and try endpoints interactively.
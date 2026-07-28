# Spring Boot Expense Tracker API

A RESTful API for tracking personal expenses, categories, and budgets. This service is built with Spring Boot and includes interactive API documentation via Swagger UI.

---

## Tech Stack

- **Java 17+**
- **Spring Boot** (Web, Validation)
- **Spring Data JPA**
- **Hibernate**
- **H2 (local/dev) or PostgreSQL/MySQL (optional)**
- **Springdoc OpenAPI** (Swagger UI)
- **Maven** (build tool)

---

## Run Locally

### Prerequisites
- Java 17 (or newer)
- Maven 3.8+ (or use the Maven Wrapper if included)

### Steps

1. **Clone the repository**
   - `git clone <your-repo-url>`
   - `cd <your-repo-folder>`

2. **Build the project**
   - `mvn clean package`

3. **Run the application**
   - `mvn spring-boot:run`
   - Or run the jar:
     - `java -jar target/*.jar`

4. **Application URL**
   - API base URL: `http://localhost:8080`

---

## Swagger / OpenAPI

Swagger UI is available at:

- `http://localhost:8080/swagger-ui.html`
- (Depending on configuration, it may also be available at) `http://localhost:8080/swagger-ui/index.html`

OpenAPI spec (if enabled):

- `http://localhost:8080/v3/api-docs`

---

## Key Endpoints

> Note: Exact paths may vary based on implementation. Check Swagger UI for the authoritative list.

### Expenses
- `GET /api/expenses` — List expenses (optionally filter by date/category)
- `GET /api/expenses/{id}` — Get expense by id
- `POST /api/expenses` — Create a new expense
- `PUT /api/expenses/{id}` — Update an existing expense
- `DELETE /api/expenses/{id}` — Delete an expense

### Categories
- `GET /api/categories` — List categories
- `GET /api/categories/{id}` — Get category by id
- `POST /api/categories` — Create a category
- `PUT /api/categories/{id}` — Update a category
- `DELETE /api/categories/{id}` — Delete a category

### Budgets (optional, if implemented)
- `GET /api/budgets` — List budgets
- `POST /api/budgets` — Create/update a budget
- `GET /api/budgets/summary` — Budget/expense summary (aggregations)

### Health (if enabled)
- `GET /actuator/health` — Service health check

---

## Notes

- For local development, an in-memory **H2** database is commonly used; data resets on restart unless configured otherwise.
- Use Swagger UI to explore and test requests quickly.
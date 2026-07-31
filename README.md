# Spring Boot Expense Tracker

Spring Boot app (Java 17) with MySQL persistence and Swagger/OpenAPI documentation.

## Prerequisites

- Java 17
- Maven 3.9+
- MySQL 8+

## Configure MySQL

1. Create a database (example):

```sql
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Create a user (example):

```sql
CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
```

3. Configure Spring datasource

Set the following in `src/main/resources/application.properties` (or `application.yml`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=UTC
spring.datasource.username=expense_user
spring.datasource.password=expense_password

# Typical Hibernate settings (adjust if your project differs)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## Run the app (Java 17)

### Using Maven

```bash
mvn clean spring-boot:run
```

### Or build a jar and run

```bash
mvn clean package
java -jar target/*.jar
```

The app will start on the configured port (commonly `http://localhost:8080`).

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API Endpoints

> Base URL examples below assume `http://localhost:8080`.

### Expenses (CRUD + edit + soft delete/undo + list/filter + monthly total)

- **Create expense**
  - `POST /api/expenses`

- **Get expense by id**
  - `GET /api/expenses/{id}`

- **Update/edit expense**
  - `PUT /api/expenses/{id}` (or `PATCH /api/expenses/{id}` depending on implementation)

- **Soft delete expense**
  - `DELETE /api/expenses/{id}` (soft delete)

- **Undo soft delete**
  - `POST /api/expenses/{id}/undo-delete` (restores a soft-deleted expense)

- **List expenses (month/search/filters/sort)**
  - `GET /api/expenses`
  - Supported query params (as implemented):
    - `month` (e.g. `2026-07` or numeric month) — filter by month
    - `search` — free-text search (e.g., description/notes)
    - Additional filters (commonly):
      - `categoryId`
      - `minAmount`
      - `maxAmount`
      - `fromDate`
      - `toDate`
      - `includeDeleted` (true/false)
    - Sorting (commonly):
      - `sort` (e.g. `date,desc` or `amount,asc`)

- **Monthly total**
  - `GET /api/expenses/monthly-total`
  - Common query params:
    - `month` (e.g. `2026-07`)

### Categories (CRUD + reassign delete)

- **Create category**
  - `POST /api/categories`

- **List categories**
  - `GET /api/categories`

- **Get category by id**
  - `GET /api/categories/{id}`

- **Update category**
  - `PUT /api/categories/{id}`

- **Delete category (with reassignment)**
  - `DELETE /api/categories/{id}?reassignToCategoryId={targetCategoryId}`
  - Deletes the category and reassigns existing expenses to the target category.

## Notes

- For the exact request/response schemas and the definitive list of query parameters, see Swagger UI.
- If your project uses a non-default port or context path, adjust the URLs accordingly.

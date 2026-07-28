# Expense Tracker v1 (Spring Boot) — Enhancements

Spring Boot backend for an Expense Tracker (v1 enhancements). This project provides REST APIs to manage expenses, categories, budgets, basic reports, and CSV import/export.  
**Note:** Authentication/user accounts are **out of scope**. The application is **single-tenant** (no per-user separation).

---

## Requirements

- Java 17+ (recommended: 17)
- Maven 3.8+
- MySQL 8.x (or compatible MySQL server)
- (Optional) An IDE (IntelliJ IDEA / Eclipse) for development

---

## Tech Stack

- Spring Boot (Web, Validation)
- Spring Data JPA / Hibernate
- MySQL
- OpenAPI/Swagger UI (springdoc)

---

## Running Locally

### 1) Create the database

Create the database in MySQL:

CREATE DATABASE expense_tracker;

(Optionally create a dedicated MySQL user and grant permissions.)

---

### 2) Configure application.yml

Update `src/main/resources/application.yml` with your MySQL connection settings. Example:

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    open-in-view: false

server:
  port: 8080

> If the project uses Flyway/Liquibase migrations, ensure those are enabled and configured accordingly.

---

### 3) Run the application

From the project root:

mvn spring-boot:run

The application will start on:

http://localhost:8080

---

## Swagger / OpenAPI

Swagger UI:

http://localhost:8080/swagger-ui/index.html

OpenAPI JSON:

http://localhost:8080/v3/api-docs

---

## API Overview (Key Endpoints)

Base path may vary depending on configuration, but typical endpoints are:

### Expenses
- GET /api/expenses  
  List expenses (often supports filters like date range, category, pagination—implementation dependent)
- GET /api/expenses/{id}  
  Get expense by ID
- POST /api/expenses  
  Create a new expense
- PUT /api/expenses/{id}  
  Update an expense
- DELETE /api/expenses/{id}  
  Delete an expense

### Categories
- GET /api/categories  
  List categories
- GET /api/categories/{id}  
  Get category by ID
- POST /api/categories  
  Create category
- PUT /api/categories/{id}  
  Update category
- DELETE /api/categories/{id}  
  Delete category

### Reports
- GET /api/reports/summary  
  Summary metrics (e.g., totals by period)
- GET /api/reports/by-category  
  Aggregation by category for a given range
- GET /api/reports/monthly  
  Monthly breakdown

> Exact report query parameters depend on the implemented controllers (check Swagger for the definitive contract).

### Budgets
- GET /api/budgets  
  List budgets
- GET /api/budgets/{id}  
  Get budget by ID
- POST /api/budgets  
  Create a budget (e.g., category + limit + period)
- PUT /api/budgets/{id}  
  Update a budget
- DELETE /api/budgets/{id}  
  Delete a budget

### CSV Import/Export
- GET /api/expenses/export/csv  
  Export expenses to CSV
- POST /api/expenses/import/csv  
  Import expenses from CSV (multipart upload)

---

## Notes / Scope

- **Single-tenant:** all data is considered part of a single shared tenant; there is no per-user partitioning.
- **Auth out of scope:** no login/registration, roles, or access control in v1 enhancements.
- For the most accurate request/response schema and parameters, consult Swagger UI.

---
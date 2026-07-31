# Spring Boot Expense Tracker

A simple **Expense Tracker** REST API built with **Spring Boot**. It allows you to create, view, update, and delete expenses.

---

## Tech Stack

- Java
- Spring Boot
- Spring Web (REST)
- Spring Data JPA
- H2 (in-memory) or any supported relational DB

---

## Prerequisites

- Java 17+ (recommended)
- Maven 3.8+

---

## How to Run

### 1) Run with Maven

mvn clean spring-boot:run

### 2) Run as a packaged JAR

mvn clean package
java -jar target/*.jar

---

## Configuration

By default, the application can run with an in-memory H2 database (if included). If you are using a different database, configure it in:

- src/main/resources/application.properties (or application.yml)

---

## API Endpoints

Base URL: http://localhost:8080

### 1) Create an Expense

- Method: POST
- Path: /api/expenses
- Content-Type: application/json

Request body example:
{
  "title": "Groceries",
  "amount": 45.25,
  "category": "FOOD",
  "date": "2026-07-31",
  "notes": "Weekly shopping"
}

### 2) Get All Expenses

- Method: GET
- Path: /api/expenses

Optional query params (if supported by your implementation):
- category
- fromDate
- toDate

Example:
GET /api/expenses

### 3) Get Expense by ID

- Method: GET
- Path: /api/expenses/{id}

Example:
GET /api/expenses/1

### 4) Update an Expense

- Method: PUT
- Path: /api/expenses/{id}
- Content-Type: application/json

Request body example:
{
  "title": "Groceries (updated)",
  "amount": 50.00,
  "category": "FOOD",
  "date": "2026-07-31",
  "notes": "Added extra items"
}

### 5) Delete an Expense

- Method: DELETE
- Path: /api/expenses/{id}

Example:
DELETE /api/expenses/1

---

## Health Check (if enabled)

- Method: GET
- Path: /actuator/health

---

## Notes

- Ensure your database configuration is correct before running in non-H2 mode.
- Port defaults to 8080 unless overridden in application properties.
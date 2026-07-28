# Spring Boot Expense Tracker v1

A simple RESTful Expense Tracker built with Spring Boot and MySQL.

## Prerequisites
- Java 17+ (or project-defined Java version)
- Maven 3.8+
- MySQL 8+
- (Optional) cURL or Postman for API testing

## MySQL Setup
1. Create database:
   CREATE DATABASE expense_tracker;

2. Create a MySQL user (optional) and grant access:
   CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
   GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
   FLUSH PRIVILEGES;

3. Configure application properties:
   Update `src/main/resources/application.properties` (or `application.yml`) with your DB settings, e.g.:
   spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=UTC
   spring.datasource.username=expense_user
   spring.datasource.password=expense_password
   spring.jpa.hibernate.ddl-auto=update

## Run the Application
- Build:
  mvn clean package

- Run:
  mvn spring-boot:run

Or run the jar:
  java -jar target/*.jar

Default server (unless configured otherwise):
- http://localhost:8080

## OpenAPI / Swagger UI
If OpenAPI is enabled, Swagger UI is typically available at:
- http://localhost:8080/swagger-ui/index.html

(Your project may also expose API docs at `/v3/api-docs`.)

## API Usage (cURL)

Assumptions (adjust paths/fields to match your API):
- Base URL: http://localhost:8080/api/v1
- Resource: /expenses
- Common fields: description, amount, category, date (YYYY-MM-DD)

### Create Expense
curl -X POST "http://localhost:8080/api/v1/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Groceries",
    "amount": 42.50,
    "category": "FOOD",
    "date": "2026-07-01"
  }'

### Get Expense by ID
curl -X GET "http://localhost:8080/api/v1/expenses/1"

### Update Expense
curl -X PUT "http://localhost:8080/api/v1/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Groceries (updated)",
    "amount": 45.00,
    "category": "FOOD",
    "date": "2026-07-01"
  }'

### Delete Expense
curl -X DELETE "http://localhost:8080/api/v1/expenses/1"

### List Expenses (all)
curl -X GET "http://localhost:8080/api/v1/expenses"

### List with Filters
Common examples (depending on implementation):

- By category:
  curl -X GET "http://localhost:8080/api/v1/expenses?category=FOOD"

- By date range:
  curl -X GET "http://localhost:8080/api/v1/expenses?from=2026-07-01&to=2026-07-31"

- By minimum/maximum amount:
  curl -X GET "http://localhost:8080/api/v1/expenses?minAmount=10&maxAmount=100"

- Combined:
  curl -X GET "http://localhost:8080/api/v1/expenses?category=FOOD&from=2026-07-01&to=2026-07-31&minAmount=10"

### Monthly Total
Example query for a month total (adjust endpoint/params to match your API):

- Using year/month params:
  curl -X GET "http://localhost:8080/api/v1/expenses/total/monthly?year=2026&month=7"

Or if your API uses a single YYYY-MM param:
  curl -X GET "http://localhost:8080/api/v1/expenses/total/monthly?month=2026-07"
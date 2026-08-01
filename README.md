# Expense Tracker v1 (Spring Boot + MySQL)

Expense Tracker v1 is a Spring Boot backend service for managing expenses. It exposes REST APIs for creating, listing, updating, and deleting expenses, backed by a MySQL database.

## Implemented User Story #1: Edit Expense

You can edit an existing expense by ID using:

- `PUT /api/expenses/{id}`

The request body contains updated fields (e.g., amount, description, date). The API returns the updated expense.

---

## Prerequisites

- Java 17
- Maven
- Docker (optional; required only if you want to run tests with Testcontainers)

---

## Local MySQL Setup

### Option A: Run MySQL with Docker

docker run --name expense-tracker-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=expense_tracker \
  -e MYSQL_USER=expense_user \
  -e MYSQL_PASSWORD=expense_password \
  -p 3306:3306 \
  -d mysql:8

### Environment Variables

Export environment variables for the application to connect to MySQL:

export DB_URL=jdbc:mysql://localhost:3306/expense_tracker
export DB_USERNAME=expense_user
export DB_PASSWORD=expense_password

If your application uses Spring Boot standard variables instead, you can also use:

export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/expense_tracker
export SPRING_DATASOURCE_USERNAME=expense_user
export SPRING_DATASOURCE_PASSWORD=expense_password

---

## How to Run

mvn spring-boot:run

---

## How to Run Tests

Tests use Testcontainers to provision an ephemeral MySQL instance automatically.

mvn test

Notes:
- Docker must be installed and running to execute tests.
- The first run may take longer while pulling Docker images.

---

## API Endpoints

- Create expense: `POST /api/expenses`
- List expenses: `GET /api/expenses`
- Update expense (Edit): `PUT /api/expenses/{id}`
- Delete expense: `DELETE /api/expenses/{id}`
- Monthly total: `GET /api/expenses/monthly-total?year=YYYY&month=M`

---

## Example cURL

### Create an Expense

curl -X POST "http://localhost:8080/api/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 12.50,
    "description": "Lunch",
    "date": "2026-08-01"
  }'

### Update an Expense

curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15.75,
    "description": "Lunch (updated)",
    "date": "2026-08-01"
  }'
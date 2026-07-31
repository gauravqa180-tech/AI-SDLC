# ExpenseTracker v1 — Spring Boot + MySQL

ExpenseTracker v1 is a simple RESTful backend service built with Spring Boot and MySQL to manage expenses and generate basic monthly reports.

## Tech Stack

- Java (JDK 17+ recommended)
- Spring Boot
- Spring Web
- Spring Data JPA (Hibernate)
- MySQL
- Maven or Gradle

## Requirements

- Java 17+ installed (`java -version`)
- MySQL 8+ installed and running
- Maven (`mvn -v`) or Gradle available
- A MySQL database created for the application (example: `expense_tracker`)

## Configuration

Database configuration is located in:

- `src/main/resources/application.yml`

Typical settings include:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### Hibernate DDL

This project uses:

- `spring.jpa.hibernate.ddl-auto=update`

This will automatically create/update tables based on your JPA entities.  
Note: In production environments, consider using migrations (Flyway/Liquibase) and avoid `update` unless you fully understand the implications.

## How to Run

### 1) Create the Database

In MySQL:

- Create a database (example):

  - `expense_tracker`

### 2) Update `application.yml`

Set your MySQL connection values:

- URL (database name, host/port)
- Username
- Password

### 3) Run the Application

Using Maven:

- `mvn spring-boot:run`

Or build and run:

- `mvn clean package`
- `java -jar target/*.jar`

Using Gradle:

- `./gradlew bootRun`

The application will start on the configured port (commonly `8080`).

## API Endpoints

Base path: `/api`

### Expenses

#### Create Expense
- **POST** `/api/expenses`
- Description: Create a new expense record.

#### List Expenses
- **GET** `/api/expenses`
- Description: Retrieve all expenses.

#### Update Expense
- **PUT** `/api/expenses/{id}`
- Description: Update an existing expense by ID.

#### Delete Expense
- **DELETE** `/api/expenses/{id}`
- Description: Delete an expense by ID.

### Reports

#### Monthly Total
- **GET** `/api/reports/monthly-total?month=YYYY-MM`
- Description: Returns the total amount of expenses for the given month (format `YYYY-MM`).

Example:

- `GET /api/reports/monthly-total?month=2026-07`

## Notes

- Ensure MySQL is running before starting the application.
- If you change entity fields, `ddl-auto=update` may alter the schema automatically; review schema changes carefully.
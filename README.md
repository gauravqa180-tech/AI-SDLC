# Expense Tracker vNext (Spring Boot + MySQL)

Expense Tracker vNext is a Spring Boot backend application backed by MySQL, intended as a practical exercise for the AI-SDLC workflow.

## Requirements

- Java 21
- Maven 3.9+
- MySQL 8.x (or compatible)

## Run locally

### 1) Configure MySQL

Create a database (example):

- Database name: `expense_tracker`

Create a user and grant permissions as needed.

### 2) Configure application properties

Set your datasource configuration (typical Spring Boot properties):

- `spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker`
- `spring.datasource.username=YOUR_USERNAME`
- `spring.datasource.password=YOUR_PASSWORD`

If you use environment variables, export them before running (optional):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### 3) Build & run

From the project root:

- Build:
  - `mvn clean package`
- Run:
  - `mvn spring-boot:run`

Alternatively, run the packaged jar (if applicable):

- `java -jar target/*.jar`

The application will start on the configured port (commonly `8080`).

## Swagger UI

Once running locally, open:

- http://localhost:8080/swagger-ui/index.html

(If your server port differs, replace `8080` accordingly.)

## Testing

Run tests with:

- `mvn test`

## Notes (AI-SDLC exercise)

This repository is used to practice an AI-assisted SDLC workflow (planning → implementation → verification). Changes are expected to be iterative, with an emphasis on clear requirements, small safe steps, and repeatable verification (build/tests) after each change.
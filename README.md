# Expense Tracker API (Spring Boot)

A RESTful Expense Tracker API built with Spring Boot to help users record, categorize, and track expenses. It provides endpoints for managing expenses and categories, and includes interactive API documentation via Swagger UI.

## Features

- Create, read, update, and delete expenses
- Categorize expenses
- Filter and query expenses (e.g., by date range/category) *(if implemented in the API)*
- API documentation with Swagger UI / OpenAPI

## Tech Stack

- Java 17+ (recommended)
- Spring Boot (Web, Validation)
- Spring Data JPA (if persistence is included)
- Database: H2 (dev) / PostgreSQL or MySQL (production) *(depending on configuration)*
- Maven or Gradle
- Swagger/OpenAPI (springdoc-openapi)

## Getting Started

### Prerequisites

- Java 17+ installed
- Maven or Gradle installed (depending on project setup)

### Run the Application

#### Using Maven
1. Install dependencies and run:
   - `mvn spring-boot:run`

#### Using Gradle
1. Run:
   - `./gradlew bootRun`

### Run as a Jar

1. Build:
- Maven: `mvn clean package`
- Gradle: `./gradlew clean build`

2. Run:
- `java -jar target/*.jar` (Maven)
- `java -jar build/libs/*.jar` (Gradle)

## Configuration

Application configuration is typically located in:

- `src/main/resources/application.yml` or `src/main/resources/application.properties`

If using a database other than H2, set:

- datasource URL, username, password
- JPA/Hibernate settings
- server port (optional)

## Swagger / OpenAPI Documentation

After starting the application, access Swagger UI at:

- http://localhost:8080/swagger-ui/index.html

OpenAPI JSON (if enabled):

- http://localhost:8080/v3/api-docs

## Health Check

If Spring Boot Actuator is enabled, common endpoints include:

- http://localhost:8080/actuator/health

## Notes

- Default server port is `8080` unless overridden.
- If you run into port conflicts, change `server.port` in your application configuration.
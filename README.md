# Expense Tracker (v1)

Spring Boot + Maven + MySQL reference implementation for the ExpenseTracker_v1 stories.

## Run locally

### Prerequisites
- Java 17+ (JDK)
- Maven 3.8+
- MySQL 8.x (running locally or reachable from your machine)
- (Optional) An IDE such as IntelliJ IDEA or VS Code

### Configure the application
Update `src/main/resources/application.yml` (or `application.properties` if used) with your MySQL connection details.

Example `application.yml`:

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: your_mysql_user
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
    show-sql: false

server:
  port: 8080

Notes:
- Create the database referenced in the JDBC URL (example: `expense_tracker`) if it does not already exist.
- `ddl-auto: update` is convenient for local development. Adjust as needed for your environment.

### Build and run
From the project root:

1) Build (optional but recommended):
mvn clean package

2) Run:
mvn spring-boot:run

The app will start on:
http://localhost:8080

## OpenAPI / Swagger UI
After the app is running, open:
http://localhost:8080/swagger-ui/index.html

(If your configuration uses a different port, replace `8080` accordingly.)
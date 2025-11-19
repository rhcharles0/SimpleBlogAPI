🌟 [SimpleBlogAPI] - TDD-Based Simple User REST API

🎯 Project Overview

This project is a RESTful API backend service implemented using Spring Boot to realize simple user (member) management functionality. Specifically, Test-Driven Development (TDD) principles were strictly applied from the design stage to aim for robust and maintainable code.
Test code drives every layer, from external request validation and business logic to data persistence.

📋 Prerequisites

Ensure you have the following software installed on your system:

+ Java Development Kit (JDK) 25: The project is built on Java 25.
+ Git: For cloning the repository.
+ Docker & Docker Compose: Required to run the PostgreSQL database container automatically via Spring Boot's Docker Compose support.
+ Maven or Gradle: The project uses a build tool (assuming Gradle based on common Spring Boot practices).


🛠️ Setup and Execution
1. Clone the Repository\
    Clone the source code from your Git repository:
    ```Bash
    git clone https://github.com/rhcharles/SimpleBlogAPI.git
    cd SimpleBlogAPI
    ```

2. Build the Application\
Use your build tool to compile the project and download all dependencies.

    Using Gradle (Recommended for Spring Boot):
    ```Bash
    ./gradlew clean build -x test
    ./gradlew bootRun
    ```
🛠️ Core Technologies and Design Features

1. Test-Driven Development (TDD) Principles

+ Controller Layer Test: Uses @WebMvcTest and MockMvc to thoroughly verify external request-response validation and exception handling (@ControllerAdvice) logic.
+ Service Layer Test: Utilizes Mockito to isolate the Repository layer and verify the accuracy of pure business logic.

2. Exception Handling and Validation

+ Custom Exception & ErrorCode: All business exceptions are defined as custom exceptions inheriting from RestApiException. HTTP status codes (404, 409, etc.) and response messages are uniformly managed via the ErrorCode Enum.
+ DTO Validation: Applies @Valid to DTOs upon Controller entry to preemptively block requests with invalid formats, returning a 400 Bad Request response that includes a list of detailed errors.

3. Technology Stack

+ Backend: Java 25, Spring Boot 3.x, Spring Boot Validation Starter, Jackson Databind 2.20.0
+ ORM: Spring Data JPA
+ Testing: JUnit 5, Mockito, Spring Boot Test, Testcontainers (for PostgreSQL)
+ Database: PostgreSQL (runtime)
+ Development: Spring Boot Docker Compose Support
  📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

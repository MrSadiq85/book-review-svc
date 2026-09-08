# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**book-review-svc** is a Spring Boot 4.1.1 REST API service built with Java 25 that provides book review endpoints. The API uses JSON files stored in `src/main/resources` as a mock database and exposes Swagger/OpenAPI documentation for interactive API testing.

**Key Design Principles:**
- No authentication required on any endpoints
- JSON files in `src/main/resources` act as the persistent data store
- Swagger UI auto-generated from endpoint annotations for local development (accessible at `http://localhost:8080/swagger-ui/index.html`)
- Stateless REST architecture

## Technology Stack

- **Framework:** Spring Boot 4.1.1
- **Build Tool:** Gradle with Java 25 toolchain
- **JSON Processing:** Jackson (Jackson Databind)
- **Documentation:** Springdoc OpenAPI Starter WebMVC UI 2.5.0
- **Boilerplate Reduction:** Lombok
- **Testing:** Spring Boot Test (JUnit 5, Mockito, AssertJ)

## Build & Run Commands

```bash
# Build the project
./gradlew build

# Run the application locally
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests ClassName

# Run a specific test method
./gradlew test --tests ClassName.methodName

# Clean build artifacts
./gradlew clean

# Check dependencies
./gradlew dependencies
```

## Project Structure

```
src/main/
├── java/com/epam/book_review_svc/
│   ├── BookReviewSvcApplication.java       # Spring Boot entry point
│   ├── controller/                         # REST controllers (endpoints)
│   ├── service/                            # Business logic & JSON file operations
│   └── model/                              # POJOs/DTOs (likely annotated with Lombok)
├── resources/
│   ├── application.properties              # Spring configuration
│   └── data/                               # JSON data files (books.json, reviews.json, etc.)
src/test/
└── java/com/epam/book_review_svc/          # Integration and unit tests
```

**Note on Package Name:** The package uses underscores (`book_review_svc`) instead of hyphens due to Java package naming conventions.

## JSON Data Store Pattern

- JSON files are stored in `src/main/resources/data/`
- Service layer classes read/write these files using Jackson's `ObjectMapper`
- Each JSON file represents a collection (e.g., `books.json` contains an array of book objects)
- No database migrations or ORM layer; all data operations are file-based

**Example Pattern:**
```java
// In a Service class
ObjectMapper mapper = new ObjectMapper();
List<Book> books = Arrays.asList(mapper.readValue(
    new File("classpath:data/books.json"), 
    Book[].class
));
```

## Swagger/OpenAPI Configuration

- Swagger UI is automatically enabled via the Springdoc dependency
- Endpoints are documented using `@Operation`, `@Tag`, and `@Schema` annotations
- No authentication is enforced locally; all endpoints are public
- Access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

When adding new endpoints, annotate them to ensure they appear in Swagger:
```java
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book management APIs")
public class BookController {
    
    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve all available books")
    public List<Book> getAllBooks() { ... }
}
```

## Workflow for New Features

1. **Define the Model/DTO** - Add a Lombok-annotated POJO in `model/`
2. **Create Service Layer** - Add file I/O logic to read/write JSON in `service/`
3. **Build REST Controller** - Add endpoints with Swagger annotations in `controller/`
4. **Test Locally** - Run `./gradlew bootRun` and verify via Swagger UI at `http://localhost:8080/swagger-ui/index.html`
5. **Write Integration Tests** - Add test cases in `src/test/`

## Key Constraints & Decisions

- **No database:** All persistence is file-based JSON in resources folder
- **No authentication:** Endpoints are open; authorization is not implemented
- **No API versioning:** Single version assumed for initial implementation
- **Stateless:** Each request is independent; no server-side session state
- **JSON files are read on startup:** Consider caching strategy for performance if data grows large

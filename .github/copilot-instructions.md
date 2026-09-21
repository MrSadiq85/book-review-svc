# GitHub Copilot Instructions for book-review-svc

## Project Overview
- **Type:** Spring Boot 4.1.1 REST API service (Java 25) with file-based JSON persistence.
- **Package Name:** `com.epam.book_review_svc` (note underscores instead of hyphens).
- **Authentication:** None; all endpoints are completely public.
- **Documentation:** Swagger/OpenAPI auto-generated (`http://localhost:8080/swagger-ui/index.html`).

## Tech Stack & Conventions
- **Framework & Build:** Spring Boot 4.1.1, Gradle (Java 25 toolchain), Lombok.
- **Data Layer:** Jackson (`ObjectMapper`) reading/writing JSON files in `src/main/resources/data/`. No database/ORM.
- **Testing:** Spring Boot Test (JUnit 5, Mockito, AssertJ).

## Project Structure
- `controller/` — REST controllers with Swagger annotations (`@Operation`, `@Tag`).
- `service/` — Business logic & JSON file I/O.
- `model/` — Lombok-annotated POJOs/DTOs.
- `src/main/resources/data/` — Mock database JSON files.

## Common Build Commands
- **Build:** `./gradlew build`
- **Run:** `./gradlew bootRun`
- **Test:** `./gradlew test` (Single test: `./gradlew test --tests ClassName`)

## Adding New Features
1. **Model:** Create a Lombok-annotated POJO in `model/`.
2. **Service:** Implement Jackson file-reading/writing logic in `service/`.
3. **Controller:** Expose endpoints in `controller/` with Springdoc OpenAPI annotations.
4. **Test:** Add integration/unit tests under `src/test/`.
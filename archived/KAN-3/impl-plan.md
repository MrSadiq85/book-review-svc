# Implementation Plan

## Executive Summary

KAN-3 is a small, public, unauthenticated Spring Boot REST API for managing books in a local JSON-backed store. The current repo is only the Spring Boot application bootstrap, so the implementation plan assumes building the service from the existing Java 25 / Spring Boot 4.1.1 foundation and aligning with the requirements and architecture in `working/requirements.md` and `working/architecture.md`.

The work should be implemented in four focused phases: foundation/configuration, domain and validation, persistence and service logic, and API/error handling with tests. The critical path is: configure the app -> define canonical book DTO/model and validation rules -> build file-backed repository with atomic writes -> implement service invariants -> expose controller and JSON error handling -> validate via integration tests.

## Task Breakdown Table

| Phase | Task | File-level focus | Dependencies | Complexity |
|---|---|---|---|---|
| 1. Foundation | Configure Spring Boot app, public access, and local JSON file settings | `src/main/resources/application.yml`, `src/main/java/com/epam/book_review_svc/config/SecurityConfig.java` | None | M |
| 1. Foundation | Define standard error envelope and API health baseline | `src/main/java/com/epam/book_review_svc/exception/ApiError.java`, `src/main/java/com/epam/book_review_svc/exception/GlobalExceptionHandler.java` | Phase 1 config; controller contract | M |
| 2. Domain & Validation | Model book resource + DTOs and Bean Validation | `src/main/java/com/epam/book_review_svc/model/dto/*RequestDto.java`, `*ResponseDto.java`, `src/main/java/com/epam/book_review_svc/model/Book.java` | Phase 1 config | M |
| 2. Domain & Validation | Normalize ISBN and enforce uniqueness rules | same DTO package + service helper methods | Domain model complete | M |
| 3. Persistence | Create JSON file repository and startup snapshot load | `src/main/java/com/epam/book_review_svc/repository/JsonFileBookRepository.java` | DTO model + file path config | H |
| 3. Persistence | Implement atomic write, lock, rollback-safe file replacement, and reload behavior | same repository file | Repository skeleton | H |
| 4. Service API | Implement resource rules for GET/PUT/DELETE | `src/main/java/com/epam/book_review_svc/service/BookService.java` | Repository + DTO validation | H |
| 4. Service API | Enforce not-found, duplicate ISBN, and immutable snapshot semantics | same service file | Repository and validation rules | H |
| 5. API Layer | Implement controller endpoints and content negotiation | `src/main/java/com/epam/book_review_svc/controller/BookController.java` | Service API and error envelope | M |
| 5. API Layer | Wire public HTTP mappings for `/api/v1/books` and `/api/v1/books/{id}` | same controller file | Phase 4 service completion | M |
| 6. Verification | Add JUnit 5 / MockMvc tests for success and failure flows | `src/test/java/com/epam/book_review_svc/...` | All implementation layers complete | H |
| 6. Verification | Run full Gradle verification and fix regressions | `build.gradle`, test classes | Feature implementation | M |

## Dependency Graph

```text
Requirements + Architecture
        |
        v
Configuration / Security / App properties
        |
        v
Book DTO + validation model
        |
        +----> ISBN normalization rules
        |
        v
JsonFileBookRepository (load + atomic replace + lock)
        |
        v
BookService (lookup, mutation invariants, copy-on-write)
        |
        v
BookController + GlobalExceptionHandler
        |
        v
Integration tests and Gradle verification
```

Critical dependency note: the repository must be stable before the service can safely do replacement and delete flows. Controller tests must wait until the service contract and exception mapping are in place.

## Blocked Tasks List

- Task: Create repository snapshot and atomic file writes
  - Blocked by: canonical DTO shape and final file path configuration
  - Reason: the repository is responsible for full-file replacement and must know the exact JSON schema and file contract.

- Task: `PUT` replacement validation and duplicate ISBN checks
  - Blocked by: DTO validation rules and ISBN normalization choice
  - Reason: uniqueness comparisons must use identical normalization across the app.

- Task: Controller-level JSON errors and content negotiation
  - Blocked by: exception mapping contract and stable public error codes
  - Reason: the controller should return a consistent response envelope instead of framework-generated errors.

- Task: Full integration verification
  - Blocked by: repository + service + controller layers all complete and stable
  - Reason: end-to-end tests rely on the exact persistence semantics and public error contract.

## Critical Path

1. Confirm application configuration and public API security policy.
2. Define book DTOs and validation rules (`id`, `isbn`, `title`, `author`).
3. Build the JSON repository snapshot and atomic replace logic.
4. Implement `BookService` rules for GET, PUT, and DELETE, including duplicate ISBN checks and missing resource handling.
5. Add `BookController` endpoints and sanitized JSON exception handling.
6. Validate through targeted tests and final Gradle verification.

This is the shortest path to a working implementation that satisfies the KAN-3 requirements without introducing unneeded complexity.

## Parallel Workstreams

### Workstream A: Persistence and domain safety
- `JsonFileBookRepository`
- ISBN normalization logic
- in-memory snapshot management
- atomic file replacement and rollback-safe write strategy

### Workstream B: API contract and error handling
- `BookController`
- `GlobalExceptionHandler`
- `ApiError` schema and HTTP status mapping
- `application/json` validation and media-type failures

### Workstream C: Testing and regression
- repository tests for file corruption/atomicity cases
- service tests for not-found and duplicate ISBN behavior
- controller tests for status codes and error envelopes
- full `./gradlew test` verification

These workstreams can overlap after the initial configuration phase; however, the repository and validation rules must be complete before controller/error tests are considered stable.

## Validation Strategy

### Unit tests
- Validate request DTO constraints: missing fields, blank strings, invalid ISBN format.
- Validate service invariants: missing resource returns `404`, duplicate ISBN returns `400`, replacement never creates a new record.
- Validate repository methods: snapshot read, file persistence, atomic replace semantics, and no partial writes on failure.

### Integration tests
- `GET /api/v1/books` returns JSON array and valid `200`.
- `GET /api/v1/books/{id}` returns `200` for existing ids and `404` for missing ids.
- `PUT /api/v1/books/{id}` with valid body returns `200` and persisted replacement.
- `PUT` with duplicate ISBN, incomplete body, or malformed JSON returns `400`.
- `DELETE /api/v1/books/{id}` returns `204` and removes the book; missing id returns `404`.
- Verify consistent JSON error schema across all failure modes without exposing internals.

### Manual verification
- Run the app locally and hit the API with curl/Postman. Confirm the data file preserves valid state across restart.
- Validate that concurrent ordinary requests do not corrupt the JSON file or lose unrelated records.
- Run `./gradlew test` and confirm all tests pass with no security or serialization regressions.

## Risk & Constraints

### Primary risks and mitigations

1. File corruption during writes
   - Risk: a failed write leaves the JSON store invalid.
   - Mitigation: write to a temp file in the same directory, then atomically move it into place; only publish the new snapshot after successful serialization and file sync.

2. Duplicate ISBN handling drift
   - Risk: normalization logic differs between validation and persistence.
   - Mitigation: centralize normalization in one helper method used by both validation and repository lookup before uniqueness comparisons.

3. Inconsistent public error contract
   - Risk: `400`/`404`/`500` responses vary by framework default behavior.
   - Mitigation: implement a single `ApiError` envelope and a `GlobalExceptionHandler` to map all domain errors consistently.

4. Security mismatch with public API requirement
   - Risk: security auto-config blocks unauthenticated access.
   - Mitigation: configure Spring Security to permit the book endpoints without authentication and keep it intentionally minimal.

5. Performance drift under representative data
   - Risk: repeated file reads or expensive full-file reinits create latency.
   - Mitigation: maintain a validated in-memory snapshot and avoid reloading on every request; use repository-level lock only during mutations.

### Explicit constraints from the story
- No database, no external integrations, no auth scope, and no distributed deployment.
- `PUT` is replacement-only; missing-target `PUT` must not silently create a book.
- Business rules are intentionally small and should remain in the application boundary rather than in a front-end or external platform.
- The implementation must be Java 25 + Spring Boot 4.1.1 according to the project toolchain.

## Recommended File Breakdown

```text
src/main/java/com/epam/book_review_svc/
  BookReviewSvcApplication.java
  config/
    SecurityConfig.java
  controller/
    BookController.java
  exception/
    ApiError.java
    GlobalExceptionHandler.java
  model/
    Book.java
    dto/
      BookRequestDto.java
      BookResponseDto.java
  repository/
    JsonFileBookRepository.java
  service/
    BookService.java

src/main/resources/
  application.yml

src/test/java/com/epam/book_review_svc/
  controller/
    BookControllerTest.java
  repository/
    JsonFileBookRepositoryTest.java
  service/
    BookServiceTest.java
```

This structure matches the architecture and keeps responsibilities clean: controller (HTTP), service (business rules), repository (local JSON persistence), and exception layer (public API contract).

## Implementation Order

1. Security and app configuration
2. DTOs, validation, and canonical book schema
3. JSON repository plus atomic persistence infrastructure
4. `BookService` resource logic and invariants
5. `BookController` and global JSON error mapping
6. Regression tests and final Gradle verification

This order reduces rework, keeps the data layer stable, and aligns directly with the critical path described in the requirements and architecture.

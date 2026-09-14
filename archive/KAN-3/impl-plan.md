# Implementation Plan: Book Review Service API

**Document Version:** 1.0  
**Status:** Ready for Execution  
**Last Updated:** 2026-09-14  
**Phase:** 1 - Core CRUD (MVP)

---

## Executive Summary

This implementation plan breaks down the approved architecture into **18 actionable tasks** organized by dependency and layer. The plan spans **2-3 weeks** of focused development (2-3 developers at 1-2 tasks per developer concurrently).

**Key Metrics:**
- **Total Phase 1 Tasks:** 18
- **Critical Path Length:** ~5-6 days (sequential blocking tasks)
- **Parallel Workstreams:** 3 (can run simultaneously after setup)
- **Estimated Total Effort:** 80-100 hours (~2-3 weeks at 40 hrs/week)
- **Task Granularity:** 1-4 hours each (Medium: ~2-3 hours average)

**Key Risks/Constraints:**
1. **Database-free architecture** - file-based JSON storage has no built-in concurrency control (acceptable for MVP)
2. **External auth dependency** - Spring Security configuration assumed available (configured externally)
3. **Jackson serialization** - careful date/time format handling required
4. **Test data consistency** - need shared fixture files for repository tests

---

## Task Breakdown: Phase 1 - Core CRUD

### Dependency & Complexity Overview

```
Setup Layer (3 tasks)
├─ TASK-001: Project structure & dependencies
├─ TASK-002: Application configuration
├─ TASK-003: Initial test setup
│
Core Models Layer (3 tasks)
├─ TASK-004: Implement Book domain model
├─ TASK-005: Implement request/response DTOs
├─ TASK-006: Implement error response DTOs
│
Repository Layer (2 tasks)
├─ TASK-007: Implement BookRepository interface
├─ TASK-008: Implement JsonFileBookRepository (file I/O)
│
Service & Validation Layer (5 tasks)
├─ TASK-009: Implement BookValidator
├─ TASK-010: Implement BookService.createBook()
├─ TASK-011: Implement BookService.listBooks()
├─ TASK-012: Implement BookService.getBook()
├─ TASK-013: Implement BookService (update/delete)
│
Controller & Exception Layer (3 tasks)
├─ TASK-014: Implement GlobalExceptionHandler
├─ TASK-015: Implement BookController (create/list/get)
├─ TASK-016: Implement BookController (update/delete)
│
Documentation & Testing (2 tasks)
├─ TASK-017: Implement integration tests (80% coverage)
├─ TASK-018: Swagger annotation and documentation
```

---

## Detailed Task Specifications

### LAYER 1: SETUP & INFRASTRUCTURE

---

#### TASK-001: Initialize Project Structure & Dependencies

**ID:** TASK-001  
**Title:** Initialize Project Structure & Dependencies  
**Component:** Project Setup  
**Complexity:** Small (1-2 hours)  
**Dependencies:** None (start here)  
**Blocks:** TASK-002, TASK-003, TASK-004

**Description:**
Verify Gradle build configuration, validate Java 25 toolchain setup, add all required dependencies to build.gradle with correct versions. This task ensures the project skeleton is ready for development.

**Acceptance Criteria:**
- [ ] Gradle build.gradle includes Spring Boot 4.1.1, Java 25 toolchain (enforced)
- [ ] All required dependencies present:
  - `spring-boot-starter-web` (REST framework)
  - `spring-boot-starter-security` (authentication/authorization)
  - `spring-boot-starter-data-jpa` (not used yet, but placeholder for future DB migration)
  - `jackson-databind` (JSON processing)
  - `jackson-datatype-jsr310` (Java 8 date/time support)
  - `springdoc-openapi-starter-webmvc-ui` (Swagger 2.5.0)
  - `lombok` (boilerplate reduction)
  - `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ)
- [ ] Project compiles with `./gradlew build`
- [ ] No dependency conflicts or missing transitive deps
- [ ] Build configuration uses consistent version variables

**Effort Estimate:** 1-2 hours

**Files Affected:**
- `/build.gradle` - Add/verify dependencies

**Success Definition:**
- `./gradlew build` completes successfully
- `./gradlew dependencies` shows all required libraries
- Classpath includes Spring Boot, Jackson, Springdoc, Lombok

---

#### TASK-002: Application Configuration & Profiles

**ID:** TASK-002  
**Title:** Application Configuration & Profiles  
**Component:** Configuration Management  
**Complexity:** Small (1-2 hours)  
**Dependencies:** TASK-001  
**Blocks:** TASK-003

**Description:**
Create application.properties and environment-specific profiles (dev, test, prod). Configure Spring Boot settings, Jackson serialization, logging, and data file locations.

**Acceptance Criteria:**
- [ ] `src/main/resources/application.properties` created with:
  - Spring application name, port (8080), context-path
  - Jackson configuration: date as ISO-8601 (not timestamps), UTC timezone
  - Logging levels: root=INFO, package=DEBUG
  - Swagger UI enabled, path configured
  - File storage path: `classpath:data/books.json`
- [ ] `src/main/resources/application-dev.properties` created (DEBUG logging, swagger enabled)
- [ ] `src/main/resources/application-test.properties` created (in-memory storage placeholder)
- [ ] `src/main/resources/application-prod.properties` created (INFO logging, swagger disabled)
- [ ] Spring can load profiles dynamically via `SPRING_PROFILES_ACTIVE`
- [ ] Configuration validates when application starts

**Effort Estimate:** 1-2 hours

**Files Affected:**
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-test.properties`
- `src/main/resources/application-prod.properties`

**Success Definition:**
- `./gradlew bootRun` starts successfully on port 8080
- Logs show correct configuration loaded
- Swagger UI accessible at `http://localhost:8080/swagger-ui/index.html`

---

#### TASK-003: Initial Test Infrastructure & Fixtures

**ID:** TASK-003  
**Title:** Initial Test Infrastructure & Fixtures  
**Component:** Testing Setup  
**Complexity:** Small (1-2 hours)  
**Dependencies:** TASK-001, TASK-002  
**Blocks:** TASK-017

**Description:**
Set up test fixtures, Spring test configuration, test base classes, and sample data for repository/integration tests. Create empty JSON test fixture with 2-3 sample books.

**Acceptance Criteria:**
- [ ] `src/test/resources/application-test.properties` configured (if not in TASK-002)
- [ ] Test fixtures created in `src/test/resources/fixtures/`:
  - `books-sample-2.json` (2 books with valid data)
  - `books-empty.json` (empty array [])
  - `books-invalid.json` (malformed JSON for error testing)
- [ ] Base test class created: `BaseIntegrationTest` with:
  - `@SpringBootTest` configuration
  - `@BeforeEach` to reset/populate fixtures
  - Utility methods: `loadFixture(filename)`, `clearFixtures()`
- [ ] Mock repository helper created for unit tests
- [ ] AssertJ custom assertions or matchers available

**Effort Estimate:** 1-2 hours

**Files Affected:**
- `src/test/java/com/epam/book_review_svc/BaseIntegrationTest.java`
- `src/test/resources/fixtures/books-sample-2.json`
- `src/test/resources/fixtures/books-empty.json`
- `src/test/resources/fixtures/books-invalid.json`

**Success Definition:**
- `./gradlew test` runs without errors (even if no tests yet)
- Fixtures load correctly when referenced in tests
- Test base class can be extended for specific test classes

---

### LAYER 2: DATA MODELS

---

#### TASK-004: Implement Book Domain Model

**ID:** TASK-004  
**Title:** Implement Book Domain Model  
**Component:** Model Layer  
**Complexity:** Small (1 hour)  
**Dependencies:** TASK-001  
**Blocks:** TASK-007, TASK-008, TASK-010

**Description:**
Create the Book POJO (Plain Old Java Object) representing the core domain entity. Include all 8 fields with Lombok annotations for boilerplate reduction and Jackson annotations for precise JSON serialization.

**Acceptance Criteria:**
- [ ] Class created: `src/main/java/com/epam/book_review_svc/model/Book.java`
- [ ] Lombok annotations applied:
  - `@Data` (generates getters, setters, equals, hashCode, toString)
  - `@Builder` (fluent builder pattern)
  - `@NoArgsConstructor`, `@AllArgsConstructor` (constructors)
- [ ] All 8 fields present with correct types:
  - `id: String` (UUID)
  - `title: String` (required)
  - `author: String` (required)
  - `ISBN: String` (required, unique)
  - `genre: String` (enum-like string)
  - `publicationDate: LocalDate` (YYYY-MM-DD format)
  - `createdBy: String` (user ID)
  - `createdAt: OffsetDateTime` (UTC timestamp, ISO-8601)
- [ ] Jackson annotations configured:
  - `@JsonProperty` for each field
  - `@JsonFormat` for date fields with correct patterns
  - `@JsonPropertyOrder` to specify field ordering in JSON
- [ ] Book is immutable-safe (final fields, no mutable collections)
- [ ] Serializes/deserializes correctly with ObjectMapper

**Effort Estimate:** 1 hour

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/model/Book.java`

**Success Definition:**
```java
// Should support:
Book book = Book.builder()
    .id("550e8400-e29b-41d4-a716-446655440000")
    .title("The Great Gatsby")
    .author("F. Scott Fitzgerald")
    .ISBN("978-0743273565")
    .genre("Fiction")
    .publicationDate(LocalDate.of(1925, 4, 10))
    .createdBy("user123")
    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
    .build();

// Serializes to JSON with correct date formats
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(book);
// "createdAt": "2026-09-14T10:30:00.000Z" (not milliseconds since epoch)
```

---

#### TASK-005: Implement Request/Response DTOs

**ID:** TASK-005  
**Title:** Implement Request/Response DTOs  
**Component:** Model Layer  
**Complexity:** Small (1 hour)  
**Dependencies:** TASK-001  
**Blocks:** TASK-015, TASK-016

**Description:**
Create request and response Data Transfer Objects (DTOs). Request DTOs contain mutable fields with Bean Validation annotations; response DTOs are immutable builders containing all fields.

**Acceptance Criteria:**
- [ ] `CreateBookRequest` class created:
  - Fields: title, author, ISBN, genre, publicationDate (all @NotBlank/@NotNull)
  - Annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
  - Bean Validation: `@NotBlank(message = "...")` on each field
- [ ] `UpdateBookRequest` class created:
  - Same fields as CreateBookRequest, all optional (no @NotNull)
  - Allows partial updates
- [ ] `BookResponse` class created:
  - All 8 fields from Book (immutable)
  - Annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
  - Jackson `@JsonProperty` and `@JsonFormat` on date fields
  - Same `@JsonPropertyOrder` as Book entity
- [ ] `PagedResponse<T>` generic class created:
  - Generic type parameter `<T>`
  - Fields: content (List<T>), page, size, totalElements, totalPages, hasNextPage, hasPreviousPage
  - Used for GET /api/books list response
- [ ] All DTOs compile and serialize/deserialize correctly

**Effort Estimate:** 1 hour

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/model/dto/CreateBookRequest.java`
- `src/main/java/com/epam/book_review_svc/model/dto/UpdateBookRequest.java`
- `src/main/java/com/epam/book_review_svc/model/dto/BookResponse.java`
- `src/main/java/com/epam/book_review_svc/model/dto/PagedResponse.java`

**Success Definition:**
```java
// CreateBookRequest with validation
CreateBookRequest req = new CreateBookRequest(
    "Title", "Author", "978-0743273565", "Fiction", 
    LocalDate.of(1925, 4, 10)
);

// BookResponse as immutable DTO
BookResponse resp = BookResponse.builder()
    .id("550e8400-e29b-41d4-a716-446655440000")
    .title("Title")
    // ... all fields
    .build();

// PagedResponse for list endpoints
PagedResponse<BookResponse> paged = PagedResponse.<BookResponse>builder()
    .content(List.of(resp))
    .page(0)
    .size(20)
    .totalElements(1)
    .totalPages(1)
    .hasNextPage(false)
    .hasPreviousPage(false)
    .build();
```

---

#### TASK-006: Implement Error Response DTOs & Exceptions

**ID:** TASK-006  
**Title:** Implement Error Response DTOs & Exceptions  
**Component:** Model Layer / Exception Handling  
**Complexity:** Small (1 hour)  
**Dependencies:** TASK-001  
**Blocks:** TASK-014

**Description:**
Create error DTOs and custom exception hierarchy for consistent error handling across the API.

**Acceptance Criteria:**
- [ ] `ErrorResponse` class created:
  - Fields: status (int), message (String), path (String), timestamp (OffsetDateTime), errors (List<FieldError>)
  - `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
  - errors field is optional (null-safe)
- [ ] `FieldError` class created:
  - Fields: field (String), message (String)
  - Used for detailed validation error reporting
- [ ] Custom exception hierarchy in `src/main/java/com/epam/book_review_svc/exception/`:
  - Abstract base: `ApiException extends RuntimeException` with abstract `getStatusCode(): int`
  - `NotFoundException extends ApiException` → HTTP 404
  - `ConflictException extends ApiException` → HTTP 409
  - `ForbiddenException extends ApiException` → HTTP 403
  - `BadRequestException extends ApiException` → HTTP 400
  - `ValidationException extends ApiException` → HTTP 400 (with field errors list)
  - `DataAccessException extends ApiException` → HTTP 500
- [ ] Each exception can include optional message and details
- [ ] Exceptions support structured error information

**Effort Estimate:** 1 hour

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/model/dto/ErrorResponse.java`
- `src/main/java/com/epam/book_review_svc/model/dto/FieldError.java`
- `src/main/java/com/epam/book_review_svc/exception/ApiException.java`
- `src/main/java/com/epam/book_review_svc/exception/NotFoundException.java`
- `src/main/java/com/epam/book_review_svc/exception/ConflictException.java`
- `src/main/java/com/epam/book_review_svc/exception/ForbiddenException.java`
- `src/main/java/com/epam/book_review_svc/exception/BadRequestException.java`
- `src/main/java/com/epam/book_review_svc/exception/ValidationException.java`
- `src/main/java/com/epam/book_review_svc/exception/DataAccessException.java`

**Success Definition:**
```java
// Throwing exceptions
throw new NotFoundException("Book not found");
throw new ForbiddenException("Only creator can update");
throw new ValidationException("Validation failed", fieldErrorList);

// ErrorResponse generated automatically by exception handler
ErrorResponse error = ErrorResponse.builder()
    .status(404)
    .message("Book not found")
    .path("/api/books/invalid-id")
    .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
    .build();
```

---

### LAYER 3: DATA ACCESS (REPOSITORY)

---

#### TASK-007: Implement BookRepository Interface

**ID:** TASK-007  
**Title:** Implement BookRepository Interface  
**Component:** Data Access Layer  
**Complexity:** Small (1 hour)  
**Dependencies:** TASK-004, TASK-001  
**Blocks:** TASK-008, TASK-010

**Description:**
Create the BookRepository interface defining the contract for CRUD operations and query patterns. This abstracts persistence mechanism from business logic.

**Acceptance Criteria:**
- [ ] Interface created: `src/main/java/com/epam/book_review_svc/repository/BookRepository.java`
- [ ] Methods defined (no implementation):
  - `Book save(Book book)` - Create new book
  - `Optional<Book> findById(String id)` - Retrieve by ID
  - `Optional<Book> findByISBN(String isbn)` - Retrieve by ISBN (unique lookup)
  - `List<Book> findAll()` - Retrieve all books
  - `Book update(String id, Book book)` - Update existing book
  - `void delete(String id)` - Delete by ID
  - `long count()` - Total book count
- [ ] No implementation details in interface (pure contract)
- [ ] Used by service layer only (repository pattern)
- [ ] Supports future database migration (interface remains unchanged)

**Effort Estimate:** 1 hour

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/repository/BookRepository.java`

**Success Definition:**
```java
// Service can depend on interface, not implementation
public class BookService {
    private final BookRepository repository; // Can swap implementations
    
    public BookResponse createBook(CreateBookRequest req) {
        // Calls repository methods, doesn't care if JSON or DB
    }
}
```

---

#### TASK-008: Implement JsonFileBookRepository (File I/O)

**ID:** TASK-008  
**Title:** Implement JsonFileBookRepository (File I/O)  
**Component:** Data Access Layer  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-004, TASK-007, TASK-001  
**Blocks:** TASK-010

**Description:**
Implement the BookRepository interface for JSON file storage. Handle file I/O with Jackson ObjectMapper, read-modify-write cycle for mutations, and proper error handling.

**Acceptance Criteria:**
- [ ] Class created: `src/main/java/com/epam/book_review_svc/repository/JsonFileBookRepository.java`
  - `@Repository` annotation (Spring component)
  - `@Slf4j` for logging
  - Implements BookRepository interface
- [ ] Constructor injection:
  - `ObjectMapper objectMapper` (Jackson for JSON serialization)
  - `ResourceLoader resourceLoader` (Spring classpath resource loading)
- [ ] Method implementations:
  - `save()`: Load all books, add new book, write back to file
  - `findById()`: Load all, search by ID, return Optional
  - `findByISBN()`: Load all, search by ISBN, return Optional
  - `findAll()`: Load books from file, return List
  - `update()`: Load all, find by ID, replace, write back
  - `delete()`: Load all, filter out by ID, write back
  - `count()`: Return size of book list
- [ ] Private helper methods:
  - `loadBooksFromFile() throws IOException`: Read JSON file using ObjectMapper
  - `saveBooksToFile(List<Book>) throws IOException`: Write JSON file with pretty printing
  - Handle FileNotFoundException gracefully (return empty list)
  - Handle file not existing (create parent directories)
- [ ] File path: `classpath:data/books.json`
- [ ] Error handling:
  - Wrap IOException in DataAccessException
  - Log all operations at appropriate levels (info, debug, error)
  - Handle missing file on first access (return empty list)
- [ ] Jackson ObjectMapper usage:
  - `readValue(file, Book[].class)` for deserialization
  - `writerWithDefaultPrettyPrinter().writeValue(file, books)` for serialization

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/repository/JsonFileBookRepository.java`
- `src/main/resources/data/books.json` (created if missing)

**Success Definition:**
```java
// Can save and retrieve books
Book book = Book.builder().id("1").title("Test").build();
repository.save(book);
Optional<Book> retrieved = repository.findById("1");
assert(retrieved.isPresent());
assert(retrieved.get().getTitle().equals("Test"));

// Books persisted to file
File file = new File("src/main/resources/data/books.json");
assert(file.exists());

// File contains valid JSON
ObjectMapper mapper = new ObjectMapper();
Book[] books = mapper.readValue(file, Book[].class);
assert(books.length >= 1);
```

---

### LAYER 4: BUSINESS LOGIC (SERVICE & VALIDATION)

---

#### TASK-009: Implement BookValidator

**ID:** TASK-009  
**Title:** Implement BookValidator  
**Component:** Service Layer  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-001, TASK-006  
**Blocks:** TASK-010, TASK-013

**Description:**
Create the validation component that enforces all business rules for book data. Aggregate validation errors and return detailed error messages.

**Acceptance Criteria:**
- [ ] Class created: `src/main/java/com/epam/book_review_svc/service/BookValidator.java`
  - `@Component` annotation (Spring-managed)
  - `@Slf4j` for logging
- [ ] Validation constants defined:
  - `TITLE_MIN_LENGTH = 1`, `TITLE_MAX_LENGTH = 500`
  - `AUTHOR_MIN_LENGTH = 1`, `AUTHOR_MAX_LENGTH = 200`
  - `VALID_GENRES = Set.of("Fiction", "Non-Fiction", "Mystery", "Romance", "Science Fiction", "Fantasy", "Biography", "History", "Self-Help", "Children", "Young Adult")`
- [ ] Public validation methods:
  - `validateCreateBookRequest(CreateBookRequest): List<ValidationError>`
  - `validateUpdateBookRequest(UpdateBookRequest): List<ValidationError>`
- [ ] Private field validation methods:
  - `validateTitle(String): List<ValidationError>`
    - Check: not null/blank, length in range
  - `validateAuthor(String): List<ValidationError>`
    - Check: not null/blank, length in range
  - `validateISBN(String): List<ValidationError>`
    - Check: not null/blank, format (ISBN-10 or ISBN-13, with optional hyphens)
    - Pattern: `^\d{10}$` or `^\d{13}$` after removing hyphens
  - `validateGenre(String): List<ValidationError>`
    - Check: not null/blank, must be in VALID_GENRES set (whitelist)
  - `validatePublicationDate(LocalDate): List<ValidationError>`
    - Check: not null, not in future (must be <= today)
- [ ] Validation error aggregation:
  - Return ALL errors, not fail-fast (better UX)
  - Each error includes field name and message
  - Each public method returns empty list if all valid, list of errors if invalid

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/service/BookValidator.java`

**Success Definition:**
```java
// Valid request - no errors
CreateBookRequest validReq = new CreateBookRequest(
    "Title", "Author", "978-0743273565", "Fiction", 
    LocalDate.of(1925, 4, 10)
);
List<ValidationError> errors = validator.validateCreateBookRequest(validReq);
assert(errors.isEmpty());

// Invalid request - multiple errors
CreateBookRequest invalidReq = new CreateBookRequest(
    "", "", "invalid-isbn", "InvalidGenre", 
    LocalDate.now().plusDays(1) // future date
);
errors = validator.validateCreateBookRequest(invalidReq);
assert(errors.size() >= 3);
assert(errors.stream().anyMatch(e -> e.getField().equals("title")));
assert(errors.stream().anyMatch(e -> e.getField().equals("ISBN")));
assert(errors.stream().anyMatch(e -> e.getField().equals("publicationDate")));
```

---

#### TASK-010: Implement BookService.createBook()

**ID:** TASK-010  
**Title:** Implement BookService.createBook() Method  
**Component:** Service Layer  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-004, TASK-007, TASK-008, TASK-009  
**Blocks:** TASK-015, TASK-017

**Description:**
Implement the createBook() method in BookService. Orchestrates authorization, validation, ISBN uniqueness check, and persistence. Generates UUID and timestamps.

**Acceptance Criteria:**
- [ ] Method signature: `BookResponse createBook(CreateBookRequest request, String userId)`
- [ ] Method implementation follows steps:
  1. Validate authorization (check hasAuthorRole - placeholder for now, will be wired in Phase 2)
     - If not author, throw ForbiddenException("Only authors can create books")
  2. Call bookValidator.validateCreateBookRequest(request)
     - If errors not empty, throw ValidationException("Validation failed", errors)
  3. Check ISBN uniqueness via bookRepository.findByISBN(request.getISBN())
     - If present, throw ConflictException("ISBN already exists")
  4. Generate UUID for book ID: `UUID.randomUUID().toString()`
  5. Create Book entity using builder:
     - Copy fields from request
     - Set createdBy = userId
     - Set createdAt = OffsetDateTime.now(ZoneOffset.UTC)
  6. Call bookRepository.save(book)
  7. Convert returned Book to BookResponse using helper method
  8. Return BookResponse
- [ ] Class annotation: `@Service`, `@Slf4j`
- [ ] Field: `BookRepository bookRepository` (injected via constructor)
- [ ] Field: `BookValidator bookValidator` (injected via constructor)
- [ ] Logging:
  - Log at INFO level when book is created (id, title)
  - Log at DEBUG level for major steps
- [ ] Private helper method: `BookResponse convertToResponse(Book book)`
  - Converts Book entity to BookResponse
  - Should be reused by all methods

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/service/BookService.java` (partial - new file)

**Success Definition:**
```java
// Happy path: valid request, author role
BookService service = new BookService(repository, validator);
CreateBookRequest req = new CreateBookRequest(...valid data...);
BookResponse response = service.createBook(req, "user123");
assert(response.getId() != null);
assert(response.getCreatedBy().equals("user123"));
assert(response.getCreatedAt() != null);
// Book is persisted to file

// Unhappy path: ISBN conflict
repository.save(Book.builder().ISBN("978-0743273565").build());
CreateBookRequest dup = new CreateBookRequest(...same ISBN...);
assertThrows(ConflictException.class, 
    () -> service.createBook(dup, "user123"));
```

---

#### TASK-011: Implement BookService.listBooks() Method

**ID:** TASK-011  
**Title:** Implement BookService.listBooks() Method  
**Component:** Service Layer  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-004, TASK-005, TASK-007, TASK-008  
**Blocks:** TASK-015, TASK-017

**Description:**
Implement the listBooks() method with in-memory pagination and sorting. Handles page/size parameters, validates ranges, applies sorting, and returns PagedResponse.

**Acceptance Criteria:**
- [ ] Method signature: `PagedResponse<BookResponse> listBooks(int page, int size, String sort)`
- [ ] Method implementation follows steps:
  1. Validate pagination parameters:
     - If page < 0, throw BadRequestException("Page must be >= 0")
     - If size < 1 or size > 100, throw BadRequestException("Size must be between 1 and 100")
  2. Call bookRepository.findAll() to get all books
  3. Apply sorting transformation via private helper: `applySort(List<Book>, String sort)`
     - Parse sort parameter: format = "field,direction" (e.g., "createdAt,desc")
     - Default: "createdAt,desc"
     - Supported fields: id, title, author, createdAt (more in Phase 5)
     - Directions: asc, desc (case-insensitive)
     - Sort in-memory using Java streams/comparators
  4. Calculate pagination metadata:
     - totalElements = sorted.size()
     - totalPages = ceil(totalElements / size)
     - hasNextPage = page < totalPages - 1
     - hasPreviousPage = page > 0
  5. Calculate slice indices:
     - startIndex = page * size
     - endIndex = min(startIndex + size, totalElements)
     - Guard: if startIndex >= totalElements, return empty content
  6. Extract page content: `sorted.subList(startIndex, endIndex)`
  7. Convert each Book to BookResponse
  8. Build PagedResponse with metadata
- [ ] Private helper: `applySort(List<Book> books, String sort): List<Book>`
- [ ] Default sort: "createdAt,desc" (newest first)
- [ ] Logging at DEBUG level for pagination parameters

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/service/BookService.java` (addition to existing)

**Success Definition:**
```java
// Mock 25 books in repository
List<Book> books = generateTestBooks(25);
repository.saveAll(books); // or multiple save() calls

// Test pagination
PagedResponse<BookResponse> page0 = service.listBooks(0, 10, "createdAt,desc");
assert(page0.getContent().size() == 10);
assert(page0.getPage() == 0);
assert(page0.getSize() == 10);
assert(page0.getTotalElements() == 25);
assert(page0.getTotalPages() == 3);
assert(page0.isHasNextPage() == true);
assert(page0.isHasPreviousPage() == false);

// Test next page
PagedResponse<BookResponse> page1 = service.listBooks(1, 10, "createdAt,desc");
assert(page1.getContent().size() == 10);
assert(page1.isHasNextPage() == true);
assert(page1.isHasPreviousPage() == true);

// Test last page
PagedResponse<BookResponse> page2 = service.listBooks(2, 10, "createdAt,desc");
assert(page2.getContent().size() == 5);
assert(page2.isHasNextPage() == false);
assert(page2.isHasPreviousPage() == true);

// Test sorting
List<Book> descending = page0.getContent();
assert(descending.get(0).getCreatedAt().isAfter(
    descending.get(1).getCreatedAt()));

// Test validation
assertThrows(BadRequestException.class, 
    () -> service.listBooks(-1, 10, "createdAt,desc"));
assertThrows(BadRequestException.class, 
    () -> service.listBooks(0, 101, "createdAt,desc"));
```

---

#### TASK-012: Implement BookService.getBook() Method

**ID:** TASK-012  
**Title:** Implement BookService.getBook() Method  
**Component:** Service Layer  
**Complexity:** Small (1 hour)  
**Dependencies:** TASK-004, TASK-005, TASK-007, TASK-008  
**Blocks:** TASK-015, TASK-017

**Description:**
Implement the getBook() method. Retrieves a single book by ID or throws NotFoundException.

**Acceptance Criteria:**
- [ ] Method signature: `BookResponse getBook(String id)`
- [ ] Method implementation:
  1. Call bookRepository.findById(id)
  2. If Optional.empty(), throw NotFoundException("Book not found")
  3. Convert Book to BookResponse
  4. Return BookResponse
- [ ] Simple 3-line implementation (highly straightforward)

**Effort Estimate:** 1 hour

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/service/BookService.java` (addition to existing)

**Success Definition:**
```java
// Happy path: book exists
Book savedBook = service.createBook(validReq, "user123"); // from TASK-010
BookResponse retrieved = service.getBook(savedBook.getId());
assert(retrieved.getId().equals(savedBook.getId()));

// Unhappy path: book not found
assertThrows(NotFoundException.class, 
    () -> service.getBook("non-existent-id"));
```

---

#### TASK-013: Implement BookService (updateBook & deleteBook)

**ID:** TASK-013  
**Title:** Implement BookService.updateBook() & deleteBook()  
**Component:** Service Layer  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-004, TASK-005, TASK-007, TASK-008, TASK-009, TASK-010  
**Blocks:** TASK-016, TASK-017

**Description:**
Implement updateBook() and deleteBook() methods. Both require creator-only authorization. Update preserves immutable fields (id, createdBy, createdAt).

**Acceptance Criteria:**

**updateBook() Method:**
- [ ] Method signature: `BookResponse updateBook(String id, UpdateBookRequest request, String userId)`
- [ ] Implementation:
  1. Call bookRepository.findById(id)
  2. If not found, throw NotFoundException("Book not found")
  3. Check authorization: if !existing.getCreatedBy().equals(userId)
     - Throw ForbiddenException("Only the creator can update this book")
  4. Call bookValidator.validateUpdateBookRequest(request)
     - If errors not empty, throw ValidationException("Validation failed", errors)
  5. Check ISBN uniqueness (if ISBN changed):
     - If !request.getISBN().equals(existing.getISBN()) AND repository has that ISBN
     - Throw ConflictException("ISBN already exists")
  6. Update mutable fields only:
     - title, author, ISBN, genre, publicationDate (from request)
  7. Preserve immutable fields:
     - id, createdBy, createdAt (from existing)
  8. Call bookRepository.update(id, existing)
  9. Convert to BookResponse and return

**deleteBook() Method:**
- [ ] Method signature: `void deleteBook(String id, String userId)`
- [ ] Implementation:
  1. Call bookRepository.findById(id)
  2. If not found, throw NotFoundException("Book not found")
  3. Check authorization: if !book.getCreatedBy().equals(userId)
     - Throw ForbiddenException("Only the creator can delete this book")
  4. Call bookRepository.delete(id)

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/service/BookService.java` (addition to existing)

**Success Definition:**
```java
// Create a book as user123
Book created = service.createBook(validReq, "user123");

// Update as creator: succeeds
UpdateBookRequest updateReq = new UpdateBookRequest("New Title", ...);
BookResponse updated = service.updateBook(created.getId(), updateReq, "user123");
assert(updated.getTitle().equals("New Title"));
assert(updated.getCreatedBy().equals("user123")); // unchanged
assert(updated.getCreatedAt().equals(created.getCreatedAt())); // unchanged

// Update as different user: fails
assertThrows(ForbiddenException.class, 
    () -> service.updateBook(created.getId(), updateReq, "otherUser"));

// Delete as creator: succeeds
service.deleteBook(created.getId(), "user123");
assertThrows(NotFoundException.class, 
    () -> service.getBook(created.getId()));

// Delete as different user: fails
Book created2 = service.createBook(validReq, "user123");
assertThrows(ForbiddenException.class, 
    () -> service.deleteBook(created2.getId(), "otherUser"));
```

---

### LAYER 5: EXCEPTION HANDLING & API

---

#### TASK-014: Implement GlobalExceptionHandler

**ID:** TASK-014  
**Title:** Implement GlobalExceptionHandler  
**Component:** Exception Handling / API Layer  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-005, TASK-006  
**Blocks:** TASK-015, TASK-016

**Description:**
Create the global exception handler for consistent error responses. Maps all custom exceptions and Spring validation errors to ErrorResponse DTO.

**Acceptance Criteria:**
- [ ] Class created: `src/main/java/com/epam/book_review_svc/exception/GlobalExceptionHandler.java`
  - `@RestControllerAdvice` annotation
  - `@Slf4j` for logging
- [ ] Exception handler methods (one per exception type):
  - `@ExceptionHandler(NotFoundException.class)` → HTTP 404
    - Response: `{"status": 404, "message": "Book not found", "path": "...", "timestamp": "...", "errors": null}`
  - `@ExceptionHandler(ForbiddenException.class)` → HTTP 403
    - Response: `{"status": 403, "message": "Only creator can...", "path": "...", "timestamp": "..."}`
  - `@ExceptionHandler(ConflictException.class)` → HTTP 409
    - Response: `{"status": 409, "message": "ISBN already exists", "path": "...", "timestamp": "..."}`
  - `@ExceptionHandler(BadRequestException.class)` → HTTP 400
    - Response: `{"status": 400, "message": "...", "path": "...", "timestamp": "..."}`
  - `@ExceptionHandler(ValidationException.class)` → HTTP 400
    - Response includes "errors" field with FieldError[]
  - `@ExceptionHandler(MethodArgumentNotValidException.class)` → HTTP 400
    - Spring Bean Validation errors from @Valid
    - Convert Spring validation errors to FieldError[] format
  - `@ExceptionHandler(Exception.class)` → HTTP 500
    - Catch-all for unexpected errors
    - Log at ERROR level
- [ ] ErrorResponse builder consistency:
  - All responses include: status, message, path, timestamp
  - timestamp = OffsetDateTime.now(ZoneOffset.UTC)
  - path = request.getRequestURI()
- [ ] Logging:
  - Warn level for client errors (4xx)
  - Error level for server errors (5xx) with stack trace

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/exception/GlobalExceptionHandler.java`

**Success Definition:**
```java
// When NotFoundException is thrown from controller:
// HTTP 404 with JSON:
// {
//   "status": 404,
//   "message": "Book not found",
//   "path": "/api/books/invalid-id",
//   "timestamp": "2026-09-14T10:30:00.000Z",
//   "errors": null
// }

// When ValidationException is thrown:
// HTTP 400 with JSON:
// {
//   "status": 400,
//   "message": "Validation failed",
//   "path": "/api/books",
//   "timestamp": "2026-09-14T10:30:00.000Z",
//   "errors": [
//     {"field": "ISBN", "message": "Invalid ISBN format"},
//     {"field": "publicationDate", "message": "Cannot be in future"}
//   ]
// }
```

---

#### TASK-015: Implement BookController (create/list/get)

**ID:** TASK-015  
**Title:** Implement BookController (Create/List/Get endpoints)  
**Component:** API Layer / REST Controller  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-004, TASK-005, TASK-006, TASK-010, TASK-011, TASK-012, TASK-014  
**Blocks:** TASK-017, TASK-018

**Description:**
Implement BookController REST endpoints for POST (create), GET (list), and GET (single). Map HTTP requests to service methods with Swagger annotations.

**Acceptance Criteria:**
- [ ] Class created: `src/main/java/com/epam/book_review_svc/controller/BookController.java`
  - `@RestController` annotation
  - `@RequestMapping("/api/books")` class-level route
  - `@Tag(name = "Books", description = "Book Catalog Management APIs")` for Swagger
  - Field: `BookService bookService` (injected)
- [ ] POST /api/books endpoint:
  - Method: `createBook(@Valid @RequestBody CreateBookRequest request, Principal principal)`
  - Extracts authenticated user from Principal.getName()
  - Calls service.createBook(request, userId)
  - Returns `ResponseEntity.status(201).body(bookResponse)` (HTTP 201 Created)
  - Includes Location header: `"/api/books/" + response.getId()`
  - Swagger:
    - `@PostMapping` mapping
    - `@Operation(summary = "Create a new book", description = "...")`
    - `@ApiResponses` with 201, 400, 403, 409 responses
- [ ] GET /api/books endpoint:
  - Method: `listBooks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt,desc") String sort)`
  - Calls service.listBooks(page, size, sort)
  - Returns `ResponseEntity.ok(pagedResponse)` (HTTP 200 OK)
  - Swagger:
    - `@GetMapping` mapping
    - `@Operation(summary = "List books with pagination")`
    - `@ApiResponses` with 200, 400 responses
    - Document parameters: page, size, sort
- [ ] GET /api/books/{id} endpoint:
  - Method: `getBook(@PathVariable String id)`
  - Calls service.getBook(id)
  - Returns `ResponseEntity.ok(bookResponse)` (HTTP 200 OK)
  - Swagger:
    - `@GetMapping("/{id}")` mapping
    - `@Operation(summary = "Get a specific book")`
    - `@ApiResponses` with 200, 404 responses
- [ ] Logging:
  - Info level: request received (endpoint, user if authenticated)
  - Debug level: request parameters

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/controller/BookController.java` (partial - new file)

**Success Definition:**
```
// All endpoints work and return correct status codes:

POST /api/books
Request: {"title": "...", "author": "...", "ISBN": "...", "genre": "...", "publicationDate": "..."}
Response: HTTP 201 Created, Location: /api/books/{id}, Body: BookResponse

GET /api/books?page=0&size=20&sort=createdAt,desc
Response: HTTP 200 OK, Body: PagedResponse<BookResponse>

GET /api/books/{id}
Response: HTTP 200 OK, Body: BookResponse
or HTTP 404 Not Found if not exists
```

---

#### TASK-016: Implement BookController (update/delete)

**ID:** TASK-016  
**Title:** Implement BookController (Update/Delete endpoints)  
**Component:** API Layer / REST Controller  
**Complexity:** Medium (2-3 hours)  
**Dependencies:** TASK-004, TASK-005, TASK-006, TASK-013, TASK-014  
**Blocks:** TASK-017, TASK-018

**Description:**
Implement BookController endpoints for PUT (update) and DELETE. Both require authenticated user extraction and authorization checks at service layer.

**Acceptance Criteria:**
- [ ] PUT /api/books/{id} endpoint:
  - Method: `updateBook(@PathVariable String id, @Valid @RequestBody UpdateBookRequest request, Principal principal)`
  - Extracts authenticated user from Principal.getName()
  - Calls service.updateBook(id, request, userId)
  - Returns `ResponseEntity.ok(bookResponse)` (HTTP 200 OK)
  - Swagger:
    - `@PutMapping("/{id}")` mapping
    - `@Operation(summary = "Update a book")`
    - `@ApiResponses` with 200, 400, 403, 404, 409 responses
- [ ] DELETE /api/books/{id} endpoint:
  - Method: `deleteBook(@PathVariable String id, Principal principal)`
  - Extracts authenticated user from Principal.getName()
  - Calls service.deleteBook(id, userId)
  - Returns `ResponseEntity.noContent()` (HTTP 204 No Content)
  - Swagger:
    - `@DeleteMapping("/{id}")` mapping
    - `@Operation(summary = "Delete a book")`
    - `@ApiResponses` with 204, 403, 404 responses
- [ ] Error handling:
  - If Principal is null (not authenticated), throw ForbiddenException or handle gracefully
  - Note: Phase 2 will add Spring Security config to enforce this
- [ ] Logging: similar to TASK-015

**Effort Estimate:** 2-3 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/controller/BookController.java` (addition to existing)

**Success Definition:**
```
PUT /api/books/{id}
Request: {"title": "Updated...", ...} or partial fields
Response: HTTP 200 OK, Body: Updated BookResponse
or HTTP 403 Forbidden if not creator
or HTTP 404 Not Found if book doesn't exist

DELETE /api/books/{id}
Response: HTTP 204 No Content
or HTTP 403 Forbidden if not creator
or HTTP 404 Not Found if book doesn't exist
```

---

### LAYER 6: DOCUMENTATION & TESTING

---

#### TASK-017: Implement Integration Tests (80% Coverage)

**ID:** TASK-017  
**Title:** Implement Integration Tests (80% Coverage)  
**Component:** Testing / QA  
**Complexity:** Large (3-4 hours)  
**Dependencies:** TASK-003, TASK-004, TASK-005, TASK-006, TASK-009, TASK-010, TASK-011, TASK-012, TASK-013, TASK-014, TASK-015, TASK-016  
**Blocks:** None (gating task - all implementation should be testable)

**Description:**
Create comprehensive integration tests covering all CRUD operations, validation, authorization, pagination, and error scenarios. Target 80% code coverage for service and controller layers.

**Acceptance Criteria:**

**Test Structure:**
- [ ] Test classes created in `src/test/java/com/epam/book_review_svc/`:
  - `controller/BookControllerIntegrationTest.java`
  - `service/BookServiceIntegrationTest.java`
  - `repository/JsonFileBookRepositoryIntegrationTest.java`
  - `service/BookValidatorTest.java`
  - `exception/GlobalExceptionHandlerTest.java`

**BookControllerIntegrationTest (Spring Boot integration tests):**
- [ ] Extends BaseIntegrationTest
- [ ] Use MockMvc for HTTP testing
- [ ] Tests for POST /api/books:
  - Valid request → HTTP 201 + Location header + BookResponse
  - Missing required field → HTTP 400 + validation errors
  - Invalid ISBN format → HTTP 400 + error message
  - Future publication date → HTTP 400 + error message
  - Duplicate ISBN → HTTP 409 Conflict
- [ ] Tests for GET /api/books:
  - Valid request with defaults → HTTP 200 + PagedResponse
  - With pagination params (page, size) → correct slice + metadata
  - page < 0 → HTTP 400
  - size > 100 → HTTP 400
  - With sort param → correctly sorted results
  - Empty list → HTTP 200 with empty content
- [ ] Tests for GET /api/books/{id}:
  - Valid ID → HTTP 200 + BookResponse
  - Invalid ID → HTTP 404 Not Found
- [ ] Tests for PUT /api/books/{id}:
  - Valid update as creator → HTTP 200 + updated book
  - Update as different user → HTTP 403 Forbidden
  - Invalid data in update → HTTP 400 + validation error
  - ISBN conflict on update → HTTP 409 Conflict
  - Book not found → HTTP 404
- [ ] Tests for DELETE /api/books/{id}:
  - Valid delete as creator → HTTP 204 No Content
  - Delete as different user → HTTP 403 Forbidden
  - Book not found → HTTP 404

**BookServiceIntegrationTest:**
- [ ] Unit tests for service layer (not requiring HTTP)
- [ ] Tests for createBook():
  - Valid create → returns BookResponse with auto-generated ID, timestamps
  - ISBN conflict → throws ConflictException
  - Invalid validation → throws ValidationException with all errors
- [ ] Tests for listBooks():
  - Pagination calculations correct
  - Sorting works (desc, asc)
  - Edge cases: last page, empty list, single item
- [ ] Tests for getBook():
  - Found → returns BookResponse
  - Not found → throws NotFoundException
- [ ] Tests for updateBook():
  - Creator can update → returns updated BookResponse
  - Non-creator cannot → throws ForbiddenException
  - Immutable fields preserved (createdBy, createdAt)
  - Mutable fields updated
- [ ] Tests for deleteBook():
  - Creator can delete
  - Non-creator cannot
  - Subsequent getBook() throws NotFoundException

**BookValidatorTest:**
- [ ] Tests for each validation rule:
  - Title validation (empty, too long, valid)
  - Author validation (empty, too long, valid)
  - ISBN validation (wrong format, 10 digits, 13 digits, with hyphens)
  - Genre validation (invalid genre, valid genre)
  - Publication date validation (future date, past date, today)
- [ ] Tests for aggregate validation:
  - Multiple validation errors collected in single response
  - Valid request returns empty error list

**GlobalExceptionHandlerTest:**
- [ ] Tests for each exception handler:
  - NotFoundException → HTTP 404 + ErrorResponse
  - ForbiddenException → HTTP 403 + ErrorResponse
  - ConflictException → HTTP 409 + ErrorResponse
  - ValidationException → HTTP 400 + ErrorResponse with FieldError[]
- [ ] Tests for Spring validation errors:
  - MethodArgumentNotValidException → HTTP 400 + field errors

**JsonFileBookRepositoryIntegrationTest:**
- [ ] Tests for file I/O:
  - Save creates valid JSON file
  - Find by ID works
  - Find by ISBN works
  - Update modifies file correctly
  - Delete removes from file
  - Load from file works
  - Missing file handled gracefully (returns empty list)

**Test Configuration:**
- [ ] Use `@SpringBootTest` with embedded server
- [ ] Mock data fixtures (from TASK-003)
- [ ] Cleanup after each test (e.g., `@BeforeEach` clear file)
- [ ] Assert statements using AssertJ for readability

**Coverage Targets:**
- [ ] Service layer: >85%
- [ ] Controller layer: >80%
- [ ] Repository layer: >80%
- [ ] Validator: >90%
- [ ] Overall: >80%

**Effort Estimate:** 3-4 hours

**Files Affected:**
- `src/test/java/com/epam/book_review_svc/controller/BookControllerIntegrationTest.java`
- `src/test/java/com/epam/book_review_svc/service/BookServiceIntegrationTest.java`
- `src/test/java/com/epam/book_review_svc/service/BookValidatorTest.java`
- `src/test/java/com/epam/book_review_svc/repository/JsonFileBookRepositoryIntegrationTest.java`
- `src/test/java/com/epam/book_review_svc/exception/GlobalExceptionHandlerTest.java`

**Success Definition:**
- [ ] `./gradlew test` passes all tests
- [ ] Code coverage report shows >80% for main classes
- [ ] All CRUD scenarios tested
- [ ] All error scenarios tested
- [ ] All validation rules tested

---

#### TASK-018: Swagger Documentation & Annotations

**ID:** TASK-018  
**Title:** Swagger Documentation & Annotations  
**Component:** Documentation  
**Complexity:** Small (1-2 hours)  
**Dependencies:** TASK-015, TASK-016  
**Blocks:** None (final polish task)

**Description:**
Add comprehensive Swagger/OpenAPI annotations to all endpoints. Ensure auto-generated documentation is complete and accurate.

**Acceptance Criteria:**
- [ ] Controller-level annotations:
  - `@Tag(name = "Books", description = "Book Catalog Management APIs")`
  - `@RequestMapping("/api/books")` with description
- [ ] Endpoint annotations for each method:
  - `@Operation(summary = "...", description = "...")`
  - `@ApiResponses` documenting all response codes:
    - 200/201/204 for success
    - 400 for validation/bad request
    - 403 for authorization failure
    - 404 for not found
    - 409 for conflict
  - `@ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class)))`
- [ ] Request/response schema documentation:
  - `@Schema` on DTO fields with descriptions
  - `@Schema(example = "...")` for example values
- [ ] Parameter documentation:
  - `@Parameter(description = "...")` on @PathVariable, @RequestParam
  - Example: `@Parameter(description = "Book ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")`
- [ ] Generated Swagger UI should show:
  - All 5 endpoints grouped under "Books" tag
  - Complete request/response schemas
  - Example requests/responses
  - Status codes and error messages
  - Parameter descriptions
- [ ] Access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`
- [ ] OpenAPI spec available at: `http://localhost:8080/v3/api-docs`

**Effort Estimate:** 1-2 hours

**Files Affected:**
- `src/main/java/com/epam/book_review_svc/controller/BookController.java` (annotations added)
- `src/main/java/com/epam/book_review_svc/model/dto/BookResponse.java` (schema annotations)
- `src/main/java/com/epam/book_review_svc/model/dto/CreateBookRequest.java` (schema annotations)
- `src/main/java/com/epam/book_review_svc/model/dto/UpdateBookRequest.java` (schema annotations)
- `src/main/java/com/epam/book_review_svc/model/dto/PagedResponse.java` (schema annotations)

**Success Definition:**
```
When running application: ./gradlew bootRun
- Navigate to http://localhost:8080/swagger-ui/index.html
- See all 5 endpoints listed under "Books" tag
- Click "Try it out" on any endpoint
- See auto-filled examples and descriptions
- Submit test requests directly from UI
```

---

## Dependency Graph

```
                    TASK-001 (Setup)
                         │
                    ┌────┴────┐
                    │         │
               TASK-002   TASK-003
               (Config)   (Tests)
                    │         │
              ┌─────┴─────────┘
              │
    ┌─────────┼─────────┐
    │         │         │
TASK-004  TASK-005  TASK-006
(Models)  (DTOs)   (Errors)
    │       │         │
    └───┬───┴────┬────┘
        │        │
    TASK-007 TASK-009
    (Repo    (Validator)
    Interface)  │
        │       │
        │   ┌───┴─────┐
        │   │         │
    TASK-008 TASK-010 TASK-014
    (Repo   (Service) (Exception
    Impl)   create   Handler)
        │       │         │
        ├───┬───┤    ┌────┤
        │   │   │    │    │
    TASK-011 TASK-012 TASK-013
    (List)   (Get)   (Update/Delete)
        │       │         │
        └───┬───┴────┬────┘
            │        │
        TASK-015 TASK-016
        (Controller (Controller
        create/    update/
        list/get)  delete)
            │        │
            └───┬────┘
                │
            TASK-017
            (Integration
            Tests)
                │
            TASK-018
            (Swagger
            Docs)
```

---

## Critical Path Analysis

**Fastest Route Through Dependencies (Sequential):**

```
TASK-001 (1-2h)
  ↓
TASK-002 (1-2h)
  ↓
TASK-004 (1h)
  ↓
TASK-007 (1h)
  ↓
TASK-008 (2-3h)
  ↓
TASK-009 (2-3h)
  ↓
TASK-010 (2-3h)
  ↓
TASK-014 (2-3h)
  ↓
TASK-015 (2-3h)
  ↓
TASK-017 (3-4h)
  ↓
TASK-018 (1-2h)

CRITICAL PATH TOTAL: 19-30 hours (~5-8 days at 4-6 hrs/day intensive focus)
```

**Can Run in Parallel (after TASK-001 & TASK-002 complete):**
- TASK-003 (test infrastructure)
- TASK-004, TASK-005, TASK-006 (all models)
- Can continue in parallel with TASK-007/008 (repository)

**Example 3-Developer Parallel Schedule (2-week timeline):**

| Day | Dev 1 | Dev 2 | Dev 3 |
|-----|-------|-------|-------|
| 1 | TASK-001 (setup) | - | - |
| 1-2 | TASK-002 (config) | - | - |
| 2 | TASK-004 (Book model) | TASK-005 (DTOs) | TASK-003 (test setup) |
| 2-3 | TASK-006 (error DTOs) | TASK-009 (validator) | TASK-007 (repo interface) |
| 3 | TASK-008 (repo impl) | TASK-010 (createBook) | TASK-011 (listBooks) |
| 3-4 | TASK-012 (getBook) | TASK-013 (update/delete) | TASK-014 (exception handler) |
| 4-5 | TASK-015 (controller p1) | TASK-016 (controller p2) | TASK-017 (tests) |
| 5 | - | TASK-018 (swagger) | - |

---

## Parallel Workstreams

**Workstream 1: Repository & Data Access**
- TASK-004 (Book model) → TASK-007 (repo interface) → TASK-008 (repo impl)
- Owner: Developer A
- Timeline: Days 2-3
- Estimated: 5-6 hours

**Workstream 2: Service & Business Logic**
- TASK-005 (DTOs) → TASK-009 (validator) → TASK-010 (create) → TASK-011 (list) → TASK-012 (get) → TASK-013 (update/delete)
- Owner: Developer B
- Timeline: Days 2-4
- Estimated: 13-16 hours

**Workstream 3: API & Exception Handling**
- TASK-006 (error DTOs) → TASK-014 (exception handler) → TASK-015 (controller p1) → TASK-016 (controller p2)
- Owner: Developer C
- Timeline: Days 2-5
- Estimated: 8-10 hours

**Workstream 4: Testing & Documentation**
- TASK-003 (test setup) → TASK-017 (tests) → TASK-018 (swagger)
- Owner: Developer D (or done serially by integration developer)
- Timeline: Days 2-5
- Estimated: 5-7 hours

---

## Blocked Tasks Summary

**No hard external dependencies block Phase 1 tasks.** All tasks can start after project setup (TASK-001 & TASK-002).

**Minor blockers (within Phase 1):**
- TASK-010 cannot start until TASK-009 (validator) is complete
- TASK-015 cannot start until TASK-010 (service) is complete
- TASK-017 (tests) is gating - must pass before Phase 2

**Phase 2 will introduce:**
- Spring Security configuration (needed for full authorization testing)
- Authentication provider integration

---

## Implementation Notes & Constraints

### JSON File Storage Notes
- All operations load entire books.json into memory (O(n) complexity)
- Acceptable for <100k books on single instance
- Each CRUD operation does read-modify-write cycle
- No concurrency control in Phase 1 (will add in Phase 3 if needed)

### Date/Time Handling
- **publicationDate:** LocalDate, format "yyyy-MM-dd" (no time)
- **createdAt:** OffsetDateTime, format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" (UTC, milliseconds)
- Jackson must be configured for these formats (see TASK-002)

### Pagination Strategy
- In-memory only (O(n log n) sort + slice)
- Default: 20 items per page
- Max page size: 100 items
- Default sort: "createdAt,desc" (newest first)

### Authorization Strategy (Phase 1 placeholder)
- All public endpoints (no auth required for MVP)
- Service layer checks authorization rules (placeholder methods)
- Phase 2 will integrate Spring Security to populate Principal

### Error Handling Strategy
- All domain exceptions extend ApiException
- GlobalExceptionHandler converts to ErrorResponse
- Validation errors aggregated (return all, not fail-fast)

---

## Success Criteria for Phase 1 Completion

**Must Have:**
- [ ] All 18 tasks completed
- [ ] `./gradlew build` passes
- [ ] `./gradlew test` passes with >80% coverage
- [ ] Application starts: `./gradlew bootRun`
- [ ] All 5 endpoints operational (via Swagger UI or curl)
- [ ] Request/response format matches specification
- [ ] All HTTP status codes correct
- [ ] All error scenarios handled gracefully

**Should Have:**
- [ ] Swagger UI accessible and complete
- [ ] Integration tests demonstrate all scenarios
- [ ] Logging shows request/response flow

**Nice to Have:**
- [ ] Performance benchmarks documented
- [ ] Load test with sample data (e.g., 1000 books)
- [ ] Docker image builds successfully

---

## Post-Phase 1: Next Steps (Phase 2)

Phase 2 will focus on Authorization & Security:

1. **Spring Security Configuration**
   - Integrate authentication provider (JWT, OAuth2, etc.)
   - Configure Principal extraction
   - Enable @PreAuthorize annotations

2. **Role-Based Access Control (RBAC)**
   - Implement hasAuthorRole() with actual Spring Security checks
   - Enable creator-only authorization enforcement
   - Add authorization tests

3. **Security Headers & CORS**
   - Add security headers (X-Frame-Options, X-Content-Type-Options, etc.)
   - Configure CORS policy
   - Implement rate limiting

4. **Request Logging Filter**
   - Add HTTP request/response logging
   - Implement timing metrics

**Estimated Effort:** 1-2 weeks (similar to Phase 1)

---

## Glossary & Quick Reference

- **CRUD:** Create, Read, Update, Delete
- **DTO:** Data Transfer Object (API models separate from domain)
- **UUID:** Universally Unique Identifier (32-char hex string)
- **ISO-8601:** International date/time format (YYYY-MM-DD)
- **Pagination:** Dividing large result sets into manageable pages
- **Repository:** Pattern that abstracts data persistence
- **Bean Validation:** JSR-380 standard (@Valid, @NotBlank, @NotNull)
- **Jackson:** JSON serialization/deserialization library
- **Springdoc:** Library auto-generating Swagger from Spring annotations
- **MockMvc:** Spring testing library for HTTP endpoint testing
- **AssertJ:** Fluent assertion library for cleaner test code

---

**Status:** READY FOR IMPLEMENTATION  
**Next Action:** Developers can pick TASK-001 and begin immediately


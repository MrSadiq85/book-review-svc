# Implementation Plan: Book Review Service REST API

## Executive Summary

The Book Review Service is a Spring Boot 4.1.1 REST API built with Java 25 that manages book reviews with comprehensive validation and centralized error handling. This implementation plan decomposes the architecture into 15 actionable tasks organized by layer and dependency. The service uses a 3-tier layered architecture (Controller → Service → Repository) with file-based JSON persistence, two-tier validation, and centralized exception handling.

- **Total Tasks:** 15 implementation tasks
- **Critical Path Length:** ~3 weeks (sequential execution with parallel opportunities)
- **Parallel Workstreams:** 3 independent paths (can run concurrently after configuration)
- **Key Risks/Constraints:**
  - Hard blocking dependency: Configuration must complete before any component development
  - File I/O operations require careful error handling and transactional consistency
  - Book validation depends on books.json file existence and format
  - Testing phase depends on all code implementation completing first

---

## Task Breakdown

### Task Breakdown Table

| Task ID | Task Name | Component | Complexity | Dependencies | Blocked By | Est. Hours |
|---------|-----------|-----------|-----------|-------------|-----------|-----------|
| IMPL-001 | Setup Configuration & Properties | Configuration | S | None | None | 2 |
| IMPL-002 | Create Domain Entities (Review, Book) | Model/Entity | S | IMPL-001 | None | 3 |
| IMPL-003 | Create Request/Response DTOs | Model/DTO | S | IMPL-001 | None | 3 |
| IMPL-004 | Create Custom Exception Hierarchy | Exception | S | IMPL-001 | None | 2 |
| IMPL-005 | Implement BookRepository | Repository | M | IMPL-001, IMPL-002 | None | 4 |
| IMPL-006 | Implement ReviewRepository | Repository | M | IMPL-001, IMPL-002 | None | 4 |
| IMPL-007 | Implement BookValidationService | Service | M | IMPL-005 | None | 3 |
| IMPL-008 | Implement ReviewService | Service | L | IMPL-006, IMPL-007, IMPL-004 | None | 6 |
| IMPL-009 | Implement GlobalExceptionHandler | Exception Handler | M | IMPL-004 | None | 3 |
| IMPL-010 | Implement ReviewController | Controller | M | IMPL-003, IMPL-008, IMPL-009 | None | 4 |
| IMPL-011 | Add Swagger/OpenAPI Annotations | Documentation | S | IMPL-010 | None | 2 |
| IMPL-012 | Create reviews.json & books.json Data Files | Data | S | IMPL-002 | None | 1 |
| IMPL-013 | Write Unit Tests (Service Layer) | Testing | M | IMPL-008, IMPL-007 | None | 5 |
| IMPL-014 | Write Integration Tests (End-to-End) | Testing | L | IMPL-010, IMPL-012 | None | 7 |
| IMPL-015 | Manual Testing & Bug Fixes | QA | M | IMPL-014 | None | 4 |

**Total Estimated Effort:** ~53 hours

---

## Detailed Task Descriptions

### IMPL-001: Setup Configuration & Properties

**Status:** Pending  
**Complexity:** Small  
**Est. Hours:** 2  
**Dependencies:** None  
**Blocks:** All other tasks (hard dependency)

**Description:**
Set up Spring Boot application configuration and baseline properties file. This is a prerequisite task that must complete before any other development begins.

**Files to Create/Modify:**
- `/src/main/resources/application.properties` - Create with all required configuration

**What Needs to Be Done:**
1. Create `application.properties` with:
   - Server port (8080)
   - Jackson serialization settings (UTC timezone, pretty printing)
   - Logging configuration (SLF4J/Logback levels)
   - Swagger/OpenAPI documentation settings
   - Error handling configuration

**Configuration Specifications:**
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/
server.error.include-message=always
server.error.include-stacktrace=on_param

# Jackson Configuration
spring.jackson.default-property-inclusion=non_null
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC

# Logging Configuration
logging.level.root=INFO
logging.level.com.epam.book_review_svc=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Swagger/OpenAPI Configuration
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
```

**Acceptance Criteria:**
- application.properties file exists with all required properties
- Server starts without configuration errors
- Logging is configured and produces output
- Jackson is configured for UTC timezone and pretty printing
- Swagger UI is accessible at `http://localhost:8080/swagger-ui/index.html`

**Implementation Notes:**
- This task is a hard blocker; no other tasks can start until configuration is complete
- Use best practices for Spring Boot configuration (externalize secrets later)
- Ensure logging doesn't produce noise but captures DEBUG level for the app package

---

### IMPL-002: Create Domain Entities (Review, Book)

**Status:** Pending  
**Complexity:** Small  
**Est. Hours:** 3  
**Dependencies:** IMPL-001 (configuration)  
**Blocks:** IMPL-005, IMPL-006

**Description:**
Create Lombok-annotated Java POJOs representing domain entities. These are the core data models persisted to JSON files.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/model/entity/Review.java`
- `/src/main/java/com/epam/book_review_svc/model/entity/Book.java`

**What Needs to Be Done:**

**Review.java:**
1. Create entity with fields:
   - `id` (String) - UUID primary key
   - `bookId` (String) - Reference to Book
   - `rating` (Integer) - 1-5 scale
   - `reviewText` (String) - Review content
   - `createdAt` (LocalDateTime) - Creation timestamp
   - `updatedAt` (LocalDateTime) - Last modification timestamp
2. Apply Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
3. Apply Jackson annotation: `@JsonInclude(JsonInclude.Include.NON_NULL)`
4. Ensure LocalDateTime serialization works with Jackson

**Book.java:**
1. Create entity with fields:
   - `id` (String) - UUID primary key
   - `title` (String) - Book title
   - `author` (String) - Book author
   - `isbn` (String) - ISBN code
   - `publicationYear` (Integer) - Year published
   - `genre` (String) - Book genre
2. Apply same Lombok and Jackson annotations as Review

**Acceptance Criteria:**
- Both entity classes compile without errors
- Lombok generates constructors, getters, setters, toString()
- Jackson can serialize/deserialize LocalDateTime objects
- Entities have no business logic (purely data containers)
- Classes are in correct package: `com.epam.book_review_svc.model.entity`

**Implementation Notes:**
- Use `java.time.LocalDateTime` for timestamp fields (ISO 8601 format)
- Ensure `@JsonInclude` excludes null fields from JSON output
- These entities are mapped directly from JSON files using ObjectMapper

---

### IMPL-003: Create Request/Response DTOs

**Status:** Pending  
**Complexity:** Small  
**Est. Hours:** 3  
**Dependencies:** IMPL-001 (configuration)  
**Blocks:** IMPL-010

**Description:**
Create Data Transfer Objects (DTOs) for API requests and responses. DTOs decouple API contracts from domain entities and provide validation boundaries.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/model/dto/request/CreateReviewRequest.java`
- `/src/main/java/com/epam/book_review_svc/model/dto/request/UpdateReviewRequest.java`
- `/src/main/java/com/epam/book_review_svc/model/dto/response/ReviewResponse.java`
- `/src/main/java/com/epam/book_review_svc/model/dto/response/ErrorResponse.java`

**What Needs to Be Done:**

**CreateReviewRequest.java:**
1. Fields with Bean Validation annotations:
   - `bookId` (@NotNull, @NotBlank)
   - `rating` (@NotNull, @Min(1), @Max(5))
   - `reviewText` (@NotBlank, @Size(min=10, max=2000))
2. Add @Schema annotations for Swagger documentation
3. Use Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor)

**UpdateReviewRequest.java:**
1. Same fields as CreateReviewRequest (all updatable)
2. Same validation annotations
3. All fields required (no partial updates)

**ReviewResponse.java:**
1. Fields (no validation annotations):
   - `id`, `bookId`, `rating`, `reviewText`, `createdAt`, `updatedAt`
2. Add @Schema annotations for Swagger
3. Use Lombok annotations

**ErrorResponse.java:**
1. Fields:
   - `status` (int) - HTTP status code
   - `message` (String) - Error message
   - `errorType` (String) - Exception class name
   - `timestamp` (LocalDateTime) - When error occurred
   - `path` (String) - Request URI
2. Use @Builder annotation for fluent construction
3. Use Lombok @Data annotation
4. Add @Schema annotations for Swagger

**Acceptance Criteria:**
- All 4 DTO classes compile without errors
- Bean Validation annotations are properly applied
- @Schema annotations exist for Swagger documentation
- No business logic in DTOs (purely data containers)
- Lombok generates required constructors and methods
- DTOs are in correct packages under `com.epam.book_review_svc.model.dto`

**Implementation Notes:**
- Request DTOs validate format/constraints at controller boundary
- Response DTOs shape API output (no secrets/internal fields exposed)
- ErrorResponse uses @Builder for easy error construction in exception handler
- @Schema annotations include examples for Swagger UI demonstration

---

### IMPL-004: Create Custom Exception Hierarchy

**Status:** Pending  
**Complexity:** Small  
**Est. Hours:** 2  
**Dependencies:** IMPL-001 (configuration)  
**Blocks:** IMPL-008, IMPL-009

**Description:**
Create a domain-specific exception hierarchy for meaningful error handling and HTTP status code mapping. All custom exceptions extend a base `BusinessException` class.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/exception/BusinessException.java`
- `/src/main/java/com/epam/book_review_svc/exception/ReviewNotFoundException.java`
- `/src/main/java/com/epam/book_review_svc/exception/BookNotFoundException.java`
- `/src/main/java/com/epam/book_review_svc/exception/InvalidRatingException.java`
- `/src/main/java/com/epam/book_review_svc/exception/DataAccessException.java`

**What Needs to Be Done:**

**BusinessException.java (Abstract Base Class):**
1. Extend `RuntimeException`
2. Fields: None (inherits from RuntimeException)
3. Methods:
   - Constructor: `BusinessException(String message)`
   - Constructor: `BusinessException(String message, Throwable cause)`
   - Abstract method: `getHttpStatusCode(): int`
4. Purpose: All domain exceptions inherit from this class

**Specific Exception Classes:**
1. **ReviewNotFoundException** → HTTP 404
   - Message: "Review with ID {id} not found"
   - getHttpStatusCode() returns 404

2. **BookNotFoundException** → HTTP 400 (Bad Request)
   - Message: "Book with ID {id} not found"
   - getHttpStatusCode() returns 400

3. **InvalidRatingException** → HTTP 400 (Bad Request)
   - Message: "Rating must be between 1 and 5"
   - getHttpStatusCode() returns 400

4. **DataAccessException** → HTTP 500 (Internal Server Error)
   - Message: "Failed to read/write file: {message}"
   - Wraps IOException as cause
   - getHttpStatusCode() returns 500

**Acceptance Criteria:**
- All 5 exception classes compile without errors
- BusinessException is abstract and cannot be instantiated directly
- Each specific exception implements getHttpStatusCode() correctly
- Exceptions can be caught by type for specific error handling
- Stack traces are preserved through cause chain
- Exception messages are descriptive and include context data

**Implementation Notes:**
- Use @Getter annotation from Lombok for HTTP status code field (if used)
- Exceptions are unchecked (extend RuntimeException) for flexibility
- Each exception maps to a specific HTTP status code as per architecture
- GlobalExceptionHandler (IMPL-009) will use getHttpStatusCode() to determine response status

---

### IMPL-005: Implement BookRepository

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 4  
**Dependencies:** IMPL-001, IMPL-002  
**Blocks:** IMPL-007

**Description:**
Implement repository for reading book data from books.json file. This is a straightforward file I/O operation using Jackson ObjectMapper.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/repository/BookRepository.java`

**What Needs to Be Done:**
1. Create class with @Repository annotation
2. Inject ObjectMapper via constructor (Spring will auto-wire)
3. Implement method: `List<Book> readAllBooks()`
   - Read from ClassPathResource("data/books.json")
   - Use ObjectMapper to deserialize JSON array to Book[]
   - Handle IOException and log errors
   - Return empty list if file doesn't exist
   - Throw DataAccessException on read failure
4. Add SLF4J logging:
   - Warn if file doesn't exist
   - Error log on IOException with stack trace
   - Info log on successful read (optional)

**Method Signature:**
```java
@Repository
public class BookRepository {
    private static final Logger log = LoggerFactory.getLogger(BookRepository.class);
    private final ObjectMapper objectMapper;
    
    public BookRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public List<Book> readAllBooks() {
        try {
            Resource resource = new ClassPathResource("data/books.json");
            File file = resource.getFile();
            
            if (!file.exists()) {
                log.warn("Books file does not exist. Returning empty list.");
                return new ArrayList<>();
            }
            
            Book[] books = objectMapper.readValue(file, Book[].class);
            return Arrays.asList(books);
        } catch (IOException e) {
            log.error("Error reading books from file", e);
            throw new DataAccessException("Failed to read books from file", e);
        }
    }
}
```

**Acceptance Criteria:**
- BookRepository class compiles and has @Repository annotation
- Constructor accepts ObjectMapper (Spring dependency injection)
- readAllBooks() successfully deserializes books.json
- Returns empty list if file doesn't exist (graceful handling)
- Throws DataAccessException on read errors
- IOException is wrapped in DataAccessException
- Logging uses SLF4J and provides useful debug information
- No caching; file is read fresh on each call
- Class is in package: `com.epam.book_review_svc.repository`

**Implementation Notes:**
- ClassPathResource automatically locates files in src/main/resources
- ObjectMapper bean is auto-configured by Spring Boot
- No caching per architecture decision for simplicity
- This enables BookValidationService to always have current book list

---

### IMPL-006: Implement ReviewRepository

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 4  
**Dependencies:** IMPL-001, IMPL-002  
**Blocks:** IMPL-008

**Description:**
Implement repository for reading and writing review data to reviews.json file. This handles both read and write operations with proper error handling and file management.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/repository/ReviewRepository.java`

**What Needs to Be Done:**
1. Create class with @Repository annotation
2. Inject ObjectMapper via constructor
3. Implement method: `List<Review> readAllReviews()`
   - Read from ClassPathResource("data/reviews.json")
   - Deserialize to Review[] array
   - Handle IOException appropriately
   - Return empty list if file doesn't exist
   - Throw DataAccessException on failure
4. Implement method: `void writeReviews(List<Review> reviews)`
   - Get file path from ClassPathResource
   - Use ObjectMapper.writerWithDefaultPrettyPrinter() for formatting
   - Write entire list (overwrites existing reviews.json)
   - Throw DataAccessException on I/O failure
5. Add comprehensive logging at debug and error levels

**Method Signatures:**
```java
@Repository
public class ReviewRepository {
    private static final Logger log = LoggerFactory.getLogger(ReviewRepository.class);
    private final ObjectMapper objectMapper;
    
    public ReviewRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public List<Review> readAllReviews() {
        // Read implementation
    }
    
    public void writeReviews(List<Review> reviews) {
        // Write implementation
    }
}
```

**Acceptance Criteria:**
- ReviewRepository class compiles with @Repository annotation
- readAllReviews() correctly deserializes reviews.json
- writeReviews() successfully persists updated list to reviews.json
- Both methods handle IOException and throw DataAccessException
- File is created if it doesn't exist (for writeReviews)
- Pretty printing is enabled for human-readable JSON
- Logging provides visibility into read/write operations
- Empty list returned if reviews.json doesn't exist on read
- All reviews in list are written (no partial updates)
- Class is in package: `com.epam.book_review_svc.repository`

**Implementation Notes:**
- ReviewRepository is write-aware unlike BookRepository
- This is where transactions at file level happen (read-modify-write)
- Pretty printing ensures reviews.json is human-readable
- Provides audit trail through logging
- Used by ReviewService for CRUD operations

---

### IMPL-007: Implement BookValidationService

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 3  
**Dependencies:** IMPL-005 (BookRepository)  
**Blocks:** IMPL-008

**Description:**
Implement service for validating book existence. This service reads books.json and checks if a requested bookId exists. No caching per architecture decision.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/service/BookValidationService.java`

**What Needs to Be Done:**
1. Create class with @Service annotation
2. Inject BookRepository via constructor
3. Implement method: `boolean isBookValid(String bookId)`
   - Check if bookId is null or blank (return false)
   - Call bookRepository.readAllBooks()
   - Stream through books and check if any book.id matches bookId
   - Return true if match found, false otherwise
4. Implement method: `List<BookResponse> getAllBooks()`
   - Call bookRepository.readAllBooks()
   - Map each Book to BookResponse (id, title, author)
   - Return list of BookResponse objects
5. Add logging for validation failures

**Method Signatures:**
```java
@Service
public class BookValidationService {
    private final BookRepository bookRepository;
    
    public BookValidationService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    public boolean isBookValid(String bookId) {
        // Implementation
    }
    
    public List<BookResponse> getAllBooks() {
        // Implementation
    }
}
```

**Acceptance Criteria:**
- BookValidationService class compiles with @Service annotation
- isBookValid() returns true if book with id exists in books.json
- isBookValid() returns false if bookId doesn't exist
- isBookValid() returns false for null or blank bookId
- getAllBooks() returns list of BookResponse (not full Book entities)
- No caching; books.json read on each validation call
- Logging tracks validation attempts (optional for performance)
- Class is in package: `com.epam.book_review_svc.service`

**Implementation Notes:**
- No caching per architecture (simplicity over performance)
- Reads books.json on every validation for data consistency
- BookResponse DTO is created in IMPL-003 (may need to add if missing)
- Used by ReviewService to validate bookId before creating/updating reviews
- Small datasets can tolerate fresh file reads

---

### IMPL-008: Implement ReviewService

**Status:** Pending  
**Complexity:** Large  
**Est. Hours:** 6  
**Dependencies:** IMPL-006, IMPL-007, IMPL-004  
**Blocks:** IMPL-010

**Description:**
Implement core business logic service for review management. This is the largest task, orchestrating repository operations, validation, and exception handling.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/service/ReviewService.java`

**What Needs to Be Done:**
1. Create class with @Service annotation
2. Inject dependencies via constructor:
   - ReviewRepository reviewRepository
   - BookValidationService bookValidationService
3. Implement method: `ReviewResponse createReview(CreateReviewRequest request)`
   - Validate book exists (throw BookNotFoundException if not)
   - Validate rating is 1-5 (throw InvalidRatingException if not)
   - Create new Review entity with UUID id
   - Set timestamps (createdAt = now, updatedAt = now)
   - Read all reviews from repository
   - Add new review to list
   - Write updated list to repository
   - Map Review to ReviewResponse and return
4. Implement method: `ReviewResponse updateReview(String id, UpdateReviewRequest request)`
   - Find review by id (throw ReviewNotFoundException if not found)
   - Validate new book exists if bookId changed
   - Validate rating is 1-5
   - Update review fields
   - Set updatedAt to current time
   - Read all reviews, update in list, write back
   - Return ReviewResponse
5. Implement method: `void deleteReview(String id)`
   - Verify review exists (throw ReviewNotFoundException if not)
   - Read all reviews
   - Remove review with matching id
   - Write updated list
   - Throw ReviewNotFoundException if removal failed
6. Implement method: `ReviewResponse getReviewById(String id)`
   - Find review by id (throw ReviewNotFoundException if not found)
   - Map to ReviewResponse and return
7. Implement method: `List<ReviewResponse> getAllReviews()`
   - Read all reviews from repository
   - Map each to ReviewResponse
   - Return list
8. Add helper methods:
   - `private Review findReviewById(String id)` - Find or throw
   - `private void validateRating(Integer rating)` - Validate 1-5 range
   - `private ReviewResponse mapToResponse(Review review)` - Entity to DTO
9. Add logging for operations and errors

**Method Signatures:**
```java
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookValidationService bookValidationService;
    
    public ReviewService(ReviewRepository reviewRepository, 
                        BookValidationService bookValidationService) {
        this.reviewRepository = reviewRepository;
        this.bookValidationService = bookValidationService;
    }
    
    public ReviewResponse createReview(CreateReviewRequest request) { }
    public ReviewResponse updateReview(String id, UpdateReviewRequest request) { }
    public void deleteReview(String id) { }
    public ReviewResponse getReviewById(String id) { }
    public List<ReviewResponse> getAllReviews() { }
    
    private Review findReviewById(String id) { }
    private void validateRating(Integer rating) { }
    private ReviewResponse mapToResponse(Review review) { }
}
```

**Acceptance Criteria:**
- ReviewService class compiles with @Service annotation
- createReview() validates book and rating before persisting
- updateReview() validates book (if changed) and rating
- deleteReview() removes review and persists changes
- getReviewById() returns ReviewResponse for valid id
- getAllReviews() returns list of all ReviewResponse objects
- All methods throw appropriate custom exceptions (ReviewNotFoundException, BookNotFoundException, InvalidRatingException)
- Timestamps are set correctly (createdAt on create, updatedAt on every change)
- UUID generated for new reviews using UUID.randomUUID().toString()
- Reviews are mapped to responses via private mapToResponse() method
- Logging tracks all operations and errors
- Class is in package: `com.epam.book_review_svc.service`

**Implementation Notes:**
- This is the main business logic orchestrator
- All persistence operations go through ReviewRepository
- All book validation goes through BookValidationService
- Exceptions are thrown (not caught) for GlobalExceptionHandler to translate
- No caching; repositories handle fresh reads/writes
- Transactional consistency at file level (read-modify-write pattern)
- This task is most complex and should be implemented carefully

---

### IMPL-009: Implement GlobalExceptionHandler

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 3  
**Dependencies:** IMPL-004 (Custom exceptions)  
**Blocks:** IMPL-010

**Description:**
Implement centralized exception handler using Spring's @ControllerAdvice annotation. This translates all exceptions to consistent error responses with appropriate HTTP status codes.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/exception/GlobalExceptionHandler.java`

**What Needs to Be Done:**
1. Create class with @ControllerAdvice annotation
2. Add SLF4J logger
3. Implement method: `ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request)`
   - Extract HTTP status code from exception.getHttpStatusCode()
   - Build ErrorResponse with:
     - status: HTTP status code
     - message: exception message
     - errorType: exception class simple name
     - timestamp: LocalDateTime.now()
     - path: request.getRequestURI()
   - Log error with exception details
   - Return ResponseEntity with ErrorResponse and appropriate HttpStatus
4. Implement method: `ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request)`
   - Extract field validation errors from ex.getBindingResult()
   - Build message from field errors (field: message format)
   - Build ErrorResponse:
     - status: 400 (Bad Request)
     - message: "Validation failed: " + concatenated errors
     - errorType: "MethodArgumentNotValidException"
     - timestamp: now
     - path: request URI
   - Log error
   - Return ResponseEntity with HTTP 400
5. Implement method: `ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request)`
   - Catch-all for unexpected exceptions
   - Build ErrorResponse:
     - status: 500 (Internal Server Error)
     - message: "An unexpected error occurred"
     - errorType: exception class simple name
     - timestamp: now
     - path: request URI
   - Log error with full stack trace
   - Return ResponseEntity with HTTP 500
6. Add @ExceptionHandler annotations to each method

**Method Signatures:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex,
        HttpServletRequest request
    ) { }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) { }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) { }
}
```

**Acceptance Criteria:**
- GlobalExceptionHandler compiles with @ControllerAdvice annotation
- handleBusinessException() catches BusinessException and subclasses
- HTTP status code extracted from exception and used in response
- handleValidationException() formats field-level validation errors
- handleGenericException() catches all other exceptions as last resort
- ErrorResponse includes all required fields (status, message, errorType, timestamp, path)
- All three methods log exceptions appropriately (error/warn levels)
- Stack traces included in debug logging
- Class is in package: `com.epam.book_review_svc.exception`

**Implementation Notes:**
- This is applied globally to all controllers via @ControllerAdvice
- Order of @ExceptionHandler methods matters (specific before generic)
- BusinessException handler runs before generic Exception handler
- Validation exception handler deals with Bean Validation failures
- ErrorResponse uses @Builder pattern for fluent construction
- Ensures all endpoints return consistent error format
- Logging provides visibility for debugging production issues

---

### IMPL-010: Implement ReviewController

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 4  
**Dependencies:** IMPL-003, IMPL-008, IMPL-009  
**Blocks:** IMPL-011

**Description:**
Implement REST controller with 5 endpoints for review management. Controller handles HTTP request/response mapping and delegates business logic to ReviewService.

**Files to Create:**
- `/src/main/java/com/epam/book_review_svc/controller/ReviewController.java`

**What Needs to Be Done:**
1. Create class with annotations:
   - @RestController
   - @RequestMapping("/api/reviews")
   - @Tag(name = "Reviews", description = "Book review management APIs")
2. Inject ReviewService via constructor
3. Implement 5 endpoints:

**Endpoint 1: POST /api/reviews (Create Review)**
- Method signature: `ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request)`
- Annotations:
  - @PostMapping
  - @Operation(summary = "Create a new review", description = "...")
  - @ApiResponse for 201 and 400
- Call: reviewService.createReview(request)
- Return: ResponseEntity with status 201 Created and ReviewResponse body

**Endpoint 2: PUT /api/reviews/{id} (Update Review)**
- Method signature: `ResponseEntity<ReviewResponse> updateReview(@PathVariable String id, @Valid @RequestBody UpdateReviewRequest request)`
- Annotations:
  - @PutMapping("/{id}")
  - @Operation(summary = "Update an existing review", description = "...")
  - @ApiResponse for 200, 404, 400
- Call: reviewService.updateReview(id, request)
- Return: ResponseEntity with status 200 OK and ReviewResponse body

**Endpoint 3: DELETE /api/reviews/{id} (Delete Review)**
- Method signature: `ResponseEntity<Void> deleteReview(@PathVariable String id)`
- Annotations:
  - @DeleteMapping("/{id}")
  - @Operation(summary = "Delete a review", description = "...")
  - @ApiResponse for 204 and 404
- Call: reviewService.deleteReview(id)
- Return: ResponseEntity with status 204 No Content

**Endpoint 4: GET /api/reviews/{id} (Get by ID)**
- Method signature: `ResponseEntity<ReviewResponse> getReviewById(@PathVariable String id)`
- Annotations:
  - @GetMapping("/{id}")
  - @Operation(summary = "Get review by ID", description = "...")
  - @ApiResponse for 200 and 404
- Call: reviewService.getReviewById(id)
- Return: ResponseEntity with status 200 OK and ReviewResponse body

**Endpoint 5: GET /api/reviews (Get All)**
- Method signature: `ResponseEntity<List<ReviewResponse>> getAllReviews()`
- Annotations:
  - @GetMapping
  - @Operation(summary = "Get all reviews", description = "...")
  - @ApiResponse for 200
- Call: reviewService.getAllReviews()
- Return: ResponseEntity with status 200 OK and List<ReviewResponse> body

**Acceptance Criteria:**
- ReviewController compiles with @RestController annotation
- All 5 endpoints are implemented with correct HTTP methods
- All POST/PUT endpoints use @Valid for automatic validation
- All endpoints have @Operation and @ApiResponse Swagger annotations
- @PathVariable binding works correctly for {id} path parameter
- @RequestBody binding works correctly for request DTOs
- All endpoints return appropriate HTTP status codes (201, 200, 204, 404, 400)
- All endpoints have correct response types (ReviewResponse, List<ReviewResponse>, Void)
- ReviewService methods are called with correct parameters
- Exceptions thrown by service are handled by GlobalExceptionHandler
- Class is in package: `com.epam.book_review_svc.controller`

**Implementation Notes:**
- Controller does NOT contain business logic (delegated to service)
- Controller does NOT handle exceptions directly (GlobalExceptionHandler handles them)
- @Valid triggers Bean Validation on request DTOs (catches format errors)
- PathVariable "id" is String (UUID format)
- POST returns 201 Created (REST convention)
- PUT returns 200 OK (successful modification)
- DELETE returns 204 No Content (successful deletion, no body)
- GET returns 200 OK with body
- All non-GET endpoints perform mutations on reviewRepository

---

### IMPL-011: Add Swagger/OpenAPI Annotations

**Status:** Pending  
**Complexity:** Small  
**Est. Hours:** 2  
**Dependencies:** IMPL-010 (ReviewController)  
**Blocks:** None (final touches)

**Description:**
Add comprehensive Swagger/OpenAPI annotations to ReviewController for auto-generated API documentation. This improves discoverability and provides interactive Swagger UI.

**Files to Modify:**
- `/src/main/java/com/epam/book_review_svc/controller/ReviewController.java` - Already has some; enhance

**What Needs to Be Done:**
1. Enhance ReviewController with detailed @Operation and @ApiResponse annotations
2. Add @RequestBody and @PathVariable descriptions
3. Ensure all DTOs have complete @Schema annotations
4. Add examples to @Schema annotations for better Swagger UI display
5. Add @ApiResponse descriptions for all error codes (400, 404, 500)
6. Document request/response examples

**Swagger Annotations to Add/Enhance:**

For each endpoint, ensure:
- @Operation(summary = "...", description = "...")
- @ApiResponse(responseCode = "201/200/204", description = "Success")
- @ApiResponse(responseCode = "400", description = "Bad Request - validation or business rule failure")
- @ApiResponse(responseCode = "404", description = "Not Found - review doesn't exist")
- @ApiResponse(responseCode = "500", description = "Internal Server Error")

For DTOs:
- @Schema(description = "...", example = "...")
- All fields documented with @Schema

**Acceptance Criteria:**
- All 5 endpoints documented with @Operation annotations
- All endpoints have @ApiResponse for 200/201/204 (success)
- All endpoints have @ApiResponse for 400, 404 (where applicable)
- All endpoints have @ApiResponse for 500 (catch-all)
- All DTO fields have @Schema with description
- Example values provided in @Schema for common fields
- Swagger UI renders all documentation clearly at `/swagger-ui/index.html`
- No compilation errors related to annotations

**Implementation Notes:**
- Swagger documentation improves API usability for consumers
- This is the final polish task before testing
- Springdoc automatically generates Swagger UI from annotations
- Examples in @Schema help API consumers understand expected values
- Descriptions should be user-friendly (target audience: API consumers)

---

### IMPL-012: Create reviews.json & books.json Data Files

**Status:** Pending  
**Complexity:** Small  
**Est. Hours:** 1  
**Dependencies:** IMPL-002 (Domain entities)  
**Blocks:** IMPL-014

**Description:**
Create initial JSON data files used by repositories. These files serve as the "database" for the application.

**Files to Create:**
- `/src/main/resources/data/reviews.json` - Initially empty array
- `/src/main/resources/data/books.json` - Pre-populated with sample books

**What Needs to Be Done:**

**reviews.json:**
1. Create empty JSON array: `[]`
2. Will be populated when first review is created via API
3. File location: `src/main/resources/data/reviews.json`

**books.json:**
1. Create JSON array with sample books (at least 3-5 books)
2. Each book has: id (UUID), title, author, isbn, publicationYear, genre
3. Example structure:
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "publicationYear": 2008,
    "genre": "Software Engineering"
  }
]
```
4. File location: `src/main/resources/data/books.json`
5. Include variety of books for testing

**Acceptance Criteria:**
- Both JSON files created in correct directory: `src/main/resources/data/`
- reviews.json is valid JSON array (empty initially)
- books.json is valid JSON array with at least 3 sample books
- All book IDs are UUID format
- All required fields present in book objects
- Files are formatted (readable, not minified)
- Files can be read by ObjectMapper without errors
- Data is consistent with Review and Book entity definitions

**Implementation Notes:**
- These are data files, not code
- Create directory `src/main/resources/data/` if it doesn't exist
- Sample books enable testing without API calls to create books
- books.json is read-only for this application (no create book endpoint)
- reviews.json will grow as tests/users create reviews
- Both files should have valid JSON format (proper escaping, etc.)

---

### IMPL-013: Write Unit Tests (Service Layer)

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 5  
**Dependencies:** IMPL-008, IMPL-007  
**Blocks:** None (can run in parallel with other tasks)

**Description:**
Write unit tests for service layer classes (ReviewService, BookValidationService). These tests use mocking to isolate service logic from repositories.

**Files to Create:**
- `/src/test/java/com/epam/book_review_svc/service/ReviewServiceTest.java`
- `/src/test/java/com/epam/book_review_svc/service/BookValidationServiceTest.java`

**What Needs to Be Done:**

**ReviewServiceTest.java:**
1. Setup: Mock ReviewRepository and BookValidationService
2. Test cases for createReview():
   - Success case: Valid request creates review with generated UUID
   - Failure case: BookNotFoundException when book doesn't exist
   - Failure case: InvalidRatingException when rating < 1 or > 5
   - Verify repository.readAllReviews() and writeReviews() called
3. Test cases for updateReview():
   - Success case: Updates existing review
   - Failure case: ReviewNotFoundException when review doesn't exist
   - Failure case: BookNotFoundException when new book invalid
   - Failure case: InvalidRatingException on invalid rating
4. Test cases for deleteReview():
   - Success case: Deletes review and persists changes
   - Failure case: ReviewNotFoundException when review doesn't exist
5. Test cases for getReviewById():
   - Success case: Returns ReviewResponse
   - Failure case: ReviewNotFoundException when not found
6. Test cases for getAllReviews():
   - Success case: Returns list of all reviews
   - Empty list case: No reviews exist
7. Use @ExtendWith(MockitoExtension.class)
8. Use @Mock for repository/service dependencies
9. Use @InjectMocks for service under test
10. Use verify() to confirm repository method calls

**BookValidationServiceTest.java:**
1. Setup: Mock BookRepository
2. Test cases for isBookValid():
   - Success case: Returns true when book exists
   - Failure case: Returns false when book doesn't exist
   - Edge case: Returns false for null bookId
   - Edge case: Returns false for blank bookId
3. Test cases for getAllBooks():
   - Success case: Returns list of BookResponse
   - Empty list case: No books exist
4. Use @ExtendWith(MockitoExtension.class)
5. Use @Mock for BookRepository
6. Use @InjectMocks for service under test

**Acceptance Criteria:**
- Both test classes compile without errors
- All service methods have test coverage
- Each test has clear arrange-act-assert structure
- Mockito @Mock and @InjectMocks used correctly
- Tests verify repository interactions with verify()
- Exception scenarios tested (all custom exceptions)
- Edge cases tested (null, blank, empty lists)
- All tests pass without errors
- Test names describe what is being tested
- Classes follow naming convention: *Test

**Implementation Notes:**
- These are unit tests (not integration tests)
- Mock all external dependencies (repositories)
- Do NOT mock the service under test
- Use when().thenReturn() for mock behavior
- Use verify() to confirm interactions
- Test one scenario per test method
- Keep tests focused and fast
- Tests enable safe refactoring later

---

### IMPL-014: Write Integration Tests (End-to-End)

**Status:** Pending  
**Complexity:** Large  
**Est. Hours:** 7  
**Dependencies:** IMPL-010, IMPL-012  
**Blocks:** IMPL-015

**Description:**
Write end-to-end integration tests for ReviewController. These tests use TestRestTemplate or MockMvc to test full HTTP request/response cycle with actual service layer (no mocks except database).

**Files to Create:**
- `/src/test/java/com/epam/book_review_svc/controller/ReviewControllerTest.java`
- `/src/test/java/com/epam/book_review_svc/integration/ReviewIntegrationTest.java`

**What Needs to Be Done:**

**ReviewControllerTest.java (or ReviewIntegrationTest.java):**
1. Use @SpringBootTest with WebEnvironment.RANDOM_PORT
2. Inject TestRestTemplate or use @AutoConfigureMockMvc
3. Test happy path: Create, Read, Update, Delete, Get All
4. Test error paths:
   - POST with invalid book ID (400 Bad Request)
   - POST with invalid rating (400 Bad Request)
   - POST with blank review text (400 Bad Request)
   - PUT with invalid review ID (404 Not Found)
   - DELETE with invalid review ID (404 Not Found)
   - GET with invalid review ID (404 Not Found)
5. Test validation errors:
   - Missing required fields
   - Rating out of range
   - Review text too short
6. Test response format:
   - 201 Created for POST
   - 200 OK for PUT/GET
   - 204 No Content for DELETE
   - 400 Bad Request for validation failures
   - 404 Not Found for missing resources
7. Verify response bodies contain correct data
8. Verify timestamps are set correctly
9. Test GET all reviews returns all created reviews

**Acceptance Criteria:**
- Integration test class compiles without errors
- Uses @SpringBootTest to load full Spring context
- Tests all 5 endpoints (POST, PUT, DELETE, GET by ID, GET all)
- Happy path tested: Create a review, retrieve it, update it, delete it
- Error paths tested: Invalid book, invalid rating, not found
- Validation errors tested: Missing fields, out-of-range values
- HTTP status codes verified for all scenarios
- Response bodies verified (correct data in responses)
- Timestamps verified (createdAt/updatedAt set correctly)
- All tests pass without errors
- Tests can run independently (no order dependency)
- Tests clean up data or use transactions for isolation

**Implementation Notes:**
- These are integration tests (full application context)
- Use actual repositories (not mocked)
- Use TestRestTemplate.postForEntity() for POST requests
- Use exchange() method for requests with expected error status
- Verify response status with getStatusCode()
- Verify response body with getBody()
- Consider using @Transactional to rollback after each test
- Or use separate test data files for isolation
- Tests validate entire request/response cycle
- Slow than unit tests but test real interactions

---

### IMPL-015: Manual Testing & Bug Fixes

**Status:** Pending  
**Complexity:** Medium  
**Est. Hours:** 4  
**Dependencies:** IMPL-014 (Integration tests pass)  
**Blocks:** None (final phase)

**Description:**
Perform manual testing of the complete application. Run the service locally, test endpoints via Swagger UI or curl/Postman, verify error handling, and fix any bugs found.

**What Needs to Be Done:**
1. Build and run the application:
   - Run `./gradlew clean build` to verify compilation
   - Run `./gradlew bootRun` to start application
   - Verify application starts without errors
   - Check logs for any warnings/errors

2. Access Swagger UI:
   - Navigate to `http://localhost:8080/swagger-ui/index.html`
   - Verify all 5 endpoints are visible
   - Verify endpoint descriptions and examples are clear
   - Verify request/response schemas are correct

3. Test happy path via Swagger:
   - Create a review with valid book ID and rating
   - Verify response is 201 Created with review ID
   - Get the review by ID
   - Verify response is 200 OK with correct data
   - Update the review
   - Verify response is 200 OK with updated data
   - Delete the review
   - Verify response is 204 No Content
   - Get all reviews
   - Verify empty list returned

4. Test error paths:
   - Create review with invalid book ID
   - Verify 400 Bad Request with error message
   - Create review with invalid rating (0 or 6)
   - Verify 400 Bad Request with error message
   - Create review with blank review text
   - Verify 400 Bad Request with validation error
   - Get review with invalid ID
   - Verify 404 Not Found
   - Update non-existent review
   - Verify 404 Not Found
   - Delete non-existent review
   - Verify 404 Not Found

5. Verify application behavior:
   - Check that reviews.json is updated after create/update/delete
   - Check logs for appropriate debug/info messages
   - Verify error responses include all fields (status, message, errorType, timestamp, path)
   - Verify timestamps are correct format and values

6. Fix any bugs found:
   - If endpoints don't work, debug and fix
   - If validation doesn't work, fix DTO annotations
   - If error responses are incorrect, fix GlobalExceptionHandler
   - If timestamps are wrong format, check LocalDateTime serialization
   - If Swagger UI missing endpoints, add missing annotations

7. Verify all endpoints conform to architecture:
   - POST returns 201, PUT returns 200, DELETE returns 204
   - All errors return appropriate HTTP codes
   - All responses are consistent with ErrorResponse format
   - Exception handling is working correctly

**Acceptance Criteria:**
- Application builds without compilation errors
- Application starts and runs on port 8080
- All 5 endpoints visible and documented in Swagger UI
- All endpoints respond to requests (no 500 errors)
- Happy path works end-to-end (create, read, update, delete)
- Error paths return appropriate HTTP status codes
- Validation errors provide meaningful messages
- reviews.json file is updated on create/update/delete
- Logging shows activity and errors
- ErrorResponse format is consistent across all error scenarios
- No unhandled exceptions or stack traces in responses
- Timestamps are in correct ISO 8601 format

**Implementation Notes:**
- This is the final QA phase before deployment
- Manual testing catches edge cases automated tests might miss
- Swagger UI is the primary interface for testing
- Can use curl/Postman for more complex scenarios
- Check logs in terminal running `./gradlew bootRun`
- Keep track of bugs found and fixes applied
- Consider creating Postman collection for repeatable tests
- Document any issues for future debugging

---

## Dependency Graph

```
IMPL-001 (Configuration)
    ├── blocks all other tasks
    ├→ IMPL-002 (Domain Entities)
    │   ├→ IMPL-005 (BookRepository)
    │   │   ├→ IMPL-007 (BookValidationService)
    │   │   │   ├→ IMPL-008 (ReviewService)
    │   │   │   │   ├→ IMPL-010 (ReviewController)
    │   │   │   │   │   ├→ IMPL-011 (Swagger Annotations)
    │   │   │   │   │   ├→ IMPL-014 (Integration Tests)
    │   │   │   │   │   └→ IMPL-015 (Manual Testing)
    │   │   │   │   └→ IMPL-013 (Unit Tests)
    │   │   │   └→ IMPL-006 (ReviewRepository)
    │   │   │       ├→ IMPL-008 (ReviewService)
    │   │   │       └→ IMPL-012 (Data Files)
    │   │   │           └→ IMPL-014 (Integration Tests)
    │   │   └→ IMPL-004 (Custom Exceptions)
    │   │       ├→ IMPL-008 (ReviewService)
    │   │       ├→ IMPL-009 (GlobalExceptionHandler)
    │   │       │   └→ IMPL-010 (ReviewController)
    │   │       └→ IMPL-013 (Unit Tests)
    │   └→ IMPL-003 (Request/Response DTOs)
    │       ├→ IMPL-010 (ReviewController)
    │       └→ IMPL-013 (Unit Tests)
    │           └→ IMPL-014 (Integration Tests)
    │
    └→ IMPL-012 (Data Files)
        └→ IMPL-014 (Integration Tests)
            └→ IMPL-015 (Manual Testing)
```

---

## Critical Path Analysis

**Critical Path (Longest dependency chain):**

1. IMPL-001 (Configuration) - 2 hours
2. IMPL-002 (Domain Entities) - 3 hours
3. IMPL-006 (ReviewRepository) - 4 hours
4. IMPL-008 (ReviewService) - 6 hours
5. IMPL-009 (GlobalExceptionHandler) - 3 hours
6. IMPL-010 (ReviewController) - 4 hours
7. IMPL-011 (Swagger Annotations) - 2 hours
8. IMPL-014 (Integration Tests) - 7 hours
9. IMPL-015 (Manual Testing & QA) - 4 hours

**Critical Path Duration:** ~35 hours (sequential execution)

---

## Parallel Workstreams

After IMPL-001 (Configuration) completes, the following workstreams can run **in parallel**:

**Workstream A (Repository + Validation):**
- IMPL-002 → IMPL-005 → IMPL-007 (Est. 10 hours)

**Workstream B (Exception Handling):**
- IMPL-004 → IMPL-009 (Est. 5 hours)

**Workstream C (DTOs):**
- IMPL-003 (Est. 3 hours)

**Workstream D (Service Layer):**
- IMPL-006 + IMPL-007 + IMPL-004 → IMPL-008 (Est. 13 hours)

**Workstream E (Data Files):**
- IMPL-012 (Est. 1 hour)

These can converge to IMPL-010 (Controller) once their dependencies are complete.

---

## Blocked Tasks Summary

### Tasks with Hard Dependencies

**IMPL-002 through IMPL-012:** Blocked by IMPL-001
- **Reason:** Configuration must be set up before any code can compile/run
- **Unblocked when:** IMPL-001 completes and application.properties exists
- **Estimated wait time:** 2 hours

**IMPL-008:** Blocked by IMPL-006, IMPL-007, IMPL-004
- **Reason:** ReviewService depends on repositories and exceptions
- **Unblocked when:** All three dependency tasks complete
- **Estimated wait time:** ~10 hours (if done in sequence)

**IMPL-010:** Blocked by IMPL-003, IMPL-008, IMPL-009
- **Reason:** Controller depends on DTOs, service, and exception handler
- **Unblocked when:** All three complete
- **Estimated wait time:** ~13 hours

**IMPL-014:** Blocked by IMPL-010, IMPL-012
- **Reason:** Integration tests need controller and data files
- **Unblocked when:** Both complete
- **Estimated wait time:** ~11 hours

**IMPL-015:** Blocked by IMPL-014
- **Reason:** Manual testing needs all integration tests passing
- **Unblocked when:** IMPL-014 passes all tests
- **Estimated wait time:** ~7 hours

---

## Risk & Constraints

### External Dependencies & Constraints

1. **File I/O Transactionality**
   - Risk: Concurrent reads/writes to JSON files could cause data loss
   - Mitigation: Single-instance deployment, no concurrent request handling
   - Constraint: Not suitable for high-concurrency scenarios

2. **Book Validation Caching**
   - Risk: books.json read on every validation impacts performance
   - Constraint: Acceptable for small datasets (<10,000 books)
   - Mitigation: Cache implementation available for future optimization

3. **JSON File Format**
   - Risk: Corrupted JSON file breaks entire application
   - Mitigation: Add file validation in repositories, provide default empty arrays
   - Constraint: No backup mechanism; file loss = data loss

4. **LocalDateTime Serialization**
   - Risk: Jackson may serialize timestamps in unexpected format
   - Constraint: Must configure ObjectMapper for UTC and ISO 8601
   - Mitigation: Set Jackson config in application.properties

5. **UUID Generation Uniqueness**
   - Risk: Extremely rare UUID collision possible
   - Constraint: UUID.randomUUID() provides sufficient uniqueness
   - Mitigation: No explicit uniqueness enforcement needed (billions of combinations)

### Potential Bottlenecks

1. **IMPL-008 (ReviewService)** - Most complex task, highest risk of bugs
   - Mitigation: Break into smaller chunks, test thoroughly
   - Impact: Blocks IMPL-010 and downstream tasks

2. **IMPL-014 (Integration Tests)** - Long duration, potential for flakiness
   - Mitigation: Use proper test isolation, avoid real file I/O
   - Impact: Blocks IMPL-015 (manual testing)

3. **File System Performance** - JSON files stored on disk
   - Mitigation: Use SSD storage, keep file sizes reasonable
   - Constraint: Not optimal for high-traffic scenarios

### Timeline Risks

1. **Testing Phase (IMPL-013, IMPL-014, IMPL-015)** consumes 16 hours
   - Risk: Bugs found in testing require fixing and re-testing
   - Mitigation: Thorough unit tests reduce integration test failures
   - Contingency: May need additional 4-5 hours for bug fixes

2. **Bean Validation & Exception Handling** complex to get right
   - Risk: Validation errors format incorrectly or incompletely
   - Mitigation: Test error paths thoroughly
   - Contingency: May need refinement in IMPL-015

---

## Execution Recommendations

### Phase 1: Foundation (Days 1)
- IMPL-001: Setup Configuration (2 hours)
- Then start parallel workstreams A, B, C, E

### Phase 2: Implementation (Days 2-3)
- Workstream A: Repository + Validation (10 hours)
- Workstream B: Exception Handling (5 hours)
- Workstream C: DTOs (3 hours)
- Workstream E: Data Files (1 hour)
- Workstream D: Service Layer (13 hours)
- IMPL-010: Controller (4 hours)
- IMPL-011: Swagger Annotations (2 hours)

### Phase 3: Testing & QA (Days 4-5)
- IMPL-013: Unit Tests (5 hours)
- IMPL-014: Integration Tests (7 hours)
- IMPL-015: Manual Testing & Bug Fixes (4 hours)

**Total Timeline:** ~5-6 days of development (with one developer working full-time)

---

## Success Criteria

### Code Quality
- All code compiles without warnings
- All tests pass (unit and integration)
- No code style violations
- Clear, readable code with meaningful names

### Functional Completeness
- All 5 endpoints implemented and working
- All validation rules enforced
- All error scenarios handled
- All HTTP status codes correct (201, 200, 204, 400, 404, 500)

### Architectural Compliance
- 3-tier layered architecture maintained
- Clear separation of concerns
- No business logic in controller
- No persistence logic in service
- Exception translation working correctly
- Centralized error handling in place

### Testing Coverage
- Unit tests for all service methods
- Integration tests for all endpoints
- Happy path and error paths tested
- Manual testing verified full flow
- No unhandled exceptions in production

### Documentation
- Swagger UI auto-generated and accurate
- All endpoints documented with descriptions
- All error responses documented
- Code comments for complex logic
- README updated with build/run instructions

---

## Conclusion

This implementation plan provides a comprehensive roadmap for developing the Book Review Service REST API. The task breakdown balances detail (specific classes, methods, files) with flexibility (allowing for refinement during implementation). The dependency analysis identifies critical path and parallelization opportunities, enabling efficient scheduling and resource allocation.

Key strengths of this plan:
- Clear, actionable tasks with success criteria
- Explicit dependency mapping enabling parallel execution
- Risk identification and mitigation strategies
- Realistic time estimates based on scope
- Architecture alignment ensuring quality
- Comprehensive testing strategy
- Manual QA phase catching edge cases

The total effort of ~53 hours (18-20 days of work for a team of 2-3 developers) is realistic for building a production-quality REST API with proper exception handling, validation, testing, and documentation.


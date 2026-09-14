# Book Review Service - System Architecture Design

## Executive Summary

The **Book Review Service** is a Spring Boot 4.1.1 REST API built with Java 25 that manages book reviews with comprehensive validation and centralized error handling. The architecture follows a **layered, stateless design** with clear separation of concerns across controller, service, and data access layers. All data is persisted to JSON files in the classpath resources folder, making it ideal for development and testing scenarios.

### Key Architectural Decisions:
- **Layered Architecture** - Clean separation between API, business logic, and persistence layers
- **Centralized Exception Handling** - `@ControllerAdvice` for consistent error responses across all endpoints
- **Custom Exception Hierarchy** - Domain-specific exceptions mapped to appropriate HTTP status codes
- **Validation Strategy** - Two-tier validation (Controller for format, Service for business rules)
- **Direct File I/O** - Simple, no abstraction layer; Jackson ObjectMapper reads/writes JSON files directly
- **No Caching** - Books.json read on each validation request for simplicity and data consistency
- **No Authentication** - All endpoints are public; authorization not required
- **Stateless Design** - Each request is independent with no server-side session state

---

## Requirements Summary

### Functional Requirements
1. **Create Review** - POST `/api/reviews` to add a new book review with validation
2. **Update Review** - PUT `/api/reviews/{id}` to modify existing review details
3. **Delete Review** - DELETE `/api/reviews/{id}` to remove a review from the system
4. **Get Review by ID** - GET `/api/reviews/{id}` to retrieve a specific review
5. **Get All Reviews** - GET `/api/reviews` to list all reviews with optional filtering/pagination

### Non-Functional Requirements
- **Data Validation** - Reviews must reference existing books with valid ratings (1-5 scale)
- **Error Handling** - Consistent error responses with meaningful messages and HTTP status codes
- **Swagger Documentation** - Auto-generated OpenAPI documentation for all endpoints
- **JSON Persistence** - File-based storage using Jackson ObjectMapper
- **Simple & Maintainable** - Straightforward implementation without over-engineering
- **Stateless Architecture** - No server-side state; each request is independent

---

## High-Level Architecture

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     API Client (REST Consumer)                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP Request/Response
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      REST Controller Layer                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ReviewController                                        │   │
│  │  - @PostMapping  /api/reviews                           │   │
│  │  - @PutMapping   /api/reviews/{id}                      │   │
│  │  - @DeleteMapping /api/reviews/{id}                     │   │
│  │  - @GetMapping   /api/reviews/{id}                      │   │
│  │  - @GetMapping   /api/reviews                           │   │
│  │                                                          │   │
│  │  Responsibilities:                                       │   │
│  │  - Parse HTTP requests                                  │   │
│  │  - Basic validation (Bean Validation, @NotNull, etc.)   │   │
│  │  - Invoke service methods                               │   │
│  │  - Return HTTP responses with appropriate status codes  │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │ DTO objects
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Service Layer                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ReviewService                                          │   │
│  │  - createReview(reviewDto)                             │   │
│  │  - updateReview(id, reviewDto)                         │   │
│  │  - deleteReview(id)                                    │   │
│  │  - getReviewById(id)                                   │   │
│  │  - getAllReviews()                                     │   │
│  │  - validateBookExists(bookId)                          │   │
│  │  - validateRating(rating)                              │   │
│  │                                                         │   │
│  │  Responsibilities:                                      │   │
│  │  - Business logic orchestration                         │   │
│  │  - Complex validation (business rules)                  │   │
│  │  - File I/O operations (read/write)                     │   │
│  │  - Exception translation                               │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BookValidationService                                 │   │
│  │  - isBookValid(bookId)                                 │   │
│  │  - getAllBooks()                                       │   │
│  │                                                         │   │
│  │  Responsibilities:                                      │   │
│  │  - Validate book existence by reading books.json       │   │
│  │  - Caching: None (read file on each check)             │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │ Domain entities
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Access Layer                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ReviewRepository                                       │   │
│  │  - readAllReviews()                                    │   │
│  │  - writeReviews(reviews)                               │   │
│  │                                                         │   │
│  │  Responsibilities:                                      │   │
│  │  - Direct file I/O (Jackson ObjectMapper)              │   │
│  │  - Serialize/deserialize JSON to Java objects          │   │
│  │  - No abstraction layer; straightforward operations    │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BookRepository                                         │   │
│  │  - readAllBooks()                                      │   │
│  │                                                         │   │
│  │  Responsibilities:                                      │   │
│  │  - Read books.json and return list of books            │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │ JSON files
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              Persistent Storage (JSON Files)                    │
│  ┌──────────────────────────┐      ┌──────────────────────────┐ │
│  │  reviews.json            │      │  books.json              │ │
│  │  [                       │      │  [                       │ │
│  │    {                     │      │    {                     │ │
│  │      id: "uuid",         │      │      id: "uuid",         │ │
│  │      bookId: "uuid",     │      │      title: "...",       │ │
│  │      rating: 4,          │      │      author: "...",      │ │
│  │      reviewText: "...",  │      │      ...                 │ │
│  │      ...                 │      │    },                    │ │
│  │    },                    │      │    ...                   │ │
│  │    ...                   │      │  ]                       │ │
│  │  ]                       │      │                          │ │
│  └──────────────────────────┘      └──────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              Cross-Cutting Concerns Layer                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  GlobalExceptionHandler (@ControllerAdvice)             │   │
│  │  - Handle all custom exceptions                         │   │
│  │  - Translate exceptions to ErrorResponse DTOs           │   │
│  │  - Set appropriate HTTP status codes                    │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Logging & Configuration                               │   │
│  │  - SLF4J/Logback for application logging               │   │
│  │  - application.properties for environment config       │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Component Design

### 1. API Layer - REST Controllers

#### ReviewController
**Location:** `src/main/java/com/epam/book_review_svc/controller/ReviewController.java`

**Responsibilities:**
- Accept HTTP requests and parse path variables, request bodies
- Apply **basic validation** using Bean Validation annotations (@NotNull, @NotBlank, @Min, @Max)
- Delegate to service layer for business logic
- Return appropriately formatted HTTP responses with correct status codes
- No business logic; purely request/response handling

**Key Methods:**

```java
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Book review management APIs")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @Operation(summary = "Create a new review", 
               description = "Add a new book review with validation")
    @ApiResponse(responseCode = "201", description = "Review created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or book not found")
    public ResponseEntity<ReviewResponse> createReview(
        @Valid @RequestBody CreateReviewRequest request
    )
    
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing review", 
               description = "Modify review details by ID")
    @ApiResponse(responseCode = "200", description = "Review updated successfully")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<ReviewResponse> updateReview(
        @PathVariable String id,
        @Valid @RequestBody UpdateReviewRequest request
    )
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review", 
               description = "Remove a review from the system")
    @ApiResponse(responseCode = "204", description = "Review deleted successfully")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ResponseEntity<Void> deleteReview(
        @PathVariable String id
    )
    
    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", 
               description = "Retrieve a specific review")
    @ApiResponse(responseCode = "200", description = "Review found")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ResponseEntity<ReviewResponse> getReviewById(
        @PathVariable String id
    )
    
    @GetMapping
    @Operation(summary = "Get all reviews", 
               description = "List all reviews in the system")
    @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully")
    public ResponseEntity<List<ReviewResponse>> getAllReviews()
}
```

---

### 2. Service Layer - Business Logic

#### ReviewService
**Location:** `src/main/java/com/epam/book_review_svc/service/ReviewService.java`

**Responsibilities:**
- Implement core business logic for review management
- **Business-level validation** - Rating range, book existence, review existence
- Orchestrate repository operations (read, modify, write)
- Translate domain exceptions to meaningful error messages
- Maintain transactional consistency (all-or-nothing file operations)
- Logging for audit trails

**Key Methods:**

```java
@Service
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final BookValidationService bookValidationService;
    
    public ReviewResponse createReview(CreateReviewRequest request) {
        // Step 1: Validate book exists
        if (!bookValidationService.isBookValid(request.getBookId())) {
            throw new BookNotFoundException("Book with ID " + request.getBookId() + " not found");
        }
        
        // Step 2: Validate rating is within 1-5 range (business rule)
        validateRating(request.getRating());
        
        // Step 3: Create Review entity
        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setBookId(request.getBookId());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        // Step 4: Persist to file
        List<Review> reviews = reviewRepository.readAllReviews();
        reviews.add(review);
        reviewRepository.writeReviews(reviews);
        
        return mapToResponse(review);
    }
    
    public ReviewResponse updateReview(String id, UpdateReviewRequest request) {
        // Step 1: Find existing review
        Review review = findReviewById(id);
        
        // Step 2: Validate new book if changed
        if (!review.getBookId().equals(request.getBookId())) {
            if (!bookValidationService.isBookValid(request.getBookId())) {
                throw new BookNotFoundException("Book with ID " + request.getBookId() + " not found");
            }
        }
        
        // Step 3: Validate rating
        validateRating(request.getRating());
        
        // Step 4: Update review entity
        review.setBookId(request.getBookId());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setUpdatedAt(LocalDateTime.now());
        
        // Step 5: Persist updated list
        List<Review> reviews = reviewRepository.readAllReviews();
        int index = reviews.indexOf(review);
        reviews.set(index, review);
        reviewRepository.writeReviews(reviews);
        
        return mapToResponse(review);
    }
    
    public void deleteReview(String id) {
        // Step 1: Verify review exists
        findReviewById(id);
        
        // Step 2: Remove from list
        List<Review> reviews = reviewRepository.readAllReviews();
        boolean removed = reviews.removeIf(r -> r.getId().equals(id));
        
        if (!removed) {
            throw new ReviewNotFoundException("Review not found");
        }
        
        // Step 3: Persist updated list
        reviewRepository.writeReviews(reviews);
    }
    
    public ReviewResponse getReviewById(String id) {
        Review review = findReviewById(id);
        return mapToResponse(review);
    }
    
    public List<ReviewResponse> getAllReviews() {
        List<Review> reviews = reviewRepository.readAllReviews();
        return reviews.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private Review findReviewById(String id) {
        return reviewRepository.readAllReviews().stream()
            .filter(r -> r.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ReviewNotFoundException("Review with ID " + id + " not found"));
    }
    
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }
    }
}
```

---

#### BookValidationService
**Location:** `src/main/java/com/epam/book_review_svc/service/BookValidationService.java`

**Responsibilities:**
- Validate book existence by reading books.json
- No caching - file read on each validation request for simplicity
- Provide central point for book validation logic
- Log validation failures for debugging

**Design Decision: No Caching**
- Books.json is read fresh on each validation request
- Ensures consistency if books are added/removed externally
- Acceptable performance for typical book datasets (hundreds/thousands)
- Simplifies implementation and eliminates cache invalidation complexity

```java
@Service
public class BookValidationService {
    
    private final BookRepository bookRepository;
    
    /**
     * Check if a book exists by ID.
     * Reads books.json on each call (no caching).
     */
    public boolean isBookValid(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            return false;
        }
        
        List<Book> books = bookRepository.readAllBooks();
        return books.stream()
            .anyMatch(b -> b.getId().equals(bookId));
    }
    
    /**
     * Retrieve all available books.
     * Reads books.json on each call.
     */
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.readAllBooks();
        return books.stream()
            .map(b -> new BookResponse(b.getId(), b.getTitle(), b.getAuthor()))
            .collect(Collectors.toList());
    }
}
```

---

### 3. Data Access Layer - Repositories

#### ReviewRepository
**Location:** `src/main/java/com/epam/book_review_svc/repository/ReviewRepository.java`

**Responsibilities:**
- Direct I/O operations using Jackson ObjectMapper
- Read/write JSON files from classpath resources
- No abstraction or caching layer
- Error handling for file I/O failures
- Clean separation of persistence logic from business logic

```java
@Repository
public class ReviewRepository {
    
    private static final Logger log = LoggerFactory.getLogger(ReviewRepository.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Read all reviews from reviews.json file.
     */
    public List<Review> readAllReviews() {
        try {
            Resource resource = new ClassPathResource("data/reviews.json");
            File file = resource.getFile();
            
            if (!file.exists()) {
                log.warn("Reviews file does not exist. Returning empty list.");
                return new ArrayList<>();
            }
            
            Review[] reviews = objectMapper.readValue(file, Review[].class);
            return Arrays.asList(reviews);
        } catch (IOException e) {
            log.error("Error reading reviews from file", e);
            throw new DataAccessException("Failed to read reviews from file", e);
        }
    }
    
    /**
     * Write all reviews to reviews.json file (overwrites existing).
     */
    public void writeReviews(List<Review> reviews) {
        try {
            Resource resource = new ClassPathResource("data/reviews.json");
            String filePath = resource.getFile().getAbsolutePath();
            
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), reviews);
            log.info("Successfully wrote {} reviews to file", reviews.size());
        } catch (IOException e) {
            log.error("Error writing reviews to file", e);
            throw new DataAccessException("Failed to write reviews to file", e);
        }
    }
}
```

---

#### BookRepository
**Location:** `src/main/java/com/epam/book_review_svc/repository/BookRepository.java`

**Responsibilities:**
- Read books.json file
- Deserialize JSON to Book objects using Jackson
- Error handling for file not found or parse errors

```java
@Repository
public class BookRepository {
    
    private static final Logger log = LoggerFactory.getLogger(BookRepository.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Read all books from books.json file.
     */
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

---

### 4. Domain Models - Entity Classes

#### Review (Domain Entity)
**Location:** `src/main/java/com/epam/book_review_svc/model/entity/Review.java`

**Persisted in:** `src/main/resources/data/reviews.json`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Review {
    private String id;
    private String bookId;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Example JSON in reviews.json:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "bookId": "123e4567-e89b-12d3-a456-426614174000",
    "rating": 4,
    "reviewText": "An excellent book that explores deep themes.",
    "createdAt": "2025-01-10T14:30:00",
    "updatedAt": "2025-01-10T14:30:00"
  }
]
```

**Fields:**
- `id` (String, UUID) - Unique identifier for the review
- `bookId` (String, UUID) - Reference to the reviewed book
- `rating` (Integer) - Review rating (1-5 scale)
- `reviewText` (String) - Detailed review text
- `createdAt` (LocalDateTime) - Review creation timestamp
- `updatedAt` (LocalDateTime) - Last modification timestamp

---

#### Book (Reference Entity)
**Location:** `src/main/java/com/epam/book_review_svc/model/entity/Book.java`

**Persisted in:** `src/main/resources/data/books.json`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private String genre;
}
```

---

### 5. DTO Layer - Request/Response Objects

#### CreateReviewRequest
**Location:** `src/main/java/com/epam/book_review_svc/model/dto/request/CreateReviewRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new review")
public class CreateReviewRequest {
    
    @NotNull(message = "Book ID must not be null")
    @NotBlank(message = "Book ID must not be blank")
    @Schema(description = "UUID of the book being reviewed", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    private String bookId;
    
    @NotNull(message = "Rating must not be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;
    
    @NotBlank(message = "Review text must not be blank")
    @Size(min = 10, max = 2000, message = "Review text must be between 10 and 2000 characters")
    @Schema(description = "Detailed review text", 
            example = "This book provides excellent insights into...")
    private String reviewText;
}
```

**Validation Rules:**
- `bookId` - Required, non-null, non-blank UUID string
- `rating` - Required, integer between 1 and 5 (inclusive)
- `reviewText` - Required, string between 10 and 2000 characters

---

#### UpdateReviewRequest
**Location:** `src/main/java/com/epam/book_review_svc/model/dto/request/UpdateReviewRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing review")
public class UpdateReviewRequest {
    
    @NotNull(message = "Book ID must not be null")
    @NotBlank(message = "Book ID must not be blank")
    @Schema(description = "UUID of the book being reviewed")
    private String bookId;
    
    @NotNull(message = "Rating must not be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Updated rating from 1 to 5")
    private Integer rating;
    
    @NotBlank(message = "Review text must not be blank")
    @Size(min = 10, max = 2000, message = "Review text must be between 10 and 2000 characters")
    @Schema(description = "Updated review text")
    private String reviewText;
}
```

---

#### ReviewResponse
**Location:** `src/main/java/com/epam/book_review_svc/model/dto/response/ReviewResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for a review")
public class ReviewResponse {
    
    @Schema(description = "Review ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Book ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String bookId;
    
    @Schema(description = "Rating (1-5)", example = "4")
    private Integer rating;
    
    @Schema(description = "Review text", example = "An excellent book...")
    private String reviewText;
    
    @Schema(description = "Review creation timestamp", example = "2025-01-10T14:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Review last update timestamp", example = "2025-01-10T14:30:00")
    private LocalDateTime updatedAt;
}
```

---

#### ErrorResponse
**Location:** `src/main/java/com/epam/book_review_svc/model/dto/response/ErrorResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response object")
public class ErrorResponse {
    
    @Schema(description = "HTTP status code", example = "404")
    private int status;
    
    @Schema(description = "Error message", example = "Review not found")
    private String message;
    
    @Schema(description = "Error type", example = "ReviewNotFoundException")
    private String errorType;
    
    @Schema(description = "Timestamp of error", example = "2025-01-10T14:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Request path where error occurred", example = "/api/reviews/123")
    private String path;
}
```

---

### 6. Exception Handling Layer

#### Custom Exception Hierarchy

**Location:** `src/main/java/com/epam/book_review_svc/exception/`

##### Base Exception Class
```java
// BusinessException.java
public abstract class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public abstract int getHttpStatusCode();
}
```

##### Specific Exception Classes

```java
// ReviewNotFoundException.java
@Getter
public class ReviewNotFoundException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.NOT_FOUND.value();
    
    public ReviewNotFoundException(String message) {
        super(message);
    }
    
    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}

// BookNotFoundException.java
@Getter
public class BookNotFoundException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.BAD_REQUEST.value();
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}

// InvalidRatingException.java
@Getter
public class InvalidRatingException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.BAD_REQUEST.value();
    
    public InvalidRatingException(String message) {
        super(message);
    }
    
    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}

// DataAccessException.java
@Getter
public class DataAccessException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR.value();
    
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}
```

---

#### Global Exception Handler
**Location:** `src/main/java/com/epam/book_review_svc/exception/GlobalExceptionHandler.java`

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex,
        HttpServletRequest request
    ) {
        log.error("Business exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(ex.getHttpStatusCode())
            .message(ex.getMessage())
            .errorType(ex.getClass().getSimpleName())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(errorResponse, 
            HttpStatus.valueOf(ex.getHttpStatusCode()));
    }
    
    /**
     * Handle validation exceptions (Bean Validation failures).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        log.error("Validation exception occurred: {}", ex.getMessage());
        
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed: " + message)
            .errorType("MethodArgumentNotValidException")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .errorType(ex.getClass().getSimpleName())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**Exception Mapping:**

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| ReviewNotFoundException | 404 Not Found | Review ID doesn't exist |
| BookNotFoundException | 400 Bad Request | Referenced book doesn't exist |
| InvalidRatingException | 400 Bad Request | Rating outside 1-5 range |
| DataAccessException | 500 Internal Server Error | File I/O failure |
| MethodArgumentNotValidException | 400 Bad Request | Bean Validation failure |
| Exception (generic) | 500 Internal Server Error | Unexpected errors |

---

## API Specifications

### Endpoint 1: Create Review

```
POST /api/reviews
Content-Type: application/json
```

**Request Body:**
```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 4,
  "reviewText": "This book provides excellent insights into modern architecture patterns and best practices."
}
```

**Success Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 4,
  "reviewText": "This book provides excellent insights into modern architecture patterns and best practices.",
  "createdAt": "2025-01-10T14:30:00",
  "updatedAt": "2025-01-10T14:30:00"
}
```

**Error Response (400 Bad Request - Invalid Book):**
```json
{
  "status": 400,
  "message": "Book with ID 123e4567-e89b-12d3-a456-426614174999 not found",
  "errorType": "BookNotFoundException",
  "timestamp": "2025-01-10T14:30:00",
  "path": "/api/reviews"
}
```

---

### Endpoint 2: Update Review

```
PUT /api/reviews/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 5,
  "reviewText": "After further reflection, this book is absolutely outstanding!"
}
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 5,
  "reviewText": "After further reflection, this book is absolutely outstanding!",
  "createdAt": "2025-01-10T14:30:00",
  "updatedAt": "2025-01-10T14:35:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Review with ID 550e8400-e29b-41d4-a716-446655440999 not found",
  "errorType": "ReviewNotFoundException",
  "timestamp": "2025-01-10T14:30:00",
  "path": "/api/reviews/550e8400-e29b-41d4-a716-446655440999"
}
```

---

### Endpoint 3: Delete Review

```
DELETE /api/reviews/{id}
```

**Success Response (204 No Content):**
```
(Empty response body)
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Review with ID 550e8400-e29b-41d4-a716-446655440999 not found",
  "errorType": "ReviewNotFoundException",
  "timestamp": "2025-01-10T14:30:00",
  "path": "/api/reviews/550e8400-e29b-41d4-a716-446655440999"
}
```

---

### Endpoint 4: Get Review by ID

```
GET /api/reviews/{id}
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 4,
  "reviewText": "Excellent book with deep insights.",
  "createdAt": "2025-01-10T14:30:00",
  "updatedAt": "2025-01-10T14:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Review with ID 550e8400-e29b-41d4-a716-446655440999 not found",
  "errorType": "ReviewNotFoundException",
  "timestamp": "2025-01-10T14:30:00",
  "path": "/api/reviews/550e8400-e29b-41d4-a716-446655440999"
}
```

---

### Endpoint 5: Get All Reviews

```
GET /api/reviews
```

**Success Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "bookId": "123e4567-e89b-12d3-a456-426614174000",
    "rating": 4,
    "reviewText": "Excellent insights into architecture.",
    "createdAt": "2025-01-10T14:30:00",
    "updatedAt": "2025-01-10T14:30:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "bookId": "123e4567-e89b-12d3-a456-426614174001",
    "rating": 5,
    "reviewText": "Absolutely fantastic!",
    "createdAt": "2025-01-09T10:15:00",
    "updatedAt": "2025-01-09T10:15:00"
  }
]
```

---

## Validation Strategy

### Two-Tier Validation Architecture

#### Tier 1: Controller-Level Validation (Format Validation)

**Purpose:** Catch malformed requests before they reach business logic

**Location:** Request DTO classes with Bean Validation annotations

**What Gets Validated:**
- Required fields (`@NotNull`, `@NotBlank`)
- String length constraints (`@Size`)
- Numeric ranges (`@Min`, `@Max`)
- Format patterns (`@Pattern`)
- Illegal characters

**Example:**
```java
@PostMapping
public ResponseEntity<ReviewResponse> createReview(
    @Valid @RequestBody CreateReviewRequest request
) { ... }
```

The `@Valid` annotation triggers Bean Validation on the request object before the method executes.

**Benefits:**
- Fails fast with clear validation error messages
- Prevents malformed data from entering business logic
- Reduces noise in service layer exception handling
- Returns HTTP 400 with validation details

---

#### Tier 2: Service-Level Validation (Business Rules)

**Purpose:** Enforce complex business rules after format validation passes

**Location:** Service layer methods

**What Gets Validated:**
- Book existence (reads books.json to verify bookId exists)
- Rating range (1-5 scale) - redundant safety check after controller validation
- Review existence (before update/delete operations)
- Data consistency rules

**Benefits:**
- Enforces domain constraints
- Prevents creation of invalid references
- Provides clear error messages for business rule violations
- Maintains data integrity

---

### Validation Flow Diagram

```
Request → Controller
    ↓
[Bean Validation (@Valid)]
    ├─ Format valid? → Continue
    └─ Format invalid? → Return 400 Bad Request + Error Details
    ↓
Service Layer
    ↓
[Business Rule Validation]
    ├─ Book exists? (reads books.json)
    ├─ Rating in 1-5 range?
    ├─ Review exists (for update/delete)?
    └─ All valid? → Continue
    ├─ Invalid book? → Throw BookNotFoundException (HTTP 400)
    ├─ Invalid rating? → Throw InvalidRatingException (HTTP 400)
    └─ Review not found? → Throw ReviewNotFoundException (HTTP 404)
    ↓
Operation (Create/Update/Delete/Get)
    ↓
Response (201/200/204 or error)
```

---

## Data Flow Diagrams

### Flow 1: Create Review (POST /api/reviews)

```
Client          Controller        Service            Repository
  │                 │                 │                  │
  ├─POST /reviews───>│                 │                  │
  │                  │                 │                  │
  │                  ├─@Valid Check─┐  │                  │
  │                  │ (format OK)  │  │                  │
  │                  │                 │                  │
  │                  ├─createReview────>│                  │
  │                  │                  ├─isBookValid──────>│
  │                  │                  │  readAllBooks   │
  │                  │                  │<──────────────────┤
  │                  │                  │  [List<Book>]   │
  │                  │                  │                  │
  │                  │                  ├─validateRating──┐│
  │                  │                  │ (OK)            ││
  │                  │                  │                  │
  │                  │                  ├─Create entity────>│
  │                  │                  │  readAllReviews │
  │                  │                  │<──────────────────┤
  │                  │                  │ [List<Review>]  │
  │                  │                  │                  │
  │                  │                  ├─writeReviews────>│
  │                  │                  │<──────────────────┤
  │                  │                  │  OK             │
  │                  │                  │                  │
  │                  │<─ReviewResponse──│                  │
  │<─201 Created─────│                  │                  │
```

---

## File Structure

### Directory Layout

```
book-review-svc/
├── src/
│   ├── main/
│   │   ├── java/com/epam/book_review_svc/
│   │   │   ├── BookReviewSvcApplication.java          [Entry point]
│   │   │   ├── controller/
│   │   │   │   └── ReviewController.java              [REST endpoints]
│   │   │   ├── service/
│   │   │   │   ├── ReviewService.java                 [Review business logic]
│   │   │   │   └── BookValidationService.java         [Book validation]
│   │   │   ├── repository/
│   │   │   │   ├── ReviewRepository.java              [Review persistence]
│   │   │   │   └── BookRepository.java                [Book persistence]
│   │   │   ├── model/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Review.java                    [Domain entity]
│   │   │   │   │   └── Book.java                      [Reference entity]
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       │   ├── CreateReviewRequest.java
│   │   │   │       │   └── UpdateReviewRequest.java
│   │   │   │       └── response/
│   │   │   │           ├── ReviewResponse.java
│   │   │   │           └── ErrorResponse.java
│   │   │   └── exception/
│   │   │       ├── BusinessException.java             [Base exception]
│   │   │       ├── ReviewNotFoundException.java
│   │   │       ├── BookNotFoundException.java
│   │   │       ├── InvalidRatingException.java
│   │   │       ├── DataAccessException.java
│   │   │       └── GlobalExceptionHandler.java        [Centralized handler]
│   │   └── resources/
│   │       ├── application.properties                 [Config]
│   │       └── data/
│   │           ├── reviews.json                       [Persistent data]
│   │           └── books.json                         [Reference data]
│   └── test/
│       └── java/com/epam/book_review_svc/
│           ├── controller/
│           │   └── ReviewControllerTest.java
│           ├── service/
│           │   ├── ReviewServiceTest.java
│           │   └── BookValidationServiceTest.java
│           └── integration/
│               └── ReviewIntegrationTest.java
├── build.gradle                                       [Gradle config]
├── settings.gradle
└── CLAUDE.md                                          [Project guidelines]
```

---

## Technology Stack

| Component | Technology | Version | Justification |
|-----------|-----------|---------|---------------|
| **Framework** | Spring Boot | 4.1.1 | Industry-standard REST API framework |
| **Language** | Java | 25 | Latest version with strong type safety |
| **Build Tool** | Gradle | Latest | Groovy-based, faster than Maven |
| **JSON Processing** | Jackson Databind | Latest | De-facto standard for JSON serialization |
| **Documentation** | Springdoc OpenAPI | 2.5.0 | Auto-generates Swagger UI |
| **Boilerplate Reduction** | Lombok | Latest | Reduces constructor/getter/setter code |
| **Testing** | Spring Boot Test + JUnit 5 | Latest | Integrated testing with Mockito, AssertJ |
| **Logging** | SLF4J + Logback | Latest | Industry-standard logging |
| **Validation** | Bean Validation (Jakarta) | Latest | Standard validation framework |

---

## Design Patterns Used

### 1. Layered Architecture (3-Tier)
**Application:** Controller → Service → Repository

**Benefit:** Clean separation of concerns, testability, maintainability

---

### 2. Repository Pattern
**Application:** ReviewRepository and BookRepository abstract data access

**Benefit:** Decouples business logic from persistence; easy to swap implementations

---

### 3. Service Layer Pattern
**Application:** ReviewService contains business logic, BookValidationService handles cross-cutting concerns

**Benefit:** Testable, reusable, clear responsibilities

---

### 4. DTO (Data Transfer Object) Pattern
**Application:** Separate request/response DTOs from domain entities

**Benefit:** Decouples API contracts, versioning-friendly, security

---

### 5. Exception Translation Pattern
**Application:** Custom exception hierarchy with GlobalExceptionHandler

**Benefit:** Consistent error handling, meaningful messages, appropriate HTTP codes

---

### 6. Centralized Exception Handler (@ControllerAdvice)
**Application:** GlobalExceptionHandler catches and translates all exceptions

**Benefit:** DRY principle, consistency across all endpoints

---

### 7. Dependency Injection Pattern
**Application:** Services/Repositories injected via constructor

**Benefit:** Loose coupling, testability, flexibility

---

### 8. Two-Tier Validation Pattern
**Application:** Controller (format) + Service (business rules)

**Benefit:** Separation of concerns, performance, clarity

---

## Configuration

### application.properties

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

---

## Non-Functional Considerations

### Performance
- **Current:** Books.json read on each validation (no caching)
- **Acceptable for:** Small datasets (< 10,000 reviews)
- **Future Optimization:** Add Redis for books.json caching

### Scalability
- **Current:** Single-instance, file-based system
- **Limitations:** No concurrent write protection, file system bottleneck
- **For Production:** Replace with database, add load balancer

### Security
- **Current:** No authentication/authorization; input validation only
- **For Production:** Add JWT authentication, rate limiting, input sanitization

### Maintainability
- **Positive:** Clear separation of concerns, consistent error handling, well-structured
- **Guidelines:** Keep services focused, add tests for business rules, document complex logic

---

## Implementation Roadmap

**Phase 1: Core Components**
1. Create Review, Book entities
2. Implement ReviewController with 5 endpoints
3. Implement ReviewService and BookValidationService
4. Implement ReviewRepository and BookRepository
5. Create DTOs (CreateReviewRequest, UpdateReviewRequest, ReviewResponse, ErrorResponse)

**Phase 2: Exception Handling & Validation**
1. Create custom exception hierarchy
2. Implement GlobalExceptionHandler
3. Add Bean Validation annotations to DTOs
4. Add business rule validation in service layer

**Phase 3: Documentation & Testing**
1. Add Swagger annotations to controller
2. Write unit tests for service layer
3. Write integration tests for controller
4. Test error scenarios and validation

---

## Summary of Key Components

| Component | Location | Responsibility |
|-----------|----------|-----------------|
| **ReviewController** | `controller/` | REST endpoint handlers, request/response mapping |
| **ReviewService** | `service/` | Business logic orchestration, validation |
| **BookValidationService** | `service/` | Book reference validation |
| **ReviewRepository** | `repository/` | File I/O for reviews.json |
| **BookRepository** | `repository/` | File I/O for books.json |
| **Review** (Entity) | `model/entity/` | Domain model for reviews |
| **Book** (Entity) | `model/entity/` | Domain model for books |
| **ReviewResponse** (DTO) | `model/dto/response/` | API response format |
| **GlobalExceptionHandler** | `exception/` | Centralized exception translation |
| **Custom Exceptions** | `exception/` | Domain-specific error types |

---

## Conclusion

This architecture provides a **clean, maintainable, and testable foundation** for the Book Review Service REST API. The layered design with clear component responsibilities enables rapid development while maintaining flexibility for future enhancements. The two-tier validation strategy ensures both format and business rule integrity, and the centralized exception handling provides consistent error responses across all endpoints.

The design prioritizes **simplicity and clarity** for the current scope (file-based JSON storage, single-instance deployment) while providing clear upgrade paths for production scenarios (database migration, caching, horizontal scaling).

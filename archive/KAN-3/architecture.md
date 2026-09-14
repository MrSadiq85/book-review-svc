# System Architecture Design: Book Review Service API

**Document Version:** 1.0  
**Last Updated:** 2026-09-10  
**Status:** Complete and Ready for Implementation

---

## Executive Summary

This document defines the complete high-level system architecture for the **book-review-svc** REST API service. The service provides comprehensive Book Catalog Management CRUD operations with role-based access control, pagination support, and comprehensive validation.

**Key Architectural Principles:**
- Clean separation of concerns across layered architecture (Controller → Service → Repository)
- File-based JSON persistence with abstracted data access layer
- Stateless REST API design enabling horizontal scalability
- Role-based access control (RBAC) enforced at service layer
- Comprehensive validation with detailed error handling
- Auto-generated API documentation via Swagger/OpenAPI

**Architecture Type:** Layered N-tier REST API with service-oriented design

---

## Requirements Summary

### Functional Requirements
The system implements 5 core REST endpoints:
1. **POST /api/books** - Create new book (author-only access)
2. **GET /api/books** - List books with pagination (all users)
3. **GET /api/books/{id}** - Get single book details (all users)
4. **PUT /api/books/{id}** - Update book (creator-only access)
5. **DELETE /api/books/{id}** - Delete book (creator-only access)

**Access Control Model:**
- Create: Requires "Author" role
- Read (list/get): Anonymous access allowed
- Update/Delete: Requires authentication + creator ownership verification

**Data Model:**
Book entity with 8 fields: id, title, author, ISBN, genre, publicationDate, createdBy, createdAt

### Non-Functional Requirements
- **Performance:** 95th percentile response < 200ms (GET < 150ms)
- **Throughput:** Support 1000+ concurrent users
- **Scale:** Handle up to 1 million book records
- **Availability:** 99.9% uptime SLA
- **Reliability:** Graceful error handling with detailed messages
- **Security:** RBAC enforcement, no credentials in logs, HTTPS in production
- **Usability:** Auto-generated Swagger documentation, consistent error format

### Key Constraints
- **File-based storage:** JSON files in `src/main/resources/data/` (not database)
- **Stateless design:** Each request independent, no server-side session state
- **Single instance (MVP):** Multi-instance deployment deferred (requires distributed file locking)
- **No authentication layer:** Pre-authenticated users assumed, role info via Spring Security
- **Spring Boot 4.1.1:** With Java 25 toolchain

---

## High-Level Architecture

### System Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          REST API Clients                                    │
│                 (Web Browser, Mobile Apps, API Clients)                      │
└────────────────────────┬────────────────────────────────────────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │   HTTP/HTTPS (Port 8080)        │
        │   Request/Response (JSON)       │
        │                                 │
┌───────▼─────────────────────────────────▼────────────────────────────────────┐
│                        Spring Boot 4.1.1 Framework                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌─────────────────────────── API Layer ──────────────────────────────────┐ │
│  │                     REST Controllers                                    │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │ BookController                                                  │  │ │
│  │  │  - POST /api/books (createBook)                                │  │ │
│  │  │  - GET /api/books (listBooks)                                 │  │ │
│  │  │  - GET /api/books/{id} (getBook)                              │  │ │
│  │  │  - PUT /api/books/{id} (updateBook)                           │  │ │
│  │  │  - DELETE /api/books/{id} (deleteBook)                        │  │ │
│  │  │                                                                │  │ │
│  │  │ @RestController, @RequestMapping, @Operation (Swagger tags) │  │ │
│  │  │ Request/Response validation annotations                      │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                  │                                           │
│  ┌───────────────────────────────▼─────────────────────────────────────────┐ │
│  │               Service Layer (Business Logic)                             │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
│  │  │ BookService                                                     │    │ │
│  │  │  - createBook(CreateBookRequest, userId) : BookResponse       │    │ │
│  │  │  - listBooks(page, size, sort) : PagedResponse               │    │ │
│  │  │  - getBook(id) : BookResponse                                 │    │ │
│  │  │  - updateBook(id, UpdateBookRequest, userId) : BookResponse  │    │ │
│  │  │  - deleteBook(id, userId) : void                             │    │ │
│  │  │                                                                │    │ │
│  │  │ Responsibilities:                                             │    │ │
│  │  │  * Business logic orchestration                               │    │ │
│  │  │  * Authorization checks (role & ownership)                    │    │ │
│  │  │  * Field validation and business rules                        │    │ │
│  │  │  * ISBN uniqueness verification                               │    │ │
│  │  │  * Pagination logic                                           │    │ │
│  │  └─────────────────────────────────────────────────────────────────┘    │ │
│  │                                                                           │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
│  │  │ BookValidator                                                   │    │ │
│  │  │  - validateTitle(title) : ValidationError[]                   │    │ │
│  │  │  - validateISBN(isbn) : ValidationError[]                     │    │ │
│  │  │  - validateGenre(genre) : ValidationError[]                   │    │ │
│  │  │  - validatePublicationDate(date) : ValidationError[]          │    │ │
│  │  │  - validateCreateBookRequest(request) : ValidationError[]     │    │ │
│  │  │  - validateUpdateBookRequest(request) : ValidationError[]     │    │ │
│  │  │                                                                │    │ │
│  │  │ Responsibilities:                                             │    │ │
│  │  │  * Field format and constraint validation                     │    │ │
│  │  │  * ISBN format validation (ISBN-10/ISBN-13)                   │    │ │
│  │  │  * Genre enum validation                                      │    │ │
│  │  │  * Publication date future date check                         │    │ │
│  │  │  * Aggregate validation error reporting                       │    │ │
│  │  └─────────────────────────────────────────────────────────────────┘    │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                  │                                           │
│  ┌───────────────────────────────▼─────────────────────────────────────────┐ │
│  │          Data Access Layer (Repository / DAO)                           │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
│  │  │ BookRepository (Interface/Abstract)                             │    │ │
│  │  │  - save(book) : Book                                           │    │ │
│  │  │  - findAll() : List<Book>                                      │    │ │
│  │  │  - findById(id) : Optional<Book>                               │    │ │
│  │  │  - findByISBN(isbn) : Optional<Book>                           │    │ │
│  │  │  - update(id, book) : Book                                     │    │ │
│  │  │  - delete(id) : void                                           │    │ │
│  │  │  - getAllBooksCount() : long                                   │    │ │
│  │  │                                                                │    │ │
│  │  │ Responsibilities:                                             │    │ │
│  │  │  * Abstract CRUD operations                                    │    │ │
│  │  │  * Query patterns (find by ID, ISBN, etc.)                     │    │ │
│  │  │  * Pagination support                                          │    │ │
│  │  └─────────────────────────────────────────────────────────────────┘    │ │
│  │                                                                           │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
│  │  │ JsonFileBookRepository (Implementation)                         │    │ │
│  │  │  Extends: BookRepository                                        │    │ │
│  │  │                                                                │    │ │
│  │  │ Internal Operations:                                          │    │ │
│  │  │  - loadBooksFromFile() : List<Book>                           │    │ │
│  │  │  - saveBooksToFile(books) : void                              │    │ │
│  │  │  - getFilePath() : String                                     │    │ │
│  │  │  - lockFile() / unlockFile() (future: for concurrency)       │    │ │
│  │  │                                                                │    │ │
│  │  │ Jackson ObjectMapper for JSON serialization/deserialization  │    │ │
│  │  │ Classpath resource reference: src/main/resources/data/       │    │ │
│  │  └─────────────────────────────────────────────────────────────────┘    │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                    Cross-Cutting Concerns Layer                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────────┐  │
│  │ Exception        │  │ Security/RBAC    │  │ Logging & Monitoring     │  │
│  │ Handler          │  │ (Spring Security)│  │ (SLF4J/Logback)          │  │
│  │                  │  │                  │  │                          │  │
│  │ @ExceptionHandler│  │ SecurityContext  │  │ RequestLoggingFilter     │  │
│  │ GlobalException  │  │ AuthorizationRec │  │ @Slf4j Annotations      │  │
│  │ Handler          │  │ @PreAuthorize    │  │                          │  │
│  │                  │  │ @Secured         │  │                          │  │
│  │ - 400 errors     │  │                  │  │ Metrics:                 │  │
│  │ - 401/403        │  │ Authenticate     │  │ - Response times         │  │
│  │ - 404/409        │  │ Authorization    │  │ - Error counts           │  │
│  │ - 500 errors     │  │ Ownership checks │  │ - API usage patterns     │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────────────┘  │
│                                                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                         Documentation Layer                                  │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │ Springdoc OpenAPI Starter WebMVC UI                                   │ │
│  │  - Auto-generated Swagger UI: /swagger-ui/index.html                  │ │
│  │  - OpenAPI spec: /v3/api-docs                                         │ │
│  │  - @Tag, @Operation, @Schema annotations on controllers               │ │
│  │  - Request/response examples                                          │ │
│  │  - Parameter documentation                                            │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┴──────────────────────────┐
        │                                                    │
┌───────▼──────────────────┐                    ┌───────────▼─────────────────┐
│   JSON File Storage       │                    │  Spring Security Context    │
│   (File System)           │                    │  (Authentication/Roles)     │
├──────────────────────────┤                    ├────────────────────────────┤
│ src/main/resources/data/ │                    │ User Identity              │
│  ├─ books.json          │                    │ Roles: AUTHOR, ADMIN, etc. │
│  │  (Array of Books)    │                    │ Permissions: READ/WRITE    │
│  │                      │                    │                            │
│  │ Format: JSON Array   │                    │ Populated by:              │
│  │ [                    │                    │ - JWT Validation           │
│  │   { Book1 },         │                    │ - OAuth2 Provider          │
│  │   { Book2 },         │                    │ - LDAP/Custom Auth         │
│  │   ...                │                    │ - Pre-authenticated User    │
│  │ ]                    │                    │                            │
│  │                      │                    │ (Configured externally)    │
│  │ Read on startup      │                    │                            │
│  │ Write on mutations   │                    │                            │
│  │ File locking:        │                    │                            │
│  │ (Platform dependent) │                    │                            │
│  └──────────────────────┘                    └────────────────────────────┘
```

---

## Detailed Component Architecture

### 1. API Layer (REST Controllers)

#### BookController
**Location:** `src/main/java/com/epam/book_review_svc/controller/BookController.java`

**Responsibilities:**
- Map HTTP requests to service method calls
- Validate HTTP request structure (method, headers, parameters)
- Return appropriate HTTP status codes
- Handle Spring-specific request binding and deserialization
- Document endpoints with Swagger annotations

**Endpoint Mappings:**

| HTTP Method | Path | Handler Method | Access Level |
|---|---|---|---|
| POST | /api/books | createBook() | Author+ |
| GET | /api/books | listBooks() | Public |
| GET | /api/books/{id} | getBook() | Public |
| PUT | /api/books/{id} | updateBook() | Creator+ |
| DELETE | /api/books/{id} | deleteBook() | Creator+ |

**Controller Implementation Pattern:**
```java
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book management APIs")
public class BookController {
    
    @PostMapping
    @Operation(summary = "Create a new book")
    public ResponseEntity<BookResponse> createBook(
        @Valid @RequestBody CreateBookRequest request,
        Principal principal
    ) {
        // Controller: Validate HTTP structure (Spring handles via @Valid)
        // Controller: Extract authenticated user
        // Service: Validate business rules, check authorization
        // Service: Persist data
        // Controller: Return HTTP 201 with Location header
    }
    
    @GetMapping
    @Operation(summary = "List books with pagination")
    public ResponseEntity<PagedResponse<BookResponse>> listBooks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        // Validation: page/size ranges
        // Service: Retrieve paginated list
        // Controller: Return HTTP 200
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a specific book")
    public ResponseEntity<BookResponse> getBook(@PathVariable String id) {
        // Service: Find book by ID
        // Controller: Return HTTP 200 or 404
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a book")
    public ResponseEntity<BookResponse> updateBook(
        @PathVariable String id,
        @Valid @RequestBody UpdateBookRequest request,
        Principal principal
    ) {
        // Service: Check authorization (creator-only)
        // Service: Validate business rules
        // Service: Persist changes
        // Controller: Return HTTP 200
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book")
    public ResponseEntity<Void> deleteBook(
        @PathVariable String id,
        Principal principal
    ) {
        // Service: Check authorization (creator-only)
        // Service: Delete from storage
        // Controller: Return HTTP 204
    }
}
```

**Annotations Used:**
- `@RestController` - Declares component as REST controller with JSON serialization
- `@RequestMapping` - Class-level route prefix
- `@PostMapping/@GetMapping/@PutMapping/@DeleteMapping` - HTTP method mapping
- `@PathVariable` - Extract path parameters
- `@RequestParam` - Extract query parameters
- `@Valid` - Trigger Bean Validation on request body
- `@Operation` / `@Tag` - Swagger/OpenAPI documentation
- `@ResponseStatus` - Override default HTTP status codes

---

### 2. Service Layer (Business Logic)

#### BookService
**Location:** `src/main/java/com/epam/book_review_svc/service/BookService.java`

**Responsibilities:**
- Orchestrate business logic flows
- Enforce authorization rules (RBAC, ownership checks)
- Coordinate validation and data persistence
- Handle pagination calculations
- Perform business-level exception handling
- Manage domain logic for ISBN uniqueness, date validation, etc.

**Core Operations:**

```java
@Service
@Slf4j
public class BookService {
    
    private final BookRepository bookRepository;
    private final BookValidator bookValidator;
    
    public BookResponse createBook(CreateBookRequest request, String userId) {
        // Step 1: Validate authorization (Author role)
        if (!hasAuthorRole(userId)) {
            throw new ForbiddenException("Only authors can create books");
        }
        
        // Step 2: Validate input fields
        List<ValidationError> errors = bookValidator.validateCreateBookRequest(request);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        
        // Step 3: Check ISBN uniqueness
        if (bookRepository.findByISBN(request.getISBN()).isPresent()) {
            throw new ConflictException("ISBN already exists");
        }
        
        // Step 4: Create domain entity
        Book book = Book.builder()
            .id(generateUUID())
            .title(request.getTitle())
            .author(request.getAuthor())
            .ISBN(request.getISBN())
            .genre(request.getGenre())
            .publicationDate(request.getPublicationDate())
            .createdBy(userId)
            .createdAt(getCurrentUTCTimestamp())
            .build();
        
        // Step 5: Persist to storage
        Book saved = bookRepository.save(book);
        
        // Step 6: Convert to response DTO
        return convertToResponse(saved);
    }
    
    public PagedResponse<BookResponse> listBooks(
        int page, int size, String sort
    ) {
        // Validate pagination parameters
        if (page < 0) {
            throw new BadRequestException("Page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }
        
        // Retrieve all books (in-memory pagination)
        List<Book> allBooks = bookRepository.findAll();
        
        // Apply sorting
        List<Book> sorted = applySort(allBooks, sort);
        
        // Calculate pagination
        int totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<Book> pageContent = sorted.subList(startIndex, endIndex);
        
        // Convert to response DTOs
        List<BookResponse> content = pageContent.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        // Build paged response
        return PagedResponse.<BookResponse>builder()
            .content(content)
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNextPage(page < totalPages - 1)
            .hasPreviousPage(page > 0)
            .build();
    }
    
    public BookResponse getBook(String id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Book not found"));
        
        return convertToResponse(book);
    }
    
    public BookResponse updateBook(
        String id, UpdateBookRequest request, String userId
    ) {
        // Step 1: Retrieve existing book
        Book existing = bookRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Book not found"));
        
        // Step 2: Check authorization (creator only)
        if (!existing.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Only the creator can update this book");
        }
        
        // Step 3: Validate update request
        List<ValidationError> errors = bookValidator.validateUpdateBookRequest(request);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        
        // Step 4: Check ISBN uniqueness (if changed, excluding self)
        if (!request.getISBN().equals(existing.getISBN())) {
            if (bookRepository.findByISBN(request.getISBN()).isPresent()) {
                throw new ConflictException("ISBN already exists");
            }
        }
        
        // Step 5: Update mutable fields only
        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setISBN(request.getISBN());
        existing.setGenre(request.getGenre());
        existing.setPublicationDate(request.getPublicationDate());
        // Do NOT update: createdBy, createdAt
        
        // Step 6: Persist changes
        Book updated = bookRepository.update(id, existing);
        
        // Step 7: Return response
        return convertToResponse(updated);
    }
    
    public void deleteBook(String id, String userId) {
        // Step 1: Retrieve existing book
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Book not found"));
        
        // Step 2: Check authorization (creator only)
        if (!book.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Only the creator can delete this book");
        }
        
        // Step 3: Delete from storage
        bookRepository.delete(id);
    }
    
    // Helper methods
    private BookResponse convertToResponse(Book book) {
        return BookResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .ISBN(book.getISBN())
            .genre(book.getGenre())
            .publicationDate(book.getPublicationDate())
            .createdBy(book.getCreatedBy())
            .createdAt(book.getCreatedAt())
            .build();
    }
}
```

**Key Design Decisions:**
- All business logic flows through service layer
- Authorization checks at service layer (not controller)
- Validation delegated to BookValidator component
- Exception handling with custom exceptions (ForbiddenException, NotFoundException, etc.)
- Uses repository abstraction for data access
- In-memory pagination (for single-instance deployment)
- Immutable fields (createdBy, createdAt) protected from updates

#### BookValidator
**Location:** `src/main/java/com/epam/book_review_svc/service/BookValidator.java`

**Responsibilities:**
- Validate individual field constraints
- Validate business rules
- Aggregate validation errors
- Return detailed error messages

**Validation Rules Implementation:**

```java
@Component
@Slf4j
public class BookValidator {
    
    private static final int TITLE_MIN_LENGTH = 1;
    private static final int TITLE_MAX_LENGTH = 500;
    private static final int AUTHOR_MIN_LENGTH = 1;
    private static final int AUTHOR_MAX_LENGTH = 200;
    private static final Set<String> VALID_GENRES = Set.of(
        "Fiction", "Non-Fiction", "Mystery", "Romance", "Science Fiction",
        "Fantasy", "Biography", "History", "Self-Help", "Children", "Young Adult"
    );
    
    public List<ValidationError> validateCreateBookRequest(CreateBookRequest req) {
        List<ValidationError> errors = new ArrayList<>();
        
        errors.addAll(validateTitle(req.getTitle()));
        errors.addAll(validateAuthor(req.getAuthor()));
        errors.addAll(validateISBN(req.getISBN()));
        errors.addAll(validateGenre(req.getGenre()));
        errors.addAll(validatePublicationDate(req.getPublicationDate()));
        
        return errors;
    }
    
    private List<ValidationError> validateTitle(String title) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (title == null || title.trim().isEmpty()) {
            errors.add(new ValidationError("title", "Title is required"));
            return errors;
        }
        
        String trimmed = title.trim();
        if (trimmed.length() < TITLE_MIN_LENGTH || trimmed.length() > TITLE_MAX_LENGTH) {
            errors.add(new ValidationError("title", 
                String.format("Title must be between %d and %d characters", 
                    TITLE_MIN_LENGTH, TITLE_MAX_LENGTH)));
        }
        
        return errors;
    }
    
    private List<ValidationError> validateISBN(String isbn) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (isbn == null || isbn.trim().isEmpty()) {
            errors.add(new ValidationError("ISBN", "ISBN is required"));
            return errors;
        }
        
        // Remove hyphens for validation
        String cleanISBN = isbn.replaceAll("-", "");
        
        // Validate ISBN-10 (10 digits) or ISBN-13 (13 digits)
        if (!cleanISBN.matches("^\\d{10}$") && !cleanISBN.matches("^\\d{13}$")) {
            errors.add(new ValidationError("ISBN", 
                "Invalid ISBN format. Expected ISBN-10 (10 digits) or ISBN-13 (13 digits)"));
        }
        
        return errors;
    }
    
    private List<ValidationError> validateGenre(String genre) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (genre == null || genre.trim().isEmpty()) {
            errors.add(new ValidationError("genre", "Genre is required"));
            return errors;
        }
        
        if (!VALID_GENRES.contains(genre)) {
            errors.add(new ValidationError("genre", 
                String.format("Genre must be one of: %s", String.join(", ", VALID_GENRES))));
        }
        
        return errors;
    }
    
    private List<ValidationError> validatePublicationDate(LocalDate date) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (date == null) {
            errors.add(new ValidationError("publicationDate", "Publication date is required"));
            return errors;
        }
        
        // Check if date is in future
        if (date.isAfter(LocalDate.now())) {
            errors.add(new ValidationError("publicationDate", 
                "Publication date cannot be in the future"));
        }
        
        return errors;
    }
}
```

**Validation Error Aggregation:**
- All validation errors collected before throwing exception
- Single response with complete error list (better UX than fail-fast)
- Field-level error details for clear remediation

---

### 3. Data Access Layer (Repository Pattern)

#### BookRepository (Interface)
**Location:** `src/main/java/com/epam/book_review_svc/repository/BookRepository.java`

**Responsibilities:**
- Define CRUD contract
- Specify query patterns
- Abstract persistence mechanism from business logic

```java
public interface BookRepository {
    
    Book save(Book book);
    
    Optional<Book> findById(String id);
    
    Optional<Book> findByISBN(String isbn);
    
    List<Book> findAll();
    
    Book update(String id, Book book);
    
    void delete(String id);
    
    long count();
}
```

#### JsonFileBookRepository (Implementation)
**Location:** `src/main/java/com/epam/book_review_svc/repository/JsonFileBookRepository.java`

**Responsibilities:**
- Implement CRUD operations on JSON file
- Handle file I/O with Jackson ObjectMapper
- Provide transaction-like behavior (read-modify-write cycle)
- Future: Implement file locking for concurrent writes

**Implementation Pattern:**

```java
@Repository
@Slf4j
public class JsonFileBookRepository implements BookRepository {
    
    private static final String DATA_FILE = "classpath:data/books.json";
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    
    @Autowired
    public JsonFileBookRepository(
        ObjectMapper objectMapper, 
        ResourceLoader resourceLoader
    ) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public Book save(Book book) {
        try {
            List<Book> books = loadBooksFromFile();
            books.add(book);
            saveBooksToFile(books);
            log.info("Book saved: {}", book.getId());
            return book;
        } catch (IOException e) {
            log.error("Error saving book", e);
            throw new DataAccessException("Failed to save book", e);
        }
    }
    
    @Override
    public Optional<Book> findById(String id) {
        try {
            List<Book> books = loadBooksFromFile();
            return books.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
        } catch (IOException e) {
            log.error("Error finding book by ID", e);
            throw new DataAccessException("Failed to retrieve book", e);
        }
    }
    
    @Override
    public Optional<Book> findByISBN(String isbn) {
        try {
            List<Book> books = loadBooksFromFile();
            return books.stream()
                .filter(b -> b.getISBN().equals(isbn))
                .findFirst();
        } catch (IOException e) {
            log.error("Error finding book by ISBN", e);
            throw new DataAccessException("Failed to check ISBN uniqueness", e);
        }
    }
    
    @Override
    public List<Book> findAll() {
        try {
            return loadBooksFromFile();
        } catch (IOException e) {
            log.error("Error loading all books", e);
            throw new DataAccessException("Failed to load books", e);
        }
    }
    
    @Override
    public Book update(String id, Book book) {
        try {
            List<Book> books = loadBooksFromFile();
            
            // Find and replace
            int index = -1;
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
            
            if (index == -1) {
                throw new NotFoundException("Book not found for update");
            }
            
            books.set(index, book);
            saveBooksToFile(books);
            log.info("Book updated: {}", id);
            return book;
        } catch (IOException e) {
            log.error("Error updating book", e);
            throw new DataAccessException("Failed to update book", e);
        }
    }
    
    @Override
    public void delete(String id) {
        try {
            List<Book> books = loadBooksFromFile();
            
            boolean removed = books.removeIf(b -> b.getId().equals(id));
            
            if (!removed) {
                throw new NotFoundException("Book not found for deletion");
            }
            
            saveBooksToFile(books);
            log.info("Book deleted: {}", id);
        } catch (IOException e) {
            log.error("Error deleting book", e);
            throw new DataAccessException("Failed to delete book", e);
        }
    }
    
    @Override
    public long count() {
        try {
            return loadBooksFromFile().size();
        } catch (IOException e) {
            log.error("Error counting books", e);
            throw new DataAccessException("Failed to count books", e);
        }
    }
    
    // Internal helper methods
    
    private List<Book> loadBooksFromFile() throws IOException {
        try {
            Resource resource = resourceLoader.getResource(DATA_FILE);
            
            if (!resource.exists()) {
                log.warn("Books file does not exist, returning empty list");
                return new ArrayList<>();
            }
            
            File file = resource.getFile();
            List<Book> books = Arrays.asList(objectMapper.readValue(file, Book[].class));
            return new ArrayList<>(books);
        } catch (FileNotFoundException e) {
            log.warn("Books file not found, returning empty list");
            return new ArrayList<>();
        }
    }
    
    private void saveBooksToFile(List<Book> books) throws IOException {
        try {
            Resource resource = resourceLoader.getResource(DATA_FILE);
            File file = resource.getFile();
            
            // Ensure parent directory exists
            file.getParentFile().mkdirs();
            
            // Write with pretty printing
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, books);
            
            log.debug("Books persisted to file, count: {}", books.size());
        } catch (IOException e) {
            log.error("Error writing books to file", e);
            throw e;
        }
    }
}
```

**Key Design Decisions:**
- File-based storage abstracted behind repository interface
- Jackson ObjectMapper handles JSON serialization/deserialization
- Read-modify-write pattern for mutations (eventual consistency)
- Exception wrapping for data access errors
- Logging at repository layer for debugging
- Future: File locking can be added without changing service layer

---

### 4. Model/DTO Layer

#### Domain Model (Book Entity)
**Location:** `src/main/java/com/epam/book_review_svc/model/Book.java`

**Responsibilities:**
- Represent core book domain entity
- Annotated with Lombok for boilerplate reduction
- Used for JSON serialization/deserialization

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "title", "author", "ISBN", "genre", "publicationDate", "createdBy", "createdAt"})
public class Book {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("author")
    private String author;
    
    @JsonProperty("ISBN")
    private String ISBN;
    
    @JsonProperty("genre")
    private String genre;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("publicationDate")
    private LocalDate publicationDate;
    
    @JsonProperty("createdBy")
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;
}
```

#### Request DTOs

**CreateBookRequest**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    @NotBlank(message = "ISBN is required")
    private String ISBN;
    
    @NotBlank(message = "Genre is required")
    private String genre;
    
    @NotNull(message = "Publication date is required")
    private LocalDate publicationDate;
}
```

**UpdateBookRequest**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {
    
    private String title;
    private String author;
    private String ISBN;
    private String genre;
    private LocalDate publicationDate;
}
```

#### Response DTOs

**BookResponse**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "title", "author", "ISBN", "genre", "publicationDate", "createdBy", "createdAt"})
public class BookResponse {
    
    private String id;
    private String title;
    private String author;
    private String ISBN;
    private String genre;
    private LocalDate publicationDate;
    private String createdBy;
    private OffsetDateTime createdAt;
}
```

**PagedResponse**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
}
```

#### Error Response DTOs

**ErrorResponse**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private int status;
    private String message;
    private String path;
    private OffsetDateTime timestamp;
    private List<FieldError> errors;  // Optional for detailed validation errors
}
```

**FieldError**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {
    
    private String field;
    private String message;
}
```

**Key DTO Design Decisions:**
- Request DTOs only contain mutable fields
- Response DTOs include all fields (immutable + mutable)
- Bean Validation annotations on request DTOs
- Jackson annotations for precise JSON serialization
- Separate DTOs from domain model for flexibility
- Response DTOs immutable (builder pattern)

---

### 5. Cross-Cutting Concerns

#### Exception Handling
**Location:** `src/main/java/com/epam/book_review_svc/exception/`

**Custom Exceptions:**
```java
// Base exception
public abstract class ApiException extends RuntimeException {
    abstract public int getStatusCode();
}

// Specific exceptions
public class NotFoundException extends ApiException {
    @Override
    public int getStatusCode() { return 404; }
}

public class ConflictException extends ApiException {
    @Override
    public int getStatusCode() { return 409; }
}

public class ForbiddenException extends ApiException {
    @Override
    public int getStatusCode() { return 403; }
}

public class BadRequestException extends ApiException {
    @Override
    public int getStatusCode() { return 400; }
}

public class ValidationException extends ApiException {
    private List<FieldError> fieldErrors;
    
    @Override
    public int getStatusCode() { return 400; }
}

public class DataAccessException extends ApiException {
    @Override
    public int getStatusCode() { return 500; }
}
```

**Global Exception Handler:**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        NotFoundException ex, HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .status(404)
            .message("Book not found")
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .build();
        
        return ResponseEntity.status(404).body(error);
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
        ForbiddenException ex, HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .status(403)
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .build();
        
        return ResponseEntity.status(403).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        ValidationException ex, HttpServletRequest request
    ) {
        List<FieldError> fieldErrors = ex.getFieldErrors();
        
        ErrorResponse error = ErrorResponse.builder()
            .status(400)
            .message("Validation failed")
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .errors(fieldErrors)
            .build();
        
        return ResponseEntity.status(400).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        List<FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(400)
            .message("Validation failed")
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .errors(fieldErrors)
            .build();
        
        return ResponseEntity.status(400).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
        Exception ex, HttpServletRequest request
    ) {
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .status(500)
            .message("Internal server error")
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .build();
        
        return ResponseEntity.status(500).body(error);
    }
}
```

#### Security & Authorization
**Location:** `src/main/java/com/epam/book_review_svc/security/`

**Authorization Service:**
```java
@Component
@Slf4j
public class AuthorizationService {
    
    public boolean hasAuthorRole(String userId) {
        // Check if user has AUTHOR role via Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));
    }
    
    public boolean isCreator(String userId, String createdBy) {
        // Verify user is the creator of the resource
        return userId != null && userId.equals(createdBy);
    }
}
```

#### Logging & Monitoring
**Location:** `src/main/java/com/epam/book_review_svc/config/`

**Request Logging Filter:**
```java
@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("HTTP {} {} - Status: {} - Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);
        }
    }
}
```

---

## Data Flow & Integration Patterns

### Request-Response Lifecycle

#### Create Book Flow
```
1. HTTP Request
   POST /api/books
   {
     "title": "...",
     "author": "...",
     "ISBN": "...",
     "genre": "...",
     "publicationDate": "..."
   }

2. BookController.createBook()
   - Extract authenticated user (Principal)
   - Deserialize JSON to CreateBookRequest (Spring + Jackson)
   - Trigger @Valid validation (Bean Validation)
   
3. BookService.createBook()
   - Check authorization (hasAuthorRole)
   - Call BookValidator.validateCreateBookRequest()
   - Check ISBN uniqueness via BookRepository.findByISBN()
   - Generate UUID for book ID
   - Create Book entity with auto-populated createdBy, createdAt
   - Call BookRepository.save()
   
4. JsonFileBookRepository.save()
   - Load current books.json file
   - Add new book to list
   - Write updated list back to books.json
   - Return saved book
   
5. BookService (continued)
   - Convert Book entity to BookResponse DTO
   - Return response to controller
   
6. BookController (continued)
   - Return ResponseEntity with HTTP 201 Created
   - Include Location header: /api/books/{id}
   - JSON response body with BookResponse

7. HTTP Response
   201 Created
   {
     "id": "...",
     "title": "...",
     "author": "...",
     "ISBN": "...",
     "genre": "...",
     "publicationDate": "...",
     "createdBy": "user123",
     "createdAt": "2026-09-10T14:30:00.000Z"
   }
```

#### List Books Flow
```
1. HTTP Request
   GET /api/books?page=0&size=20&sort=createdAt,desc

2. BookController.listBooks()
   - Extract query parameters: page, size, sort
   - Validate parameter ranges (0 <= page, 1 <= size <= 100)
   
3. BookService.listBooks()
   - Call BookRepository.findAll() to get all books
   - Apply sort transformation (in-memory sort by field + direction)
   - Calculate pagination: startIndex, endIndex, totalPages
   - Extract page slice from sorted list
   - Convert each Book to BookResponse
   - Build PagedResponse with metadata
   
4. BookController (continued)
   - Return ResponseEntity with HTTP 200 OK
   - JSON response body with PagedResponse

7. HTTP Response
   200 OK
   {
     "content": [ { Book1 }, { Book2 }, ... ],
     "page": 0,
     "size": 20,
     "totalElements": 150,
     "totalPages": 8,
     "hasNextPage": true,
     "hasPreviousPage": false
   }
```

#### Update Book Flow (with Authorization)
```
1. HTTP Request
   PUT /api/books/{id}
   {
     "title": "Updated Title",
     "author": "...",
     ...
   }

2. BookController.updateBook()
   - Extract path variable: id
   - Extract authenticated user (Principal)
   - Deserialize JSON to UpdateBookRequest
   - Trigger @Valid validation
   
3. BookService.updateBook()
   - Call BookRepository.findById(id)
   - Check createdBy matches authenticated user (owner check)
   - If not owner: throw ForbiddenException → 403
   - Call BookValidator.validateUpdateBookRequest()
   - If ISBN changed: check uniqueness (excluding self)
   - Update mutable fields (title, author, ISBN, genre, publicationDate)
   - Preserve immutable fields (id, createdBy, createdAt)
   - Call BookRepository.update()
   
4. JsonFileBookRepository.update()
   - Load current books.json
   - Find book by ID and replace in list
   - Write updated list back to books.json
   
5. BookService (continued)
   - Convert updated Book to BookResponse
   - Return response to controller
   
6. BookController (continued)
   - Return ResponseEntity with HTTP 200 OK

7. HTTP Response
   200 OK
   { Updated BookResponse }
```

### Pagination Logic

**In-Memory Pagination (for JSON file storage):**

```
Input:
- total books: 150
- page: 2 (0-indexed)
- size: 20
- sort: createdAt,desc

Processing:
1. Load all books from JSON file
2. Sort by createdAt descending
3. Calculate metadata:
   - totalElements: 150
   - totalPages: ceil(150 / 20) = 8
   - hasNextPage: page (2) < totalPages (8) - 1 = true
   - hasPreviousPage: page (2) > 0 = true
4. Calculate slice:
   - startIndex: 2 * 20 = 40
   - endIndex: min(40 + 20, 150) = 60
   - return books[40:60]

Response:
{
  "content": [ books 40-60 ],
  "page": 2,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "hasNextPage": true,
  "hasPreviousPage": true
}
```

### Entity Relationship Diagram (Logical)

```
Book Entity
-----------
├─ id (PK, UUID)
├─ title (string, required)
├─ author (string, required)
├─ ISBN (string, required, unique)
├─ genre (enum, required)
├─ publicationDate (date, required)
├─ createdBy (string, required, immutable) → User ID/Username
└─ createdAt (datetime, required, immutable, UTC)

JSON Storage (books.json):
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "The Great Gatsby",
    ...
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "title": "To Kill a Mockingbird",
    ...
  }
]
```

---

## Technology Stack & Justifications

### Framework & Core Libraries

| Component | Technology | Version | Justification |
|---|---|---|---|
| **Framework** | Spring Boot | 4.1.1 | Modern REST framework with built-in features (security, validation, documentation) |
| **Java Version** | Java | 25 | Latest LTS/feature version, modern language features, performance improvements |
| **Build Tool** | Gradle | 8.5+ | Dependency management, multi-project support, active maintenance |
| **JSON Processing** | Jackson | Latest | Industry standard for JSON serialization/deserialization in Java |
| **Validation** | Bean Validation (JSR-380) | Via Spring Boot | Standard validation framework, integrates with Spring |
| **Boilerplate Reduction** | Lombok | Latest | Reduces verbosity of getters, setters, constructors, equals/hashCode |
| **Documentation** | Springdoc OpenAPI | 2.5.0 | Auto-generates Swagger/OpenAPI docs from annotations, no manual maintenance |
| **Testing** | JUnit 5 + Mockito | Via Spring Boot Test | Standard testing stack, excellent Spring integration |

### Architectural Patterns Used

| Pattern | Implementation | Benefit |
|---|---|---|
| **Layered Architecture** | Controller → Service → Repository | Clear separation of concerns, testability |
| **Repository Pattern** | Interface + Implementation | Abstract persistence mechanism, easy to swap JSON ↔ Database |
| **Dependency Injection** | Spring @Component/@Service/@Repository | Loose coupling, testability, lifecycle management |
| **Exception Translation** | Global @ExceptionHandler | Consistent error responses, no try-catch scattered |
| **DTO Pattern** | Request/Response DTOs separate from domain | API contract stability, flexibility in domain changes |
| **Validation Layer** | Dedicated BookValidator | Reusable validation logic, centralized rules |
| **Authorization Service** | Injected AuthorizationService | Centralized access control, testable |

### Build Configuration

**Gradle Build Profile:**
- Spring Boot 4.1.1 plugin for packaging
- Java 25 toolchain (enforced via gradle)
- Dependency management plugin for version resolution
- Test framework: JUnit 5

---

## Scalability & Performance Considerations

### Single-Instance Deployment (MVP)

**Horizontal Scaling Challenges:**
- **File-based storage:** Read-modify-write race conditions with concurrent writes
- **In-memory pagination:** Full dataset loaded into memory
- **No distributed cache:** Each instance loads JSON file independently

**Performance Characteristics:**
- **Load all books:** O(n) file I/O, all books into memory
- **Find by ID:** O(n) linear search through in-memory list
- **Find by ISBN:** O(n) linear search
- **Pagination:** O(n log n) in-memory sort + slice
- **Create/Update/Delete:** O(n) read all + rewrite all

**Expected Performance (with ~10k books):**
- GET /api/books (list): ~50-100ms
- GET /api/books/{id}: ~20-50ms
- POST /api/books: ~100-200ms (read-modify-write)
- PUT /api/books/{id}: ~150-300ms (read-modify-write)
- DELETE /api/books/{id}: ~100-200ms (read-modify-write)

### Future Optimization: Database Migration

**Recommended Path (when scaling beyond MVP):**
1. **Phase 1:** Add caching layer (Redis) for read-heavy queries
2. **Phase 2:** Migrate to relational database (PostgreSQL/MySQL)
   - Add database indexes on: ISBN (unique), createdBy, createdAt
   - Use native pagination queries
   - Enable true concurrent writes with ACID transactions
3. **Phase 3:** Add query optimization layer
   - Lazy loading for related data (if books get ratings, reviews, etc.)
   - Batch operations for bulk imports
   - Full-text search indexing

**Repository Pattern Advantage:**
Repository interface remains unchanged → Service and controller layers unaffected by persistence migration

### Caching Strategy

**Current Implementation (MVP - No Cache):**
- Every request triggers file I/O
- Suitable for development/testing
- Acceptable for <1000 concurrent users

**Recommended Caching (for scaling):**
1. **Query-Level Cache (Spring Cache):**
   ```java
   @Cacheable(value = "books")
   public List<Book> findAll() { ... }
   
   @CacheEvict(value = "books")
   public Book save(Book book) { ... }
   ```

2. **HTTP Response Caching:**
   - GET endpoints: Cache-Control: public, max-age=300 (5 min)
   - PUT/DELETE: No-cache (immediate consistency)

3. **Application-Level Cache:**
   - In-memory cache with TTL (Spring Cache + Caffeine)
   - Distributed cache (Redis) for multi-instance deployments

---

## Security Architecture

### Authentication & Authorization

**Current Design (Assumes Pre-Authenticated):**
- User identity available via Spring Security `Principal`
- Roles populated by authentication provider (configured externally)
- No auth implementation in API layer

**Authorization Enforcement:**
- **Create:** Requires ROLE_AUTHOR
- **Read:** No restrictions (public)
- **Update:** Requires authentication + ownership (createdBy == userId)
- **Delete:** Requires authentication + ownership (createdBy == userId)

**Implementation Location:**
- Service layer (BookService) performs all authorization checks
- Controller passes authenticated user to service
- Exception handler converts authorization errors to HTTP 403

**Recommended Authentication Integration Points:**
1. **JWT Token Validation:** Spring Security OAuth2 Resource Server
2. **OAuth2 Provider:** Spring Security OAuth2 Client
3. **LDAP/Directory:** Spring Security LDAP
4. **Custom Auth:** Implement AuthenticationProvider

### Data Protection

**Current (MVP):**
- No encryption for stored ISBN (acceptable for public book catalog)
- No encryption for createdBy field (acceptable, audit trail only)
- HTTPS enforcement at infrastructure level (not API level)

**Recommended for Production:**
- Enable HTTPS with TLS 1.2+ (at load balancer/proxy)
- Sanitize all logs (no credentials, no sensitive data)
- Add API rate limiting (Spring Cloud Gateway or nginx)
- Implement CORS policy for cross-origin requests

### Input Validation

**Security-Related Validation:**
- Title/Author max length: Prevent DoS via very long strings
- ISBN format validation: Prevent invalid data injection
- Publication date validation: Business logic enforcement
- Genre enum: Whitelist approach, no code injection via user input

**Not Implemented (Out of Scope):**
- SQL injection prevention (using JSON files, not SQL)
- XSS prevention (API returns JSON, not HTML)
- CSRF tokens (stateless REST, not applicable)

---

## Error Handling & Resilience

### HTTP Status Code Mapping

| Scenario | HTTP Status | Thrown Exception | Handler |
|---|---|---|---|
| Book created successfully | 201 Created | N/A | BookController.createBook() |
| Book found | 200 OK | N/A | BookController.getBook() |
| Books listed | 200 OK | N/A | BookController.listBooks() |
| Book updated | 200 OK | N/A | BookController.updateBook() |
| Book deleted | 204 No Content | N/A | BookController.deleteBook() |
| Invalid input | 400 Bad Request | ValidationException | GlobalExceptionHandler |
| Missing authentication | 401 Unauthorized | Spring Security | Spring Security Filter |
| Insufficient permissions | 403 Forbidden | ForbiddenException | GlobalExceptionHandler |
| Book not found | 404 Not Found | NotFoundException | GlobalExceptionHandler |
| ISBN duplicate | 409 Conflict | ConflictException | GlobalExceptionHandler |
| Server error | 500 Internal Server Error | Exception | GlobalExceptionHandler |

### Error Response Format

**Validation Error Response (400):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00.000Z",
  "errors": [
    {
      "field": "ISBN",
      "message": "Invalid ISBN format"
    },
    {
      "field": "publicationDate",
      "message": "Publication date cannot be in the future"
    }
  ]
}
```

**Authorization Error Response (403):**
```json
{
  "status": 403,
  "message": "Only the creator can update this book",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00.000Z"
}
```

**Not Found Error Response (404):**
```json
{
  "status": 404,
  "message": "Book not found",
  "path": "/api/books/invalid-id",
  "timestamp": "2026-09-10T14:30:00.000Z"
}
```

### Graceful Degradation

**File System Errors:**
- If books.json is unreadable: Throw DataAccessException → 500 Internal Server Error
- If books.json is corrupted: Jackson parse exception → 500
- If write permission denied: IOException → 500

**Recommended Improvements:**
- Implement file backup/recovery strategy
- Add retry logic for transient I/O errors
- Implement circuit breaker pattern for persistent failures
- Add health check endpoint (`/actuator/health`)

---

## API Documentation & Usability

### Swagger/OpenAPI Integration

**Auto-Generated Documentation Endpoint:**
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Accessible without authentication** (can be restricted in production)

**Annotation-Based Documentation:**

```java
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book Catalog Management APIs")
public class BookController {
    
    @PostMapping
    @Operation(
        summary = "Create a new book",
        description = "Create a new book in the catalog (author role required)",
        tags = {"Books"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Book created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "403", description = "User lacks author role"),
        @ApiResponse(responseCode = "409", description = "ISBN already exists")
    })
    public ResponseEntity<BookResponse> createBook(
        @Valid @RequestBody
        @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json"
        )
        CreateBookRequest request,
        Principal principal
    ) { ... }
}
```

**Documentation Auto-Generation Benefits:**
- Always in sync with code (source of truth)
- Interactive testing via Swagger UI
- No manual API documentation maintenance
- Clear parameter/response documentation
- Example requests/responses

---

## Implementation Roadmap

### Phase 1: Core CRUD (MVP)
- [ ] BookController with 5 endpoints
- [ ] BookService with business logic
- [ ] BookValidator for field validation
- [ ] JsonFileBookRepository implementation
- [ ] Global exception handler
- [ ] Response DTOs and error format
- [ ] Integration tests (80% coverage)
- [ ] Swagger documentation

**Estimated Effort:** 2-3 weeks

### Phase 2: Authorization & Security
- [ ] Spring Security configuration
- [ ] Role-based access control (RBAC)
- [ ] Creator-only authorization checks
- [ ] Request logging filter
- [ ] Security headers configuration
- [ ] Authentication provider integration

**Estimated Effort:** 1-2 weeks

### Phase 3: Monitoring & Operations
- [ ] Centralized logging (SLF4J/Logback)
- [ ] Metrics collection (Micrometer)
- [ ] Health check endpoints (Spring Actuator)
- [ ] Performance monitoring
- [ ] Error tracking (Sentry, ELK, etc.)

**Estimated Effort:** 1 week

### Phase 4: Scalability & Database Migration
- [ ] Add caching layer (Redis/Caffeine)
- [ ] Database migration (PostgreSQL)
- [ ] Connection pooling
- [ ] Query optimization & indexing
- [ ] Distributed transaction handling
- [ ] Multi-instance deployment testing

**Estimated Effort:** 3-4 weeks

### Phase 5: Advanced Features
- [ ] Advanced search/filtering
- [ ] Sorting by multiple fields
- [ ] Bulk operations (import/export)
- [ ] API versioning (v1, v2)
- [ ] Rate limiting
- [ ] Caching headers

**Estimated Effort:** 2-3 weeks

---

## Testing Strategy

### Unit Tests (40% of test suite)

**Service Layer Tests:**
- BookService CRUD operations
- Authorization checks (hasAuthorRole, isCreator)
- Business rule validation (ISBN uniqueness, date validation)
- Error scenarios (not found, conflict, forbidden)

**Repository Tests:**
- BookRepository interface contracts
- File I/O mocking with Mockito
- Error handling (IOException, corrupted JSON)

**Validator Tests:**
- Each validation rule in isolation
- Boundary cases (min/max length, edge dates)
- Multiple validation errors aggregation

### Integration Tests (40% of test suite)

**Controller Tests:**
- HTTP request/response mapping
- @Valid Bean Validation integration
- Status code assertions (200, 201, 204, 400, 403, 404, 409)
- Error response format validation
- Spring context startup

**Repository Integration Tests:**
- Actual file I/O operations
- JSON deserialization correctness
- Concurrent write scenarios (race conditions)

**Full CRUD Flow Tests:**
- Create → List → Get → Update → Delete workflow
- Data integrity across operations
- Authorization enforcement in integrated flow

### Acceptance Tests (20% of test suite)

**Specification-Based Tests:**
- Test each acceptance criteria from requirements
- End-to-end scenarios
- Performance benchmarks (response time SLA)
- Pagination edge cases

**Example Test Scenarios:**
```
✓ AC-1: Only users with author role can create books
✓ AC-2: Non-authors receive HTTP 403 Forbidden response
✓ AC-3: All required fields must be present in request
✓ AC-4: Missing required fields return HTTP 400 Bad Request
✓ AC-5: Invalid ISBN format returns HTTP 400 Bad Request
✓ AC-6: Duplicate ISBN returns HTTP 409 Conflict
✓ AC-7: Created book includes auto-generated ID
✓ AC-8: CreatedBy field populated with authenticated user
✓ AC-9: CreatedAt field populated with current timestamp (UTC)
✓ AC-10: Response includes HTTP 201 Created status code
```

### Test Configuration

**Test Profile (application-test.properties):**
```properties
# Use in-memory data store for tests
app.data.storage=memory

# Disable external service calls
app.external.enabled=false

# Shorter timeouts for testing
server.servlet.session.timeout=1m

# Logging level for debugging
logging.level.com.epam.book_review_svc=DEBUG
```

**Test Data Setup:**
- Use @BeforeEach to populate test data
- Clear state between tests via @Transactional (or manual cleanup)
- Mock file system for repository tests
- Fixture files for large dataset testing

---

## Configuration Management

### Application Configuration

**Main Configuration File:**
`src/main/resources/application.properties`

```properties
# Application metadata
spring.application.name=book-review-svc
server.servlet.context-path=/
server.port=8080

# Jackson configuration
spring.jackson.serialization.indent-output=true
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC

# Logging configuration
logging.level.root=INFO
logging.level.com.epam.book_review_svc=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n

# Springdoc OpenAPI (Swagger)
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui/index.html

# File storage configuration
app.data.file.location=classpath:data/books.json
app.data.file.encoding=UTF-8
```

**Environment-Specific Profiles:**
- `application-dev.properties` - Development settings
- `application-prod.properties` - Production settings
- `application-test.properties` - Test settings

**Runtime Configuration (Environment Variables):**
```bash
# Spring Boot reads these automatically
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=9090
export LOGGING_LEVEL_COM_EPAM_BOOK_REVIEW_SVC=INFO
```

---

## Deployment Architecture

### Single-Instance Deployment (MVP)

```
┌─────────────────────────────────────────────┐
│            Load Balancer / Reverse Proxy    │
│           (HTTPS termination, CORS)         │
└──────────────────┬──────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
┌───────▼──────────┐  ┌──────▼────────────┐
│  Spring Boot      │  │  Spring Boot      │
│  App Instance 1   │  │  App Instance 2   │
│  :8080            │  │  :8081            │
└───────┬──────────┘  └──────┬────────────┘
        │                    │
        └──────┬─────────────┘
               │
        ┌──────▼────────────┐
        │  Shared File      │
        │  System / NFS     │
        │  books.json       │
        │  (with locking)   │
        └───────────────────┘
```

**Note:** Multi-instance deployment requires:
- Distributed file locking (Java file locks, Redis-based locks, etc.)
- OR migration to database with ACID transactions
- OR sticky sessions + single instance per file

### Container Deployment

**Docker Image (Dockerfile):**
```dockerfile
FROM openjdk:25-slim

WORKDIR /app

COPY build/libs/book-review-svc-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

**Docker Compose (for local development):**
```yaml
version: '3'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./src/main/resources/data:/app/data
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - LOGGING_LEVEL_COM_EPAM_BOOK_REVIEW_SVC=DEBUG
```

---

## Monitoring, Logging & Observability

### Logging Strategy

**Logging Levels:**
- **ERROR:** Errors that require immediate attention (IO failures, unexpected exceptions)
- **WARN:** Potentially harmful situations (missing files, deprecated API use)
- **INFO:** Important business events (book created, book deleted, user authorization denied)
- **DEBUG:** Detailed flow information (entering method, exiting method, state transitions)
- **TRACE:** Very detailed information (parameter values, return values)

**Log Format:**
```
2026-09-10 14:30:00 INFO  BookService - Book created with ID: 550e8400-e29b-41d4-a716-446655440000
2026-09-10 14:30:05 WARN  JsonFileBookRepository - Books file not found, returning empty list
2026-09-10 14:30:10 ERROR GlobalExceptionHandler - Unexpected error handling request, HTTP 500
2026-09-10 14:30:15 DEBUG BookService - Authorization check: user=user123, createdBy=user456, result=DENIED
```

**Log Destinations:**
- **Development:** Console output (Spring Boot default)
- **Production:** Centralized logging (ELK stack, Splunk, Datadog, etc.)

### Metrics & Monitoring

**Spring Boot Actuator Endpoints:**
- `/actuator/health` - Application health status
- `/actuator/metrics` - Available metrics
- `/actuator/metrics/http.server.requests` - HTTP request metrics

**Key Metrics to Monitor:**
- Request count by endpoint
- Response time (p50, p95, p99)
- Error rate (4xx, 5xx)
- Authorization failures (403 count)
- Data volume (number of books, file size)

**Recommended Monitoring Tools:**
- **Time-Series DB:** Prometheus
- **Visualization:** Grafana
- **APM:** New Relic, DataDog, Elastic APM

---

## Future Extensibility

### Hook Points for New Features

1. **Search/Filtering:**
   - Add BookSearchRepository interface
   - Extend BookRepository with findByTitle(), findByGenre(), etc.
   - Implement full-text search (Elasticsearch)

2. **Book Ratings/Reviews:**
   - Add separate RatingService and RatingRepository
   - Book entity could reference ratings (1:N relationship)
   - New endpoints: POST /api/books/{id}/ratings

3. **Advanced Pagination:**
   - Add cursor-based pagination (more scalable than offset)
   - Support multiple sort fields
   - Implement filtering predicates

4. **API Versioning:**
   - @RequestMapping("/api/v1/books") for versioning
   - Maintain backward compatibility
   - Support v1 and v2 simultaneously during migration

5. **Caching Layer:**
   - Add @Cacheable/@CacheEvict annotations
   - Integrate Redis for distributed caching
   - Implement cache invalidation strategy

6. **Batch Operations:**
   - POST /api/books/import for bulk import
   - DELETE /api/books/purge for bulk delete
   - Implement chunked processing for large files

7. **Database Migration:**
   - Replace JsonFileBookRepository with JdbcBookRepository
   - Use Spring Data JPA for ORM
   - Leverage indexes for performance
   - Enable true concurrent writes

### Technology Upgrade Paths

| Current | Upgrade Recommendation | When | Effort |
|---|---|---|---|
| JSON file storage | PostgreSQL/MySQL database | When >100k books or multi-instance | High |
| In-memory pagination | Database-native pagination | With database migration | Medium |
| No caching | Redis/Caffeine caching | When response time < 100ms required | Medium |
| Spring Boot 4.1.1 | Future versions (5.x, etc.) | When security patches available | Low |
| Gradle build | Maven (if needed) | Only if team preference changes | Medium |
| Manual API docs | GraphQL (if needed) | If clients prefer query flexibility | High |

---

## Architectural Trade-offs

### Performance vs. Complexity

**Decision: In-Memory Pagination (Simple)**
- **Trade-off:** O(n) complexity vs. O(log n) database query
- **Justification:** MVP with <100k books expected; complexity not worth it yet
- **Future:** Migrate to database queries when scale exceeds in-memory feasibility

**Decision: File-Based Storage (Simple)**
- **Trade-off:** Limited scalability vs. zero infrastructure
- **Justification:** MVP deployment simplicity; no database server needed
- **Future:** Database migration when multi-instance deployment needed

### Flexibility vs. Simplicity

**Decision: Repository Pattern (Flexible)**
- **Trade-off:** Slightly more code vs. ability to swap persistence later
- **Justification:** Future scalability requires abstraction; worth the investment
- **Payoff:** Database migration requires only new repository implementation

**Decision: DTO Layer (Flexible)**
- **Trade-off:** More classes to maintain vs. API stability
- **Justification:** Separates API contract from domain model; enables schema evolution
- **Payoff:** Domain changes don't break API consumers

### Reliability vs. Latency

**Decision: Synchronous File I/O (Reliable)**
- **Trade-off:** Slower response time vs. guaranteed consistency
- **Justification:** Correctness over performance for MVP
- **Future:** Asynchronous operations with eventual consistency for high-throughput scenarios

### Security vs. Usability

**Decision: Public Read Endpoints (Usable)**
- **Trade-off:** Accessibility vs. security
- **Justification:** Book catalog is public; no PII in data
- **Future:** Add API key/token for rate limiting if needed

**Decision: Role-Based Authorization (Secure)**
- **Trade-off:** More code vs. granular access control
- **Justification:** Prevents unauthorized modifications; essential for trust

---

## Known Limitations & Recommendations

### Concurrency Limitations

**Limitation:** File-based storage doesn't support true concurrent writes

**Workarounds:**
1. **Single Instance:** Acceptable for MVP; one JVM process per file
2. **File Locking:** Platform-dependent (Java NIO file locking); unreliable across network filesystems
3. **Distributed Lock:** Redis/Zookeeper-based locking; adds complexity
4. **Database:** ACID transactions (recommended for production)

**Recommendation:** Implement file locking as Phase 3, then migrate to database by Phase 4

### Scalability Limitations

**Limitation:** O(n) complexity for all operations due to in-memory loading

**Threshold:** Performance acceptable up to ~50k books
- Beyond 50k: Consider database migration
- Beyond 1M: Database is mandatory

**Recommendation:** Monitor book count and implement alerts for approaching threshold

### Search Performance

**Limitation:** No indexing; full-file scan for every operation

**Example:** `findByISBN()` is O(n) linear search

**Recommendation:** Plan for Elasticsearch/database index by Phase 4

### No Soft Deletes

**Limitation:** Deleted books cannot be recovered

**Recommendation:** Consider implementing soft delete (logical deletion with flag) in Phase 2 if audit trail needed

### No API Versioning

**Limitation:** Single API version; breaking changes require all clients to upgrade simultaneously

**Recommendation:** Implement API versioning (/api/v1/, /api/v2/) before breaking changes in Phase 5

---

## Summary: Architecture at a Glance

```
User Request
    ↓
[Controller Layer]
├─ HTTP Request binding
├─ Parameter validation
├─ Swagger documentation
    ↓
[Service Layer]
├─ Authorization checks
├─ Business logic
├─ Validation orchestration
├─ Pagination logic
    ↓
[Repository Layer]
├─ Data persistence abstraction
├─ File I/O operations
├─ Query patterns
    ↓
[JSON File Storage]
├─ books.json in resources/data/
├─ Array-based structure
├─ Jackson serialization

    ↓
HTTP Response (JSON)
├─ Consistent error format
├─ Pagination metadata
├─ Auto-generated Swagger docs
```

### Component Interaction Matrix

| Component | Depends On | Provides |
|---|---|---|
| BookController | BookService, Principal | HTTP endpoints, Swagger docs |
| BookService | BookRepository, BookValidator, AuthorizationService | Business logic, CRUD orchestration |
| BookValidator | None | Validation rules, error aggregation |
| BookRepository | None (interface) | CRUD contract |
| JsonFileBookRepository | ObjectMapper, ResourceLoader | File I/O implementation |
| GlobalExceptionHandler | Custom exceptions | Consistent error responses |
| Spring Security | None | Authentication context, authorization |

---

## Glossary

- **CRUD:** Create, Read, Update, Delete operations
- **DTO:** Data Transfer Object (API request/response models)
- **RBAC:** Role-Based Access Control
- **JSON:** JavaScript Object Notation (data format)
- **REST:** Representational State Transfer (API architectural style)
- **HTTP Status:** Codes indicating request outcome (200, 201, 400, 403, 404, 409, 500)
- **Pagination:** Dividing large result sets into manageable pages
- **Repository:** Pattern that abstracts data access
- **Validator:** Component that enforces business rules
- **Principal:** Authenticated user identity in Spring Security
- **Bean Validation:** JSR-380 standard validation annotations (@Valid, @NotBlank, etc.)
- **Jackson:** JSON processing library
- **Springdoc:** Library that auto-generates Swagger from Spring annotations
- **OpenAPI:** Standard specification for REST API documentation
- **Swagger UI:** Interactive web interface for API exploration

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-09-10 | Architecture Designer | Initial comprehensive architecture document with complete component design, technology justifications, data flow patterns, and scalability roadmap |

---

**Document Status:** COMPLETE & READY FOR IMPLEMENTATION  
**Next Steps:** Begin Phase 1 implementation of core CRUD endpoints


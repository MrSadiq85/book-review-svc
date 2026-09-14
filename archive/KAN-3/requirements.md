# Requirements Document: Book Catalog Management CRUD APIs

## Story Metadata
- **JIRA Issue:** KAN-3
- **Title:** Implement Book Catalog Management CRUD APIs
- **Status:** In Progress
- **Priority:** High
- **Created:** 2026-09-10
- **Project:** book-review-svc

## Story Summary

Implement a comprehensive Book Catalog Management API that provides secure REST endpoints for creating, reading, updating, and deleting book records. The API enforces role-based access control where authors can create books, all users can read books, and only the book creator can modify or delete their own entries. The service includes pagination support, comprehensive validation, and follows REST API best practices with appropriate HTTP status codes and error handling.

---

## Functional Requirements

### FR-1: Create Book (Author-Only Access)
- **Description:** Authenticated users with author role must be able to create new book records in the catalog with complete metadata.
- **Actor/User:** Author (authenticated user with author role)
- **Endpoint:** `POST /api/books`
- **Workflow/Steps:**
  1. Client submits POST request with book details in JSON body
  2. System validates user authentication and author role
  3. System validates all required fields and field formats
  4. System verifies ISBN uniqueness across existing books
  5. System generates unique book ID
  6. System records creator information (createdBy, createdAt)
  7. System persists book record to JSON data store
  8. System returns created book with HTTP 201 status

- **Expected Outcome:** New book record created with all metadata populated and returned to client
- **Acceptance Criteria:**
  - [ ] AC-1: Only users with author role can create books
  - [ ] AC-2: Non-authors receive HTTP 403 Forbidden response
  - [ ] AC-3: All required fields must be present in request
  - [ ] AC-4: Missing required fields return HTTP 400 Bad Request
  - [ ] AC-5: Invalid ISBN format returns HTTP 400 Bad Request
  - [ ] AC-6: Duplicate ISBN returns HTTP 409 Conflict
  - [ ] AC-7: Created book includes auto-generated ID
  - [ ] AC-8: CreatedBy field populated with authenticated user
  - [ ] AC-9: CreatedAt field populated with current timestamp (UTC)
  - [ ] AC-10: Response includes HTTP 201 Created status code

### FR-2: List Books with Pagination
- **Description:** All users (authenticated or anonymous) can retrieve a paginated list of all books in the catalog.
- **Actor/User:** All users
- **Endpoint:** `GET /api/books`
- **Query Parameters:**
  - `page` (optional, default=0): Zero-based page number
  - `size` (optional, default=20): Number of records per page (max=100)
  - `sort` (optional, default=createdAt,desc): Sorting criteria (field,direction)

- **Workflow/Steps:**
  1. Client submits GET request with optional pagination parameters
  2. System validates pagination parameters (page >= 0, 1 <= size <= 100)
  3. System retrieves books from data store with offset and limit
  4. System calculates total count and page metadata
  5. System returns paginated results with metadata

- **Expected Outcome:** Paginated list of books with metadata (total, page number, page size)
- **Acceptance Criteria:**
  - [ ] AC-1: Anonymous users can list books
  - [ ] AC-2: Default page size is 20 records
  - [ ] AC-3: Maximum page size is 100 records
  - [ ] AC-4: Invalid page number returns HTTP 400 Bad Request
  - [ ] AC-5: Invalid page size returns HTTP 400 Bad Request
  - [ ] AC-6: Response includes total count of all books
  - [ ] AC-7: Response includes current page number and size
  - [ ] AC-8: Response includes hasNextPage boolean flag
  - [ ] AC-9: Response includes hasPreviousPage boolean flag
  - [ ] AC-10: Empty result returns HTTP 200 with empty array
  - [ ] AC-11: Returns HTTP 200 OK status code

### FR-3: Get Single Book by ID
- **Description:** All users can retrieve detailed information about a specific book by its unique identifier.
- **Actor/User:** All users
- **Endpoint:** `GET /api/books/{id}`
- **Path Parameters:**
  - `id` (required): Unique book identifier

- **Workflow/Steps:**
  1. Client submits GET request with book ID in path
  2. System validates book ID format (UUID or numeric)
  3. System searches for book by ID in data store
  4. If found: return complete book record
  5. If not found: return HTTP 404 Not Found

- **Expected Outcome:** Complete book record with all metadata fields
- **Acceptance Criteria:**
  - [ ] AC-1: Anonymous users can retrieve book details
  - [ ] AC-2: Valid book ID returns HTTP 200 OK
  - [ ] AC-3: Non-existent book ID returns HTTP 404 Not Found
  - [ ] AC-4: Invalid ID format returns HTTP 400 Bad Request
  - [ ] AC-5: Response includes all book fields (id, title, author, ISBN, genre, publicationDate, createdBy, createdAt)
  - [ ] AC-6: CreatedBy field shows book creator's username/ID
  - [ ] AC-7: CreatedAt field shows creation timestamp

### FR-4: Update Book (Creator-Only Access)
- **Description:** Only the creator of a book can update its metadata. Other users receive authorization error.
- **Actor/User:** Book creator (original author who created the book)
- **Endpoint:** `PUT /api/books/{id}`
- **Path Parameters:**
  - `id` (required): Unique book identifier

- **Workflow/Steps:**
  1. Client submits PUT request with book ID and updated fields
  2. System validates user authentication
  3. System retrieves existing book record
  4. System verifies current user is book creator
  5. If not creator: return HTTP 403 Forbidden
  6. System validates all updated fields
  7. System verifies ISBN uniqueness (if ISBN changed, excluding current book)
  8. System updates book record with new values
  9. System maintains createdBy and createdAt (immutable)
  10. System persists changes to data store
  11. System returns updated book record

- **Expected Outcome:** Book record updated with new values, immutable fields preserved
- **Acceptance Criteria:**
  - [ ] AC-1: Only book creator can update the book
  - [ ] AC-2: Non-creators receive HTTP 403 Forbidden response
  - [ ] AC-3: Unauthenticated users receive HTTP 401 Unauthorized
  - [ ] AC-4: Non-existent book ID returns HTTP 404 Not Found
  - [ ] AC-5: Invalid field values return HTTP 400 Bad Request
  - [ ] AC-6: Duplicate ISBN (if ISBN changed) returns HTTP 409 Conflict
  - [ ] AC-7: CreatedBy field cannot be modified (remains original creator)
  - [ ] AC-8: CreatedAt field cannot be modified (remains original timestamp)
  - [ ] AC-9: UpdatedAt field (if present) updated to current timestamp
  - [ ] AC-10: Partial updates allowed (only provided fields updated)
  - [ ] AC-11: Returns HTTP 200 OK with updated record

### FR-5: Delete Book (Creator-Only Access)
- **Description:** Only the creator of a book can delete it permanently from the catalog. Other users receive authorization error.
- **Actor/User:** Book creator (original author who created the book)
- **Endpoint:** `DELETE /api/books/{id}`
- **Path Parameters:**
  - `id` (required): Unique book identifier

- **Workflow/Steps:**
  1. Client submits DELETE request with book ID
  2. System validates user authentication
  3. System retrieves book record
  4. System verifies current user is book creator
  5. If not creator: return HTTP 403 Forbidden
  6. System removes book record from data store
  7. System returns success response

- **Expected Outcome:** Book record permanently removed from catalog
- **Acceptance Criteria:**
  - [ ] AC-1: Only book creator can delete the book
  - [ ] AC-2: Non-creators receive HTTP 403 Forbidden response
  - [ ] AC-3: Unauthenticated users receive HTTP 401 Unauthorized
  - [ ] AC-4: Non-existent book ID returns HTTP 404 Not Found
  - [ ] AC-5: Deletion is permanent and cannot be reversed
  - [ ] AC-6: Deleted book no longer appears in list endpoint
  - [ ] AC-7: Subsequent GET requests for deleted book return HTTP 404
  - [ ] AC-8: Returns HTTP 204 No Content on successful deletion

### FR-6: Field Validation
- **Description:** All book fields are validated for correct format, type, and business rules.
- **Expected Behavior:** Invalid data rejected with clear error messages

**Validation Rules:**
- **id**: Auto-generated UUID or numeric ID, immutable
- **title**: Required, string, 1-500 characters, non-empty after trimming
- **author**: Required, string, 1-200 characters, represents book's author name
- **ISBN**: Required, unique across all books, format: ISBN-10 (10 digits) or ISBN-13 (13 digits with hyphens or without)
  - Valid formats: "978-0-123456-78-9" or "9780123456789"
  - Invalid formats rejected with HTTP 400 Bad Request
- **genre**: Required, string, predefined list: Fiction, Non-Fiction, Mystery, Romance, Science Fiction, Fantasy, Biography, History, Self-Help, Children, Young Adult
- **publicationDate**: Required, date format ISO-8601 (YYYY-MM-DD), cannot be in the future
- **createdBy**: Auto-populated from authenticated user, immutable, cannot be manually set
- **createdAt**: Auto-populated with UTC timestamp, immutable, ISO-8601 format with timezone

**Error Response Format:**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "ISBN",
      "message": "Invalid ISBN format. Expected ISBN-10 or ISBN-13"
    },
    {
      "field": "publicationDate",
      "message": "Publication date cannot be in the future"
    }
  ]
}
```

- **Acceptance Criteria:**
  - [ ] AC-1: Required fields cannot be null or empty
  - [ ] AC-2: Field length constraints enforced
  - [ ] AC-3: ISBN format validated (ISBN-10 or ISBN-13)
  - [ ] AC-4: Genre must be from predefined list
  - [ ] AC-5: PublicationDate cannot be in future
  - [ ] AC-6: Date fields accept ISO-8601 format
  - [ ] AC-7: Invalid data returns HTTP 400 with detailed error messages
  - [ ] AC-8: Error response includes field name and validation message

### FR-7: Error Handling and HTTP Status Codes
- **Description:** API returns appropriate HTTP status codes and error responses for all scenarios.

**HTTP Status Codes:**
- **200 OK**: Successful GET or PUT request
- **201 Created**: Successful POST request (book created)
- **204 No Content**: Successful DELETE request
- **400 Bad Request**: Validation failure, missing required fields, invalid format
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: User lacks permission (non-author creating, non-creator updating/deleting)
- **404 Not Found**: Book not found, invalid endpoint
- **409 Conflict**: Duplicate ISBN or business logic conflict
- **500 Internal Server Error**: Unexpected server error

**Error Response Format (for 4xx errors):**
```json
{
  "status": 400,
  "message": "Invalid request",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

- **Acceptance Criteria:**
  - [ ] AC-1: 200 returned for successful GET requests
  - [ ] AC-2: 201 returned for successful POST requests
  - [ ] AC-3: 204 returned for successful DELETE requests
  - [ ] AC-4: 400 returned for validation failures
  - [ ] AC-5: 401 returned for missing authentication
  - [ ] AC-6: 403 returned for insufficient permissions
  - [ ] AC-7: 404 returned for non-existent resources
  - [ ] AC-8: 409 returned for duplicate ISBN
  - [ ] AC-9: All error responses include status code and message
  - [ ] AC-10: Error messages are descriptive and actionable

---

## Non-Functional Requirements

### Performance
- **Response Time**: API responses must complete within 200ms for 95th percentile (GET operations < 150ms)
- **Throughput**: System must support minimum 1000 concurrent users
- **Data Volume**: Handle up to 1 million book records
- **Pagination Performance**: Large result sets (size=100) must return within 250ms
- **Search/Filter**: Future filter operations must complete within 300ms

**Acceptance Criteria:**
- [ ] AC-1: 95% of GET requests complete within 200ms
- [ ] AC-2: 95% of POST/PUT requests complete within 300ms
- [ ] AC-3: DELETE requests complete within 150ms
- [ ] AC-4: Pagination queries optimized for performance
- [ ] AC-5: No N+1 query patterns in implementation
- [ ] AC-6: Database operations use appropriate indexing (createdAt, createdBy)

### Security & Compliance
- **Authentication**: Role-based access control (RBAC) enforced
  - Author role required for book creation
  - All users can read books
  - Creator-only access for updates/deletes
- **Authorization**: Endpoint-level and resource-level authorization checks
- **Data Protection**: 
  - ISBN is searchable but not encrypted in current implementation
  - CreatedBy may contain user PII (username/email)
  - No explicit data retention policy (out of scope)
- **API Security**:
  - HTTPS required for production (enforced at infrastructure level, not API level)
  - No API key/token exposed in logs or error messages
  - SQL injection prevention: JSON data model (not applicable to current file-based storage)
  - CSRF tokens not required for stateless REST API (handled by client/framework)
- **Compliance**: 
  - No PII storage beyond createdBy field
  - No GDPR right-to-delete implemented (deletion is creator-only)
  - Audit logging: track who created/modified books (createdBy field)

**Acceptance Criteria:**
- [ ] AC-1: Author role required for POST /api/books
- [ ] AC-2: Creator role required for PUT/DELETE operations
- [ ] AC-3: All authenticated users can GET /api/books
- [ ] AC-4: Anonymous users cannot perform write operations
- [ ] AC-5: User roles validated on every request
- [ ] AC-6: No credentials in API responses or logs
- [ ] AC-7: HTTPS enforced in production environment
- [ ] AC-8: CreatedBy populated from authenticated session

### Usability & Accessibility
- **Supported Browsers/Devices**: 
  - API is backend-only (no UI); consumed by client applications
  - Clients must support: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
  - Mobile clients: iOS Safari 14+, Android Chrome 90+
- **API Documentation**: 
  - Swagger/OpenAPI documentation auto-generated and available at `/swagger-ui/index.html`
  - All endpoints documented with operation summaries and parameter descriptions
  - Example request/response payloads included
- **Error Messages**: 
  - Clear, actionable error messages in user's language
  - Field-level validation errors with specific guidance
  - HTTP status codes aligned with REST conventions
- **Response Format**: 
  - Consistent JSON structure across all endpoints
  - ISO-8601 dates in all responses
  - Null fields omitted or explicitly included as null (consistent approach)

**Acceptance Criteria:**
- [ ] AC-1: Swagger documentation auto-generated and accessible
- [ ] AC-2: All endpoints documented with summaries
- [ ] AC-3: Error messages include specific field and validation reason
- [ ] AC-4: Date fields consistently formatted (ISO-8601)
- [ ] AC-5: Response structure consistent across all endpoints
- [ ] AC-6: HTTP status codes follow REST conventions

### Scalability & Reliability
- **Availability Requirement**: 99.9% uptime SLA for production
- **Load Handling**: 
  - Horizontal scaling: Stateless API can be deployed across multiple instances
  - No server-side session state maintained
  - Each request is independent
- **Data Persistence**: 
  - JSON file-based storage allows easy backup
  - Consider future migration to database for improved scalability
  - File locking mechanism to prevent concurrent modification conflicts
- **Error Recovery**: 
  - Graceful degradation on file system errors
  - Automatic retry logic for transient failures
  - Clear error messages for persistent failures
- **Monitoring**: 
  - API response time metrics
  - Error rate monitoring (4xx, 5xx counts)
  - User authentication/authorization audit logs
  - Book CRUD operation metrics

**Acceptance Criteria:**
- [ ] AC-1: 99.9% uptime maintained in production
- [ ] AC-2: API stateless and can be deployed on multiple instances
- [ ] AC-3: No session state stored server-side
- [ ] AC-4: File system errors handled gracefully
- [ ] AC-5: Error logs include timestamp and error details
- [ ] AC-6: Monitoring infrastructure in place for metrics collection

### Integrations & Dependencies
- **External Systems**: None required for core functionality
- **Data Exchange Format**: JSON exclusively
- **Internal Dependencies**: 
  - Spring Boot 4.1.1 REST framework
  - Jackson for JSON serialization/deserialization
  - Springdoc OpenAPI for Swagger documentation
  - Lombok for boilerplate reduction
  - Spring Security for authentication/authorization (future enhancement)
- **File Storage**: 
  - All data stored in `src/main/resources/data/` JSON files
  - Files: `books.json` (array of book objects)
  - JSON structure: `[ { id, title, author, ISBN, genre, publicationDate, createdBy, createdAt }, ... ]`
- **Synchronization**: 
  - File-based storage (single instance) or distributed lock for multi-instance (future)
  - No real-time sync required for MVP

**Acceptance Criteria:**
- [ ] AC-1: JSON used for all request/response payloads
- [ ] AC-2: Books persisted to books.json file
- [ ] AC-3: File persistence layer abstracted in service layer
- [ ] AC-4: No external API dependencies for core functionality
- [ ] AC-5: Jackson correctly serializes/deserializes Book objects
- [ ] AC-6: Swagger documentation auto-generated from Spring annotations

---

## Constraints

### Technical Constraints
- **File-Based Storage**: Current implementation uses JSON files as data store (not a traditional database)
  - Scalability limited to single instance or with file locking
  - No built-in transactions or ACID properties
  - Future migration to database recommended for production scale
- **No Database**: Design constrains to file I/O operations
  - No SQL queries or prepared statements
  - No connection pooling or session management
  - JSON parsing overhead for each request (consider caching)
- **Java 25 Requirement**: Must use Java 25 toolchain (per project spec)
  - Gradle 8.5+ required for Java 25 support
  - Some libraries may not have Java 25 compatibility yet
- **Spring Boot 4.1.1**: Framework version may have limited community support if very new
  - Verify library compatibility with Spring Boot 4.1.1

### Business Constraints
- **MVP Scope**: Initial release focuses on core CRUD operations
  - Advanced search/filtering deferred to future release
  - No full-text search or complex queries in MVP
  - Single-language support (English)
- **No Authentication System**: Current implementation assumes pre-authenticated users
  - Integration with authentication provider required at application layer
  - Role information passed via Spring Security or headers
- **Single Tenant**: No multi-tenant support planned for MVP
  - All books in shared namespace
  - Future releases may add multi-tenancy

### External Constraints
- **File System Limitations**: 
  - JSON file I/O performance degrades with very large files (1M+ records)
  - File locking required for concurrent write access (platform-dependent)
  - No native support for distributed file systems in MVP
- **Library Versions**: 
  - Springdoc OpenAPI 2.5.0 compatibility with Spring Boot 4.1.1
  - Jackson Databind version constraints
  - Lombok may have Java 25 compatibility issues

### Known Limitations
- **Concurrency**: File-based storage does not support true concurrent writes
  - Recommendation: Implement file locking or migrate to database
  - Current implementation assumes mostly read-heavy workload
- **Search Performance**: Linear file scan for filtering (no indexing)
  - Future: Add database with proper indexing for large datasets
- **No Soft Deletes**: Deleted books cannot be recovered
  - Consider audit trail or soft delete approach in future versions
- **ISBN Validation**: Basic format check only (not verification against ISBN registry)
  - Checksum validation not implemented in MVP
- **No API Versioning**: Single API version assumed
  - Future releases require versioning strategy (v1, v2, etc.)

---

## Assumptions

1. **Authentication Pre-Configured**: User identity and role information are available via Spring Security context
   - Risk if false: Authorization checks will fail; authentication layer must be implemented
   
2. **Author Role Exists**: "Author" role is defined in the application's role system
   - Risk if false: Role-based access control logic requires adjustment
   
3. **File System Writable**: Application has read/write permissions on `src/main/resources/data/` directory
   - Risk if false: Runtime errors when persisting books; requires adjusted file permissions
   
4. **JSON Data Format**: Books are stored as JSON array in `books.json` file
   - Risk if false: Service layer must be adapted to different data format
   
5. **Single-Instance Deployment**: MVP assumes single application instance in production
   - Risk if false: File locking issues in multi-instance deployment; database migration required
   
6. **No Data Migration Needed**: Existing books.json is compatible with new schema (or empty)
   - Risk if false: Migration script required before deployment
   
7. **UTC Timestamps**: All createdAt timestamps use UTC timezone for consistency
   - Risk if false: Timezone handling must be standardized across clients and API
   
8. **Stateless Clients**: Clients do not rely on server-side session state
   - Risk if false: State management must be added to API or clients refactored
   
9. **Error Handling**: Clients properly handle HTTP status codes and error response format
   - Risk if false: Client implementations must be updated to parse errors

---

## Scope Boundaries

### In Scope
- REST API endpoints for CRUD operations on books (create, read, list, update, delete)
- Role-based access control (author-only create, creator-only update/delete)
- Pagination support for book list endpoint
- Comprehensive field validation with error messages
- HTTP status codes and error responses per REST conventions
- Swagger/OpenAPI documentation auto-generated
- JSON file-based persistence
- Spring Boot REST framework implementation
- JUnit 5 and integration tests
- Mock data in JSON files for local development

### Out of Scope
- User management system (assumes pre-authenticated users)
- Authentication provider implementation (LDAP, OAuth, JWT validation)
- Advanced search/filtering beyond basic list with pagination
- Sorting by custom fields (only default createdAt,desc supported in MVP)
- Book rating/review functionality (separate feature)
- Export functionality (CSV, PDF export)
- Bulk operations (import/export books in batch)
- Caching layer (HTTP caching headers, Redis, etc.)
- GraphQL API (REST only in MVP)
- Database migration (file-based storage only)
- Real-time updates (WebSocket, Server-Sent Events)
- Multi-tenancy support
- API versioning (single version in MVP)
- Soft deletes or audit trail (hard delete only)
- ISBN registry verification
- Book cover images or media storage
- Book availability/inventory tracking

---

## Data Model

### Book Entity

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10",
  "createdBy": "user123",
  "createdAt": "2026-09-10T14:30:00Z"
}
```

**Field Definitions:**
- **id** (UUID/String): Unique identifier for the book, auto-generated on creation, immutable
- **title** (String, 1-500 chars): Name of the book, required, non-empty after trimming
- **author** (String, 1-200 chars): Author name as published on book, required
- **ISBN** (String, pattern: ISBN-10 or ISBN-13): Unique international standard book number, required, unique constraint
- **genre** (Enum): Classification of book, required, one of: Fiction, Non-Fiction, Mystery, Romance, Science Fiction, Fantasy, Biography, History, Self-Help, Children, Young Adult
- **publicationDate** (Date, ISO-8601): Date book was first published, required, cannot be in future
- **createdBy** (String): User ID or username of book creator, auto-populated from authentication, immutable
- **createdAt** (DateTime, ISO-8601 with Z timezone): UTC timestamp when book was created, auto-populated, immutable

---

## API Endpoints

### 1. Create Book
**POST /api/books**

**Purpose**: Create a new book record (author role only)

**Request**:
```json
{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10"
}
```

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <token> (if required)
```

**Response (201 Created)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10",
  "createdBy": "user123",
  "createdAt": "2026-09-10T14:30:00Z"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Validation failed",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00Z",
  "errors": [
    {
      "field": "ISBN",
      "message": "Invalid ISBN format"
    }
  ]
}
```

**Error Response (403 Forbidden)**:
```json
{
  "status": 403,
  "message": "Only users with author role can create books",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

**Error Response (409 Conflict)**:
```json
{
  "status": 409,
  "message": "Book with ISBN 978-0-7432-7356-5 already exists",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

---

### 2. List Books with Pagination
**GET /api/books**

**Purpose**: Retrieve paginated list of all books (all users)

**Query Parameters**:
```
page=0           (optional, default: 0, min: 0)
size=20          (optional, default: 20, min: 1, max: 100)
sort=createdAt,desc   (optional, default: createdAt,desc)
```

**Request Headers**:
```
Accept: application/json
```

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "The Great Gatsby",
      "author": "F. Scott Fitzgerald",
      "ISBN": "978-0-7432-7356-5",
      "genre": "Fiction",
      "publicationDate": "1925-04-10",
      "createdBy": "user123",
      "createdAt": "2026-09-10T14:30:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "title": "To Kill a Mockingbird",
      "author": "Harper Lee",
      "ISBN": "978-0-06-112008-4",
      "genre": "Fiction",
      "publicationDate": "1960-07-11",
      "createdBy": "user456",
      "createdAt": "2026-09-09T10:15:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "hasNextPage": true,
  "hasPreviousPage": false
}
```

**Error Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Invalid pagination parameters",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00Z",
  "errors": [
    {
      "field": "size",
      "message": "Size must be between 1 and 100"
    }
  ]
}
```

---

### 3. Get Single Book
**GET /api/books/{id}**

**Purpose**: Retrieve details of a specific book (all users)

**Path Parameters**:
```
id = "550e8400-e29b-41d4-a716-446655440000"  (required, UUID or numeric ID)
```

**Request Headers**:
```
Accept: application/json
```

**Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10",
  "createdBy": "user123",
  "createdAt": "2026-09-10T14:30:00Z"
}
```

**Error Response (404 Not Found)**:
```json
{
  "status": 404,
  "message": "Book not found",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Invalid book ID format",
  "path": "/api/books/invalid-id",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

---

### 4. Update Book
**PUT /api/books/{id}**

**Purpose**: Update a book record (creator only)

**Path Parameters**:
```
id = "550e8400-e29b-41d4-a716-446655440000"  (required)
```

**Request**:
```json
{
  "title": "The Great Gatsby (Revised Edition)",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10"
}
```

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <token> (if required)
```

**Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby (Revised Edition)",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10",
  "createdBy": "user123",
  "createdAt": "2026-09-10T14:30:00Z"
}
```

**Error Response (403 Forbidden)**:
```json
{
  "status": 403,
  "message": "Only the book creator can update this book",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

**Error Response (404 Not Found)**:
```json
{
  "status": 404,
  "message": "Book not found",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

**Error Response (409 Conflict)**:
```json
{
  "status": 409,
  "message": "Book with ISBN 978-0-1234567-89-1 already exists",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

---

### 5. Delete Book
**DELETE /api/books/{id}**

**Purpose**: Delete a book record (creator only)

**Path Parameters**:
```
id = "550e8400-e29b-41d4-a716-446655440000"  (required)
```

**Request Headers**:
```
Authorization: Bearer <token> (if required)
```

**Response (204 No Content)**:
```
(empty body)
```

**Error Response (403 Forbidden)**:
```json
{
  "status": 403,
  "message": "Only the book creator can delete this book",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

**Error Response (404 Not Found)**:
```json
{
  "status": 404,
  "message": "Book not found",
  "path": "/api/books/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-09-10T14:30:00Z"
}
```

---

## Pagination Specification

### Overview
The list endpoint (`GET /api/books`) supports offset-based pagination to retrieve books in manageable chunks.

### Query Parameters
- **page** (integer, default: 0)
  - Zero-based page index
  - Valid range: 0 or greater
  - Example: `page=0` retrieves first page

- **size** (integer, default: 20)
  - Number of records per page
  - Valid range: 1-100 (enforced maximum)
  - Example: `size=50` retrieves 50 books per page

- **sort** (string, default: "createdAt,desc")
  - Sorting criteria in format: `field,direction`
  - Supported fields: `createdAt`, `title`, `author` (future expansion)
  - Direction: `asc` (ascending) or `desc` (descending)
  - Example: `sort=title,asc` sorts by title ascending

### Response Metadata
Response includes pagination metadata to support client-side navigation:

```json
{
  "content": [ /* array of book objects */ ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "hasNextPage": true,
  "hasPreviousPage": false
}
```

**Metadata Fields:**
- **content**: Array of book objects on current page
- **page**: Current zero-based page index
- **size**: Number of records per page
- **totalElements**: Total count of all books in catalog
- **totalPages**: Total number of pages (calculated as ceil(totalElements / size))
- **hasNextPage**: Boolean indicating if next page exists
- **hasPreviousPage**: Boolean indicating if previous page exists

### Example Usage

**Request - First Page (default)**:
```
GET /api/books
```
Returns first 20 books (page 0, size 20)

**Request - Specific Page and Size**:
```
GET /api/books?page=2&size=50
```
Returns books 100-150 (third page with 50 items per page)

**Request - Custom Sorting**:
```
GET /api/books?sort=title,asc&page=0&size=20
```
Returns first 20 books sorted by title alphabetically

**Request - Combined Parameters**:
```
GET /api/books?page=1&size=25&sort=createdAt,desc
```
Returns second page with 25 items per page, newest books first

### Edge Cases
- **Empty Result**: `page=0&size=20` on empty catalog returns HTTP 200 with empty content array
- **Out-of-Range Page**: `page=100` when only 5 pages exist returns HTTP 200 with empty content array (acceptable) or HTTP 400 (stricter validation)
- **Invalid Size**: `size=200` (exceeds max) returns HTTP 400 Bad Request
- **Invalid Page**: `page=-1` returns HTTP 400 Bad Request

### Sorting Behavior
- Default sort: `createdAt,desc` (newest books first)
- Sort is applied after filtering (if any filters implemented in future)
- Stable sort ensures deterministic ordering across paginated requests

---

## Testing Requirements

### Test Coverage Goals
- **Minimum Code Coverage**: 80% overall (goal: 90%)
- **Branch Coverage**: 85% (including error paths)
- **Critical Paths**: 100% coverage for authorization and validation

### Unit Tests

#### Service Layer Tests (Book Service)
```
BookService
├── testCreateBook_Success
├── testCreateBook_InvalidTitle
├── testCreateBook_InvalidISBN_Format
├── testCreateBook_InvalidISBN_Duplicate
├── testCreateBook_InvalidGenre
├── testCreateBook_InvalidPublicationDate_Future
├── testCreateBook_PopulatesCreatedBy
├── testCreateBook_PopulatesCreatedAt
├── testGetBook_Success
├── testGetBook_NotFound
├── testListBooks_WithDefault Pagination
├── testListBooks_WithCustomPageSize
├── testListBooks_WithCustomSort
├── testListBooks_PageSizeExceedsMax_Returns400
├── testListBooks_InvalidPageNumber_Returns400
├── testListBooks_EmptyResult
├── testUpdateBook_Success
├── testUpdateBook_NotFound
├── testUpdateBook_PartialUpdate
├── testUpdateBook_CreatedByImmutable
├── testUpdateBook_CreatedAtImmutable
├── testUpdateBook_InvalidISBN_Duplicate
├── testUpdateBook_InvalidationRules
├── testDeleteBook_Success
├── testDeleteBook_NotFound
├── testDeleteBook_RemovesFromList
```

#### Validation Tests
```
ValidationTests
├── testTitle_Required
├── testTitle_LengthConstraints (1-500 chars)
├── testTitle_TrimmedBeforeValidation
├── testAuthor_Required
├── testAuthor_LengthConstraints (1-200 chars)
├── testISBN_Required
├── testISBN_Format_ISBN10 (10 digits)
├── testISBN_Format_ISBN13 (13 digits with hyphens)
├── testISBN_Format_ISBN13_NoHyphens
├── testISBN_InvalidFormat_ReturnsValidationError
├── testISBN_Uniqueness_Across Books
├── testGenre_Required
├── testGenre_PredefinedValues (Fiction, Non-Fiction, etc.)
├── testGenre_InvalidValue_Returns400
├── testPublicationDate_Required
├── testPublicationDate_Format_ISO8601
├── testPublicationDate_CannotBeFuture
├── testPublicationDate_InvalidFormat_Returns400
```

### Integration Tests

#### REST Endpoint Tests (Controller Layer)
```
BookControllerIntegrationTests
├── testCreateBook_POST_Success_Returns201
├── testCreateBook_POST_MissingRequiredFields_Returns400
├── testCreateBook_POST_InvalidValidation_Returns400
├── testCreateBook_POST_DuplicateISBN_Returns409
├── testCreateBook_POST_UnauthorizedRole_Returns403
├── testListBooks_GET_Success_Returns200
├── testListBooks_GET_WithPagination_Returns200
├── testListBooks_GET_InvalidPageSize_Returns400
├── testListBooks_GET_EmptyResult_Returns200WithEmptyArray
├── testGetBook_GET_Success_Returns200
├── testGetBook_GET_NotFound_Returns404
├── testGetBook_GET_InvalidId_Returns400
├── testUpdateBook_PUT_Success_Returns200
├── testUpdateBook_PUT_NotFound_Returns404
├── testUpdateBook_PUT_Unauthorized_Returns403
├── testUpdateBook_PUT_InvalidData_Returns400
├── testUpdateBook_PUT_DuplicateISBN_Returns409
├── testUpdateBook_PUT_PreservesCreatedBy
├── testUpdateBook_PUT_PartialUpdate
├── testDeleteBook_DELETE_Success_Returns204
├── testDeleteBook_DELETE_NotFound_Returns404
├── testDeleteBook_DELETE_Unauthorized_Returns403
├── testDeleteBook_DELETE_NoContentInResponse
```

#### Authorization Tests
```
AuthorizationTests
├── testCreateBook_Author_Success
├── testCreateBook_NonAuthor_Fails403
├── testCreateBook_Unauthenticated_Fails401
├── testUpdateBook_Creator_Success
├── testUpdateBook_NonCreator_Fails403
├── testUpdateBook_Unauthenticated_Fails401
├── testDeleteBook_Creator_Success
├── testDeleteBook_NonCreator_Fails403
├── testDeleteBook_Unauthenticated_Fails401
├── testListBooks_Anonymous_Success
├── testGetBook_Anonymous_Success
```

#### Full CRUD Flow Tests
```
FullCrudFlowTests
├── testCreateBook_Then_ListBooks_AppearsInList
├── testCreateBook_Then_GetById_Returns_CreatedBook
├── testCreateBook_Then_UpdateBook_ReflectsChanges
├── testCreateBook_Then_UpdateBook_Then_DeleteBook_FullFlow
├── testCreateBook_WithMaxLengthFields
├── testCreateBook_Then_ListBooks_PaginationWorks
├── testCreateMultipleBooks_Then_ListBooks_AllPresent
├── testCreateMultipleBooks_Then_Sort_ByCreatedAt
├── testCreateBook_Then_UpdateISBN_Fails_IfDuplicateExists
└── testCreateBook_Then_UpdateBook_CreatedByUnchanged
```

#### Pagination Tests
```
PaginationTests
├── testListBooks_DefaultPagination_Returns20Items
├── testListBooks_Size50_Returns50Items
├── testListBooks_MaxSize100_Enforced
├── testListBooks_SizeExceedsMax_Returns400
├── testListBooks_PageSize1_Works
├── testListBooks_Page0_First20Items
├── testListBooks_Page1_Next20Items
├── testListBooks_InvalidPageNumber_Returns400
├── testListBooks_HasNextPage_True_When_MoreResults
├── testListBooks_HasNextPage_False_When_LastPage
├── testListBooks_HasPreviousPage_False_Page0
├── testListBooks_HasPreviousPage_True_Page1Plus
├── testListBooks_TotalElements_CorrectCount
├── testListBooks_TotalPages_CalculatedCorrectly
├── testListBooks_EmptyDatabase_ReturnsEmptyPage
└── testListBooks_CustomSort_ByTitle_Ascending
```

#### HTTP Status Code Tests
```
HttpStatusCodeTests
├── testCreateBook_Success_Status201
├── testCreateBook_BadRequest_Status400
├── testCreateBook_Forbidden_Status403
├── testCreateBook_Conflict_Status409
├── testListBooks_Success_Status200
├── testListBooks_BadRequest_Status400
├── testGetBook_Success_Status200
├── testGetBook_NotFound_Status404
├── testGetBook_BadRequest_Status400
├── testUpdateBook_Success_Status200
├── testUpdateBook_BadRequest_Status400
├── testUpdateBook_NotFound_Status404
├── testUpdateBook_Forbidden_Status403
├── testUpdateBook_Conflict_Status409
├── testDeleteBook_Success_Status204
├── testDeleteBook_NotFound_Status404
├── testDeleteBook_Forbidden_Status403
└── testDeleteBook_NoContent_EmptyBody
```

### Test Scenarios by Category

#### Happy Path Tests
1. Create book with valid data → Book persisted and returned with ID
2. List books with default pagination → 20 books returned
3. Get book by ID → Book details returned correctly
4. Update book fields → Updated values persisted
5. Delete book → Book removed from catalog

#### Edge Cases
1. Create book with minimum length title (1 char) → Success
2. Create book with maximum length title (500 chars) → Success
3. ISBN-10 format validation
4. ISBN-13 format validation with hyphens
5. ISBN-13 format validation without hyphens
6. Publication date exactly today (boundary) → Allowed
7. Publication date exactly tomorrow (boundary) → Rejected
8. List books with page=0, size=1 → Minimum pagination
9. List books with page=large number → Empty result
10. List books with size=100 (maximum) → 100 items returned

#### Error Cases
1. Create book with missing title → HTTP 400, error message for title field
2. Create book with missing ISBN → HTTP 400, error message for ISBN field
3. Create book with empty title (all whitespace) → HTTP 400
4. Create book with title > 500 chars → HTTP 400
5. Create book with invalid ISBN format → HTTP 400, specific format guidance
6. Create book with duplicate ISBN → HTTP 409 Conflict
7. Create book with future publication date → HTTP 400
8. Create book with invalid genre → HTTP 400, list valid values
9. Get non-existent book → HTTP 404
10. Get book with invalid ID format → HTTP 400
11. Update non-existent book → HTTP 404
12. Update book as non-creator → HTTP 403
13. Update book with invalid data → HTTP 400
14. Update book ISBN to duplicate value → HTTP 409
15. Delete non-existent book → HTTP 404
16. Delete book as non-creator → HTTP 403
17. List books with size=0 → HTTP 400
18. List books with size=101 (exceeds max) → HTTP 400
19. List books with page=-1 → HTTP 400

#### Access Control Tests
1. Author creates book → Success
2. Non-author attempts to create book → HTTP 403
3. Anonymous user attempts to create book → HTTP 401 or 403
4. Creator updates their own book → Success
5. Non-creator attempts to update book → HTTP 403
6. Creator deletes their own book → Success
7. Non-creator attempts to delete book → HTTP 403
8. Anonymous user lists books → Success
9. Anonymous user gets single book → Success

#### Data Integrity Tests
1. Create book → createdBy auto-populated correctly
2. Create book → createdAt auto-populated with UTC timestamp
3. Update book → createdBy unchanged
4. Update book → createdAt unchanged
5. Update book → No new updatedAt field added (immutable design)
6. Delete and recreate book with same ISBN → New book has different ID
7. Concurrent creates with same ISBN → Only one persists (filesystem behavior)

---

## Response Examples

### Successful Create Book Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "ISBN": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationDate": "1925-04-10",
  "createdBy": "user123",
  "createdAt": "2026-09-10T14:30:00.000Z"
}
```

### Successful List Books Response
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "The Great Gatsby",
      "author": "F. Scott Fitzgerald",
      "ISBN": "978-0-7432-7356-5",
      "genre": "Fiction",
      "publicationDate": "1925-04-10",
      "createdBy": "user123",
      "createdAt": "2026-09-10T14:30:00.000Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNextPage": false,
  "hasPreviousPage": false
}
```

### Validation Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "path": "/api/books",
  "timestamp": "2026-09-10T14:30:00.000Z",
  "errors": [
    {
      "field": "ISBN",
      "message": "Invalid ISBN format. Expected ISBN-10 (10 digits) or ISBN-13 (13 digits)"
    },
    {
      "field": "publicationDate",
      "message": "Publication date cannot be in the future"
    }
  ]
}
```

---

## Related Items

- **Linked JIRA Issues**: 
  - KAN-1: User Authentication & Role Management (dependency for authorization)
  - KAN-2: Book Review API (related feature, separate story)
  - KAN-4: API Documentation & Testing (quality assurance, separate story)

- **Related Documentation**: 
  - Swagger/OpenAPI specification (auto-generated at build time)
  - Spring Boot 4.1.1 documentation
  - REST API design best practices guide
  - ISBN validation standards (ISBN-10 and ISBN-13)

- **Stakeholders**: 
  - Product Owner: Approval of scope and acceptance criteria
  - Backend Engineers: Implementation of REST endpoints and service layer
  - QA/Test Engineers: Integration and acceptance testing
  - API Consumers: Client applications consuming the REST API
  - Security Team: Review of access control and data protection

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-09-10 | Requirements Agent | Initial comprehensive requirements document compiled with complete API specifications, validation rules, testing requirements, and access control model |

---

**Document Generated:** 2026-09-10T14:35:00Z
**Last Updated:** 2026-09-10T14:35:00Z
**Status:** Complete and ready for development

---

## Summary

This requirements document provides comprehensive specification for the **Book Catalog Management CRUD APIs (KAN-3)** with:

- **5 core REST endpoints** (POST, GET list, GET by ID, PUT, DELETE)
- **Role-based access control** with author creation and creator-only updates/deletes
- **Complete data model** with 8 fields (id, title, author, ISBN, genre, publicationDate, createdBy, createdAt)
- **Comprehensive validation** rules for each field with error handling
- **Pagination support** with configurable page size (1-100, default 20)
- **85+ acceptance criteria** covering functional, non-functional, and edge cases
- **50+ test scenarios** with 80% minimum code coverage requirement
- **Detailed HTTP status codes** and error response formats
- **Request/response examples** for all endpoints
- **Full integration with Spring Boot 4.1.1** REST framework

All requirements are testable, measurable, and ready for implementation.

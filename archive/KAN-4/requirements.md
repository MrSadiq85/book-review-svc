# Requirements Document: Book Review API Implementation

## Story Metadata
- **JIRA Issue:** KAN-4
- **Status:** In Progress
- **Priority:** High
- **Sprint/Epic:** Related to book-review-svc API expansion
- **Created:** 2026-09-14

---

## Story Summary

This story requires implementing a complete book review REST API for the book-review-svc Spring Boot application. The API will enable users to create, read, update, and delete book reviews with validation rules, ownership-based access control, and comprehensive error handling. Reviews will be persisted as JSON files in the resources folder, following the existing application architecture.

---

## Functional Requirements

### FR-1: Create Book Review
- **Description:** Allow authenticated users to create a new review for a book
- **Actor/User:** Any user (identified by X-User-Id header)
- **Endpoint:** `POST /api/reviews`
- **Request Format:**
  ```json
  {
    "bookId": "string (required)",
    "rating": "integer 1-5 (required)",
    "reviewText": "string 10-100 characters (required)"
  }
  ```
- **Request Headers:**
  - `X-User-Id`: User identifier (required, string)
- **Response Format (201 Created):**
  ```json
  {
    "reviewId": "string (UUID)",
    "bookId": "string",
    "userId": "string",
    "rating": "integer 1-5",
    "reviewText": "string",
    "createdAt": "ISO 8601 timestamp",
    "updatedAt": "ISO 8601 timestamp"
  }
  ```
- **Workflow/Steps:**
  1. Client sends POST request with book ID, rating, and review text
  2. System validates X-User-Id header is present
  3. System validates rating is between 1-5 (inclusive)
  4. System validates reviewText length is between 10-100 characters
  5. System verifies the specified book exists in books.json
  6. System generates unique reviewId (UUID)
  7. System records current timestamp as createdAt and updatedAt
  8. System persists review to reviews.json
  9. System returns created review object with 201 status
- **Expected Outcome:** Review is successfully created and stored; user receives confirmation with review ID
- **Acceptance Criteria:**
  - [ ] AC-1.1: HTTP 201 returned when review created successfully with all required fields
  - [ ] AC-1.2: HTTP 400 returned when X-User-Id header missing
  - [ ] AC-1.3: HTTP 400 returned when rating outside 1-5 range
  - [ ] AC-1.4: HTTP 400 returned when reviewText < 10 characters
  - [ ] AC-1.5: HTTP 400 returned when reviewText > 100 characters
  - [ ] AC-1.6: HTTP 400 returned when required fields (bookId, rating, reviewText) missing
  - [ ] AC-1.7: HTTP 404 returned when referenced bookId does not exist
  - [ ] AC-1.8: Review is persisted to reviews.json file
  - [ ] AC-1.9: Generated reviewId is unique (UUID format)
  - [ ] AC-1.10: createdAt and updatedAt timestamps are automatically set

### FR-2: Retrieve All Reviews
- **Description:** Allow users to retrieve all reviews with optional filtering by book
- **Actor/User:** Any user (no authentication required)
- **Endpoint:** `GET /api/reviews`
- **Query Parameters:**
  - `bookId` (optional): Filter reviews by specific book ID
- **Response Format (200 OK):**
  ```json
  [
    {
      "reviewId": "string",
      "bookId": "string",
      "userId": "string",
      "rating": "integer 1-5",
      "reviewText": "string",
      "createdAt": "ISO 8601 timestamp",
      "updatedAt": "ISO 8601 timestamp"
    }
  ]
  ```
- **Workflow/Steps:**
  1. Client sends GET request with optional bookId query parameter
  2. System retrieves all reviews from reviews.json
  3. If bookId parameter provided, system filters reviews matching that book
  4. System returns array of reviews (empty array if no matches)
- **Expected Outcome:** User receives list of reviews matching criteria (all reviews or filtered by book)
- **Acceptance Criteria:**
  - [ ] AC-2.1: HTTP 200 returned with all reviews when no filter provided
  - [ ] AC-2.2: HTTP 200 returned with filtered reviews when bookId query parameter provided
  - [ ] AC-2.3: Empty array returned when no reviews exist for requested bookId
  - [ ] AC-2.4: Response is JSON array with correct review structure

### FR-3: Retrieve Single Review
- **Description:** Allow users to retrieve a specific review by ID
- **Actor/User:** Any user (no authentication required)
- **Endpoint:** `GET /api/reviews/{reviewId}`
- **Path Parameters:**
  - `reviewId`: Review identifier (required, string/UUID)
- **Response Format (200 OK):**
  ```json
  {
    "reviewId": "string",
    "bookId": "string",
    "userId": "string",
    "rating": "integer 1-5",
    "reviewText": "string",
    "createdAt": "ISO 8601 timestamp",
    "updatedAt": "ISO 8601 timestamp"
  }
  ```
- **Workflow/Steps:**
  1. Client sends GET request with specific reviewId in path
  2. System searches reviews.json for matching reviewId
  3. If found, system returns review object
  4. If not found, system returns 404 error
- **Expected Outcome:** User receives the requested review or appropriate error
- **Acceptance Criteria:**
  - [ ] AC-3.1: HTTP 200 returned with review when reviewId exists
  - [ ] AC-3.2: HTTP 404 returned when reviewId does not exist

### FR-4: Update Book Review
- **Description:** Allow review owner to update their existing review
- **Actor/User:** Review owner (identified by X-User-Id header matching review's userId)
- **Endpoint:** `PUT /api/reviews/{reviewId}`
- **Path Parameters:**
  - `reviewId`: Review identifier (required, string/UUID)
- **Request Headers:**
  - `X-User-Id`: User identifier (required, string)
- **Request Format:**
  ```json
  {
    "rating": "integer 1-5 (optional, update if provided)",
    "reviewText": "string 10-100 characters (optional, update if provided)"
  }
  ```
- **Response Format (200 OK):**
  ```json
  {
    "reviewId": "string",
    "bookId": "string",
    "userId": "string",
    "rating": "integer 1-5",
    "reviewText": "string",
    "createdAt": "ISO 8601 timestamp",
    "updatedAt": "ISO 8601 timestamp (current timestamp)"
  }
  ```
- **Workflow/Steps:**
  1. Client sends PUT request with reviewId and optional rating/reviewText
  2. System validates X-User-Id header is present
  3. System retrieves review from reviews.json
  4. If review not found, return 404
  5. System checks if X-User-Id matches review's userId
  6. If user is not owner, return 403 Forbidden
  7. If rating provided, validate it is 1-5
  8. If reviewText provided, validate length 10-100 characters
  9. System updates only provided fields
  10. System updates updatedAt to current timestamp (keep createdAt unchanged)
  11. System persists updated review to reviews.json
  12. System returns updated review object with 200 status
- **Expected Outcome:** Review is updated (if user is owner) or appropriate error returned
- **Acceptance Criteria:**
  - [ ] AC-4.1: HTTP 200 returned when review updated successfully by owner
  - [ ] AC-4.2: HTTP 400 returned when rating provided but outside 1-5 range
  - [ ] AC-4.3: HTTP 400 returned when reviewText provided but < 10 characters
  - [ ] AC-4.4: HTTP 400 returned when reviewText provided but > 100 characters
  - [ ] AC-4.5: HTTP 403 returned when user attempts to update review they don't own
  - [ ] AC-4.6: HTTP 404 returned when reviewId does not exist
  - [ ] AC-4.7: HTTP 400 returned when X-User-Id header missing
  - [ ] AC-4.8: Only provided fields are updated (partial updates allowed)
  - [ ] AC-4.9: createdAt timestamp remains unchanged after update
  - [ ] AC-4.10: updatedAt timestamp is set to current time

### FR-5: Delete Book Review
- **Description:** Allow review owner to delete their review
- **Actor/User:** Review owner (identified by X-User-Id header matching review's userId)
- **Endpoint:** `DELETE /api/reviews/{reviewId}`
- **Path Parameters:**
  - `reviewId`: Review identifier (required, string/UUID)
- **Request Headers:**
  - `X-User-Id`: User identifier (required, string)
- **Response Format (204 No Content):**
  - Empty response body
- **Workflow/Steps:**
  1. Client sends DELETE request with reviewId
  2. System validates X-User-Id header is present
  3. System retrieves review from reviews.json
  4. If review not found, return 404
  5. System checks if X-User-Id matches review's userId
  6. If user is not owner, return 403 Forbidden
  7. System removes review from reviews.json
  8. System returns 204 No Content status
- **Expected Outcome:** Review is deleted (if user is owner) or appropriate error returned
- **Acceptance Criteria:**
  - [ ] AC-5.1: HTTP 204 returned when review deleted successfully by owner
  - [ ] AC-5.2: HTTP 403 returned when user attempts to delete review they don't own
  - [ ] AC-5.3: HTTP 404 returned when reviewId does not exist
  - [ ] AC-5.4: HTTP 400 returned when X-User-Id header missing
  - [ ] AC-5.5: Review is removed from reviews.json file
  - [ ] AC-5.6: No response body returned on successful delete

### FR-6: Validation Error Scenarios
- **Scenario:** Invalid rating values (0, 6, negative, non-integer)
- **Expected Behavior:** HTTP 400 Bad Request with error message indicating rating must be 1-5
- **Impact:** Prevents invalid data from being persisted

- **Scenario:** Review text too short (1-9 characters)
- **Expected Behavior:** HTTP 400 Bad Request with error message indicating minimum 10 characters required
- **Impact:** Ensures meaningful review content

- **Scenario:** Review text too long (101+ characters)
- **Expected Behavior:** HTTP 400 Bad Request with error message indicating maximum 100 characters allowed
- **Impact:** Prevents excessively long reviews that degrade UX

- **Scenario:** Missing required field (bookId, rating, or reviewText)
- **Expected Behavior:** HTTP 400 Bad Request with error message listing missing field(s)
- **Impact:** Ensures data completeness

- **Scenario:** Non-existent book reference
- **Expected Behavior:** HTTP 404 Not Found with message indicating book does not exist
- **Impact:** Maintains referential integrity with books collection

- **Scenario:** Missing X-User-Id header on create/update/delete
- **Expected Behavior:** HTTP 400 Bad Request with error message indicating header required
- **Impact:** Ensures user identification for all write operations

- **Scenario:** User attempts to update/delete another user's review
- **Expected Behavior:** HTTP 403 Forbidden with message indicating insufficient permissions
- **Impact:** Enforces ownership-based access control

---

## Non-Functional Requirements

### Performance
- **Response Time:** All endpoints must respond within 200ms (95th percentile) for typical operations
- **Throughput:** System must support minimum 100 concurrent users without degradation
- **Data Volume:** System must handle minimum 10,000 reviews in reviews.json file
- **File I/O:** JSON file reads/writes should use efficient ObjectMapper patterns with minimal memory overhead

### Security & Compliance
- **User Identification:** X-User-Id header used for user identification (no OAuth/JWT in scope)
- **Authorization Model:** Ownership-based access control
  - Any authenticated user can create reviews
  - Only review owner can update their review
  - Only review owner can delete their review
  - All users can retrieve any review (no read restrictions)
- **Data Protection:**
  - No PII beyond user ID stored in reviews
  - Reviews stored as JSON in application resources folder (not encrypted)
  - No data retention/deletion policy required at this stage
- **Compliance:** No specific compliance requirements (GDPR, HIPAA not in scope for initial release)

### Usability & Accessibility
- **API Documentation:** Swagger/OpenAPI annotations required on all endpoints
- **Error Messages:** Clear, actionable error messages in all error responses
- **Content-Type:** All endpoints support application/json; all responses return JSON
- **HTTP Method Semantics:** Correct HTTP verbs used (POST create, GET read, PUT update, DELETE delete)

### Scalability & Reliability
- **Availability Requirement:** 99% uptime target (operational hours)
- **Load Handling:** System should support linear scaling with additional reviews
- **File-Based Storage:** Acknowledge JSON files may have concurrency issues with simultaneous writes (acceptable for Phase 1)
- **Caching:** Consider caching books.json in memory for validation (performance optimization)
- **Error Recovery:** System should gracefully handle malformed JSON files and recover

### Integrations & Dependencies
- **External Systems:** 
  - Dependency on existing books.json file (must be accessible)
  - Dependency on Spring Boot framework and Jackson ObjectMapper
- **Data Exchange Format:** JSON (request/response bodies, persistent storage)
- **Synchronization Requirements:** File-based; no real-time synchronization with external systems
- **Dependency on Books API:** Reviews API depends on books existing; must validate bookId exists before creating review

---

## Data Model

### Review Entity
```json
{
  "reviewId": "string (UUID, e.g., 550e8400-e29b-41d4-a716-446655440000)",
  "bookId": "string (references existing book)",
  "userId": "string (extracted from X-User-Id header)",
  "rating": "integer (1-5, inclusive)",
  "reviewText": "string (10-100 characters)",
  "createdAt": "ISO 8601 timestamp (e.g., 2026-09-14T10:30:45.123Z)",
  "updatedAt": "ISO 8601 timestamp (initially same as createdAt, updated on modification)"
}
```

### Validation Rules
| Field | Type | Required | Constraints | Error Code |
|-------|------|----------|-------------|-----------|
| bookId | String | Yes | Must reference existing book in books.json | 404 |
| userId | String | Yes | Extracted from X-User-Id header; must be present | 400 |
| rating | Integer | Yes | Must be between 1-5 (inclusive) | 400 |
| reviewText | String | Yes | Length must be 10-100 characters (inclusive) | 400 |
| reviewId | String | Auto-generated | UUID format; unique | N/A |
| createdAt | ISO 8601 | Auto-generated | Current timestamp on creation | N/A |
| updatedAt | ISO 8601 | Auto-generated | Current timestamp on creation/update | N/A |

### reviews.json Structure
```json
{
  "reviews": [
    {
      "reviewId": "550e8400-e29b-41d4-a716-446655440000",
      "bookId": "book-001",
      "userId": "user-123",
      "rating": 5,
      "reviewText": "Excellent book, highly recommended for all readers.",
      "createdAt": "2026-09-14T10:30:45.123Z",
      "updatedAt": "2026-09-14T10:30:45.123Z"
    }
  ]
}
```

---

## Authentication & Authorization

### Authentication Mechanism
- **No formal authentication system** (OAuth, JWT out of scope)
- User identified via `X-User-Id` HTTP header on all write operations (create, update, delete)
- Read operations (GET) do not require X-User-Id header
- Header value is treated as trust boundary (assumption that client manages user identification)

### Authorization Model
- **Create Review:** Any user with valid X-User-Id header can create a review
- **Read Review:** No authorization required; all reviews readable by any client
- **Update Review:** Only the user who created the review (matching userId) can update it
- **Delete Review:** Only the user who created the review (matching userId) can delete it
- **Ownership Verification:** System compares X-User-Id header value with review's userId field

### Error Responses
- **403 Forbidden:** Returned when user attempts operation on review they do not own
- **400 Bad Request:** Returned when X-User-Id header missing on write operations

---

## Error Handling

### HTTP Status Codes
| Status Code | Scenario | Response Body |
|-------------|----------|---------------|
| 201 Created | Review successfully created | Review object with all fields |
| 200 OK | Review successfully retrieved or updated | Review object or array of reviews |
| 204 No Content | Review successfully deleted | Empty body |
| 400 Bad Request | Validation failure (invalid rating, text length, missing fields, missing header) | Error message describing validation failure |
| 403 Forbidden | User lacks authorization (not review owner) | Error message indicating insufficient permissions |
| 404 Not Found | Resource not found (review or referenced book) | Error message indicating resource not found |
| 500 Internal Server Error | Unexpected server error (file I/O failure, etc.) | Generic error message + logging |

### Error Response Format
```json
{
  "status": "integer (HTTP status code)",
  "message": "string (user-friendly error message)",
  "timestamp": "ISO 8601 timestamp",
  "path": "string (endpoint path)"
}
```

### Specific Error Messages
- **Rating Validation:** "Rating must be between 1 and 5"
- **Text Length (Too Short):** "Review text must be at least 10 characters"
- **Text Length (Too Long):** "Review text must not exceed 100 characters"
- **Missing X-User-Id:** "X-User-Id header is required"
- **Missing Field:** "Required field missing: [fieldName]"
- **Book Not Found:** "Referenced book with ID [bookId] does not exist"
- **Review Not Found:** "Review with ID [reviewId] not found"
- **Unauthorized Update/Delete:** "You are not authorized to modify this review (owner-only operation)"

### Logging & Monitoring
- All error conditions should be logged at appropriate levels (WARN for validation errors, ERROR for system failures)
- Include timestamp and user ID in logs for traceability
- 500 errors should include stack traces in server logs (not in response body)

---

## Constraints

### Technical Constraints
- **Framework:** Must be implemented using Spring Boot 4.1.1 with Java 25
- **Data Storage:** JSON files in `src/main/resources/data/` (no database)
- **JSON Processing:** Must use Jackson ObjectMapper (already available in project)
- **Concurrency:** JSON file-based storage may have race conditions on simultaneous writes (acceptable for Phase 1)
- **File System:** Requires write access to src/main/resources/data/ directory
- **Package Structure:** Code must follow existing package structure (controller, service, model layers)

### Business Constraints
- **Authentication Scope:** No OAuth/JWT implementation; X-User-Id header sufficient
- **Data Retention:** No deletion/archival policy required (all reviews kept indefinitely)
- **Audit Trail:** No audit logging of review modifications required
- **Review Uniqueness:** Multiple reviews allowed per user per book (no uniqueness constraint)

### External Constraints
- **Dependency on Books API:** books.json must exist and be accessible for validation
- **Jackson Dependency:** Must have Jackson (ObjectMapper, etc.) available in classpath
- **Spring Boot Configuration:** Must integrate with existing application.properties
- **Lombok Dependency:** Can use Lombok for model classes (already in project)

### Known Limitations
- **File Concurrency:** If two requests attempt to write reviews simultaneously, data loss possible (limitation accepted)
- **Scalability:** JSON file approach will degrade with very large datasets (10K+ reviews)
- **No Transactions:** No transaction rollback capability if operation partially fails
- **No Change Notifications:** Consumers not notified of review changes (no event system)
- **Partial Reads:** Reading entire reviews.json into memory on each request (consider caching for optimization)

---

## Assumptions

1. **X-User-Id Header Trust:** System assumes X-User-Id header value is accurate and already authenticated by the calling system. There is no validation that the provided user ID corresponds to a real user.
   - *Risk if false:* Users could impersonate other users and modify/delete their reviews. *Mitigation:* Implement proper authentication layer in future phases if needed.

2. **books.json is Authoritative:** System assumes books.json file is the source of truth for valid book IDs and is always available.
   - *Risk if false:* Reviews could reference non-existent books if books.json becomes unavailable or is deleted. *Mitigation:* Implement graceful degradation or cache books in memory.

3. **Single Application Instance:** Assumes only one instance of the application is running against the JSON files. No distributed locking implemented.
   - *Risk if false:* Concurrent requests from multiple instances could corrupt JSON data. *Mitigation:* Use database in production or implement file locking.

4. **JSON File Format is Valid:** Assumes reviews.json is well-formed JSON when present. No recovery mechanism for corrupted files.
   - *Risk if false:* Malformed JSON would cause application to crash on first review operation. *Mitigation:* Implement JSON validation and backup/recovery procedures.

5. **HTTP Header Preservation:** Assumes X-User-Id header is preserved through all middleware/proxies and reaches the application unchanged.
   - *Risk if false:* Header might be stripped or modified by intermediaries, breaking user identification. *Mitigation:* Use formal authentication token system (JWT) in future.

6. **Review Text is Plain Text:** Assumes reviewText contains only plain text (no HTML, markdown, or special formatting).
   - *Risk if false:* XSS vulnerabilities if text is rendered as HTML without sanitization. *Mitigation:* Implement input sanitization if HTML rendering planned.

7. **UTC Timestamps:** Assumes all timestamps should be in UTC (ISO 8601 format with Z suffix).
   - *Risk if false:* Timezone handling inconsistencies could cause confusion in reports. *Mitigation:* Document timezone requirement explicitly.

8. **No Review Versioning:** Assumes updates overwrite previous review text completely; no history of previous versions retained.
   - *Risk if false:* Users might want to see previous review versions or changes. *Mitigation:* Implement audit log if version history needed.

9. **Reviews Independent of Books:** Assumes a book can be deleted without cascade-deleting its reviews (reviews can reference deleted books).
   - *Risk if false:* Orphaned reviews could accumulate if books are deleted. *Mitigation:* Implement cascade delete logic in books API if needed.

10. **No Rating Modification Cooldown:** Assumes users can update their review rating as frequently as desired without cooldown period.
    - *Risk if false:* System could be gamed with rapid rating manipulation. *Mitigation:* Implement rate limiting or cooldown period if needed.

---

## Scope Boundaries

### In Scope
- Create new book reviews with rating and text
- Read individual reviews or filter all reviews by book ID
- Update review rating and/or text (owner only)
- Delete reviews (owner only)
- Validation of rating (1-5) and text length (10-100 characters)
- Ownership-based access control (update/delete own reviews only)
- HTTP header-based user identification (X-User-Id)
- JSON file-based persistence in src/main/resources/data/
- Swagger/OpenAPI documentation for all endpoints
- Error handling with appropriate HTTP status codes

### Out of Scope (Phase 2 or Later)
- OAuth/JWT authentication (currently using simple header-based user ID)
- User management system (user directory, account creation)
- Review rating aggregation/statistics endpoints (average rating, rating distribution)
- Review sorting/pagination (currently returns all reviews matching filter)
- Review moderation or flagging inappropriate reviews
- Email notifications on review creation/updates
- Duplicate review prevention (users can post multiple reviews per book)
- Full-text search on review text
- Review comments/replies or nested discussions
- Database migration (staying with JSON files)
- Audit logging of all review changes
- Review approval workflow
- Internationalization/localization of reviews
- Analytics or usage metrics
- API rate limiting or throttling
- Review helpfulness voting ("was this review helpful?")

---

## Test Scenarios

### Test Focus Areas (as clarified)
1. **Validation Failures** - Ensure incorrect inputs are rejected with appropriate error codes
2. **Success Paths** - Ensure happy path workflows function correctly

### Happy Path Scenarios

#### Scenario 1: Create Review Successfully
- **Setup:** Valid X-User-Id header, existing bookId, rating 1-5, reviewText 10-100 chars
- **Action:** POST /api/reviews with valid review data
- **Expected Result:** HTTP 201, review returned with generated reviewId, timestamps set

#### Scenario 2: Retrieve All Reviews
- **Setup:** Multiple reviews in reviews.json
- **Action:** GET /api/reviews (no filter)
- **Expected Result:** HTTP 200, array of all reviews returned

#### Scenario 3: Retrieve Reviews by Book
- **Setup:** Multiple reviews for different books
- **Action:** GET /api/reviews?bookId=book-001
- **Expected Result:** HTTP 200, array containing only reviews for book-001

#### Scenario 4: Update Own Review
- **Setup:** Review exists with userId matching X-User-Id header
- **Action:** PUT /api/reviews/{reviewId} with new rating/text and matching X-User-Id
- **Expected Result:** HTTP 200, updated review returned with new values and current updatedAt

#### Scenario 5: Delete Own Review
- **Setup:** Review exists with userId matching X-User-Id header
- **Action:** DELETE /api/reviews/{reviewId} with matching X-User-Id
- **Expected Result:** HTTP 204, review removed from reviews.json

### Validation Error Scenarios

#### Scenario 6: Create Review - Rating Below Minimum
- **Setup:** POST /api/reviews with rating = 0
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Rating must be between 1 and 5"

#### Scenario 7: Create Review - Rating Above Maximum
- **Setup:** POST /api/reviews with rating = 6
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Rating must be between 1 and 5"

#### Scenario 8: Create Review - Review Text Too Short
- **Setup:** POST /api/reviews with reviewText = "Short" (5 chars)
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Review text must be at least 10 characters"

#### Scenario 9: Create Review - Review Text Too Long
- **Setup:** POST /api/reviews with reviewText of 101 characters
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Review text must not exceed 100 characters"

#### Scenario 10: Create Review - Missing Required Field (bookId)
- **Setup:** POST /api/reviews without bookId
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Required field missing: bookId"

#### Scenario 11: Create Review - Missing Required Field (rating)
- **Setup:** POST /api/reviews without rating
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Required field missing: rating"

#### Scenario 12: Create Review - Missing Required Field (reviewText)
- **Setup:** POST /api/reviews without reviewText
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Required field missing: reviewText"

#### Scenario 13: Create Review - Missing X-User-Id Header
- **Setup:** POST /api/reviews without X-User-Id header
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "X-User-Id header is required"

#### Scenario 14: Create Review - Non-Existent Book
- **Setup:** POST /api/reviews with bookId = "non-existent-book"
- **Action:** Submit request
- **Expected Result:** HTTP 404, error message "Referenced book with ID non-existent-book does not exist"

#### Scenario 15: Update Review - Rating Invalid
- **Setup:** PUT /api/reviews/{reviewId} with rating = 10
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Rating must be between 1 and 5"

#### Scenario 16: Update Review - Text Too Short
- **Setup:** PUT /api/reviews/{reviewId} with reviewText = "Short"
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Review text must be at least 10 characters"

#### Scenario 17: Update Review - Text Too Long
- **Setup:** PUT /api/reviews/{reviewId} with reviewText of 101 characters
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "Review text must not exceed 100 characters"

#### Scenario 18: Update Review - Not Owner
- **Setup:** Review exists with userId = "user-123", request with X-User-Id = "user-456"
- **Action:** PUT /api/reviews/{reviewId}
- **Expected Result:** HTTP 403, error message "You are not authorized to modify this review (owner-only operation)"

#### Scenario 19: Update Review - Review Not Found
- **Setup:** PUT /api/reviews/non-existent-id
- **Action:** Submit request
- **Expected Result:** HTTP 404, error message "Review with ID non-existent-id not found"

#### Scenario 20: Update Review - Missing X-User-Id Header
- **Setup:** PUT /api/reviews/{reviewId} without X-User-Id header
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "X-User-Id header is required"

#### Scenario 21: Delete Review - Not Owner
- **Setup:** Review exists with userId = "user-123", request with X-User-Id = "user-456"
- **Action:** DELETE /api/reviews/{reviewId}
- **Expected Result:** HTTP 403, error message "You are not authorized to modify this review (owner-only operation)"

#### Scenario 22: Delete Review - Review Not Found
- **Setup:** DELETE /api/reviews/non-existent-id
- **Action:** Submit request
- **Expected Result:** HTTP 404, error message "Review with ID non-existent-id not found"

#### Scenario 23: Delete Review - Missing X-User-Id Header
- **Setup:** DELETE /api/reviews/{reviewId} without X-User-Id header
- **Action:** Submit request
- **Expected Result:** HTTP 400, error message "X-User-Id header is required"

---

## Testing Requirements

### Testing Framework & Tools
- **Framework:** JUnit 5 (already in project via Spring Boot Test)
- **Mocking:** Mockito (already available)
- **Assertions:** AssertJ (already available)
- **Scope:** Integration tests covering REST endpoints and service layer
- **Test Execution:** Via `./gradlew test` command

### Test Categories

#### Unit Tests (Service Layer)
- Test validation logic for rating (1-5 range)
- Test validation logic for reviewText (10-100 char length)
- Test ownership verification (comparing userId with X-User-Id)
- Test UUID generation for reviewId
- Test timestamp generation and update logic

#### Integration Tests (REST Controller)
- Test POST /api/reviews success and validation failures (scenarios 6-14)
- Test GET /api/reviews and GET /api/reviews?bookId=X filtering
- Test GET /api/reviews/{reviewId} success and 404 cases
- Test PUT /api/reviews/{reviewId} success and validation failures (scenarios 15-20)
- Test DELETE /api/reviews/{reviewId} success and authorization failures (scenarios 21-23)

#### Test Data Requirements
- Fixture with sample books in books.json (for validation testing)
- Fixture with sample reviews in reviews.json (for filtering/retrieval testing)
- Test user IDs (e.g., "test-user-1", "test-user-2" for ownership testing)

#### Minimal Test Coverage
- At least 2 tests per endpoint
- Coverage for validation failures (rating, text length, missing fields)
- Coverage for success paths (create, update, delete own review)
- Coverage for authorization (403 on non-owner update/delete)
- Coverage for not-found scenarios (404 on missing review/book)

### Test Execution
- Tests should be isolated and executable independently
- Tests should clean up state between runs (e.g., reset reviews.json to known state)
- All tests must pass before code can be committed to main branch

---

## Related Items

### Linked JIRA Issues
- **Depends On:** KAN-1 (Book Management API - must be implemented first)
- **Related To:** KAN-2 (Likely API management or documentation)
- **Related To:** KAN-3 (Likely related feature or enhancement)
- **Part of:** book-review-svc project

### Related Documentation
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Springdoc OpenAPI: https://springdoc.org/
- Jackson ObjectMapper: https://github.com/FasterXML/jackson
- REST API Best Practices: See project CLAUDE.md for architectural guidance

### Stakeholders
- **Product Owner:** Responsible for acceptance and prioritization
- **Development Team:** Implements review endpoints and validation
- **QA Engineers:** Test all scenarios and validation cases
- **DevOps:** Manage deployment and monitoring (if applicable)

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-09-14 | Requirements Agent | Initial requirements document generated based on user clarifications |

---

## Clarifications Recorded

### Q1: What are the primary actors/users who will interact with this feature?
**Answer:** Any authenticated user (via X-User-Id header) can create reviews. Any user can retrieve reviews. Only review owners can update/delete their reviews.

### Q2: What is the happy path workflow?
**Answer:** User creates review (POST) → System validates and stores → User retrieves review (GET) → User updates/deletes own review (PUT/DELETE)

### Q3: What are the edge cases or error scenarios?
**Answer:** Invalid rating (0, 6+), text length violations (< 10 or > 100 chars), missing fields, non-existent books, missing X-User-Id header, unauthorized ownership attempts

### Q4: Are there any data dependencies or integrations?
**Answer:** Depends on books.json for bookId validation; must verify book exists before creating review

### Q5: What are the performance expectations?
**Answer:** < 200ms response time (95th percentile), support 100+ concurrent users, handle 10K+ reviews

### Q6: What security/compliance requirements apply?
**Answer:** Ownership-based access control; X-User-Id header for user identification; no OAuth/JWT; no PII handling required

### Q7: What browsers/devices must this work on?
**Answer:** REST API (backend); no frontend requirements; accessible to any HTTP client

### Q8: What technical constraints exist?
**Answer:** Spring Boot 4.1.1, Java 25, JSON file storage, Jackson ObjectMapper, no database

### Q9: What assumptions are we making?
**Answer:** Single application instance, X-User-Id header is trusted, books.json authoritative, JSON files well-formed

### Additional Clarification: Max review text length?
**Answer:** 100 characters maximum

### Additional Clarification: Test focus areas?
**Answer:** Validation failures and success paths (items 1 & 4)

---

**Document Generated:** 2026-09-14T14:30:00Z
**Last Updated:** 2026-09-14T14:30:00Z
**Status:** Ready for Implementation

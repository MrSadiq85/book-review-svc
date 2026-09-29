# Code Review Summary

**Review scope:** Current working tree Java changes, excluding Markdown files.

**Overall assessment:** The POST book-creation flow is functional and the existing tests pass, but two production issues should be addressed before merge.

## Findings

### 🟠 High — Duplicate ISBN check is not atomic

**File:** `src/main/java/com/epam/book_review_svc/service/BookService.java`  
**Lines:** 40–42

The service checks for an existing ISBN and then calls the repository create operation separately. Concurrent POST requests can both read the same book list, pass the duplicate check, and persist duplicate ISBNs. The repository write lock serializes writes but does not protect the preceding read-and-check operation.

**Recommendation:** Move ISBN uniqueness validation and creation into one repository write-locked operation, or add a repository method that atomically checks and creates the book.

### 🟡 Medium — Unexpected exceptions are not logged

**File:** `src/main/java/com/epam/book_review_svc/exception/GlobalExceptionHandler.java`  
**Lines:** 58–60

The handler returns a sanitized response for unexpected exceptions, but it does not log the underlying exception. This removes server-side diagnostic context even though the response includes a correlation ID.

**Recommendation:** Add server-side exception logging with the generated correlation ID while keeping exception details out of the client response.

## Category Summary

| Category | Findings |
|---|---:|
| Correctness | 1 |
| Security | 0 direct vulnerabilities |
| Error handling | 1 |
| Code clarity | 0 blocking issues |
| DRY | 0 significant issues |

## Recommendation

Fix the atomic duplicate-check/create operation first, then add server-side logging for unexpected exceptions before merging.

# System Architecture Design

## Executive Summary

This document defines the architecture for KAN-3, a small public, unauthenticated JSON REST API for managing books. The service is a single Spring Boot application backed by one local JSON file. It supports collection and item retrieval, full replacement of an existing book, and deletion. It deliberately excludes reviews, ratings, authentication, external integrations, databases, pagination, advanced search, and distributed deployment.

The selected API base path is versioned as `/api/v1/books`. `PUT` is replacement-only: targeting an ISBN-independent resource identifier that does not exist returns `404`; it never performs an implicit create. Every mutation validates the complete proposed state before an atomic file replacement. If validation or persistence fails, the last valid JSON state remains available.

## Requirements Summary

### Goals

- Expose standard JSON REST operations for books:
  - `GET /api/v1/books`
  - `GET /api/v1/books/{id}`
  - `PUT /api/v1/books/{id}`
  - `DELETE /api/v1/books/{id}`
- Return `404` for missing resources, including `PUT` of a missing identifier.
- Return `400` for malformed/incomplete representations and duplicate ISBNs.
- Use one consistent JSON error envelope without stack traces, filesystem paths, or other internal details.
- Persist successful replacements and deletions across restart in local JSON storage.
- Preserve the previous valid state after validation, serialization, or write failure.
- Target approximately one second or less for normal local requests over a representative dataset.

### Non-goals

- Authentication, authorization, users, roles, or tenant isolation.
- Reviews, ratings, comments, recommendations, or other review workflows.
- A database, cache, message broker, cloud storage, or external service.
- `POST`, `PATCH`, upsert semantics, bulk operations, pagination, sorting, filtering, or advanced search.
- Horizontal/distributed scaling, multi-node coordination, or a formal throughput SLA.
- Application-level encryption or regulatory compliance controls beyond safe error handling.

### Repository and implementation constraints

- Current application is a Spring Boot web application using Gradle, Jackson, Lombok, and Hibernate Validator.
- DTOs/POJOs belong under `com.epam.book_review_svc.model.dto` (or a relevant subpackage).
- DTO names should use `*CreateRequestDto`, `*UpdateRequestDto`, and `*ResponseDto`; replacement uses an update DTO.
- DTOs should use Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`), Jackson annotations where needed, and Bean Validation annotations such as `@NotBlank`, `@NotNull`, and `@Positive` as appropriate.

## High-Level Architecture

```text
HTTP client
    |
    v
Spring MVC / JSON message conversion
    |
    v
BookController  ---->  GlobalExceptionHandler
    |
    v
BookService (validation, resource rules, orchestration)
    |
    v
BookRepository / JsonFileStore
    |       ^
    |       |
    +--> in-memory validated snapshot
    |
    v
Atomic JSON file replacement
```

The controller is HTTP-focused and thin. The service owns business rules and coordinates a single consistent mutation. The persistence adapter owns file format, locking, temporary-file handling, and replacement. No component calls an external system.

## Component Design

### API/controller layer

`BookController` exposes `/api/v1/books`, accepts and returns JSON, delegates to the service, and maps successful operations to deterministic HTTP statuses:

- Collection GET: `200` with a JSON array.
- Item GET: `200` with one book.
- Existing-item PUT: `200` with the replacement representation.
- Existing-item DELETE: `204` with an empty body.

Content negotiation should require or prefer `application/json`. Unsupported media types and malformed JSON should be converted to the same public error envelope, with an appropriate `4xx` status (normally `400`, or `415` where the framework distinguishes the media type).

### DTO and validation layer

Use a request DTO for the full PUT representation and a response DTO for output. The URL `id` is authoritative for the targeted resource; the body must not be allowed to silently change the resource identifier. Required book fields and formats are validated at the boundary, then revalidated by the service before mutation.

The canonical book schema must match the domain model established during implementation. At minimum, the representation includes:

```json
{
  "id": "book-123",
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch"
}
```

`id`, `isbn`, `title`, and `author` are the baseline fields for this architecture; any additional fields must be explicitly modeled, validated, serialized, and covered by tests rather than accepted as untyped data. ISBN is normalized before uniqueness comparison (for example, trimming and applying the chosen ISBN canonicalization consistently).

### Service layer

`BookService`:

1. Loads the current snapshot.
2. Resolves the URL identifier.
3. Returns `404` when the target is absent.
4. Validates the complete replacement and normalized ISBN uniqueness.
5. Builds a new complete snapshot without mutating the current snapshot.
6. Requests an atomic persistence operation.
7. Publishes/returns the committed snapshot only after persistence succeeds.

Delete follows the same copy-on-write pattern: locate first, create a snapshot without the target, persist it atomically, then return success.

### Persistence layer

`JsonFileBookRepository` (name illustrative) stores an array or object containing all books in one UTF-8 JSON file. On startup it loads and validates the file into an in-memory snapshot. An absent file is treated as an empty store if that is the selected bootstrap policy; malformed existing data is a startup/configuration failure rather than silently discarded data.

The repository should expose operations equivalent to `readSnapshot`, `replaceSnapshot`, and `reloadIfNeeded`, while keeping Jackson and filesystem details out of controllers and error payloads.

## Technology Stack

- **Runtime and API:** Java 25 toolchain with Spring Boot 4.1.1 and Spring MVC.
- **Serialization:** Jackson JSON with explicit DTOs and controlled deserialization.
- **Validation:** Jakarta/Hibernate Bean Validation at the request boundary, supplemented by service-level invariant checks.
- **Persistence:** UTF-8 JSON file accessed through a repository adapter; no SQL/NoSQL database.
- **Build and tests:** Gradle, JUnit 5, Spring Boot Test, and Mockito/AssertJ where useful.
- **Code style:** Lombok and the repository's DTO instruction file for DTO boilerplate and annotations.
- **Security framework:** Existing Spring Security dependency configured to permit the intentionally public API; no authentication provider is required.

### Error handling

`GlobalExceptionHandler` handles validation, JSON decoding, missing resources, duplicate ISBN conflicts, unsupported media types, and persistence failures. Public messages are stable and sanitized. Internal causes are logged server-side with a correlation identifier, never serialized into the response.

## Data Flow

### Read flow

```text
GET -> controller -> service -> repository snapshot -> response DTO -> JSON 200
```

Reads use the latest committed in-memory snapshot. The repository may lazily reload only when explicitly configured; ordinary requests must not reread and parse the file unnecessarily.

### Full replacement flow

```text
PUT body
  -> JSON deserialize
  -> DTO/schema validation
  -> locate id (404 if absent)
  -> normalize and check ISBN uniqueness (400 on duplicate)
  -> construct candidate complete snapshot
  -> serialize candidate to a temporary file
  -> flush/close temporary file
  -> atomically move temp file over live file
  -> publish candidate snapshot
  -> return 200
```

The live file is not truncated before the candidate is valid and durable enough for the operating environment. Temporary files must be created in the same directory so an atomic rename is available. If atomic move is unsupported, the repository must fail safely rather than overwrite the live file in place, or use a documented same-directory replace fallback with the same last-valid-state guarantee.

### Delete flow

Delete uses the same candidate-file process. A missing identifier returns `404` before any write. A successful delete returns `204` and an empty body.

## API Contract

### Endpoints

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/api/v1/books` | `200` array of books | `500` sanitized server error |
| GET | `/api/v1/books/{id}` | `200` book | `404` if absent |
| PUT | `/api/v1/books/{id}` | `200` replacement book | `400` invalid/duplicate; `404` if absent; sanitized `5xx` on persistence failure |
| DELETE | `/api/v1/books/{id}` | `204`, empty body | `404` if absent; sanitized `5xx` on persistence failure |

There is no `POST`; clients cannot create resources through this contract. There is no `PATCH`; PUT requires a complete representation.

### Request and response schemas

`PUT` request (`application/json`):

```json
{
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch"
}
```

The body must contain every required field, contain no invalid values, and represent the complete replacement. The response is the persisted book, including its stable `id`.

Collection response:

```json
[
  {
    "id": "book-123",
    "isbn": "9780134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch"
  }
]
```

### Error schema

All public errors use this envelope:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "The book representation is invalid.",
  "path": "/api/v1/books/book-123",
  "correlationId": "8d7e..."
}
```

`status`, `code`, and `message` are always present. `path` and `correlationId` are consistently included for every error if implemented. `details` may contain field-level validation entries, but must never contain stack traces, exception class names, absolute paths, request secrets, or raw filesystem messages. Recommended codes are `VALIDATION_ERROR`, `DUPLICATE_ISBN`, `BOOK_NOT_FOUND`, `MALFORMED_JSON`, `UNSUPPORTED_MEDIA_TYPE`, and `PERSISTENCE_ERROR`.

## Validation and Status Semantics

- Blank/missing required fields, malformed JSON, invalid ISBN format, and incomplete PUT bodies: `400`.
- ISBN matching another persisted book after normalization: `400` with `DUPLICATE_ISBN`.
- URL identifier not found for GET, PUT, or DELETE: `404` with `BOOK_NOT_FOUND`.
- A PUT never creates a missing resource.
- Persistence or serialization failure: sanitized `500` (or a deliberately documented `503` if the service treats the local store as temporarily unavailable); no candidate state is published.
- Unexpected errors: sanitized `500`.
- Error response `Content-Type` is `application/json`.

## Concurrency and Consistency

The expected deployment is one service instance with ordinary concurrent clients. Serialize mutations with a process-local read/write lock or equivalent single-writer mechanism. Reads may run concurrently against an immutable snapshot, while a mutation constructs a new snapshot and swaps it only after a successful atomic file replacement.

This provides:

- No lost updates between concurrent PUT/DELETE operations in one process.
- No readers observing a partially built in-memory state.
- No partially written live JSON file under normal local filesystem behavior.
- All-or-nothing mutation visibility.

Cross-process coordination and distributed locking are out of scope. If multiple instances are ever introduced, this design must be replaced or extended with an external coordination/persistence mechanism.

## Scalability & Performance Considerations

The target is approximately one second for normal local requests over a representative dataset. Keeping a validated immutable snapshot in memory makes reads bounded by serialization rather than repeated disk parsing. Writes are expected to be infrequent enough that serializing the complete file and performing one atomic replacement is acceptable.

The design scales vertically only within modest file sizes. It intentionally does not promise horizontal scaling: one process should own the file, and file size, request body size, and book count should have defensible operational limits. If latency or dataset size outgrows this model, the repository interface provides a seam for a database-backed implementation, but that is outside this story.

## Configuration

Use application properties/environment overrides for:

- JSON store path, with a safe local default outside generated build output.
- Whether an absent store initializes as empty.
- Optional maximum request body size and maximum book count/file size safeguards.
- Logging level and server port.

The resolved path must not be returned in API errors. Startup should fail clearly and log an operationally useful but appropriately scoped message when the configured file is unreadable or invalid.

## Error Handling & Resilience

The service follows fail-closed mutation behavior. It validates and serializes a candidate state before touching the live file, writes in the same directory, flushes and closes the temporary file, and then atomically replaces the previous file. Any failure before replacement leaves the previous valid file and in-memory snapshot untouched. A failure after replacement must be reconciled by treating the replacement as the committed state and reloading it before serving subsequent mutations.

At the API boundary, all anticipated failures are converted to deterministic JSON errors. Unexpected failures are logged with a correlation ID and returned as a generic sanitized `500`. Startup rejects an unreadable or malformed existing store rather than silently losing records. Tests must simulate validation, serialization, permission, interrupted-write, and concurrent mutation failures.

## Security Architecture

The API is intentionally public and unauthenticated. If Spring Security remains on the classpath, security configuration must explicitly permit the API endpoints and disable accidental default login/basic-auth behavior that would violate the contract. No user identity or authorization checks are performed.

Defensive controls still apply:

- Validate JSON structure, lengths, formats, and request sizes.
- Normalize ISBN before uniqueness checks.
- Avoid reflection-based or polymorphic JSON deserialization.
- Do not echo untrusted raw exception text.
- Consider deployment-level TLS, network controls, and rate limiting as operational concerns, not application requirements.
- Keep local file permissions restrictive where the host environment permits.

## Observability

Log structured request outcome data: method, route template, status, duration, operation type, and correlation ID. Do not log complete book payloads or sensitive request content by default. Log persistence failures with the correlation ID and sanitized technical context sufficient for operators to diagnose them.

Expose only non-sensitive health/readiness information if an actuator endpoint is later added; do not expose the JSON file contents or filesystem path. Track latency for reads and mutations so the approximately one-second target can be verified.

## Testing Strategy

- **Controller/API tests:** status codes, JSON content types, schemas, empty DELETE body, malformed JSON, unsupported media type, and consistent error envelopes.
- **Service unit tests:** missing target behavior, full replacement semantics, required-field validation, ISBN normalization, duplicate detection, and no mutation on failure.
- **Persistence tests:** startup load, restart durability, UTF-8 serialization, temporary-file replacement, malformed-store handling, and preservation of the prior file after simulated write/serialization failure.
- **Concurrency tests:** simultaneous ordinary reads and serialized PUT/DELETE operations; assert valid JSON and no unrelated record loss.
- **End-to-end tests:** execute the requirements scenarios against a temporary local store, including restart after PUT and DELETE.
- **Performance smoke test:** representative local dataset and normal GET/PUT/DELETE requests should meet the approximately one-second interactive target without turning this into a distributed load-testing project.

## Implementation Roadmap

1. Confirm the canonical `Book` fields and identifier format, then create DTOs following the repository DTO instructions.
2. Implement validation and response/error DTOs plus the global exception handler.
3. Implement the JSON repository with startup loading, immutable snapshots, same-directory temporary files, and atomic replacement.
4. Implement the service rules for lookup, replacement, duplicate ISBN detection, and deletion.
5. Implement versioned controller routes and explicit public security configuration.
6. Add unit, persistence, controller, concurrency, restart, and performance smoke tests.
7. Configure the JSON path and operational logging; verify no source code or error response exposes internal filesystem details.
8. Run the full test suite and manually verify the documented HTTP contract.

## Architectural Trade-Offs

- **Local JSON instead of a database:** minimizes dependencies and satisfies the story, but is suitable only for a single coordinated instance and modest data volume.
- **Immutable in-memory snapshot plus atomic file replacement:** adds implementation complexity and temporary-file management, but gives fast reads and protects the last valid state.
- **Full PUT only:** keeps semantics deterministic and avoids merge ambiguity, but clients must send all required fields.
- **Public unauthenticated API:** satisfies the explicit scope, but places abuse prevention and network exposure controls on the deployment environment.
- **Versioned `/api/v1` path:** makes future incompatible evolution possible with minimal ambiguity, while adding a small amount of URL ceremony now.
- **`404` for missing PUT:** avoids surprising upsert behavior and makes resource existence explicit, at the cost of requiring a separate creation capability if the product later needs one.

## Future Extensibility

Potential future changes should be additive and isolated behind the service/repository boundary:

- Add `POST` for creation without changing PUT replacement semantics.
- Introduce pagination or search as separate, explicitly versioned query capabilities.
- Replace the JSON repository with a database adapter while retaining controller/service contracts.
- Add authentication and authorization through a security boundary rather than embedding identity checks in domain logic.
- Add reviews and ratings as separate resources.
- Add optimistic version/ETag support if multi-client conflict detection becomes necessary.

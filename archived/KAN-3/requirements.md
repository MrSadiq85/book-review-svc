# Requirements Document: KAN-3 Book Review Service REST API

## Story Metadata

- **Issue:** KAN-3
- **Title:** Book Review Service REST API
- **Repository:** `MrSadiq85/book-review-svc`
- **Status:** Requirements clarified and ready for implementation
- **Document purpose:** Define the functional and non-functional requirements for the unauthenticated book resource API. This document does not authorize or include implementation work.

## Story Summary

The service shall expose a REST API for managing books in the book-review service. Clients must be able to retrieve one book or a collection of books, fully replace an existing book, and delete a book. Data shall be persisted locally in JSON format. The API is public and unauthenticated. Invalid requests and duplicate ISBNs must produce predictable JSON error responses.

## Functional Requirements

### Resource and access

1. The service shall expose a book resource through HTTP endpoints.
2. The API shall be publicly accessible without authentication or authorization.
3. The API shall support JSON request and response representations.
4. The API shall support:
   - `GET` for retrieving a single book by identifier.
   - `GET` for retrieving the collection of books.
   - `PUT` for full replacement of a book identified by its resource identifier.
   - `DELETE` for deleting a book identified by its resource identifier.
   - `POST` for creating a new book.

### Retrieval

5. A successful single-book `GET` shall return the requested book and an HTTP success status.
6. A successful collection `GET` shall return all currently persisted books and an HTTP success status.
7. A request for a book identifier that does not exist shall return HTTP `404`.

### Replacement

8. `PUT` shall replace the complete representation of the targeted existing book; partial updates are out of scope.
9. A replacement request shall be validated before persistence.
10. A replacement with a duplicate ISBN belonging to a different book shall be rejected with HTTP `400`.
11. A replacement containing invalid or missing required data shall be rejected with HTTP `400`.
12. A failed replacement shall not partially modify the persisted data.
13. The behavior for a `PUT` targeting a missing identifier shall be consistent with the API contract and documented by the implementation; absent an explicit create/upsert requirement, it shall not silently create a new resource.

### Deletion

14. A successful deletion shall remove the targeted book from local persistence.
15. A successful deletion shall return an empty response body and an HTTP success status.
16. Deleting a missing book shall return HTTP `404`.
17. A failed deletion shall not remove or corrupt any other book.

### Validation and errors

18. ISBN values shall be unique across persisted books.
19. Duplicate ISBN submissions shall return HTTP `400`.
20. Validation failures, including malformed or incomplete book representations, shall return HTTP `400`.
21. Missing resources shall return HTTP `404`.
22. Error responses shall use one consistent JSON error schema across supported endpoints.
23. Error responses shall contain enough machine-readable information to identify the HTTP error and enough human-readable information to explain the failure. Exact field names and whether a correlation/timestamp field is included shall be fixed consistently during implementation.
24. Error responses shall not expose stack traces, filesystem details, or other internal implementation details.

### Persistence

25. The service shall persist book data in a local JSON file or equivalent local JSON-backed store.
26. Successful create/update/delete operations supported by the implementation shall survive service restart through the local JSON persistence mechanism.
27. The persistence layer shall preserve valid existing records when one request fails validation.
28. Concurrent or interrupted writes shall not leave the JSON store in an unreadable or partially written state under the normal operating conditions targeted by the service.

## Non-Functional Requirements

### Performance

1. Under normal local deployment conditions and a representative dataset, read and write requests should complete within a reasonable interactive response time (target: approximately 1 second at the API boundary).
2. The service should support normal concurrent client access without lost updates or corrupted JSON persistence.
3. No specialized high-throughput, distributed, or horizontal-scaling requirement is defined for this story.

### Reliability and consistency

4. The service shall return deterministic HTTP status codes for the defined success and error cases.
5. Persisted JSON shall remain valid after successful operations and service restart.
6. A request shall either complete its intended persistence change or leave the prior valid state intact.

### Security and compliance

7. No authentication or authorization is required; the API is intentionally public and unauthenticated.
8. No additional security or regulatory/compliance requirements are defined for this story.
9. Application-level encryption is not required for API traffic or local JSON persistence by this story.

### Usability and compatibility

10. The API shall use standard HTTP semantics and JSON content types.
11. The API shall be usable by ordinary HTTP clients without a browser-specific UI requirement.
12. No additional browser, device, WCAG, or localization requirement is defined.

## Constraints

- Persistence is limited to local JSON storage; an external database is not required.
- No external integrations are required.
- The API is unauthenticated and publicly accessible.
- `PUT` is a full replacement operation, not a patch operation.
- Delete success must have an empty response body.
- Missing resources must map to HTTP `404`.
- Duplicate ISBN and validation failures must map to HTTP `400`.
- Error responses must be JSON and consistent.
- This requirements task does not include source-code implementation, deployment automation, or production infrastructure design.

## Assumptions

- The managed resource is a book and each book has a stable unique identifier used in the URL.
- ISBN is a required, normalized identity attribute for uniqueness checking.
- The book representation contains the fields defined by the originating story/domain model; required-field and format validation will follow that model.
- The local JSON file is available to the running service and is writable by its process.
- A single service instance or otherwise coordinated access to the local JSON store is the expected deployment model.
- “Reasonable performance” means approximately one-second interactive response time for a representative local dataset, rather than a contractual throughput SLA.
- If the originating story does not explicitly define missing-target `PUT` behavior, implementation will choose and document a single non-upsert behavior rather than silently creating records.

## Scope Boundaries

### In scope

- JSON REST endpoints for single-item and collection retrieval.
- Full replacement of an existing book.
- Deletion of an existing book.
- Input validation and duplicate ISBN detection.
- `400` and `404` handling.
- Consistent JSON error responses.
- Local JSON persistence and preservation of valid data.
- Public unauthenticated access.

### Out of scope

- User accounts, login, roles, permissions, or authorization.
- Encryption, regulatory compliance controls, or security hardening beyond avoiding internal details in errors.
- External databases, third-party services, messaging, search systems, or integrations.
- Partial updates/PATCH semantics.
- Reviews, ratings, comments, recommendations, or other domain capabilities not required by KAN-3.
- UI/front-end development.
- Bulk import/export, pagination, sorting, filtering, and advanced search unless separately requested.
- Distributed deployment, horizontal scaling, and formal load-testing targets.

## Questions & Clarifications

All essential clarification questions for KAN-3 are resolved:

1. **Actors and permissions:** Public unauthenticated clients; no permission differences.
2. **Workflow:** Clients retrieve a single book or the list, fully replace an existing book with `PUT`, or delete a book with `DELETE`.
3. **Edge cases:** Missing resources return `404`; duplicate ISBN and validation failures return `400`; errors use consistent JSON; delete success has an empty body.
4. **Integrations and persistence:** No external integrations; use local JSON persistence.
5. **Performance:** Use reasonable local-service performance defaults, with an approximately one-second interactive target for representative data.
6. **Security and compliance:** No requirements beyond public unauthenticated access; no encryption is needed.
7. **Usability and accessibility:** Standard HTTP/JSON client compatibility; no additional browser, device, or WCAG requirement.
8. **Constraints and assumptions:** Full replacement semantics, local JSON storage, and the scope boundaries above apply.

No further clarification is essential before implementation planning.

## Test Scenarios

1. `GET` an existing book by identifier and verify a successful JSON representation is returned.
2. `GET` the collection and verify all persisted books are returned as JSON.
3. `GET` a missing identifier and verify HTTP `404` with the consistent JSON error schema.
4. `PUT` a valid complete replacement for an existing book and verify the response and persisted representation contain the replacement.
5. Restart the service after a successful replacement and verify the replacement remains available.
6. `PUT` an invalid or incomplete representation and verify HTTP `400`, JSON error output, and no persisted change.
7. `PUT` a representation whose ISBN duplicates another book and verify HTTP `400`, JSON error output, and no persisted change.
8. `DELETE` an existing book and verify a success status with an empty body.
9. Restart or reload after deletion and verify the deleted book is absent.
10. `DELETE` a missing identifier and verify HTTP `404` with the consistent JSON error schema.
11. Submit malformed JSON or an unsupported representation and verify a JSON client error response without internal details.
12. Exercise simultaneous ordinary requests and verify the JSON store remains valid and no unrelated records are lost.
13. Verify requests succeed without authentication credentials.
14. Verify representative local requests meet the approximately one-second interactive response target.

## Related Items

- **JIRA issue:** KAN-3
- **Repository:** `MrSadiq85/book-review-svc`
- **Source of truth for workflow:** `HELP.md`
- **Next workflow input:** This document can be used for architecture design and implementation planning.

## Version History

| Version | Date | Change |
|---|---|---|
| 1.0 | 2026-09-23 | Consolidated KAN-3 story scope and all clarified decisions into the required structured requirements document. |

# Design Review Report

## Executive Summary

The proposed architecture is broadly aligned with the KAN-3 requirements: it uses a single Spring Boot REST API, a public unauthenticated contract, a JSON-backed local store, and a full `PUT` replacement model with `404`/`400` semantics and a consistent error envelope. The core design choice of immutable in-memory snapshots plus atomic file replacement is sound and matches the requirement to preserve the previous valid state during failed writes.

The remaining concerns are not about feature coverage but about operational correctness and contract precision. The design needs tighter guarantees for crash durability, the lifecycle of malformed JSON stores, and exact error schema enforcement. The stack choice also needs explicit verification because it currently targets Java 25 / Spring Boot 4.1.1, which raises compatibility risk for developers and CI environments.

## Architecture Overview

The architecture describes a small, single-instance Spring Boot application exposing a versioned API at `/api/v1/books`. The flow is straightforward:

- `BookController` handles HTTP semantics and JSON conversion.
- `BookService` enforces business rules such as duplicate ISBN checks, missing-resource logic, and validation before mutation.
- `JsonFileBookRepository` keeps a local JSON store and applies atomic same-directory file replacement.
- A global exception handler returns sanitized JSON errors without exposing internals.

This design matches the requirements for unauthenticated public access, local persistence, `PUT` replacement semantics, and data preservation when validation fails. The key strength is the copy-on-write pattern: the service constructs a candidate state before mutation and only swaps it into place once the new JSON is durable.

## Critical Findings

### Risks Identified

- Severity: HIGH — Crash durability is not yet specified strongly enough for the requirement that writes must not leave the JSON store unreadable or partially written.
  - The design states an atomic rename is used, but it does not explicitly require `fsync` on the temp file and directory before rename, nor does it define recovery behavior after an interrupted write or OS crash.
  - Requirement 28 explicitly calls out interrupted writes and readability guarantees; the architecture should document the exact durability contract, not just the rename strategy.
  - Recommended mitigation: define a write sequence that writes to a temp file in the same directory, flushes and closes it, calls `FileChannel.force(true)`/equivalent on the temp file and parent directory, and only then renames atomically. Add a startup recovery check for malformed file state.

- Severity: HIGH — The repository design assumes a single local JSON file is the source of truth, but it needs explicit malformed-store handling and lock behavior.
  - Requirement 27/28 requires preserving valid state and avoiding corrupted JSON after failures; the architecture mentions malformed existing data is a startup/configuration failure, but not how the service behaves if the file is invalid while the app is already running.
  - Without a file lock or controlled single-writer gate, concurrent mutating requests can still race in a multi-threaded local process if the implementation is not careful.
  - Recommended mitigation: document a process-local lock for all writes, a single-writer critical section, a store-health check on startup, and a safe-fail strategy when the store is unreadable or malformed.

- Severity: MEDIUM — The technology stack appears riskier than needed for a small project and may create operational friction.
  - The build file pins Java 25 and Spring Boot 4.1.1, which is a high-risk choice for a small local JSON API. If the target environment or CI has a different Java toolchain, builds will fail even though the domain problem is small and straightforward.
  - Recommended mitigation: align the toolchain to a broadly supported LTS version (for example Java 21 or a project-supported standard) unless the team has confirmed the runtime is intentionally pinned to Java 25.

- Severity: MEDIUM — Security configuration is described as explicit and permissive, but it is underspecified for a public API built with Spring Security.
  - The design says “security configuration must explicitly permit the API endpoints,” but it does not state whether CSRF is disabled, whether the API is stateless, or whether default Spring Security auto-config should be overridden to avoid accidental login/basic auth requirements.
  - This is a real contract risk because the app includes the Spring Security starter and the requirement is explicit: no auth or authorization is required.
  - Recommended mitigation: add a clear security configuration section with `permitAll()` for the public endpoints, stateless session management, and explicit CSRF handling consistent with REST JSON usage.

### Gaps Identified

- Severity: MEDIUM — The error payload contract is documented as a stable envelope, but the exact fields are still not fixed tightly enough for implementation consistency.
  - The requirements say the error responses should have a single schema, fixed field names, and machine-readable plus human-readable fields. The architecture offers a recommended envelope but leaves room for variation (`path`, `correlationId`, `details`, optional timestamp fields).
  - Recommended mitigation: specify a single canonical schema in the architecture, including required fields and exact values for `status`, `code`, `message`, and whether `path`/`correlationId` are always required.

- Severity: MEDIUM — The design does not explicitly define the behavior for malformed existing store files after startup or during a live run.
  - Requirement 25-28 requires local persistence to remain valid and for failed writes not to corrupt data. The architecture says malformed existing data is a startup/configuration failure, but it does not clearly mandate fail-fast startup and operational messaging.
  - Recommended mitigation: add an explicit startup policy: if the store is unreadable or invalid JSON, fail startup with a clear message and do not silently discard data; if the file is absent and configured to initialize empty, do so only under explicit policy.

- Severity: LOW — `PUT` contract clarity could be stronger around body-to-URL identifier mismatch and request validation.
  - Requirements say the URL identifier is authoritative, and the architecture mentions the body must not silently change the resource identifier, but it does not clearly define whether a mismatch should be `400`, `404`, or a validation error from the service edge.
  - Recommended mitigation: state explicitly: if `id` in the body differs from the path variable, reject with `400` as invalid representation before any persistence logic runs.

### Anti-Patterns Detected

- Severity: MEDIUM — Single-file storage is a strong SPOF and creates a brittle operational hotspot for a public API.
  - This is acceptable for the stated scope, but the architecture should not treat it as “safe enough” without explicit operational guardrails, backup/fail-fast behavior, and lock discipline.

- Severity: LOW — The repository responsibilities are broad and could drift into a “god object” if it covers startup loading, validation, transaction-like mutation, and atomic replacement logic together.
  - Recommended mitigation: keep the repository focused on persistence semantics and keep validation and orchestration in the service layer, even when using a single JSON file.

- Severity: LOW — The architecture currently implies a best-effort rename approach rather than a documented durability playbook.
  - That is a classic brittle pattern for file-based state management; if the design is not explicit about atomicity and crash recovery, it becomes difficult to prove correctness under failure conditions.

## Design Decision Validation

- Validated: Single-instance, public, unauthenticated API: aligns with requirements 1-3, 7, 8, 10, and 11.
- Validated: `PUT` replacement-only semantics with `404` on missing target: aligns with requirements 8, 9, 11-13 and the scope constraints.
- Validated: Local JSON persistence and no external database: aligns with requirements 25-28 and the constraints.
- Validated: Immutable snapshot and atomic replacement model: aligns with requirements 12, 17, 27, and 28, assuming the durability and lock guarantees are made explicit.
- Partially validated: Error schema consistency: the intent is correct, but the exact contract should be tightened to avoid drift in implementation.
- Needs improvement: Build/runtime compatibility: Java 25/Spring Boot 4.1.1 is not justified by the requirements and should be reviewed for supportability.

## Recommendations for architecture.md Update

1. Add a dedicated “Durability and crash-safety” subsection under persistence.
   - Define the exact write sequence, `fsync` requirements, and recovery policy when the temp file or rename fails.
2. Add a concrete “Malformed store policy” subsection.
   - Specify startup failure behavior for unreadable or invalid JSON and the condition under which an absent store may initialize to empty.
3. Tighten the “Error schema” contract.
   - Make the field names and required presence explicit, and declare whether `path` and `correlationId` are always included.
4. Specify lock strategy.
   - State that all write operations share a process-local lock and that reads may operate on an immutable snapshot without locking.
5. Define body-vs-path identifier validation clearly.
   - Reject mismatched `id` values in the request body with `400` before resource lookup.
6. Revisit the Java toolchain.
   - Confirm that Java 25 is required or reduce the version to a maintained LTS supported by the team and CI.
7. Add file/size safeguards as explicit operational limits.
   - Document maximum JSON file size and request body size so the design remains within the intended local-service use case.

## Questions for Clarification

1. Is Java 25 intentionally required, or is the team targeting a more standard LTS runtime for local development and CI?
2. For a `PUT` request where the body contains an `id` different from the URL path, should the service reject with `400` before any validation or leave that as a service-specific rule?
3. What is the acceptable durability target under abrupt process termination or power loss: best-effort atomic replace, or explicit fsync-and-rename guarantees?
4. What exact error schema is expected by downstream consumers—must `path`, `correlationId`, and `timestamp` be mandatory for every error payload?

## Agreed Design Decisions

- The service will remain a single-instance Spring Boot application with a local JSON-backed store.
- The API will remain public and unauthenticated.
- `PUT` is a full replacement operation, not a create or patch.
- Missing IDs return `404`; invalid/incomplete or duplicate-ISBN requests return `400`.
- Mutations will preserve the last valid state and avoid partial persistence.
- The API will expose a single consistent JSON error contract without stack traces or internal implementation details.

## Next Steps

1. Freeze the exact `Book` schema and validation rules before implementation.
2. Upgrade or confirm the Java toolchain compatibility and build environment.
3. Implement the JSON repository with explicit file locking, atomic replacement, and startup validation.
4. Add integration tests for malformed JSON, duplicate ISBNs, missing items, restart durability, and concurrent mutation safety.
5. Finalize the exact error envelope and document it in both the API contract and implementation tests.

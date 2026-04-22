---
name: api-consistency
description: Review or enforce REST API consistency in this project — HTTP status codes, response shapes, error envelopes, OpenAPI annotations, PATCH semantics. Use whenever the user adds or modifies a controller, asks what status code to return, questions whether an endpoint matches conventions, mentions Swagger/OpenAPI, or wants a conventions check on a new resource. Also trigger on "review this endpoint", "is this REST-compliant", "does this match our API style".
---

# API consistency

This project's REST surface follows a consistent shape enforced primarily by `GlobalExceptionHandler` and a small set of annotations on each controller. When adding or reviewing an endpoint, check all four dimensions below.

## Status codes

- **POST create** → 200 OK with the created body (project convention — see `ClientController.create`). Not 201 Created unless the user explicitly asks.
- **GET / PUT / PATCH** → 200 OK with the current body.
- **DELETE** → 200 OK with no body (project uses `ResponseEntity.ok().build()` — no 204 here).
- **400 Bad Request** — `@Valid` violations or malformed bodies. Let `GlobalExceptionHandler` emit it; never throw `ResponseStatusException(BAD_REQUEST)` for bean-validation failures.
- **404 Not Found** — throw the project's domain exception and let the handler map it.
- **409 Conflict** — unique-constraint or optimistic-lock violations.
- **5xx** — never throw yourself; unchecked exceptions become 500 via the handler.

## Response shape

- **Success**: DTO mapped from entity via MapStruct, wrapped in `ResponseEntity<Dto>`. Never return entities directly.
- **Error**: the `ApiError` envelope (`correlationId` UUID, `timestamp`, `path`, `message`, optionally `fieldErrors`). Do not invent a second error shape.
- **Paginated list**: `ResponseEntity<Page<Dto>>` — Spring's native `Page` shape (`content`, `number`, `size`, `totalElements`, `totalPages`, `first`, `last`). No custom envelope.

## Annotations and conventions

- `@RestController` + `@RequestMapping("/api/v1/<resource>")` at class level.
- `@RequestBody` body parameters are always `@Valid`.
- Every endpoint has `@Operation(summary = "...")` so springdoc-openapi generates a useful Swagger UI entry.
- PATCH uses `consumes = "application/merge-patch+json"` and `JsonMergePatchUtils` (RFC 7386). Don't invent a custom PATCH.
- IDs in paths use the domain type (`UUID` on clients; `AddressId` composite on addresses).

## Why these matter

- A consistent error envelope lets client teams parse failures once. A parallel shape breaks their code silently.
- Spring's `Page` shape is what Spring Data documents — reinventing it drifts from the framework.
- `@Operation` summaries are what a new developer reads in Swagger UI; missing ones slow onboarding.

## By symptom

- **"What status should this return?"** — apply the table above; lean on `GlobalExceptionHandler` rather than custom statuses.
- **"Does this endpoint match conventions?"** — walk status, body type, `@Valid`, `@Operation`, paginated shape if list.
- **"Swagger is wrong"** — check `@Operation`, DTO schema annotations, and (for PATCH) the `@RequestBody` schema override that tells Swagger the target shape rather than raw `JsonNode`.
- **"DELETE returns 200 here but 204 elsewhere"** — do NOT change the existing convention without asking; flag the inconsistency and let the user decide.

---
name: e2e
description: Validate a feature end-to-end against the running app in this project — start deps, hit endpoints, assert on responses and DB state. Use whenever the user wants to smoke-test a new endpoint, validate a feature "actually works", check a PR's behavior before merge, or asks "does this work end-to-end". Also trigger on "smoke test this", "verify the feature", "run against the app", "test it live".
---

# End-to-end validation

The project has no test suite yet, so validation happens by running the app and exercising real HTTP endpoints against a real Postgres. Keep the flow small and deterministic.

## Setup

1. **Start Postgres**: `docker compose up -d`. It listens on `localhost:5432` with creds `pgadmin/pgadmin` (dev only).
2. **Start the app**: `mvn spring-boot:run -pl gnx-ai-training-app`. Flyway runs migrations on boot (`gnx-ai-training-app/src/main/resources/db/migration/`). Hibernate DDL is `validate` — if the schema diverges the app won't start. That's intended.
3. **Sanity check**: `curl localhost:8080/actuator/health` should return `{"status":"UP"}`.

## Happy-path sweep (CRUD resource)

1. **POST** to create. Capture the returned `id`.
2. **GET /{id}**. Body matches the POST body plus generated fields (`id`, `addresses[].clientId` wired by `AddressIdentifierAssigner`).
3. **GET search** paginated: `?page=0&size=10`. Verify envelope `totalElements`, `totalPages`, `first`, `last`.
4. **PUT** with a new body — full replacement semantics.
5. **PATCH** with `Content-Type: application/merge-patch+json` and a partial body — merge semantics (null deletes a field per RFC 7386).
6. **DELETE**. Then GET by id → expect 404.

## Edge cases to hit

- **`@Valid` violation**: POST a body missing a required field → expect 400 with `ApiError.fieldErrors` populated.
- **404 on unknown id**: GET a random UUID → expect 404 with `ApiError`.
- **Pagination boundary**: seed 150 clients, GET with `size=100` → `totalElements=150`, `totalPages=2`, `first=true`, `last=false`.
- **Case-insensitive search**: POST `firstName="Alice"`, then search `?firstName=alice` → should match.
- **PATCH null semantics**: PATCH `{"phoneNumber": null}` → field cleared; other fields unchanged.

## Guardrails

- **Don't assume state.** Reset between runs: `docker compose down -v && docker compose up -d` wipes the DB. Or issue DELETEs in teardown.
- **Check the correlationId on failures** — `ApiError.correlationId` matches the `GlobalExceptionHandler` log entry; use it to find the server-side stack trace.
- **Hibernate DDL is `validate`, not `update`**. If schema is wrong, fix the Flyway migration; don't drop-and-recreate in prod-like envs.
- **Happy path passing is not merge-ready.** With no unit tests, an edge-case failure is the only signal you have.

## By symptom

- **"App won't start"** — check Flyway migration log and Hibernate validate log. Validate failures name the missing/extra column.
- **"Search returns empty"** — verify data inserted (psql), then check case and empty-string-vs-null behavior.
- **"PATCH doesn't update"** — confirm `Content-Type: application/merge-patch+json` and a JSON object body (not raw array or scalar).

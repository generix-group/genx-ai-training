---
name: bug-analysis
description: Structured bug diagnosis for this REST API — reproduce first, trace layer by layer, hypothesize, fix at the right layer, validate. Use whenever the user reports wrong behavior, a failing endpoint, unexpected response, data corruption, mysterious 500, or asks "why does X do Y". Also trigger on vague cues: "something broke", "this endpoint is weird", "X isn't returning what I expect", "customer says Y is wrong".
---

# Bug analysis

Don't guess. Reproduce first, trace the layers, verify hypotheses before editing code.

## The flow

1. **Reproduce.** Get the exact failing request: `curl`, Swagger UI at `/swagger-ui.html`, or a test. Capture body, query params, headers, response — including the `correlationId` in `ApiError` for 5xx (it pins the log line).
2. **Trace layer-by-layer.** `ClientController` → `ClientService` (+ `ClientUpdater` on update/patch) → `ClientRepositoryImpl` → `ClientJpaRepository` → DB. Bugs usually live at a handoff: DTO↔entity mapping, Pageable threading, Specification building, merge-patch semantics.
3. **Hypothesize then verify.** Suspected SQL bug? Turn on `spring.jpa.show-sql=true` + `hibernate.format_sql=true` and re-run. Suspected data problem? Query Postgres directly: `docker compose exec postgres psql -U pgadmin gnxaitraining`. Suspected null handling? Log the param in the service before acting.
4. **Fix at the right layer.** A case-insensitive-search bug is in `ClientSpecifications`, not the controller. A partial-update bug is in `ClientUpdater` (ignoreNull=true) or `JsonMergePatchUtils`, not MapStruct. A 500 with `LazyInitializationException` is an `@EntityGraph` problem on the repo — not a `@Transactional` problem in the controller.
5. **Validate with the original request.** Re-run what you captured in step 1. A unit test passing only proves the unit test passes.

## Guardrails

- **Use the correlationId.** Every `ApiError` response carries one; `GlobalExceptionHandler` logs it at WARN (4xx) or ERROR (5xx). Grep logs for it before reaching for a debugger.
- **Don't patch symptoms.** If GET returns wrong data, find where the filter is applied — don't post-process in the controller.
- **PATCH is RFC 7386 merge patch.** `null` in the patch means *delete the field*, not *skip*. `ClientUpdater.ignoreNull=true` is the patch path; `false` is the PUT path. Mixing them corrupts data.
- **`open-in-view: false`.** Lazy associations accessed outside the service boundary throw `LazyInitializationException`. The fix is `@EntityGraph` on the repo method, not session widening.

## Why these matter

- The error envelope is designed to make 500s debuggable via correlationId — skipping that step is slower than it feels.
- Wrong-layer fixes leak: a controller-level workaround gets copy-pasted to the next controller and the bug becomes a pattern.

## By symptom

- **"Wrong field value returned"** — check the MapStruct mapper's `@AfterMapping` hooks (bidirectional relationship wiring) and entity getters.
- **"Search returns nothing / wrong rows"** — trace `ClientSpecifications.build`. Each criterion is added only if non-null; empty-string vs null is a common trap.
- **"500 Internal Server Error"** — grab `correlationId` from response, grep logs. Usually a constraint violation, a `LazyInitializationException`, or a schema drift against Hibernate validate.
- **"PATCH clobbered a field"** — verify the controller reads current state, applies merge patch, and calls the patch path of `ClientUpdater` (ignoreNull=true).

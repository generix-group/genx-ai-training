---
name: search-debug
description: Diagnose why the /api/v1/clients search endpoint returns wrong, missing, or extra rows. Use whenever the user reports a search filter not working, missing matches, extra matches, case-sensitivity surprise, or a pagination/sort interaction with search. Also trigger on "search returns nothing for X", "why isn't Y in the results", "this filter doesn't work", "search is off", "totalElements looks wrong".
---

# Search debugging

The search endpoint is `GET /api/v1/clients` with query params `firstName`, `lastName`, `email`, `phoneNumber` plus Spring's `Pageable`. Filtering is built in `ClientSpecifications.build(...)` and consumed by `ClientRepositoryImpl.search` + `ClientJpaRepository.findAll(Specification, Pageable)`.

## The flow

1. **Request** → `ClientController.search` receives `@RequestParam` strings (any can be null) + `@PageableDefault(size = 100) Pageable`.
2. **Service** → `ClientService.search` passes through to `ClientRepositoryImpl.search` which builds a `Specification` via `ClientSpecifications`.
3. **Specification** → each criterion becomes a `col LIKE '%val%'` predicate **only if the string is non-null and non-blank** (`hasText` check). All non-null predicates are AND-ed.
4. **Repository** → `findAll(specification, pageable)` emits the paginated SQL.
5. **Mapping** → `page.map(clientMapper::toDto)` in the controller.

## Common failure modes

- **Case sensitivity.** The current spec uses `criteriaBuilder.like(root.get(attr), "%val%")` — **no `LOWER()` on either side**. So `?firstName=alice` will NOT match `Alice`. This contradicts the DB index comment (which mentions `LOWER()` indexes) — the index is useless until the spec is fixed. Fix: wrap both the column and pattern in `criteriaBuilder.lower(...)`.
- **`phoneNumber` is silently ignored.** `ClientSpecifications.build` takes `phoneNumber` as a parameter but never AND-s it into the specification. `?phoneNumber=555` returns all rows regardless. Fix: add `andIfHasText(spec, phoneNumber, "phoneNumber")`.
- **Empty string vs null.** `?firstName=` arrives as an empty string, not null. The current `hasText` check (null + `isBlank()`) already skips empty/whitespace values — good. Don't "fix" what isn't broken; confirm by logging the built Specification before blaming this.
- **Partial vs exact match.** The pattern is `LIKE '%val%'` — substring match. `?firstName=ali` matches Alice, Natalia, Ali. If the user wants exact match, that's a design change, not a bug.
- **Broken totals.** `ClientRepositoryImpl.search` today calls `findAll(specification)` without Pageable and wraps in `PageImpl<>(clients, pageable, clients.size())` — `totalElements` is the full result count, not the DB total, AND pagination is a lie (all rows materialized into memory). Fix: switch to `clientJpaRepository.findAll(specification, pageable)`.
- **Sort on unsafe column.** `Pageable.getSort()` comes from the raw query — a column the entity doesn't expose can 500 or silently no-op. Whitelist safe columns at the controller.
- **N+1 on addresses.** The DTO includes `addresses`. The paginated repo overload has `@EntityGraph("addresses")`; the non-paginated one does not. Because the repo currently calls the non-paginated overload (see bug above), addresses load lazily → N+1.

## Guardrails

- **Turn on SQL logs locally** before guessing: `spring.jpa.show-sql=true` + `hibernate.format_sql=true` in `application.yml` or via CLI.
- **Hit Postgres directly** to confirm the data is there: `docker compose exec postgres psql -U pgadmin gnxaitraining -c "select * from clients where lower(first_name) like '%alice%';"`.
- **Don't add a post-filter in the controller** to "fix" search results — the fix belongs in `ClientSpecifications` or the repo.

## By symptom

- **"Search returns nothing but rows exist in psql"** — most likely case-sensitivity. `ClientSpecifications.containsIgnoreCase` is misnamed; it does NOT lowercase. `?firstName=alice` won't match `Alice`. Fix the spec to use `criteriaBuilder.lower(...)` on both sides.
- **"phoneNumber filter does nothing"** — not a bug in your request; `ClientSpecifications.build` drops the argument on the floor. Add the missing `andIfHasText` call.
- **"Search returns too many"** — check AND vs OR in the combinator (currently AND via `.and(...)`); confirm the right criterion is non-blank.
- **"totalElements is wrong"** — the `PageImpl(..., clients.size())` bug in `ClientRepositoryImpl.search`. Switch to `clientJpaRepository.findAll(specification, pageable)`.
- **"Same row on pages 2 and 3"** — stable-sort issue. Append primary-key tiebreaker to the sort.
- **"Search is slow"** — check for N+1 (see pagination skill) and for missing indexes on the searched columns. Note: the `LOWER(first_name)` indexes only help once the spec actually uses `LOWER()`.

---
name: pagination
description: Add, fix, or review server-side pagination for Spring Data JPA endpoints in this project. Use whenever the user asks to paginate a controller, mentions that a list endpoint is slow or returns too many rows, reports broken page metadata (totalElements, totalPages, first/last), asks about Pageable or Page, mentions N+1 on a paged query, or wants to validate sort parameters. Also trigger on vague cues like "this endpoint returns too much", "paginate the search", or "the search got slow since we added X" — pagination problems hide behind generic performance complaints more often than not.
---

# Pagination

This project exposes paginated endpoints (canonical example: `ClientController.search`) backed by Spring Data JPA Specifications. Follow the pattern below and watch the guardrails — the common traps all cost real latency or correctness.

## The pattern

1. **Controller** — accept `Pageable` via `@ParameterObject @PageableDefault(size = 100)`, return `ResponseEntity<Page<Dto>>`. Spring already serializes `Page` with `content`, `number`, `size`, `totalElements`, `totalPages`, `first`, `last`. Don't wrap this in a custom envelope — clients expect the Spring shape, and rewriting it loses information or drifts over time.

2. **Service** — pass `Pageable` straight through to the repository. Never `findAll()` then `subList()` — you've loaded the whole table into memory and thrown away the benefit.

3. **Repository** — return `Page<Entity>` from a Specifications-based `search(criteria, pageable)` (see `ClientRepository.search`) or a derived query. If the DTO reads any lazy association, annotate the repo method with `@EntityGraph(attributePaths = {...})` so the associations load in the same query.

4. **Mapping** — use `page.map(mapper::toDto)` in the controller. Never do `.getContent()` then `new PageImpl<>(...)` — the metadata goes wrong almost every time, and the original `Page` is already the right shape.

## Guardrails

- **Default page size**: 100, applied per-endpoint via `@PageableDefault(size = 100)` (project convention — see `ClientController.search`).
- **Max page size**: cap it. Without a cap a client can request `size=100000` and OOM the app. Set `spring.data.web.pageable.max-page-size` in `application.yml`, or validate manually for a per-endpoint cap.
- **Safe sort**: `Pageable.getSort()` is built from the raw query string. Unfiltered, a client can sort by any entity field — including sensitive columns or non-indexed ones that cripple performance. Maintain a whitelist of sortable fields per endpoint and drop or reject anything outside it.
- **Stable ordering**: if the sort column has ties, rows can appear on two pages or not at all. Always append the primary key as a tiebreaker (e.g. `Sort.by("lastName").and(Sort.by("id"))`).
- **N+1 on paged queries**: the worst offender and the one you should suspect first. A size=100 request becomes 101 SQL queries if the DTO touches a lazy association. Fix with `@EntityGraph` on the repo method; verify with Hibernate SQL logs that you see one query, not one-per-row.

## Why these matter

- Returning `List<Dto>` instead of `Page<Dto>` drops totalElements/totalPages — the client's "next page" button breaks.
- Unbounded sort is both a latency risk and a minor info leak (the caller learns the entity's schema by probing).
- N+1 is usually invisible at page 1 and catastrophic at size=100 — so it ships to prod.
- `open-in-view: false` is set in this project (see `application.yml`). That means lazy associations accessed **after** the service returns will throw — another reason to pre-fetch with `@EntityGraph` instead of relying on session-scoped lazy loading.

## By symptom

- **"Add pagination to X"** — apply the 4-step pattern. Ask for defaults, max, and the sort whitelist if the user hasn't specified them.
- **"The page metadata is wrong"** — look for a manual `PageImpl` rebuild in the service or `.getContent()` in the mapper; replace with `page.map(...)`.
- **"The endpoint is slow"** — suspect N+1 first. Check the repo method for `@EntityGraph` covering every association the DTO reads. Turn on `spring.jpa.properties.hibernate.format_sql=true` and `spring.jpa.show-sql=true` locally and confirm one query per page, not one per row.
- **"Review pagination on X"** — walk the 4 steps + guardrails and flag anything missing. Pay particular attention to sort safety and max page size, which tend to be forgotten.

## Out of scope

- Consuming externally paginated APIs (different concern; cursor/token-based pagination, retries, rate limits).
- Cursor/keyset pagination — this project uses offset pagination throughout. If the user explicitly asks for keyset pagination, flag that it's a deliberate architectural shift, not a small change.

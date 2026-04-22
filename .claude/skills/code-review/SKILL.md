---
name: code-review
description: Review Java changes in this project for smells, duplication, readability, and alignment with project conventions — Lombok, MapStruct, module boundaries, Spring idioms, Error Prone, Google Java Format. Use whenever the user asks to review a class, checks quality before merge, says "what do you think of this", pastes a diff, or wants a sanity check on a new file. Also trigger on "look at this code", "any suggestions", "is this clean", "code review this".
---

# Code review

Review for correctness first, then project conventions, then style. Flag, don't rewrite — unless the user asks.

## What to check

1. **Module boundaries** (per CLAUDE.md). `api → core → domain`; `infrastructure` implements core's repo interfaces; `app` bootstraps. Controllers + DTOs + mappers live in `api`. Service interfaces/impls, repo interfaces, updaters, search-criteria records in `core`. JPA entities in `domain`. JPA repo impls + specifications in `infrastructure`. A misplaced class is usually the root cause of a circular compile error or a layering violation flagged by ArchUnit-style reviews.
2. **Lombok usage.** `@RequiredArgsConstructor` for DI classes. `@Slf4j` for loggers. Value objects → `@Value`. Avoid `@Data` on JPA entities — it generates `equals`/`hashCode` on mutable fields and breaks first-level caches and collection-based operations.
3. **MapStruct, not manual mapping.** DTO↔Entity conversion lives in `api/mapper/` with `@Mapper(componentModel = "spring")`. Bidirectional relationship wiring goes in `@AfterMapping` (see `ClientMapper` for the address wiring pattern). Never hand-write a mapper — MapStruct already generated it.
4. **Paginated endpoints return `Page<Dto>`**, never `List<Dto>`. Service passes `Pageable` through untouched. Use `page.map(mapper::toDto)` in the controller; never `.getContent()` + `new PageImpl<>()`.
5. **Lazy associations behind `@EntityGraph`.** With `open-in-view: false`, any lazy access outside the service boundary throws. Repo methods whose DTO reads a lazy collection must declare `@EntityGraph(attributePaths = {...})`.
6. **Error handling via `GlobalExceptionHandler`**, not try/catch + custom responses in controllers. Throw domain exceptions; let the handler emit `ApiError`.

## Guardrails

- **Don't demand tests that don't exist.** The project has no test suite yet. Suggesting "would benefit from a test" is fine; blocking on test coverage is not.
- **Respect Google Java Format.** The `git-code-format-maven-plugin` enforces this. `mvn git-code-format:format-code` fixes; `validate-code-format` fails builds otherwise.
- **Error Prone warnings are on.** Don't dismiss them. Common offenders: `UnusedVariable`, `MissingOverride`, `EqualsIncompatibleType`, `FutureReturnValueIgnored`.
- **Flyway migrations are immutable once applied.** Never edit a migration already run in any environment — add a new one.
- **Annotation-processor order matters** (from CLAUDE.md): Error Prone → Lombok → MapStruct (with `lombok-mapstruct-binding`). Don't reorder without knowing why.

## By symptom

- **"Is this class in the right place?"** — walk the module boundaries; most misplacements are a layering violation.
- **"Too much boilerplate"** — suggest Lombok annotations instead of hand-written constructors/getters; MapStruct instead of manual mappers.
- **"Duplicate code across controllers"** — usually a shared specification or response helper is missing, rarely a base class.
- **"This method is long"** — don't reflexively extract. Long *because* multi-branch business logic lives there is fine; long *because* mapping + validation + side-effect chained together is a smell.

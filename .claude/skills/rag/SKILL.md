---
name: rag
description: Consult authoritative documentation (official Spring / Hibernate / Postgres / Jackson / RFC docs) before implementing or advising on a non-trivial change in this project. Use whenever the user asks "how does X work", "what's the idiomatic way to Y", "find the doc for Z", or seeks to verify behavior claimed by another source. Also trigger on "I think Spring does X, can you check", "is this the right API for Y", "look up how Z works before changing the code", or when Claude isn't confident about current-version semantics.
---

# Documentation lookup

For any non-trivial change, read the authoritative doc for the version used by this project (Spring Boot 3.5, Hibernate / JPA under Boot 3.5, PostgreSQL 16, Jackson, RFC 7386 for merge patch, Jakarta Bean Validation). Claude's parametric knowledge is often 1-2 years behind and Spring's API surface drifts every minor — verify before committing.

## Source of truth, by topic

- **Spring Boot / Spring Data / Spring MVC** — `docs.spring.io`, matching the version in `pom.xml`. Reference docs > javadoc > tutorials.
- **Hibernate / JPA** — `hibernate.org/orm/documentation/<X.Y>/` matching the Hibernate version Spring Boot 3.5 pulls in. Javadoc for specific annotations at `javadoc.io`.
- **PostgreSQL** — `postgresql.org/docs/16/`. Always include the version in the URL; behavior differs between majors.
- **Jackson** — `fasterxml.com/jackson-docs/` and the `jackson-databind` javadoc.
- **RFC-defined behavior** (merge patch, JSON patch, HTTP semantics, UUID, Authorization) — the RFC itself at `datatracker.ietf.org/doc/html/rfc<n>`.
- **Jakarta Bean Validation** — `jakarta.ee/specifications/bean-validation/`.
- **Error Prone** — `errorprone.info/bugpatterns` for a specific warning name.

## The workflow

1. **Narrow the question.** "How does `@EntityGraph` interact with `Pageable` in Spring Data JPA 3.5" is a better query than "how does pagination work".
2. **Fetch the authoritative doc** with WebFetch. Read the relevant section, not the whole page.
3. **Cross-check against the code** — does the project use the API the way the doc describes?
4. **Cite the link** in your explanation. The user should verify in one click.
5. **Don't copy-paste verbatim.** Adapt to project module boundaries and conventions.

## Guardrails

- **Version match matters.** Spring Boot 3.5 pulls a specific Hibernate minor; docs for Hibernate 7.x may describe APIs that don't exist in the version used here. Check `mvn dependency:tree | grep hibernate-core` before quoting behavior.
- **Blog posts are secondary sources.** They go stale and rarely say which version they apply to. Use only to confirm intuition; the spec/doc is canonical.
- **GitHub issues are not canonical.** They reflect a moment in time; the resolution may have shipped in a later version with different semantics.
- **Stack Overflow is 2014 by default.** Verify the accepted answer against current docs before acting.

## By symptom

- **"Is this the idiomatic way?"** — consult the reference doc's how-to / using section for the specific API.
- **"This behavior seems weird"** — the RFC or reference doc usually says exactly what's specified vs implementation-defined.
- **"What changed between versions?"** — Spring Boot release notes at `github.com/spring-projects/spring-boot/wiki/Spring-Boot-<X.Y>-Release-Notes`; Hibernate migration guide in the docs.
- **"Claude, are you sure?"** — if the question hinges on precise version behavior, fetch the doc; don't rely on training-data recall.

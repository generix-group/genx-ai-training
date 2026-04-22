# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build all modules
mvn clean install

# Build without tests (there are no tests yet)
mvn clean install -DskipTests

# Start PostgreSQL (required before running the app)
docker compose up -d

# Run the application
mvn spring-boot:run -pl gnx-ai-training-app

# Format code (Google Java Format via git-code-format plugin)
mvn git-code-format:format-code

# Validate formatting
mvn git-code-format:validate-code-format
```

## Architecture

Multi-module Maven project with strict layered dependencies:

```
api → core → domain
 ↓      ↓
infrastructure (implements core's repository interfaces)
 ↓
app (bootstraps everything, owns configuration and Flyway migrations)
```

**Module responsibilities:**
- **domain**: JPA entities only (`ClientEntity`, `AddressEntity` with `AddressId` composite key)
- **core**: Service interfaces/impls, repository interfaces, validation, exceptions, search criteria. Contains `ClientUpdater` which separates full update (`ignoreNull=false`) from patch (`ignoreNull=true`)
- **infrastructure**: JPA repository implementations, `ClientSpecifications` for dynamic query building. Uses `@EntityGraph` to prevent N+1 on address loading
- **api**: REST controllers, DTOs, MapStruct mappers, `GlobalExceptionHandler`, `JsonMergePatchUtils` (RFC 7386)
- **app**: `@SpringBootApplication`, `application.yml`, Flyway migrations, Spring Data Web config

**Base package**: `com.generixgroup.gnxaitraining`

## Key Patterns

- **DTO ↔ Entity mapping** is done via MapStruct in the API layer (`ClientMapper`). The mapper has an `@AfterMapping` hook to wire bidirectional address relationships.
- **JSON Merge Patch** (PATCH endpoint): `JsonMergePatchUtils` handles RFC 7386 merge semantics recursively. The controller accepts `application/merge-patch+json`.
- **Address management**: Addresses have no standalone CRUD. They are embedded within Client using a composite key (`client_id` + `seq`). `AddressIdentifierAssigner` auto-assigns IDs and bidirectional refs.
- **Search**: `ClientSpecifications` builds composable JPA Specification predicates from `ClientSearchCriteria`. Case-insensitive LIKE matching.
- **Error handling**: `GlobalExceptionHandler` produces `ApiError` responses with unique `correlationId` (UUID), timestamp, and path. 4xx logged at WARN, 5xx at ERROR.
- **Annotation processors** (order matters in compiler config): Error Prone → Lombok → MapStruct (with lombok-mapstruct-binding)

## API

- Base path: `/api/v1/clients`
- Endpoints: POST, GET (by ID), GET (search with pagination, default page size 100), PUT, PATCH, DELETE
- Actuator: `/actuator/health`, `/actuator/info`, `/actuator/prometheus`, `/actuator/metrics`
- Swagger UI available via springdoc-openapi

## Database

- PostgreSQL 16 (docker-compose provides a local instance)
- Flyway migrations in `gnx-ai-training-app/src/main/resources/db/migration/`
- Hibernate DDL mode: `validate` (schema managed by Flyway only)
- `open-in-view: false`
- Tables: `clients` (UUID PK), `client_addresses` (composite PK: `client_id` + `seq`, FK cascade delete)
- Case-insensitive indexes on searchable fields using `LOWER()`

## Tooling

- Java 21 (enforced: [21, 23))
- Maven 3.9+ (enforced)
- Spring Boot 3.5.x
- Google Java Format (via git-code-format-maven-plugin)
- Error Prone for static analysis
- SonarQube configured (excludes generated code, DTOs, MapperImpls)
- Lombok + MapStruct (requires lombok-mapstruct-binding for interop)


# GNX AI Training Project

## Overview
This project is a multi-module Maven Spring Boot application designed for AI training purposes.

## Modules
- gnx-ai-training-domain: JPA entities (ClientEntity, AddressEntity)
- gnx-ai-training-core: Business logic (services)
- gnx-ai-training-infrastructure: Persistence layer (Spring Data JPA, Specifications)
- gnx-ai-training-api: REST controllers, DTOs, error handling
- gnx-ai-training-app: Application bootstrap, configuration

## Tech Stack
- Java 21
- Spring Boot 3.5.x
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- SLF4J / Logback
- Micrometer / Prometheus
- Maven (multi-modules)

## Architecture
Controller → Service → Repository  
DTO ↔ Domain mapping via MapStruct (in controller)

## Features
- CRUD Client
- Embedded Addresses (no standalone CRUD)
- Pagination / Sorting / Search
- JSON Merge Patch
- Validation (Jakarta)
- Global Exception Handling
- Observability (Actuator)

## Database
- PostgreSQL
- Flyway migrations
- UUID identifiers
- Client 1..N Address (composite key client_id + seq)

## Run (local, non-docker)
1. Start PostgreSQL
2. Configure application.yml or env vars
3. Run GnxAiTrainingApplication

## Run (docker)
docker compose up -d

## Build
mvn clean install

## API Base Path
/api/v1/clients

## Actuator
- /actuator/health
- /actuator/info
- /actuator/prometheus
- /actuator/metrics

## Notes
- No security enabled
- No tests
- Designed for extension

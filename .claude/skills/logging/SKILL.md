---
name: logging
description: Review or improve logging in this project — levels, MDC/correlationId, PII safety, error paths. Use whenever the user asks to add logging, says "this is hard to debug", reports missing log context, mentions log levels or log shape, or wants to audit instrumentation. Also trigger on "where are the logs for X", "add logs to Y", "log level for Z", "this should log more/less".
---

# Logging

The project already has an error log path via `GlobalExceptionHandler`: 5xx is logged at ERROR with the stack trace, 4xx (handled `ClientNotFoundException` / `InvalidClientException`) is logged at INFO with just the message, and bean-validation 400s from `handleMethodArgumentNotValid` are not logged at all. A `correlationId` (UUID) is generated per error and returned in `ApiError`, but it is not pushed to MDC — so it only appears in the handler's own log line, not in upstream logs from the same request. When adding new logging, don't duplicate what the handler already emits and don't log what shouldn't be logged.

## Conventions

- **Use `@Slf4j` from Lombok.** No manual `LoggerFactory.getLogger(...)`.
- **Levels**
  - `DEBUG` — local dev only; chatty detail (SQL parameters, raw bodies after redaction).
  - `INFO` — business state transitions worth keeping across restarts (client created/updated/deleted, scheduled job ran). *One per meaningful action*, not per method call.
  - `WARN` — recoverable issues (retry, degraded fallback, unexpected-but-handled state). Note the handler currently logs 4xx at INFO, not WARN — if you think a particular client-error deserves WARN, escalate it in the handler rather than re-logging at the controller.
  - `ERROR` — unhandled failures. 5xx is already ERROR'd by the handler with a stack trace; don't log-and-rethrow upstream.
- **correlationId today lives only in the `ApiError` response and the handler's own log line.** It is not in MDC, so normal request logs don't carry it. If a user wants request-wide traceability, the fix is a servlet filter that puts a correlationId into MDC at request entry and clears it at exit — flag that as the right place, not ad-hoc `MDC.put` calls inside services.
- **Structured fields, not string concatenation**: `log.info("Client created clientId={} firstName={}", id, firstName)` — SLF4J formatting, not `String.format` or `+`. Allows log shippers to parse.

## Guardrails — what NOT to log

- **Never log passwords, tokens, or secrets.** They leak via log shipping.
- **Client personal data** — `email`, `phoneNumber`, possibly `lastName` — is PII. Don't log whole records. Log `clientId` + one scrub-safe identifier.
- **Request bodies at INFO are not loggable.** If truly needed for debugging, DEBUG, toggled off in prod.
- **Don't log and rethrow.** The handler logs at the boundary; an intermediate `log.error(ex); throw;` produces a duplicate stack trace with no new info.
- **Don't log the happy path of every read.** GET /clients/{id} at INFO is noise at scale.

## Why these matter

- Log volume is silent cost. INFO on every read at 1 req/s ≈ 86k lines/day per instance. At 100 req/s you drown.
- PII in logs is a compliance incident. The easiest fix is to never put it there.
- Duplicate error logs (handler + controller) make root-cause slower — two places to grep instead of one.

## By symptom

- **"I can't debug a failed request"** — grab the `correlationId` from the `ApiError` response; grep logs. If nothing matches, the handler isn't being hit — the exception is being swallowed somewhere lower.
- **"Should I log this?"** — if a human investigating an incident would care, yes. If it's already visible in the HTTP response, probably not.
- **"Too many INFO lines"** — demote read-path logs to DEBUG; leave state transitions at INFO.
- **"Where does the correlationId get set?"** — `GlobalExceptionHandler` generates it per-error and returns it in `ApiError`, but does not push it to MDC. So grep will only hit the handler's own log line. For successful-request traceability (or handler logs that include upstream service logs under the same id), add a servlet filter that populates MDC at request entry.

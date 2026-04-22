---
name: validation
description: Review, add, or fix input validation on this project's REST API — bean validation, custom constraints, cross-field rules, and error-response alignment. Use whenever the user asks to add validation, says an endpoint accepts invalid input, reports a missing 400 response, asks about `@NotBlank`/`@Email`/`@Size`, or wants to audit DTO constraints. Also trigger on "X isn't validated", "add @Valid to Y", "this should 400 on Z", "validation is loose here".
---

# Input validation

Validation in this project happens at the controller boundary via Jakarta Bean Validation. The DTOs (`Client`, `Address`) carry the constraints; `@Valid` on the `@RequestBody` parameter activates them; `MethodArgumentNotValidException` is caught by `GlobalExceptionHandler` and turned into a 400 with per-field errors in the `ApiError` envelope.

## The pattern

1. **Put constraints on the DTO**, not on the entity. Entity-level constraints are a DB safety net; bean validation at the boundary is the user-facing contract.
2. **Use the right annotation**:
   - `@NotNull` — field must be present (but could be blank).
   - `@NotBlank` — strings that must be non-null AND non-empty AND non-whitespace.
   - `@NotEmpty` — collections/arrays that must be non-empty.
   - `@Email` — built-in pattern (permissive; not RFC-5322-strict — usually fine).
   - `@Size(min=, max=)` — string length or collection size.
   - `@Pattern(regexp=...)` — structured formats (phone numbers, codes). Anchor with `^...$` to avoid partial matches.
   - `@Positive` / `@PositiveOrZero` — numeric guards.
3. **Cascade into nested DTOs** with `@Valid` on the nested field. Without it, nested constraints don't fire.
4. **Activate on the controller**: `@RequestBody @Valid final Dto dto`. Missing `@Valid` means constraints are silently ignored.
5. **Cross-field rules** (e.g. "if A is present then B is required") — a class-level `@AssertTrue` on a `boolean isValid()` method, or a custom `@Constraint` annotation with its `ConstraintValidator`.

## Guardrails

- **PATCH is different.** With JSON merge patch (RFC 7386), `null` means *delete the field*. Validating the partial body is subtle — validate the *merged result* instead. The project reads current state, applies the merge via `JsonMergePatchUtils`, and validates the result before save.
- **Validate at one layer.** Duplicating `@NotBlank` in the service on top of the DTO leads to inconsistent messages. Trust the boundary.
- **Provide useful messages.** `@NotBlank(message = "firstName is required")` beats the default `"must not be blank"` when `fieldErrors` is returned.
- **Don't over-constrain.** `@Size(min=2, max=50)` on `firstName` rejects "Li" and 51-char surnames. Ask before tightening; false rejections are customer complaints.
- **Don't `throw new IllegalArgumentException`** for user-facing validation — becomes a 500, not a 400. Use bean-validation annotations or the project's validation exception that `GlobalExceptionHandler` maps to 400.

## Why these matter

- A boundary contract is stable; scattered inline validation is not.
- Client teams rely on `ApiError.fieldErrors` — silent validation produces silent bugs for them.
- Over-constraining is a higher-cost mistake than under-constraining because it rejects legitimate traffic; you find out via customer complaints, not logs.

## By symptom

- **"This endpoint accepts invalid X"** — check (a) the DTO has the constraint, (b) the controller parameter has `@Valid`, (c) nested fields have `@Valid` for cascading.
- **"Error response doesn't list which field failed"** — `GlobalExceptionHandler`'s mapping of `MethodArgumentNotValidException` must populate `ApiError.fieldErrors`. Fix there, not in the DTO.
- **"Custom rule: if A set, B required"** — class-level `@AssertTrue` or a custom `@Constraint`. Not in the controller.
- **"PATCH of one field now fails validation"** — you're validating the patch document; validate the merged result instead.

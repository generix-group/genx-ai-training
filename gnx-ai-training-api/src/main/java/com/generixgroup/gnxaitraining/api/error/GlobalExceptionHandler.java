package com.generixgroup.gnxaitraining.api.error;

import com.generixgroup.gnxaitraining.api.dto.ApiError;
import com.generixgroup.gnxaitraining.api.dto.FieldValidationError;
import com.generixgroup.gnxaitraining.core.service.exception.ClientNotFoundException;
import com.generixgroup.gnxaitraining.core.service.exception.InvalidClientException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception,
      @NonNull final HttpHeaders headers,
      @NonNull final HttpStatusCode status,
      final WebRequest request) {

    final var correlationId = UUID.randomUUID().toString();
    final var path = request.getDescription(false).replace("uri=", "");

    final var fieldErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldValidationError)
            .toList();


    final var body =
        ApiError.builder()
            .code("VALIDATION_ERROR")
            .message("The request contains validation errors.")
            .timestamp(OffsetDateTime.now(ZoneId.systemDefault()))
            .path(path)
            .correlationId(correlationId)
            .fieldErrors(fieldErrors)
            .build();

    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(ClientNotFoundException.class)
  public ResponseEntity<ApiError> handleClientNotFound(
      final ClientNotFoundException exception, final HttpServletRequest request) {

    return buildErrorResponse(
        "CLIENT_NOT_FOUND", exception.getMessage(), request, HttpStatus.NOT_FOUND, exception);
  }

  @ExceptionHandler(InvalidClientException.class)
  public ResponseEntity<ApiError> handleInvalidClient(
      final InvalidClientException exception, final HttpServletRequest request) {

    return buildErrorResponse(
        "INVALID_CLIENT", exception.getMessage(), request, HttpStatus.BAD_REQUEST, exception);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpectedException(
      final Exception exception, final HttpServletRequest request) {

    return buildErrorResponse(
        "INTERNAL_ERROR",
        "An unexpected error occurred.",
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        exception);
  }

  private FieldValidationError toFieldValidationError(final FieldError fieldError) {
    return FieldValidationError.builder()
        .field(fieldError.getField())
        .message(fieldError.getDefaultMessage())
        .build();
  }

  private ResponseEntity<ApiError> buildErrorResponse(
      final String code,
      final String message,
      final HttpServletRequest request,
      final HttpStatus status,
      final Exception exception) {

    final var correlationId = UUID.randomUUID().toString();
    final var path = request.getRequestURI();

    logAtLevel(status, code, correlationId, path, exception);

    final var body =
        ApiError.builder()
            .code(code)
            .message(message)
            .timestamp(OffsetDateTime.now(ZoneId.systemDefault()))
            .path(path)
            .correlationId(correlationId)
            .fieldErrors(List.of())
            .build();

    return ResponseEntity.status(status).body(body);
  }

  private void logAtLevel(
      final HttpStatus status,
      final String code,
      final String correlationId,
      final String path,
      final Exception exception) {

    if (status.is5xxServerError()) {
      log.error("Unhandled error [code={}]", code, exception);
      return;
    }

    if (status.is4xxClientError()) {
      log.info("Client error [message={}]", exception.getMessage());
    }
  }
}

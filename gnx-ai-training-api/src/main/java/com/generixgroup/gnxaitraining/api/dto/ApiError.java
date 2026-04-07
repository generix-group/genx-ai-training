package com.generixgroup.gnxaitraining.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API error payload")
public class ApiError {

  private String code;
  private String message;
  private OffsetDateTime timestamp;
  private String path;
  private String correlationId;
  private List<FieldValidationError> fieldErrors;
}

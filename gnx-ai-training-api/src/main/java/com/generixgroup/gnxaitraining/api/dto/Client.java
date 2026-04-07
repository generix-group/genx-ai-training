package com.generixgroup.gnxaitraining.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
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
@Schema(description = "Client payload")
public class Client {

  @Schema(description = "Client identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
  private UUID id;

  @Schema(description = "Client first name", example = "John")
  @NotBlank
  @Size(max = 100)
  private String firstName;

  @Schema(description = "Client last name", example = "Doe")
  @NotBlank
  @Size(max = 100)
  private String lastName;

  @Schema(description = "Client email", example = "john.doe@example.com")
  @NotBlank
  @Email
  @Size(max = 255)
  private String email;

  @Schema(description = "Client phone number", example = "+1-514-555-0101")
  @NotBlank
  @Size(max = 30)
  private String phoneNumber;

  @Valid
  @NotEmpty
  @Schema(description = "Client addresses")
  private List<Address> addresses;
}

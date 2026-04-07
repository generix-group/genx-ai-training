package com.generixgroup.gnxaitraining.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for client create, update, and patch operations")
public class ClientRequestDto {

  @Schema(description = "Client first name", example = "Jane")
  @Size(max = 100)
  private String firstName;

  @Schema(description = "Client last name", example = "Doe")
  @Size(max = 100)
  private String lastName;

  @Schema(description = "Client email", example = "jane.doe@example.com")
  @Email
  @Size(max = 255)
  private String email;

  @Schema(description = "Client phone number", example = "+1-514-555-0101")
  @Size(max = 30)
  private String phoneNumber;

  @Valid
  @Schema(description = "Client addresses")
  private List<AddressDto> addresses;
}

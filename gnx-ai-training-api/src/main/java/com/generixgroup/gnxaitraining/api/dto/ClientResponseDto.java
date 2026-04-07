package com.generixgroup.gnxaitraining.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Client response payload")
public class ClientResponseDto {

  @Schema(description = "Client identifier")
  private UUID id;

  @Schema(description = "Client first name")
  private String firstName;

  @Schema(description = "Client last name")
  private String lastName;

  @Schema(description = "Client email")
  private String email;

  @Schema(description = "Client phone number")
  private String phoneNumber;

  @Schema(description = "Client addresses")
  private List<AddressDto> addresses;
}

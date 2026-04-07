package com.generixgroup.gnxaitraining.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Client address")
public class AddressDto {

  @Schema(description = "Address line 1", example = "123 Main Street")
  @Size(max = 255)
  private String street1;

  @Schema(description = "Address line 2", example = "Suite 400")
  @Size(max = 255)
  private String street2;

  @Schema(description = "City", example = "Montreal")
  @Size(max = 100)
  private String city;

  @Schema(description = "State or province", example = "Quebec")
  @Size(max = 100)
  private String state;

  @Schema(description = "Postal code", example = "H2X 1Y4")
  @Size(max = 30)
  private String postalCode;

  @Schema(description = "Country", example = "Canada")
  @Size(max = 100)
  private String country;

  @Schema(description = "Address type", example = "HOME")
  @Size(max = 50)
  private String type;
}

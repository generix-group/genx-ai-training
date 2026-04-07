package com.generixgroup.gnxaitraining.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Client address")
public class Address {

  @Schema(description = "Address line 1", example = "123 Main Street")
  @NotBlank
  @Size(max = 255)
  private String street1;

  @Schema(description = "Address line 2", example = "Suite 400")
  @Size(max = 255)
  private String street2;

  @Schema(description = "City", example = "Montreal")
  @NotBlank
  @Size(max = 100)
  private String city;

  @Schema(description = "State or province", example = "Quebec")
  @NotBlank
  @Size(max = 100)
  private String state;

  @Schema(description = "Postal code", example = "H2X 1Y4")
  @NotBlank
  @Size(max = 30)
  private String postalCode;

  @Schema(description = "Country", example = "Canada")
  @NotBlank
  @Size(max = 100)
  private String country;

  @Schema(description = "Address type", example = "HOME")
  @NotBlank
  @Size(max = 50)
  private String type;
}

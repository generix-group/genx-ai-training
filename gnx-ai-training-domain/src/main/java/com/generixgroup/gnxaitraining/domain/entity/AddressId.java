package com.generixgroup.gnxaitraining.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AddressId implements Serializable {

  @Column(name = "client_id", nullable = false, updatable = false)
  private UUID clientId;

  @Column(name = "seq", nullable = false)
  private Integer seq;
}

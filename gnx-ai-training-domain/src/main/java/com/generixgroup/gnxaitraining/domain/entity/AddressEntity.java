package com.generixgroup.gnxaitraining.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity {

  @EmbeddedId private AddressId id;

  @MapsId("clientId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false, updatable = false)
  private ClientEntity client;

  @Column(name = "street1", nullable = false)
  private String street1;

  @Column(name = "street2")
  private String street2;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "state", nullable = false, length = 100)
  private String state;

  @Column(name = "postal_code", nullable = false, length = 30)
  private String postalCode;

  @Column(name = "country", nullable = false, length = 100)
  private String country;

  @Column(name = "type", nullable = false, length = 50)
  private String type;
}

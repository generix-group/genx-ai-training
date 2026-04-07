package com.generixgroup.gnxaitraining.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity {

  @Id
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "phone_number", nullable = false, length = 30)
  private String phoneNumber;

  @Builder.Default
  @OneToMany(
      mappedBy = "client",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("id.seq ASC")
  private List<AddressEntity> addresses = new ArrayList<>();

  public void replaceAddresses(final List<AddressEntity> newAddresses) {
    addresses.clear();
    if (null == newAddresses) {
      return;
    }

    newAddresses.forEach(this::addAddress);
  }

  public void addAddress(final AddressEntity address) {
    address.setClient(this);
    addresses.add(address);
  }
}

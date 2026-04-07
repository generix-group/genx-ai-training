package com.generixgroup.gnxaitraining.core.service.validation;

import com.generixgroup.gnxaitraining.core.service.exception.InvalidClientException;
import com.generixgroup.gnxaitraining.domain.entity.AddressEntity;
import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientValidator {

  public void validate(final ClientEntity client) {
    requireNotBlank(client.getFirstName(), "firstName is required.");
    requireNotBlank(client.getLastName(), "lastName is required.");
    requireNotBlank(client.getEmail(), "email is required.");
    requireNotBlank(client.getPhoneNumber(), "phoneNumber is required.");

    final var addresses = client.getAddresses();
    if (addresses == null || addresses.isEmpty()) {
      throw new InvalidClientException("At least one address is required.");
    }

    addresses.forEach(this::validateAddress);
  }

  private void validateAddress(final AddressEntity address) {
    requireNotBlank(address.getStreet1(), "address.street1 is required.");
    requireNotBlank(address.getCity(), "address.city is required.");
    requireNotBlank(address.getState(), "address.state is required.");
    requireNotBlank(address.getPostalCode(), "address.postalCode is required.");
    requireNotBlank(address.getCountry(), "address.country is required.");
    requireNotBlank(address.getType(), "address.type is required.");
  }

  private void requireNotBlank(final String value, final String message) {
    if (null == value || value.isBlank()) {
      throw new InvalidClientException(message);
    }
  }
}

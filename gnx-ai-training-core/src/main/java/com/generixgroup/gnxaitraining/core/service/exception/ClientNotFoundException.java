package com.generixgroup.gnxaitraining.core.service.exception;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException {

  public ClientNotFoundException(final UUID clientId) {
    super("Client not found for id: " + clientId);
  }
}

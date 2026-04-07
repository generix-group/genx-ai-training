package com.generixgroup.gnxaitraining.core.service;

import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

  ClientEntity create(ClientEntity client);

  void delete(UUID id);

  ClientEntity findById(UUID id);

  ClientEntity patch(UUID id, ClientEntity client);

  Page<ClientEntity> search(
      String firstName, String lastName, String email, String phoneNumber, Pageable pageable);

  ClientEntity update(UUID id, ClientEntity client);
}

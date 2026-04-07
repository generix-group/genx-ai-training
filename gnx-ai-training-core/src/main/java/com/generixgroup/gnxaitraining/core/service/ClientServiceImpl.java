package com.generixgroup.gnxaitraining.core.service;

import com.generixgroup.gnxaitraining.core.repository.ClientRepository;
import com.generixgroup.gnxaitraining.core.service.assignment.AddressIdentifierAssigner;
import com.generixgroup.gnxaitraining.core.service.exception.ClientNotFoundException;
import com.generixgroup.gnxaitraining.core.service.update.ClientUpdater;
import com.generixgroup.gnxaitraining.core.service.validation.ClientValidator;
import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {
  private final AddressIdentifierAssigner addressIdentifierAssigner;
  private final ClientRepository clientRepository;
  private final ClientValidator clientValidator;
  private final ClientUpdater clientUpdater;

  @Override
  public ClientEntity create(final ClientEntity client) {
    client.setId(null);
    clientValidator.validate(client);
    addressIdentifierAssigner.assign(client);

    return clientRepository.save(client);
  }

  @Override
  public void delete(final UUID clientId) {
    if (!clientRepository.existsById(clientId)) {
      throw new ClientNotFoundException(clientId);
    }
    clientRepository.deleteById(clientId);
  }

  @Override
  @Transactional(readOnly = true)
  public ClientEntity findById(final UUID clientId) {
    return getClientOrThrow(clientId);
  }

  private ClientEntity getClientOrThrow(final UUID clientId) {
    return clientRepository
        .findById(clientId)
        .orElseThrow(() -> new ClientNotFoundException(clientId));
  }

  @Override
  public ClientEntity patch(final UUID clientId, final ClientEntity client) {
    final var existingClient = getClientOrThrow(clientId);
    clientUpdater.applyPatch(existingClient, client);
    clientValidator.validate(existingClient);
    addressIdentifierAssigner.assign(existingClient);
    return clientRepository.save(existingClient);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClientEntity> search(
      final String firstName,
      final String lastName,
      final String email,
      final String phoneNumber,
      final Pageable pageable) {
    final var criteria =
        ClientSearchCriteria.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();
    return clientRepository.search(criteria, pageable);
  }

  @Override
  public ClientEntity update(final UUID clientId, final ClientEntity client) {
    final var existingClient = getClientOrThrow(clientId);
    clientUpdater.applyFullUpdate(existingClient, client);
    clientValidator.validate(existingClient);
    addressIdentifierAssigner.assign(existingClient);
    return clientRepository.save(existingClient);
  }
}

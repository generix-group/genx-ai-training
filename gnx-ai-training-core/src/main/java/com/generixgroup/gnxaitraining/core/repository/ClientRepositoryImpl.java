package com.generixgroup.gnxaitraining.core.repository;

import com.generixgroup.gnxaitraining.core.service.ClientSearchCriteria;
import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import com.generixgroup.gnxaitraining.infrastructure.persistence.ClientJpaRepository;
import com.generixgroup.gnxaitraining.infrastructure.persistence.ClientSpecifications;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {
  private final ClientJpaRepository clientJpaRepository;

  @Override
  public void deleteById(final UUID id) {
    clientJpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(final UUID id) {
    return clientJpaRepository.existsById(id);
  }

  @Override
  public Optional<ClientEntity> findById(final UUID id) {
    return clientJpaRepository.findById(id);
  }

  @Override
  public ClientEntity save(final ClientEntity client) {
    return clientJpaRepository.save(client);
  }

  @Override
  public Page<ClientEntity> search(final ClientSearchCriteria criteria, final Pageable pageable) {
    final var specification =
        ClientSpecifications.build(
            criteria.firstName(), criteria.lastName(), criteria.email(), criteria.phoneNumber());
    final var clients = clientJpaRepository.findAll(specification);
    return new PageImpl<>(clients, pageable, clients.size());
  }
}

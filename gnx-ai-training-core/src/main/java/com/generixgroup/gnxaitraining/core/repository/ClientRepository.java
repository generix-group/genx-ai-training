package com.generixgroup.gnxaitraining.core.repository;

import com.generixgroup.gnxaitraining.core.service.ClientSearchCriteria;
import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientRepository {

  void deleteById(UUID id);

  boolean existsById(UUID id);

  Optional<ClientEntity> findById(UUID id);

  ClientEntity save(ClientEntity client);

  Page<ClientEntity> search(ClientSearchCriteria criteria, Pageable pageable);
}

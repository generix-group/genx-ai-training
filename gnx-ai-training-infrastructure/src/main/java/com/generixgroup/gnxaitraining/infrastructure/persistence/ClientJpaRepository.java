package com.generixgroup.gnxaitraining.infrastructure.persistence;

import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

public interface ClientJpaRepository
    extends JpaRepository<ClientEntity, UUID>, JpaSpecificationExecutor<ClientEntity> {
  @EntityGraph(attributePaths = "addresses")
  @NonNull
  Page<ClientEntity> findAll(Specification<ClientEntity> specification, @NonNull Pageable pageable);

  @EntityGraph(attributePaths = "addresses")
  @NonNull
  Optional<ClientEntity> findById(@NonNull UUID id);
}

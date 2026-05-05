package com.generixgroup.gnxaitraining.infrastructure.persistence;

import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ClientSpecifications {

  private ClientSpecifications() {}

  public static Specification<ClientEntity> build(
      final String firstName, final String lastName, final String email, final String phoneNumber) {

    var specification = noFilter();
    specification = andIfHasText(specification, firstName, "firstName");
    specification = andIfHasText(specification, lastName, "lastName");
    specification = andIfHasText(specification, email, "email");
    specification = andIfHasText(specification, phoneNumber, "phoneNumber");
    return specification;
  }

  private static Specification<ClientEntity> noFilter() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
  }

  private static Specification<ClientEntity> andIfHasText(
      final Specification<ClientEntity> base, final String value, final String attributeName) {
    if (hasText(value)) {
      return base.and(containsIgnoreCase(attributeName, value));
    }
    return base;
  }

  private static Specification<ClientEntity> containsIgnoreCase(
      final String attributeName, final String value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(
            root.get(attributeName), toContainsPattern(value));
  }

  private static String toContainsPattern(final String value) {
    return "%" + value + "%";
  }

  private static boolean hasText(final String value) {
    return null != value && !value.isBlank();
  }
}

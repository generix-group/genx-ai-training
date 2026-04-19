package com.generixgroup.gnxaitraining.core.service.update;

import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;

@Component
public class ClientUpdater {

  public void applyFullUpdate(final ClientEntity target, final ClientEntity source) {
    apply(target, source.getFirstName(), ClientEntity::setFirstName, false);
    apply(target, source.getLastName(), ClientEntity::setLastName, false);
    apply(target, source.getEmail(), ClientEntity::setEmail, false);
    apply(target, source.getPhoneNumber(), ClientEntity::setPhoneNumber, false);
    apply(target, source.getAddresses(), ClientEntity::replaceAddresses, false);
  }

  public void applyPatch(final ClientEntity target, final ClientEntity source) {
    apply(target, source.getFirstName(), ClientEntity::setFirstName, false);
    apply(target, source.getLastName(), ClientEntity::setLastName, false);
    apply(target, source.getEmail(), ClientEntity::setEmail, false);
    apply(target, source.getPhoneNumber(), ClientEntity::setPhoneNumber, false);
    apply(target, source.getAddresses(), ClientEntity::replaceAddresses, false);
  }

  private <T> void apply(
      final ClientEntity target,
      final T value,
      final BiConsumer<ClientEntity, T> consumer,
      final boolean ignoreNull) {
    if (ignoreNull && null == value) {
      return;
    }
    consumer.accept(target, value);
  }
}

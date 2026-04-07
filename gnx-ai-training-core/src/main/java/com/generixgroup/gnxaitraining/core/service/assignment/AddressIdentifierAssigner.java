package com.generixgroup.gnxaitraining.core.service.assignment;

import com.generixgroup.gnxaitraining.domain.entity.AddressId;
import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class AddressIdentifierAssigner {

  public void assign(final ClientEntity client) {
    final var addresses = client.getAddresses();
    if (null == addresses) {
      return;
    }

    IntStream.range(0, addresses.size())
        .forEach(
            index -> {
              final var address = addresses.get(index);
              address.setClient(client);
              address.setId(new AddressId(client.getId(), index + 1));
            });
  }
}

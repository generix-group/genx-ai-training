package com.generixgroup.gnxaitraining.api.mapper;

import com.generixgroup.gnxaitraining.api.dto.Address;
import com.generixgroup.gnxaitraining.api.dto.Client;
import com.generixgroup.gnxaitraining.domain.entity.AddressEntity;
import com.generixgroup.gnxaitraining.domain.entity.ClientEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

  @Mapping(target = "addresses", source = "addresses")
  ClientEntity toEntity(Client client);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "client", ignore = true)
  AddressEntity toEntity(Address address);

  Client toDto(ClientEntity clientEntity);

  Address toDto(AddressEntity addressEntity);

  @AfterMapping
  default void linkAddresses(@MappingTarget final ClientEntity clientEntity) {
    if (null == clientEntity.getAddresses()) {
      return;
    }

    clientEntity.getAddresses().forEach(address -> address.setClient(clientEntity));
  }
}

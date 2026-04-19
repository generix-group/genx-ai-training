package com.generixgroup.gnxaitraining.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generixgroup.gnxaitraining.api.dto.Client;
import com.generixgroup.gnxaitraining.api.mapper.ClientMapper;
import com.generixgroup.gnxaitraining.api.util.JsonMergePatchUtils;
import com.generixgroup.gnxaitraining.core.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
  private final ClientMapper clientMapper;
  private final ClientService clientService;
  private final ObjectMapper objectMapper;

  @PostMapping
  @Operation(summary = "Create a client")
  public ResponseEntity<Client> create(
      @Valid @org.springframework.web.bind.annotation.RequestBody final Client client) {
    var savedClient = clientService.create(clientMapper.toEntity(client));
    return ResponseEntity.ok(clientMapper.toDto(savedClient));
  }

  @GetMapping("/{clientId}")
  @Operation(summary = "Get a client by id")
  public ResponseEntity<Client> getById(@PathVariable final UUID clientId) {
    var client = clientService.findById(clientId);
    return ResponseEntity.ok(clientMapper.toDto(client));
  }

  @GetMapping
  @Operation(summary = "Search clients with pagination, sorting, and filters")
  public ResponseEntity<Page<Client>> search(
      @RequestParam(required = false) final String firstName,
      @RequestParam(required = false) final String lastName,
      @RequestParam(required = false) final String email,
      @RequestParam(required = false) final String phoneNumber,
      @ParameterObject @PageableDefault(size = 100) final Pageable pageable) {
    var response =
        clientService
            .search(firstName, lastName, email, phoneNumber, pageable)
            .map(clientMapper::toDto);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{clientId}")
  @Operation(summary = "Replace a client")
  public ResponseEntity<Client> update(
      @PathVariable final UUID clientId,
      @Valid @org.springframework.web.bind.annotation.RequestBody final Client client) {
    var updatedClient = clientService.update(clientId, clientMapper.toEntity(client));
    return ResponseEntity.ok(clientMapper.toDto(updatedClient));
  }

  @PatchMapping(
      value = "/{clientId}",
      consumes = "application/merge-patch+json",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Partially update a client using JSON Merge Patch",
      requestBody =
          @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = Client.class))))
  public ResponseEntity<Client> patch(
      @PathVariable final UUID clientId,
      @org.springframework.web.bind.annotation.RequestBody final JsonNode mergePatchNode) {
    var currentState = clientMapper.toDto(clientService.findById(clientId));
    var patchedState =
        JsonMergePatchUtils.applyMergePatch(
            objectMapper, mergePatchNode, currentState, Client.class);
    var patchedClient = clientService.patch(clientId, clientMapper.toEntity(patchedState));
    return ResponseEntity.ok(clientMapper.toDto(patchedClient));
  }

  @DeleteMapping("/{clientId}")
  @Operation(summary = "Delete a client")
  public ResponseEntity<Void> delete(@PathVariable final UUID clientId) {
    clientService.delete(clientId);
    return ResponseEntity.ok().build();
  }
}

package com.generixgroup.gnxaitraining.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.generixgroup.gnxaitraining.core.service.exception.InvalidClientException;

public final class JsonMergePatchUtils {

  private JsonMergePatchUtils() {}

  public static <T> T applyMergePatch(
      final ObjectMapper objectMapper,
      final JsonNode mergePatchNode,
      final T targetObject,
      final Class<T> targetClass) {
    try {
      final var targetNode = objectMapper.valueToTree(targetObject);
      final var patchedNode = mergePatch(targetNode, mergePatchNode);
      return objectMapper.treeToValue(patchedNode, targetClass);
    } catch (final Exception exception) {
      throw new InvalidClientException("Invalid JSON Merge Patch payload.");
    }
  }

  private static JsonNode mergePatch(final JsonNode targetNode, final JsonNode patchNode) {
    if (!patchNode.isObject()) {
      return patchNode;
    }

    final var resultNode = (ObjectNode) targetNode.deepCopy();

    final var fieldNames = patchNode.fieldNames();
    while (fieldNames.hasNext()) {
      final var fieldName = fieldNames.next();
      final var patchValue = patchNode.get(fieldName);

      if (patchValue.isNull()) {
        resultNode.set(fieldName, patchValue);
      } else {
        final var currentValue = resultNode.get(fieldName);

        if (currentValue != null && currentValue.isObject() && patchValue.isObject()) {
          resultNode.set(fieldName, mergePatch(currentValue, patchValue));
        } else {
          resultNode.set(fieldName, patchValue);
        }
      }
    }

    return resultNode;
  }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.serializers.DeserializeFieldCheck;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;

import java.util.Map;

/**
 * A serializer provides low-level serialization support for a type, using a mapping of type handlers for each field of that type.
 *
 */
public class Serializer {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    private final ClassMetadata<?, ?> classMetadata;
    private final Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers;

    public Serializer(ClassMetadata<?, ?> classMetadata, Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers) {
        this.fieldHandlers = fieldHandlers;
        this.classMetadata = classMetadata;
    }

    /**
     * @param field The metadata for a field of the type handled by this serializer.
     * @return The TypeHandler for the given field
     */
    public TypeHandler<?> getHandlerFor(FieldMetadata<?, ?> field) {
        return fieldHandlers.get(field);
    }

    /**
     * Serializes a field of a provided container
     *
     * @param field     The metadata for the field to serialize
     * @param container The object containing the field
     * @param context   The current serialization context
     * @return The serialized value of the field
     */
    @SuppressWarnings("unchecked")
    public PersistedData serialize(FieldMetadata<?, ?> field, Object container, PersistedDataSerializer context) {
        Object rawValue = field.getValue(container);
        if (rawValue != null) {
            TypeHandler handler = getHandlerFor(field);
            if (handler != null) {
                return handler.serialize(rawValue, context);
            }
        }
        return context.serializeNull();
    }

    /**
     * Serializes the given value, that was originally obtained from the given field.
     * <br><br>
     * This is provided for performance, to avoid obtaining the same value twice.
     *
     * @param rawValue The value to serialize
     * @return The serialized value
     */
    @SuppressWarnings("unchecked")
    public PersistedData serializeValue(FieldMetadata<?, ?> fieldMetadata, Object rawValue, PersistedDataSerializer context) {
        return fieldHandlers.get(fieldMetadata).serialize(rawValue, context);
    }

    /**
     * Deserializes a value onto an object
     *
     * @param target        The object to deserialize the field onto
     * @param fieldMetadata The metadata of the field
     * @param data          The serialized value of the field
     */
    public void deserializeOnto(Object target, FieldMetadata<?, ?> fieldMetadata, PersistedData data) {
        TypeHandler<?> handler = getHandlerFor(fieldMetadata);
        if (handler == null) {
            logger.error("No type handler for type {} used by {}::{}", fieldMetadata.getType(), target.getClass(), fieldMetadata);
        } else {
            try {
                Object deserializedValue = handler.deserializeOrNull(data);
                fieldMetadata.setValue(target, deserializedValue);
            } catch (DeserializationException e) {
                logger.error("Unable to deserialize field '{}' from '{}'", fieldMetadata.getName(), data.toString(), e);
            }
        }
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target  The object to deserialize onto
     * @param values  The collection of values to apply to the object
     */
    public void deserializeOnto(Object target, PersistedDataMap values) {
        deserializeOnto(target, values, DeserializeFieldCheck.NullCheck.newInstance());
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     * @param check  A check to filter which fields to deserialize
     */
    public void deserializeOnto(Object target, PersistedDataMap values, DeserializeFieldCheck check) {
        for (Map.Entry<String, PersistedData> field : values.entrySet()) {
            FieldMetadata<?, ?> fieldInfo = classMetadata.getField(field.getKey());

            if (fieldInfo != null && check.shouldDeserialize(classMetadata, fieldInfo)) {
                deserializeOnto(target, fieldInfo, field.getValue());
            } else if (fieldInfo == null) {
                logger.warn("Cannot deserialize unknown field '{}' onto '{}'", field.getKey(), classMetadata.getId());
            }
        }
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     */
    public void deserializeOnto(Object target, Map<FieldMetadata<?, ?>, PersistedData> values) {
        deserializeOnto(target, values, DeserializeFieldCheck.NullCheck.newInstance());
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     * @param check  A check to filter which fields to deserialize
     */
    public void deserializeOnto(Object target, Map<FieldMetadata<?, ?>, PersistedData> values, DeserializeFieldCheck check) {
        for (Map.Entry<FieldMetadata<?, ?>, PersistedData> field : values.entrySet()) {
            if (check.shouldDeserialize(classMetadata, field.getKey())) {
                deserializeOnto(target, field.getKey(), field.getValue());
            }
        }
    }


}

// Copyright 2022 The Terasology Foundation
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
public class Serializer<C> {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    private final ClassMetadata<C, ?> classMetadata;
    private final Map<FieldMetadata<C, ?>, TypeHandler<?>> fieldHandlers;

    public Serializer(ClassMetadata<C, ? extends FieldMetadata<C, ?>> classMetadata, Map<FieldMetadata<C, ?>, TypeHandler<?>> fieldHandlers) {
        this.fieldHandlers = fieldHandlers;
        this.classMetadata = classMetadata;
    }

    /**
     * @param field The metadata for a field of the type handled by this serializer.
     * @return The TypeHandler for the given field
     */
    public <F> TypeHandler<F> getHandlerFor(FieldMetadata<C, F> field) {
        return (TypeHandler<F>) fieldHandlers.get(field);
    }

    /**
     * Serializes a field of a provided container
     *
     * @param field     The metadata for the field to serialize
     * @param container The object containing the field
     * @param context   The current serialization context
     * @return The serialized value of the field
     */
    public <F> PersistedData serialize(FieldMetadata<C, F> field, C container, PersistedDataSerializer context) {
        F rawValue = field.getValue(container);
        if (rawValue != null) {
            TypeHandler<F> handler = getHandlerFor(field);
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
    public <F> PersistedData serializeValue(FieldMetadata<C, F> fieldMetadata, F rawValue, PersistedDataSerializer context) {
        @SuppressWarnings("unchecked") TypeHandler<F> handler = (TypeHandler<F>) fieldHandlers.get(fieldMetadata);
        return handler.serialize(rawValue, context);
    }

    /**
     * Deserializes a value onto an object
     *
     * @param target        The object to deserialize the field onto
     * @param fieldMetadata The metadata of the field
     * @param data          The serialized value of the field
     */
    public <F> void deserializeOnto(C target, FieldMetadata<C, F> fieldMetadata, PersistedData data) {
        TypeHandler<F> handler = getHandlerFor(fieldMetadata);
        if (handler == null) {
            logger.error("No type handler for type {} used by {}::{}", fieldMetadata.getType(), target.getClass(), fieldMetadata);
        } else {
            try {
                F deserializedValue = handler.deserializeOrNull(data);
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
    public void deserializeOnto(C target, PersistedDataMap values) {
        deserializeOnto(target, values, DeserializeFieldCheck.NullCheck.newInstance());
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     * @param check  A check to filter which fields to deserialize
     */
    public void deserializeOnto(C target, PersistedDataMap values, DeserializeFieldCheck check) {
        values.entrySet().forEach(field -> goomp(target, check, field.getKey(), field.getValue()));
    }

    private void goomp(C target, DeserializeFieldCheck check, String fieldName, PersistedData data) {
        var fieldInfo = classMetadata.getField(fieldName);
        if (fieldInfo != null && check.shouldDeserialize(classMetadata, fieldInfo)) {
            deserializeOnto(target, fieldInfo, data);
        } else if (fieldInfo == null) {
            logger.warn("Cannot deserialize unknown field '{}' onto '{}'", fieldName, classMetadata.getId());
        }
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     */
    public void deserializeOnto(C target, Map<FieldMetadata<C, ?>, PersistedData> values) {
        deserializeOnto(target, values, DeserializeFieldCheck.NullCheck.newInstance());
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     * @param check  A check to filter which fields to deserialize
     */
    public void deserializeOnto(C target, Map<FieldMetadata<C, ?>, PersistedData> values, DeserializeFieldCheck check) {
        values.forEach((field, data) -> {
            if (check.shouldDeserialize(classMetadata, field)) {
                deserializeOnto(target, field, data);
            }
        });
    }


}

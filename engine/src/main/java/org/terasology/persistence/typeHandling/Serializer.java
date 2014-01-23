/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.persistence.typeSerialization.DeserializeFieldCheck;

import java.util.Map;

/**
 * A serializer provides low-level serialization support for a type, using a mapping of type handlers for each field of that type.
 *
 * @author Immortius
 */
public class Serializer {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    private ClassMetadata<?, ?> classMetadata;
    private Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers;

    public Serializer(ClassMetadata<?, ?> classMetadata, Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers) {
        this.fieldHandlers = fieldHandlers;
        this.classMetadata = classMetadata;
    }

    /**
     * @param field The metadata for a field of the type handled by this serializer.
     * @return The TypeHandler for the given field
     */
    @SuppressWarnings("unchecked")
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
    public PersistedData serialize(FieldMetadata<?, ?> field, Object container, SerializationContext context) {
        Object rawValue = field.getValue(container);
        if (rawValue != null) {
            TypeHandler handler = getHandlerFor(field);
            if (handler != null) {
                return handler.serialize(rawValue, context);
            }
        }
        return null;
    }

    /**
     * Serializes the given value, that was originally obtained from the given field.
     * <p/>
     * This is provided for performance, to avoid obtaining the same value twice.
     *
     * @param rawValue The value to serialize
     * @return The serialized value
     */
    @SuppressWarnings("unchecked")
    public PersistedData serializeValue(FieldMetadata<?, ?> fieldMetadata, Object rawValue, SerializationContext context) {
        return fieldHandlers.get(fieldMetadata).serialize(rawValue, context);
    }

    /**
     * Deserializes a value onto an object
     *
     * @param target        The object to deserialize the field onto
     * @param fieldMetadata The metadata of the field
     * @param data          The serialized value of the field
     * @param context       The deserialization context
     */
    public void deserializeOnto(Object target, FieldMetadata<?, ?> fieldMetadata, PersistedData data, DeserializationContext context) {
        TypeHandler<?> handler = getHandlerFor(fieldMetadata);
        Object deserializedValue = handler.deserialize(data, context);
        if (deserializedValue != null) {
            fieldMetadata.setValue(target, deserializedValue);
        }
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target  The object to deserialize onto
     * @param values  The collection of values to apply to the object
     * @param context The deserialization context
     */
    public void deserializeOnto(Object target, PersistedDataMap values, DeserializationContext context) {
        deserializeOnto(target, values, DeserializeFieldCheck.NullCheck.newInstance(), context);
    }

    /**
     * Deserializes a Collection of name-values onto an object
     *
     * @param target The object to deserialize onto
     * @param values The collection of values to apply to the object
     * @param check  A check to filter which fields to deserialize
     */
    public void deserializeOnto(Object target, PersistedDataMap values, DeserializeFieldCheck check, DeserializationContext context) {
        for (Map.Entry<String, PersistedData> field : values.entrySet()) {
            FieldMetadata<?, ?> fieldInfo = classMetadata.getField(field.getKey());

            if (fieldInfo != null && check.shouldDeserialize(classMetadata, fieldInfo)) {
                deserializeOnto(target, fieldInfo, field.getValue(), context);
            } else if (fieldInfo == null) {
                logger.warn("Cannot deserialize unknown field '{}' onto '{}'", field.getKey(), classMetadata.getUri());
            }
        }
    }
}

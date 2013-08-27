/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeSerialization;

import org.terasology.classMetadata.FieldMetadata;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * A serializer provides low-level serialization support for a type, using a mapping of type handlers for each field of that type.
 *
 * @author Immortius
 */
public class Serializer {

    private Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers;

    public Serializer(Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers) {
        this.fieldHandlers = fieldHandlers;
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
     * @return The serialized value of the field
     */
    @SuppressWarnings("unchecked")
    public EntityData.Value serialize(FieldMetadata<?, ?> field, Object container) {
        Object rawValue = field.getValue(container);
        if (rawValue != null) {
            TypeHandler handler = getHandlerFor(field);
            return handler.serialize(rawValue);
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
    public EntityData.Value serializeValue(FieldMetadata<?, ?> fieldMetadata, Object rawValue) {
        return fieldHandlers.get(fieldMetadata).serialize(rawValue);
    }

    /**
     * Serializes the field for the given object
     *
     * @param container The object containing this field
     * @return The Name-Value pair holding this field
     */
    public EntityData.NameValue serializeNameValue(FieldMetadata<?, ?> fieldMetadata, Object container, boolean usingFieldIds) {
        Object rawValue = fieldMetadata.getValue(container);
        if (rawValue == null) {
            return null;
        }

        EntityData.Value value = serializeValue(fieldMetadata, rawValue);
        if (value != null) {
            if (usingFieldIds) {
                return EntityData.NameValue.newBuilder().setNameIndex(fieldMetadata.getId()).setValue(value).build();
            } else {
                return EntityData.NameValue.newBuilder().setName(fieldMetadata.getName()).setValue(value).build();
            }
        }
        return null;
    }

    /**
     * Deserializes a value onto an object
     *
     * @param target        The object to deserialize the field onto
     * @param fieldMetadata The metadata of the field
     * @param value         The serialized value of the field
     */
    public void deserializeOnto(Object target, FieldMetadata<?, ?> fieldMetadata, EntityData.Value value) {
        TypeHandler<?> handler = getHandlerFor(fieldMetadata);
        Object deserializedValue = handler.deserialize(value);
        if (deserializedValue != null) {
            fieldMetadata.setValue(target, deserializedValue);
        }
    }
}

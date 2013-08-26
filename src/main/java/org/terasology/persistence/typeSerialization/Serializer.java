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

import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius
 */
public class Serializer {

    private Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers;

    public Serializer(Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlers) {
        this.fieldHandlers = fieldHandlers;
    }

    @SuppressWarnings("unchecked")
    public TypeHandler<?> getHandlerFor(FieldMetadata<?, ?> field) {
        return fieldHandlers.get(field);
    }

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
     * Serializes the given value, that was originally obtained from this field.
     * <p/>
     * This is provided for performance, to avoid obtaining the same value twice via reflection.
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

    public void deserializeOnto(Object target, FieldMetadata<?, ?> fieldMetadata, EntityData.Value value) {
        TypeHandler<?> handler = getHandlerFor(fieldMetadata);
        Object deserializedValue = handler.deserialize(value);
        if (deserializedValue != null) {
            fieldMetadata.setValue(target, deserializedValue);
        }
    }
}

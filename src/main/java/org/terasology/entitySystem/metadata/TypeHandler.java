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
package org.terasology.entitySystem.metadata;

import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * A type handler provides methods for serializing, deserializing and copying a specific type.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface TypeHandler<T> {
    /**
     * Serializes a single value.
     * This method should return null if the value cannot or should not be serialized. An example would be if value itself is null.
     *
     * @param value The value to serialize - may be null
     * @return The serialized value, or null if the value cannot or should not be serialized.
     */
    EntityData.Value serialize(T value);

    /**
     * Deserializes a single value.
     * @param value The serialized info to deserialize
     * @return The deserialized value - this may be null
     */
    T deserialize(EntityData.Value value);

    /**
     * Creates a copy of the value.  For immutable types or types that should be shared across object, this can be the original value. For a mutable type that should not be
     * shared, this should be a new instance of the value.
     * @param value The value to copy
     * @return A safe to use copy of the value.
     */
    T copy(T value);

    /**
     * Serializes a collection of this type.  This allows for efficiency for types that can be serialized more efficiently in this way, such as primitives
     * @param value The values to serialize
     * @return The serialized values.
     */
    EntityData.Value serialize(Iterable<T> value);

    /**
     * Deserializes a collection of this type.
     * @param value The serialized collection
     * @return A list of the resultant values.
     */
    List<T> deserializeList(EntityData.Value value);
}

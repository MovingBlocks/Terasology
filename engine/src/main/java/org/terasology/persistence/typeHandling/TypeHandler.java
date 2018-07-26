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

/**
 */
public interface TypeHandler<T> {

    /**
     * Serializes a single value.
     * This method should return null if the value cannot or should not be serialized. An example would be if value itself is null.
     *
     * @param value The value to serialize - may be null
     * @param serializer The serializer used to serialize simple values
     * @return The serialized value.
     */
    PersistedData serialize(T value, PersistedDataSerializer serializer);

    /**
     * Deserializes a single value.
     *
     * @param data The persisted data to deserialize from
     * @return The deserialized value.
     * @throws org.terasology.persistence.typeHandling.DeserializationException if there was an error deserializing the data
     */
    T deserialize(PersistedData data);
}

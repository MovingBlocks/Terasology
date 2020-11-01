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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Serializes objects of type {@link T} to and from a {@link PersistedData}.
 */
public abstract class TypeHandler<T> {
    /**
     * Serializes a single non-null value.
     *
     * @param value      The value to serialize - will never be null.
     * @param serializer The serializer used to serialize simple values
     * @return The serialized value.
     */
    protected abstract PersistedData serializeNonNull(T value, PersistedDataSerializer serializer);

    /**
     * Serializes a single value.
     *
     * The default implementation of this method returns {@link PersistedDataSerializer#serializeNull()}
     * if {@code value} is null, otherwise delegates to {@link #serializeNonNull}.
     *
     * @param value The value to serialize - may be null
     * @param serializer The serializer used to serialize simple values
     * @return The serialized value.
     */
    public PersistedData serialize(T value, PersistedDataSerializer serializer) {
        if (value == null) {
            return serializer.serializeNull();
        }

        return serializeNonNull(value, serializer);
    }

    /**
     * Deserializes a single value to the type {@link T}.
     *
     * @param data The persisted data to deserialize from.
     * @return The deserialized value. {@link Optional#empty()} if the value could not be deserialized.
     */
    public abstract Optional<T> deserialize(PersistedData data);

    /**
     * Deserializes a single value to the type {@link T}. If the type was not serialized
     * (i.e. {@link #deserialize(PersistedData)} returned {@link Optional#empty()}), null is returned.
     *
     * @param data The persisted data to deserialize from.
     * @return The deserialized value. {@code null} if the value could not be deserialized.
     */
    public final T deserializeOrNull(PersistedData data) {
        return deserialize(data).orElse(null);
    }

    /**
     * Deserializes a single value to the type {@link T}. If the type was not serialized
     * (i.e. {@link #deserialize(PersistedData)} returned {@link Optional#empty()}), the value retrieved
     * from the {@link Supplier} is returned.
     *
     * @param data     The persisted data to deserialize from.
     * @param supplier The {@link Supplier} from which to retrieve the value to be returned if
     *                 {@code data} could not be deserialized.
     * @return The deserialized value. If the value could not be deserialized, the value returned by
     * {@code supplier.get()} is returned.
     */
    public final T deserializeOrGet(PersistedData data, Supplier<T> supplier) {
        return deserialize(data).orElseGet(supplier);
    }

    /**
     * Deserializes a single value to the type {@link T}. If the type was not serialized
     * (i.e. {@link #deserialize(PersistedData)} returned {@link Optional#empty()}), a
     * {@link DeserializationException} is thrown.
     *
     * @param data The persisted data to deserialize from.
     * @return The deserialized value.
     * @throws DeserializationException if {@code data} could not be deserialized to a value of type {@link T}.
     */
    public final T deserializeOrThrow(PersistedData data) throws DeserializationException {
        return deserializeOrThrow(data, "Unable to deserialize " + data);
    }

    /**
     * Deserializes a single value to the type {@link T}. If the type was not serialized
     * (i.e. {@link #deserialize(PersistedData)} returned {@link Optional#empty()}), a
     * {@link DeserializationException} is thrown.
     *
     * @param data The persisted data to deserialize from.
     * @param errorMessage The error message to use if the value could not be deserialized.
     * @return The deserialized value.
     * @throws DeserializationException if {@code data} could not be deserialized to a value of type {@link T}.
     */
    public final T deserializeOrThrow(PersistedData data, String errorMessage) throws DeserializationException {
        return deserialize(data)
                .orElseThrow(() -> new DeserializationException(errorMessage));
    }
}

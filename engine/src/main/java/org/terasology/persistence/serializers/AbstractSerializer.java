/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.persistence.serializers;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

/**
 * The abstract class that all serializers derive from. It by default provides the ability to
 * serialize/deserialize an object to/from a {@link PersistedData} using the given
 * {@link PersistedDataSerializer}.
 * <p>
 * Implementors simply need to specify the type of {@link PersistedDataSerializer} to use
 * and can provide convenience methods that use {@link #serialize(Object, TypeInfo)} and
 * {@link #deserialize(PersistedData, TypeInfo)}.
 */
public abstract class AbstractSerializer {
    protected final TypeHandlerLibrary typeHandlerLibrary;
    protected final PersistedDataSerializer persistedDataSerializer;

    protected AbstractSerializer(TypeHandlerLibrary typeHandlerLibrary, PersistedDataSerializer persistedDataSerializer) {
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.persistedDataSerializer = persistedDataSerializer;
    }

    /**
     * Serializes the given object to a {@link PersistedData} using the stored
     * {@link #persistedDataSerializer} by loading a
     * {@link org.terasology.persistence.typeHandling.TypeHandler TypeHandler} from the
     * {@link #typeHandlerLibrary}.
     *
     * @param object   The object to serialize.
     * @param typeInfo A {@link TypeInfo} specifying the type of the object to serialize.
     * @param <T>      The type of the object to serialize.
     * @return A {@link PersistedData}, if the serialization was successful. Serialization
     * usually fails only because an appropriate type handler could not be found for the
     * given type.
     */
    public <T> Optional<PersistedData> serialize(T object, TypeInfo<T> typeInfo) {
        return typeHandlerLibrary.getTypeHandler(typeInfo)
                .map(typeHandler -> typeHandler.serialize(object, persistedDataSerializer));
    }

    /**
     * Deserializes an object of the given type from a {@link PersistedData} using the stored
     * {@link #persistedDataSerializer} by loading a
     * {@link org.terasology.persistence.typeHandling.TypeHandler TypeHandler} from the
     * {@link #typeHandlerLibrary}.
     *
     * @param data     The {@link PersistedData} containing the serialized representation of the object.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}, if the deserialization was successful.
     * Deserialization usually fails when an appropriate type handler could not be found for the
     * type {@link T} <i>or</i> if the serialized object representation in {@code data} does
     * not represent an object of type {@link T}.
     */
    public <T> Optional<T> deserialize(PersistedData data, TypeInfo<T> typeInfo) {
        return typeHandlerLibrary.getTypeHandler(typeInfo).flatMap(typeHandler -> typeHandler.deserialize(data));
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * The abstract class that all serializers derive from. It by default provides the ability to serialize/deserialize an
 * object to/from a {@link PersistedData} using the given {@link PersistedDataSerializer}.
 * <p>
 * Implementors simply need to specify the type of {@link PersistedDataSerializer} to use and can provide convenience
 * methods that use {@link #serializeToPersisted(Object, TypeInfo)} and {@link #deserializeFromPersisted(PersistedData,
 * TypeInfo)}.
 */
public final class Serializer<D extends PersistedData> {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    private final TypeHandlerLibrary typeHandlerLibrary;
    private final PersistedDataSerializer persistedDataSerializer;
    private final PersistedDataWriter<D> writer;
    private final PersistedDataReader<D> reader;

    public Serializer(TypeHandlerLibrary typeHandlerLibrary, PersistedDataSerializer persistedDataSerializer,
                         PersistedDataWriter<D> writer, PersistedDataReader<D> reader) {
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.persistedDataSerializer = persistedDataSerializer;
        this.writer = writer;
        this.reader = reader;
    }

    /**
     * Serializes the given object to a {@link PersistedData} using the stored {@link #persistedDataSerializer} by
     * loading a {@link org.terasology.persistence.typeHandling.TypeHandler TypeHandler} from the {@link
     * #typeHandlerLibrary}.
     *
     * @param object The object to serialize.
     * @param typeInfo A {@link TypeInfo} specifying the type of the object to serialize.
     * @param <T> The type of the object to serialize.
     * @return A {@link PersistedData}, if the serialization was successful. Serialization usually fails only because an
     *         appropriate type handler could not be found for the given type.
     */
    private <T> Optional<D> serializeToPersisted(T object, TypeInfo<T> typeInfo) {
        return typeHandlerLibrary.getTypeHandler(typeInfo)
                .map(typeHandler -> (D) typeHandler.serialize(object, persistedDataSerializer));
    }

    /**
     * Deserializes an object of the given type from a {@link PersistedData} using the stored {@link
     * #persistedDataSerializer} by loading a {@link org.terasology.persistence.typeHandling.TypeHandler TypeHandler}
     * from the {@link #typeHandlerLibrary}.
     *
     * @param data The {@link PersistedData} containing the serialized representation of the object.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T> The type to deserialize the object as.
     * @return The deserialized object of type {@link T}, if the deserialization was successful. Deserialization usually
     *         fails when an appropriate type handler could not be found for the type {@link T} <i>or</i> if the
     *         serialized object representation in {@code data} does not represent an object of type {@link T}.
     */
    private <T> Optional<T> deserializeFromPersisted(D data, TypeInfo<T> typeInfo) {
        return typeHandlerLibrary.getTypeHandler(typeInfo).flatMap(typeHandler -> typeHandler.deserialize(data));
    }

    /**
     * Deserialize an object of type from {@link InputStream}
     *
     * @param type The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param inputStream Tbe {@link InputStream} containing data for deserialization.
     * @param <T> The type to deserialize the object as.
     * @return an Object if deserialization is success, empty otherwise
     */
    public <T> Optional<T> deserialize(TypeInfo<T> type, InputStream inputStream) {
        try {
            D persistedData = reader.read(inputStream);
            return deserializeFromPersisted(persistedData, type);
        } catch (IOException e) {
            logger.error("Cannot deserialize type [{}]", type, e);
        }
        return Optional.empty();
    }

    /**
     * Deserialize an object of type from {@link InputStream}
     *
     * @param type The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param bytes Tbe ByteArray containing data for deserialization.
     * @param <T> The type to deserialize the object as.
     * @return an Object if deserialization is success, empty otherwise
     */
    public <T> Optional<T> deserialize(TypeInfo<T> type, byte[] bytes) {
        try {
            D persistedData = reader.read(bytes);
            return deserializeFromPersisted(persistedData, type);
        } catch (IOException e) {
            logger.error("Cannot deserialize type [{}]", type, e);
        }
        return Optional.empty();
    }

    /**
     * Serializes the given object to a ByteArray
     *
     * @param object The object to serialize.
     * @param type A {@link TypeInfo} specifying the type of the object to serialize.
     * @param <T> The type of the object to serialize.
     * @return A ByteArray, if the serialization was successful. Serialization usually fails only because an appropriate
     *         type handler could not be found for the given type.
     */
    public <T> Optional<byte[]> serialize(T object, TypeInfo<T> type) {
        Optional<D> persistedData = serializeToPersisted(object, type);
        if (persistedData.isPresent()) {
            return Optional.of(writer.writeBytes(persistedData.get()));
        } else {
            logger.error("Cannot serialize [{}]", type);
            return Optional.empty();
        }
    }

    /**
     * Serializes and write the given object to  {@link OutputStream}
     *
     * @param object The object to serialize.
     * @param type A {@link TypeInfo} specifying the type of the object to serialize.
     * @param outputStream A {@link OutputStream} which will used for writing.
     * @param <T> The type of the object to serialize.
     */
    public <T> void serialize(T object, TypeInfo<T> type, OutputStream outputStream) {
        Optional<D> persistedData = serializeToPersisted(object, type);
        if (persistedData.isPresent()) {
            try {
                writer.writeTo(persistedData.get(), outputStream);
            } catch (IOException e) {
                logger.error("Cannot serialize [{}]", type, e);
            }
        } else {
            logger.error("Cannot serialize [{}]", type);
        }
    }
}

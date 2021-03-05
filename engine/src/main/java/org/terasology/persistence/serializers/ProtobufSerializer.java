// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.serializers;

import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedData;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
import org.terasology.persistence.serializers.AbstractSerializer;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.TypeInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * {@link ProtobufSerializer} provides the ability to serialize and deserialize objects to and
 * from a binary format using Protobuf.
 */
public class ProtobufSerializer extends AbstractSerializer {
    /**
     * Constructs a new {@link ProtobufSerializer} using the given {@link TypeHandlerLibrary}.
     */
    public ProtobufSerializer(TypeHandlerLibrary typeHandlerLibrary) {
        super(typeHandlerLibrary, new ProtobufPersistedDataSerializer());
    }

    /**
     * Serializes the given object to bytes and returns the serialized byte array.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param <T>      The type of the object to be serialized.
     * @return The serialized byte array.
     * @throws SerializationException Thrown when serialization fails.
     * @throws IOException            Thrown if there was an error creating the
     *                                {@link ByteArrayOutputStream}.
     * @see #writeBytes(Object, TypeInfo, OutputStream)
     */
    public <T> byte[] toBytes(T object, TypeInfo<T> typeInfo)
            throws SerializationException, IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            writeBytes(object, typeInfo, stream);
            return stream.toByteArray();
        }
    }

    /**
     * Serializes the given object to bytes and writes the bytes to the file with the given name.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param fileName The name of the file to write the serialized bytes to.
     * @param <T>      The type of the object to be serialized.
     * @throws SerializationException Thrown when serialization fails.
     * @throws IOException            Thrown if there was an error writing the bytes to the file.
     * @see #writeBytes(Object, TypeInfo, File)
     */
    public <T> void writeBytes(T object, TypeInfo<T> typeInfo, String fileName)
            throws SerializationException, IOException {
        writeBytes(object, typeInfo, new File(fileName));
    }

    /**
     * Serializes the given object to bytes and writes the bytes to the {@link File}
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param file     The {@link File} to write the serialized bytes to.
     * @param <T>      The type of the object to be serialized.
     * @throws SerializationException Thrown when serialization fails.
     * @throws IOException            Thrown if there was an error writing the bytes to the file.
     * @see #writeBytes(Object, TypeInfo, File)
     */
    public <T> void writeBytes(T object, TypeInfo<T> typeInfo, File file)
            throws SerializationException, IOException {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            writeBytes(object, typeInfo, stream);
        }
    }

    /**
     * Serializes the given object to bytes and writes the bytes to the {@link OutputStream}.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param stream   The {@link OutputStream} to write the serialized bytes to.
     * @param <T>      The type of the object to be serialized.
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeBytes(Object, TypeInfo, File)
     */
    public <T> void writeBytes(T object, TypeInfo<T> typeInfo, OutputStream stream)
            throws SerializationException {
        Optional<PersistedData> serialized = this.serialize(object, typeInfo);

        if (!serialized.isPresent()) {
            throw new SerializationException("Could not find a TypeHandler for the type " + typeInfo);
        }

        ProtobufPersistedData persistedData = (ProtobufPersistedData) serialized.get();

        try {
            persistedData.getValue().writeDelimitedTo(stream);
        } catch (IOException e) {
            throw new SerializationException("Could not write bytes to stream", e);
        }
    }

    /**
     * Deserializes an object of type {@link T} from the bytes in the {@link InputStream}.
     *
     * @param stream   The {@link InputStream} that contains the bytes to be deserialized.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws SerializationException Thrown if the deserialization fails.
     */
    public <T> T fromBytes(InputStream stream, TypeInfo<T> typeInfo) throws SerializationException {
        EntityData.Value value;

        try {
            value = EntityData.Value.parseDelimitedFrom(stream);
        } catch (IOException e) {
            throw new SerializationException("Could not parse bytes from Stream", e);
        }

        Optional<T> deserialized = this.deserialize(new ProtobufPersistedData(value), typeInfo);

        if (!deserialized.isPresent()) {
            throw new SerializationException("Could not deserialize object of type " + typeInfo);
        }

        return deserialized.get();
    }

    /**
     * Deserializes an object of type {@link T} from the bytes in the {@link File}.
     *
     * @param file     The {@link File} that contains the bytes to be deserialized.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws IOException            Thrown if there was an error reading from the file.
     * @throws SerializationException Thrown if the deserialization fails.
     * @see #fromBytes(InputStream, TypeInfo)
     */
    public <T> T fromBytes(File file, TypeInfo<T> typeInfo) throws IOException, SerializationException {
        try (InputStream stream = new FileInputStream(file)) {
            return fromBytes(stream, typeInfo);
        }
    }

    /**
     * Deserializes an object of type {@link T} from the given bytes.
     *
     * @param bytes    The bytes to be deserialized.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws IOException            Thrown if there was an error creating a {@link ByteArrayInputStream}.
     * @throws SerializationException Thrown if the deserialization fails.
     * @see #fromBytes(InputStream, TypeInfo)
     */
    public <T> T fromBytes(byte[] bytes, TypeInfo<T> typeInfo) throws IOException, SerializationException {
        try (InputStream reader = new ByteArrayInputStream(bytes)) {
            return fromBytes(reader, typeInfo);
        }
    }
}

/*
 * Copyright 2018 MovingBlocks
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
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.protobuf.ProtobufPersistedData;
import org.terasology.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
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
 * ProtobufSerializer provides the ability to serialize and deserialize between objects and bytes
 * <p>
 * Serialized bytes can be forwarded/written to various output types
 * <p>
 * Various input types of serialized bytes can be deserialized and returned as PersistedData objects
 */
public class ProtobufSerializer extends AbstractSerializer {
    public ProtobufSerializer(TypeHandlerLibrary typeHandlerLibrary) {
        super(typeHandlerLibrary, new ProtobufPersistedDataSerializer());
    }

    /**
     * Converts the object into an array of bytes and forwards it to {@link #writeBytes(Object, TypeInfo, OutputStream)}
     *
     * @param object      the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @return the byte array of the object
     * @throws IOException throws if there is an error writing to the stream
     */
    public <T> byte[] toBytes(T object, TypeInfo<T> typeInfo) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            writeBytes(object, typeInfo, stream);
            return stream.toByteArray();
        }
    }

    /**
     * Takes a string path of a file and writes the serialized bytes into that file
     *
     * @param object      the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param fileName    the path of the file in which it is being written to
     * @throws IOException gets thrown if there is an error writing to the file
     * @see #writeBytes(Object, TypeInfo, File)
     */
    public <T> void writeBytes(T object, TypeInfo<T> typeInfo, String fileName)
            throws SerializationException, IOException {
        writeBytes(object, typeInfo, new File(fileName));
    }

    /**
     * Writes an object's bytes to a file.
     *
     * @param object      the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param file        file that the bytes will be written to
     * @throws IOException gets thrown if there is an error writing to the file
     * @see #writeBytes(Object, TypeInfo, OutputStream)
     */
    public <T> void writeBytes(T object, TypeInfo<T> typeInfo, File file)
            throws SerializationException, IOException {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            writeBytes(object, typeInfo, stream);
        }
    }

    /**
     * Writes a given object to an OutputStream using protobuf and TypeHandler
     * serialization
     *
     * @param object      the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param stream      stream that the bytes will be written to
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
     * Gets the PersistedData from a byte stream (InputStream).
     *
     * @param stream InputStream that will be deserialized
     * @return deserialized ProtobufPersistedData object
     * @throws SerializationException if there is an issue parsing the stream
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
     * Gets the PersistedData from a File.
     *
     * @param file contains the bytes that will be deserialized
     * @return deserialized ProtobufPersistedData object
     * @throws IOException gets thrown if there is an issue reading the file
     * @see #fromBytes(InputStream, TypeInfo)
     */
    public <T> T fromBytes(File file, TypeInfo<T> typeInfo) throws IOException, SerializationException {
        try (InputStream stream = new FileInputStream(file)) {
            return fromBytes(stream, typeInfo);
        }
    }

    /**
     * Gets the PersistedData from an array of bytes.
     *
     * @param bytes array of bytes to be deserialized
     * @return deserialized ProtobufData object
     * @throws IOException gets thrown if there is an issue creating the InputStream
     * @see #fromBytes(InputStream, TypeInfo)
     */
    public <T> T fromBytes(byte[] bytes, TypeInfo<T> typeInfo) throws IOException {
        try (InputStream reader = new ByteArrayInputStream(bytes)) {
            return fromBytes(reader, typeInfo);
        }
    }
}

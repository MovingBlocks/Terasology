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
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.protobuf.ProtobufPersistedData;
import org.terasology.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
import org.terasology.protobuf.EntityData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProtobufSerializer {
	/**
	 * Converts the object into an array of bytes.
	 * @see writeBytes(T, TypeHandler<T>, OutputStream) writes the serialized bytes into an OutputStream instead of a byte array
	 * @see writeBytes(T, TypeHandler<T>, File) writes the serialized bytes into a File instead of a byte array
	 * @param object the object to be serialized
	 * @param typeHandler contains how the object will be serialized
	 * @return the byte array of the object
	 * @throws IOException throws if there is an error writing to the stream
	 */
    public <T> byte[] toBytes(T object, TypeHandler<T> typeHandler) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            writeBytes(object, typeHandler, stream);
            return stream.toByteArray();
        }
    }

    /**
     * Writes an object's bytes to a file.
     * @see toBytes(T, TypeHandler<T>) writes the serialized bytes into a byte array instead of a File
     * @see writeBytes(T, TypeHandler<T>, OutputStream) writes the serialized bytes into an OutputStream instead of a File
     * @param object the object to be serialized
     * @param typeHandler contains how the object will be serialized
     * @param file file that the bytes will be written to
     * @throws IOException gets thrown if there is an error writing to the file
     */
    public <T> void writeBytes(T object, TypeHandler<T> typeHandler, File file) throws IOException {
        writeBytes(object, typeHandler, new FileOutputStream(file));
    }

    /**
     * Writes an object's bytes to a stream.
     * @see toBytes(T, TypeHandler<T>) writes the serialized bytes into a byte array instead of an OutputStream
     * @see writeBytes(T, TypeHandler<T>, File) writes serialized bytes to a File instead of an OutputStream
     * @param object the object to be serialized
     * @param typeHandler contains how the object will be serialized
     * @param stream stream that the bytes will be written to
     * @throws IOException will be thrown if there is an error writing to the stream
     */
    public <T> void writeBytes(T object, TypeHandler<T> typeHandler, OutputStream stream) throws IOException {
        ProtobufPersistedData persistedData =
            (ProtobufPersistedData) typeHandler.serialize(object, new ProtobufPersistedDataSerializer());

        persistedData.getValue().writeDelimitedTo(stream);
    }

    /**
     * Gets the PersistedData from a byte stream (InputStream).
     * @see persistedDatafromBytes(File) gets serialized bytes from a File instead of an InputStream
     * @see persistedDatafromBytes(byte[]) gets serialized bytes from a byte array instead of an InputStream
     * @param stream InputStream that will be deserialized
     * @return deserialized ProtobufPersistedData object
     * @throws IOException if there is an issue parsing the stream
     */
    public PersistedData persistedDatafromBytes(InputStream stream) throws IOException {
        EntityData.Value value = EntityData.Value.parseDelimitedFrom(stream);

        return new ProtobufPersistedData(value);
    }

    /**
     * Gets the PersistedData from a File.
     * @see persistedDatafromBytes(InputStream) gets serialized bytes from an InputStream instead of File
     * @see persistedDatafromBytes(byte[]) gets serialized bytes from a byte array instead of File
     * @param file contains the bytes that will be deserialized
     * @return deserialized ProtobufPersistedData object
     * @throws IOException gets thrown if there is an issue reading the file
     */
    public PersistedData persistedDatafromBytes(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return persistedDatafromBytes(stream);
        }
    }

    /**
     * Gets the PersistedData from an array of bytes.
     * @see persistedDatafromBytes(InputStream) gets serialized bytes from an InputStream instead of a byte array 
     * @see persistedDatafromBytes(File) gets serialized bytes from a File instead of a byte array
     * @param bytes array of bytes to be deserialized
     * @return deserialized ProtobufData object
     * @throws IOException gets thrown if there is an issue creating the InputStream
     */
    public PersistedData persistedDatafromBytes(byte[] bytes) throws IOException {
        try (InputStream reader = new ByteArrayInputStream(bytes)) {
            return persistedDatafromBytes(reader);
        }
    }
}

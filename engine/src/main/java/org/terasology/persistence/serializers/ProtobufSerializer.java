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

// TODO: Document
public class ProtobufSerializer {
    public <T> byte[] toBytes(T object, TypeHandler<T> typeHandler) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            writeBytes(object, typeHandler, stream);
            return stream.toByteArray();
        }
    }

    public <T> void writeBytes(T object, TypeHandler<T> typeHandler, File file) throws IOException {
        writeBytes(object, typeHandler, new FileOutputStream(file));
    }

    public <T> void writeBytes(T object, TypeHandler<T> typeHandler, OutputStream stream) throws IOException {
        ProtobufPersistedData persistedData =
            (ProtobufPersistedData) typeHandler.serialize(object, new ProtobufPersistedDataSerializer());

        persistedData.getValue().writeDelimitedTo(stream);
    }

    public PersistedData persistedDatafromBytes(InputStream stream) throws IOException {
        EntityData.Value value = EntityData.Value.parseDelimitedFrom(stream);

        return new ProtobufPersistedData(value);
    }

    public PersistedData persistedDatafromBytes(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return persistedDatafromBytes(stream);
        }
    }

    public PersistedData persistedDatafromBytes(byte[] bytes) throws IOException {
        try (InputStream reader = new ByteArrayInputStream(bytes)) {
            return persistedDatafromBytes(reader);
        }
    }
}

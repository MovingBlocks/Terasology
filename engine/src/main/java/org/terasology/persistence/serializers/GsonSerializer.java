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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.gson.GsonPersistedData;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.reflection.TypeInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;

/**
 * GsonSerializer provides the ability to serialize and deserialize objects to and from JSON
 * <br>
 * Serialized JSON can be forwarded/written to various output types <br>
 * <br>
 * Various input types can be deserialized and returned as PersistedData types
 */
public class GsonSerializer extends AbstractSerializer {
    private Gson gson;

    /**
     * Constructs a new GsonSerializer object
     */
    public GsonSerializer(TypeHandlerLibrary typeHandlerLibrary) {
        super(typeHandlerLibrary, new GsonPersistedDataSerializer());

        this.gson = new Gson();
    }

    /**
     * Writes the serialized persisted data as a JSON to {@link Writer} and returns the
     * JSON as a string.
     *
     * @param object   the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @return contents of the JSON as a string
     * @throws SerializationException Thrown if serialization fails.
     * @see #writeJson(Object, TypeInfo, Writer)
     */
    public <T> String toJson(T object, TypeInfo<T> typeInfo) throws SerializationException {
        StringWriter writer = new StringWriter();

        writeJson(object, typeInfo, writer);

        return writer.toString();
    }

    /**
     * Writes an object's serialized persisted data to the {@link Writer} as JSON.
     *
     * @param object   the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param writer   The writer in which the JSON will be written to
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeJson(Object, TypeInfo, OutputStream)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, Writer writer) throws SerializationException {
        Optional<PersistedData> serialized = this.serialize(object, typeInfo);

        if (!serialized.isPresent()) {
            throw new SerializationException("Could not find a TypeHandler for the type " + typeInfo);
        }

        GsonPersistedData persistedData = (GsonPersistedData) serialized.get();

        try {
            gson.toJson(persistedData.getElement(), writer);
        } catch (JsonIOException e) {
            throw new SerializationException("Could not write JSON to writer", e);
        }
    }

    /**
     * Writes an object's serialized persisted data to the {@link OutputStream} as JSON.
     *
     * @param object   the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param stream   stream that the data will be written to
     * @throws IOException            will be thrown if there is an error writing to the stream
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeJson(Object, TypeInfo, Writer)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, OutputStream stream) throws IOException, SerializationException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            writeJson(object, typeInfo, writer);
        }
    }

    /**
     * Writes the object's persisted data to the {@link File} as JSON.
     *
     * @param object   the file to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param file     file that the bytes will be written to
     * @throws IOException            gets thrown if there is an issue writing to the file.
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeJson(Object, TypeInfo, Writer)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, File file) throws IOException, SerializationException {
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writeJson(object, typeInfo, writer);
        }
    }

    /**
     * Writes an object's persisted data to {@link File} of a specified file name as JSON
     *
     * @param object   the object to be serialized
     * @param typeInfo contains how the object will be serialized
     * @param fileName the file name where the JSON will be written
     * @throws IOException gets thrown if there is an error writing to the file at the specified location
     * @see #writeJson(Object, TypeInfo, File)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, String fileName) throws IOException {
        writeJson(object, typeInfo, new File(fileName));
    }

    /**
     * Gets the PersistedData from the {@link Reader}'s contents.
     *
     * @param reader Reader object that contains the contents that will be deserialized
     * @return deserialized GsonPersistedData object
     * @see #persistedDataFromJson(InputStream)
     */
    public PersistedData persistedDataFromJson(Reader reader) {
        JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

        return new GsonPersistedData(jsonElement);
    }

    /**
     * Gets the PersistedData from an {@link InputStream}'s contents.
     *
     * @param stream Contents of the InputStream will be serialized
     * @return deserialized GsonPersistedData object
     * @throws IOException if there is an issue parsing the stream
     * @see #persistedDataFromJson(Reader)
     */
    public PersistedData persistedDataFromJson(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return persistedDataFromJson(reader);
        }
    }

    /**
     * Gets the PersistedData from a {@link File} object's contents.
     *
     * @param file File object containing the JSON that will be deserialized
     * @return deserialized GsonPersistedData object
     * @throws IOException gets thrown if there is an issue reading the File object
     * @see #persistedDataFromJson(String)
     */
    public PersistedData persistedDataFromJson(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return persistedDataFromJson(reader);
        }
    }

    /**
     * Gets the PersistedData from a {@link String}'s contents.
     *
     * @param json the String that will be deserialized
     * @return deserialized GsonPersistedData Object
     * @see #persistedDataFromJson(Reader)
     */
    public PersistedData persistedDataFromJson(String json) {
        try (StringReader reader = new StringReader(json)) {
            return persistedDataFromJson(reader);
        }
    }
}

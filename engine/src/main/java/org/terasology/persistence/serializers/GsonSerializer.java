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
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.gson.GsonPersistedData;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;

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

/**
 * GsonSerializer provides the ability to serialize and deserialize objects to and from JSON
 * <br>
 * Serialized JSON can be forwarded/written to various output types <br>
 * <br>
 * Various input types can be deserialized and returned as PersistedData types
 *
 */
public class GsonSerializer {
    private Gson gson;

    /**
     * Constructs a new GsonSerializer object
     */
    public GsonSerializer() {
        this.gson = new Gson();
    }

    /**
     * Writes the serialized persisted data as a JSON to {@link Writer} and returns the JSON as a string.
     * 
     * @param object      the object to be serialized
     * @param typeHandler contains how the object will be serialized
     * @return contents of the JSON as a string
     */
    public <T> String toJson(T object, TypeHandler<T> typeHandler) {
        StringWriter writer = new StringWriter();

        writeJson(object, typeHandler, writer);

        return writer.toString();
    }

    /**
     * Writes an object's serialized persisted data to the {@link Writer} as JSON. 
     * 
     * @see #writeJson(Object, TypeHandler, OutputStream)
     * @param object the object to be serialized
     * @param typeHandler contains how the object will be serialized
     * @param writer The writer in which the JSON will be written to
     */
    public <T> void writeJson(T object, TypeHandler<T> typeHandler, Writer writer) {
        GsonPersistedData persistedData = (GsonPersistedData) typeHandler.serialize(object,
                new GsonPersistedDataSerializer());

        gson.toJson(persistedData.getElement(), writer);
    }

    /**
     * Writes an object's serialized persisted data to the {@link OutputStream} as JSON.
     * 
     * @see #writeJson(Object, TypeHandler, Writer)
     * @param object the object to be serialized
     * @param typeHandler contains how the object will be serialized
     * @param stream stream that the data will be written to
     * @throws IOException will be thrown if there is an error writing to the stream
     */
    public <T> void writeJson(T object, TypeHandler<T> typeHandler, OutputStream stream) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            writeJson(object, typeHandler, writer);
        }
    }

    /**
     * Writes the object's persisted data to the {@link File} as JSON.
     * 
     * @see #writeJson(Object, TypeHandler, String)
     * @param object the file to be serialized
     * @param typeHandler contains how the object will be serialized
     * @param file file that the bytes will be written to
     * @throws IOException gets thrown if there is an issue writing to the file
     */
    public <T> void writeJson(T object, TypeHandler<T> typeHandler, File file) throws IOException {
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writeJson(object, typeHandler, writer);
        }
    }

    /**
     * Writes an object's persisted data to {@link File} of a specified file name as JSON
     * 
     * @see #writeJson(Object, TypeHandler, String)
     * @param object the object to be serialized
     * @param typeHandler contains how the object will be serialized
     * @param fileName the file name where the JSON will be written
     * @throws IOException gets thrown if there is an error writing to the file at the specified location
     */
    public <T> void writeJson(T object, TypeHandler<T> typeHandler, String fileName) throws IOException {
        writeJson(object, typeHandler, new File(fileName));
    }

    /**
     * Gets the PersistedData from the {@link Reader}'s contents.
     * 
     * @see #persistedDataFromJson(InputStream)
     * @param reader Reader object that contains the contents that will be deserialized
     * @return deserialized GsonPersistedData object
     */
    public PersistedData persistedDataFromJson(Reader reader) {
        JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

        return new GsonPersistedData(jsonElement);
    }

    /**
     * Gets the PersistedData from an {@link InputStream}'s contents.
     * 
     * @see #persistedDataFromJson(Reader)
     * @param stream Contents of the InputStream will be serialized
     * @return deserialized GsonPersistedData object
     * @throws IOException if there is an issue parsing the stream
     */
    public PersistedData persistedDataFromJson(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return persistedDataFromJson(reader);
        }
    }

    /**
     * Gets the PersistedData from a {@link File} object's contents.
     * 
     * @see #persistedDataFromJson(String)
     * @param file File object containing the JSON that will be deserialized
     * @return deserialized GsonPersistedData object
     * @throws IOException gets thrown if there is an issue reading the File object
     */
    public PersistedData persistedDataFromJson(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return persistedDataFromJson(reader);
        }
    }

    /**
     * Gets the PersistedData from a {@link String}'s contents.
     * 
     * @see #persistedDataFromJson(Reader)
     * @param json the String that will be deserialized
     * @return deserialized GsonPersistedData Object
     */
    public PersistedData persistedDataFromJson(String json) {
        try (StringReader reader = new StringReader(json)) {
            return persistedDataFromJson(reader);
        }
    }
}

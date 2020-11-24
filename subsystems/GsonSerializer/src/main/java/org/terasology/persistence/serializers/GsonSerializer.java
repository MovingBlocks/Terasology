// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
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
 * {@link GsonSerializer} provides the ability to serialize and deserialize objects to and from JSON.
 */
public class GsonSerializer extends AbstractSerializer {
    private Gson gson;

    /**
     * Constructs a new {@link GsonSerializer} object with the given {@link TypeHandlerLibrary}.
     */
    public GsonSerializer(TypeHandlerLibrary typeHandlerLibrary) {
        super(typeHandlerLibrary, new GsonPersistedDataSerializer());

        this.gson = new Gson();
    }

    /**
     * Serializes the given object to JSON and returns the serialized JSON as a {@link String}.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param <T>      The type of the object to be serialized.
     * @return The serialized JSON as a {@link String}.
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeJson(Object, TypeInfo, Writer)
     */
    public <T> String toJson(T object, TypeInfo<T> typeInfo) throws SerializationException {
        StringWriter writer = new StringWriter();

        writeJson(object, typeInfo, writer);

        return writer.toString();
    }

    /**
     * Serializes the given object to JSON and writes the serialized JSON object to the
     * {@link Writer}.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param writer   The {@link Writer} to which the JSON will be written.
     * @param <T>      The type of the object to be serialized.
     * @throws SerializationException Thrown when serialization fails.
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
     * Serializes the given object to JSON and writes the serialized JSON object to the
     * {@link OutputStream}.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param stream   The {@link OutputStream} to which the JSON will be written.
     * @param <T>      The type of the object to be serialized.
     * @throws SerializationException Thrown when serialization fails.
     * @throws IOException            Thrown if there is an error in writing to the
     *                                {@link OutputStream}.
     * @see #writeJson(Object, TypeInfo, Writer)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, OutputStream stream)
            throws IOException, SerializationException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            writeJson(object, typeInfo, writer);
        }
    }

    /**
     * Serializes the given object to JSON and writes the serialized JSON object to the
     * {@link File}.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param file     The {@link File} that the JSON will be written to.
     * @param <T>      The type of the object to be serialized.
     * @throws IOException            Thrown if there is an issue writing to the file.
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeJson(Object, TypeInfo, Writer)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, File file)
            throws IOException, SerializationException {
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writeJson(object, typeInfo, writer);
        }
    }

    /**
     * Serializes the given object to JSON and writes the serialized JSON object to the
     * file with the given file name.
     *
     * @param object   The object to be serialized.
     * @param typeInfo The {@link TypeInfo} specifying the type of the object to be serialized.
     * @param fileName The name of the file that the JSON will be written to.
     * @param <T>      The type of the object to be serialized.
     * @throws IOException            Thrown if there is an issue writing to the file.
     * @throws SerializationException Thrown when serialization fails.
     * @see #writeJson(Object, TypeInfo, File)
     */
    public <T> void writeJson(T object, TypeInfo<T> typeInfo, String fileName)
            throws IOException, SerializationException {
        writeJson(object, typeInfo, new File(fileName));
    }

    /**
     * Deserializes an object of type {@link T} from the JSON in the {@link Reader}.
     *
     * @param reader   The {@link Reader} that contains the JSON to be deserialized.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws SerializationException Thrown if the deserialization fails.
     */
    public <T> T fromJson(Reader reader, TypeInfo<T> typeInfo) throws SerializationException {
        JsonElement jsonElement;

        try {
            jsonElement = gson.fromJson(reader, JsonElement.class);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new SerializationException("Could not read JSON from reader", e);
        }

        Optional<T> deserialized = deserialize(new GsonPersistedData(jsonElement), typeInfo);

        if (!deserialized.isPresent()) {
            throw new SerializationException("Could not deserialize object of type " + typeInfo);
        }

        return deserialized.get();
    }

    /**
     * Deserializes an object of type {@link T} from the JSON in the {@link InputStream}.
     *
     * @param stream   The {@link InputStream} containing the serialized JSON.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws IOException            Thrown if there is an issue reading from the {@link InputStream}.
     * @throws SerializationException Thrown if the deserialization fails.
     * @see #fromJson(Reader, TypeInfo)
     */
    public <T> T fromJson(InputStream stream, TypeInfo<T> typeInfo) throws IOException, SerializationException {
        try (Reader reader = new InputStreamReader(stream)) {
            return fromJson(reader, typeInfo);
        }
    }

    /**
     * Deserializes an object of type {@link T} from the JSON in the {@link File}.
     *
     * @param file     The file containing the JSON to be deserialized.
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws IOException            Thrown if there is an issue reading from the {@link File}.
     * @throws SerializationException Thrown if the deserialization fails.
     * @see #fromJson(Reader, TypeInfo)
     */
    public <T> T fromJson(File file, TypeInfo<T> typeInfo) throws IOException, SerializationException {
        try (Reader reader = new FileReader(file)) {
            return fromJson(reader, typeInfo);
        }
    }

    /**
     * Deserializes an object of type {@link T} from the JSON in the {@link File}.
     *
     * @param json     The JSON to be deserialized
     * @param typeInfo The {@link TypeInfo} specifying the type to deserialize the object as.
     * @param <T>      The type to deserialize the object as.
     * @return The deserialized object of type {@link T}.
     * @throws SerializationException Thrown if the deserialization fails.
     * @see #fromJson(Reader, TypeInfo)
     */
    public <T> T fromJson(String json, TypeInfo<T> typeInfo) throws SerializationException {
        try (StringReader reader = new StringReader(json)) {
            return fromJson(reader, typeInfo);
        }
    }
}

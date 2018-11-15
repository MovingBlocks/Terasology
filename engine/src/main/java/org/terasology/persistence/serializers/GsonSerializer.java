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

// TODO: Document
public class GsonSerializer {
    private Gson gson;

    public GsonSerializer() {
        this.gson = new Gson();
    }

    public <T> String toJson(T object, TypeHandler<T> typeHandler) throws IOException {
        StringWriter writer = new StringWriter();

        writeJson(object, typeHandler, writer);

        return writer.toString();
    }

    public <T> void writeJson(T object, TypeHandler<T> typeHandler, Writer writer) throws IOException {
        GsonPersistedData persistedData =
                (GsonPersistedData) typeHandler.serialize(object, new GsonPersistedDataSerializer());

        gson.toJson(persistedData.getElement(), writer);

        writer.close();
    }

    public <T> void writeJson(T object, TypeHandler<T> typeHandler, OutputStream stream) throws IOException {
        writeJson(object, typeHandler, new BufferedWriter(new OutputStreamWriter(stream)));
    }

    public <T> void writeJson(T object, TypeHandler<T> typeHandler, File file) throws IOException {
        writeJson(object, typeHandler, new BufferedWriter(new FileWriter(file)));
    }

    public <T> void writeJson(T object, TypeHandler<T> typeHandler, String path) throws IOException {
        writeJson(object, typeHandler, new File(path));
    }

    public PersistedData persistedDataFromJson(Reader reader) {
        JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

        return new GsonPersistedData(jsonElement);
    }

    public PersistedData persistedDataFromJson(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return persistedDataFromJson(reader);
        }
    }

    public PersistedData persistedDataFromJson(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return persistedDataFromJson(reader);
        }
    }

    public PersistedData persistedDataFromJson(String json) {
        try (StringReader reader = new StringReader(json)) {
            return persistedDataFromJson(reader);
        }
    }
}

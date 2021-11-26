// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.terasology.persistence.serializers.PersistedDataWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GsonPersistedDataWriter implements PersistedDataWriter<GsonPersistedData> {

    private final Gson gson;

    private final Charset charset;

    public GsonPersistedDataWriter(Gson gson, Charset charset) {
        this.gson = gson;
        this.charset = charset;
    }

    public GsonPersistedDataWriter(Gson gson) {
        this(gson, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] writeBytes(GsonPersistedData data) {
        return gson.toJson(data.getElement()).getBytes(charset);
    }

    @Override
    public void writeTo(GsonPersistedData data, OutputStream outputStream) throws IOException {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream))) {
            gson.toJson(data.getElement(), writer);
        }

    }

    @Override
    public void writeTo(GsonPersistedData data, ByteBuffer byteBuffer) throws IOException {
        byteBuffer.put(writeBytes(data));
    }
}

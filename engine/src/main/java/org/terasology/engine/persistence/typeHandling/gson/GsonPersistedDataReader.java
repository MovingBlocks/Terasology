// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.terasology.persistence.serializers.PersistedDataReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GsonPersistedDataReader implements PersistedDataReader<GsonPersistedData> {

    private final Gson gson;

    private final Charset charset;

    public GsonPersistedDataReader(Gson gson, Charset charset) {
        this.gson = gson;
        this.charset = charset;
    }

    public GsonPersistedDataReader(Gson gson) {
        this(gson, StandardCharsets.UTF_8);
    }

    @Override
    public GsonPersistedData read(InputStream inputStream) throws IOException {
        JsonElement jsonObject = gson.fromJson(new InputStreamReader(inputStream), JsonElement.class);
        return new GsonPersistedData(jsonObject);
    }

    @Override
    public GsonPersistedData read(byte[] byteBuffer) throws IOException {
        JsonElement jsonObject = gson.fromJson(new String(byteBuffer, charset), JsonElement.class);
        return new GsonPersistedData(jsonObject);
    }

    @Override
    public GsonPersistedData read(ByteBuffer byteBuffer) throws IOException {
        throw new UnsupportedOperationException("Idk how to parse this.");
    }
}

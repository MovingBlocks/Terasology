// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonElement;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;

import java.nio.ByteBuffer;

/**
 */
public abstract class AbstractGsonPersistedData implements PersistedData {

    private byte[] cachedDecodedBytes;


    public abstract JsonElement getElement();

    @Override
    public final String getAsString() {
        return getElement().getAsString();
    }

    @Override
    public final double getAsDouble() {
        return getElement().getAsDouble();
    }

    @Override
    public final float getAsFloat() {
        return getElement().getAsFloat();
    }

    @Override
    public final int getAsInteger() {
        return getElement().getAsInt();
    }

    @Override
    public final long getAsLong() {
        return getElement().getAsLong();
    }

    @Override
    public final boolean getAsBoolean() {
        return getElement().getAsBoolean();
    }

    @Override
    public final byte[] getAsBytes() {
        if (!isBytes()) {
            throw new DeserializationException("Data is not a valid bytes array");
        } else {
            return cachedDecodedBytes;
        }
    }

    @Override
    public final ByteBuffer getAsByteBuffer() {
        return ByteBuffer.wrap(getAsBytes());
    }

    @Override
    public abstract PersistedDataArray getAsArray();

    @Override
    public PersistedDataMap getAsValueMap() {
        return new GsonPersistedDataMap(getElement().getAsJsonObject());
    }

    @Override
    public final boolean isString() {
        return getElement().isJsonPrimitive() && getElement().getAsJsonPrimitive().isString();
    }

    @Override
    public final boolean isNumber() {
        return getElement().isJsonPrimitive() && getElement().getAsJsonPrimitive().isNumber();
    }

    @Override
    public final boolean isBoolean() {
        return getElement().isJsonPrimitive() && getElement().getAsJsonPrimitive().isBoolean();
    }

    @Override
    public final boolean isBytes() {
        if (getElement().isJsonPrimitive() && getElement().getAsJsonPrimitive().isString()) {
            if (cachedDecodedBytes != null) {
                return true;
            }
            try {
                cachedDecodedBytes = BaseEncoding.base64().decode(getElement().getAsString());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public final boolean isArray() {
        return getElement().isJsonArray();
    }

    @Override
    public final boolean isValueMap() {
        return getElement().isJsonObject();
    }

    @Override
    public boolean isNull() {
        return getElement().isJsonNull();
    }

    @Override
    public String toString() {
        return getElement().toString();
    }
}

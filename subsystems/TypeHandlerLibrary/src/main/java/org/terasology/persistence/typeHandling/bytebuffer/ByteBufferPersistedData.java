// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ByteBuffer-backed persisted data representation.
 * <pre>
 * 1 byte - BBType
 * 0..n bytes - data
 * </pre>
 */
public class ByteBufferPersistedData implements PersistedData {
    protected final ByteBuffer byteBuffer;
    protected final int position;
    protected final BBType type;
    private final boolean typeForced;

    public ByteBufferPersistedData(ByteBuffer byteBuffer, int position, byte type) {
        this.byteBuffer = byteBuffer;
        this.position = position;
        this.type = BBType.parse(type);
        this.typeForced = true;
    }

    public ByteBufferPersistedData(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.position = byteBuffer.position();
        this.type = BBType.parse(byteBuffer.get());
        this.typeForced = false;
    }

    public ByteBufferPersistedData(ByteBuffer byteBuffer, int position) {
        this.byteBuffer = byteBuffer;
        this.position = position;
        byteBuffer.position(position);
        this.type = BBType.parse(byteBuffer.get());
        this.typeForced = false;
    }

    public ByteBufferPersistedData(ByteBuffer byteBuffer, byte type) {
        this(byteBuffer, byteBuffer.position(), type);
    }


    public ByteBuffer getData() {
        return byteBuffer;
    }

    @Override
    public String getAsString() {
        if (!isString()) {
            throw new ClassCastException(String.format("Source is not of type string: %s", type.name()));
        }
        resetPosition();
        int size = byteBuffer.getInt();
        byte[] bytes = new byte[size];
        byteBuffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void resetPosition() {
        byteBuffer.position(position + (typeForced ? 0 : 1));
    }

    @Override
    public double getAsDouble() {
        if (!isNumber()) {
            throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
        }
        resetPosition();
        switch (type) {
            case INTEGER:
                return getData().getInt();
            case FLOAT:
                return getData().getFloat();
            case DOUBLE:
                return getData().getDouble();
            case LONG:
                return (double) getData().getLong();
        }
        throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
    }

    @Override
    public float getAsFloat() {
        if (!isNumber()) {
            throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
        }
        resetPosition();
        switch (type) {
            case INTEGER:
                return (float) getData().getInt();
            case FLOAT:
                return getData().getFloat();
            case DOUBLE:
                return (float) getData().getDouble();
            case LONG:
                return (float) getData().getLong();
        }
        throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
    }

    @Override
    public int getAsInteger() {
        if (!isNumber()) {
            throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
        }
        resetPosition();
        switch (type) {
            case INTEGER:
                return getData().getInt();
            case FLOAT:
                return (int) getData().getFloat();
            case DOUBLE:
                return (int) getData().getDouble();
            case LONG:
                return (int) getData().getLong();
        }
        throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
    }

    @Override
    public long getAsLong() {
        if (!isNumber()) {
            throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
        }
        resetPosition();
        switch (type) {
            case INTEGER:
                return getData().getInt();
            case FLOAT:
                return (long) getData().getFloat();
            case DOUBLE:
                return (long) getData().getDouble();
            case LONG:
                return getData().getLong();
        }
        throw new ClassCastException(String.format("Source is not of type number: %s", type.name()));
    }


    @Override
    public boolean getAsBoolean() {
        if (!isBoolean()) {
            throw new ClassCastException(String.format("Source is not of type boolean: %s", type.name()));
        }
        resetPosition();
        return byteBuffer.get() != 0; // Don't Packed booleans
    }

    @Override
    public byte[] getAsBytes() {
        if (!isBytes()) {
            throw new DeserializationException(String.format("Source is not of type bytes or bytebuffer: %s", type.name()));
        }
        resetPosition();
        int size = byteBuffer.getInt();
        byte[] bytes = new byte[size];
        byteBuffer.get(bytes);
        return bytes;
    }

    @Override
    public ByteBuffer getAsByteBuffer() {
        if (!isBytes()) {
            throw new DeserializationException(String.format("Source is not of type bytes or bytebuffer: %s", type.name()));
        }
        resetPosition();
        return ByteBuffer.wrap(getAsBytes());
    }

    @Override
    public PersistedDataArray getAsArray() {
        if (!isArray()) {
            throw new IllegalStateException(String.format("Source is not of type array: %s", type.name()));
        }
        byteBuffer.position(position);
        return new ByteBufferPersistedDataArray(byteBuffer);
    }

    @Override
    public PersistedDataMap getAsValueMap() {
        if (!isValueMap()) {
            throw new IllegalStateException(String.format("Source is not of type valuemap: %s", type.name()));
        }
        byteBuffer.position(position);
        return new ByteBufferPersistedDataMap(byteBuffer);
    }

    @Override
    public boolean isString() {
        return type == BBType.STRING;
    }

    @Override
    public boolean isNumber() {
        return type == BBType.FLOAT
                || type == BBType.DOUBLE
                || type == BBType.INTEGER
                || type == BBType.LONG;
    }

    @Override
    public boolean isBoolean() {
        return type == BBType.BOOLEAN;
    }

    @Override
    public boolean isBytes() {
        return type == BBType.BYTES || type == BBType.BYTEBUFFER;
    }

    @Override
    public boolean isArray() {
        return type.isArray();
    }

    @Override
    public boolean isValueMap() {
        return type == BBType.VALUEMAP;
    }

    @Override
    public boolean isNull() {
        return type == BBType.NULL;
    }
}

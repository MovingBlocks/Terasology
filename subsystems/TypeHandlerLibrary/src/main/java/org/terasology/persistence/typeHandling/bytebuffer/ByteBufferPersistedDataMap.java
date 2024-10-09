// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ByteBuffer-backed persisted data map representation.
 * <pre>
 *     1 byte -  type = 10
 *     4 byte - size
 *     8 * size bytes - refmap
 *     0..n bytes - key and value data.
 * </pre>
 * <p>
 * Use refmap - links to key and value positions. provide almost constant read time.
 */
public class ByteBufferPersistedDataMap extends ByteBufferPersistedData implements PersistedDataMap {

    public ByteBufferPersistedDataMap(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    @Override
    public boolean has(String name) {
        return index(name) != -1;
    }

    @Override
    public PersistedData get(String name) {
        return new ByteBufferPersistedData(byteBuffer, index(name));
    }

    @Override
    public float getAsFloat(String name) {
        return get(name).getAsFloat();
    }

    @Override
    public int getAsInteger(String name) {
        return get(name).getAsInteger();
    }

    @Override
    public double getAsDouble(String name) {
        return get(name).getAsDouble();
    }

    @Override
    public long getAsLong(String name) {
        return get(name).getAsLong();
    }

    @Override
    public String getAsString(String name) {
        return get(name).getAsString();
    }

    @Override
    public boolean getAsBoolean(String name) {
        return get(name).getAsBoolean();
    }

    @Override
    public PersistedDataMap getAsMap(String name) {
        return get(name).getAsValueMap();
    }

    @Override
    public PersistedDataArray getAsArray(String name) {
        return get(name).getAsArray();
    }

    @Override
    public Set<Map.Entry<String, PersistedData>> entrySet() {
        int refPositions = position + 1 + 4;
        int size = size();
        Map<String, PersistedData> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            int keyPos = position + byteBuffer.getInt(refPositions + 8 * i);
            int valuePos = position + byteBuffer.getInt(refPositions + 8 * i + 4);
            String key = new ByteBufferPersistedData(byteBuffer, keyPos, BBType.STRING.getCode()).getAsString();
            PersistedData value = new ByteBufferPersistedData(byteBuffer, valuePos);
            map.put(key, value);
        }
        return map.entrySet();
    }

    private int size() {
        return byteBuffer.getInt(position + 1);
    }

    private int index(String name) {
        int refPositions = position + 1 + 4;
        int size = size();
        for (int i = 0; i < size; i++) {
            int keyPos = position + byteBuffer.getInt(refPositions + 8 * i);
            String candidateKey = new ByteBufferPersistedData(byteBuffer, keyPos, BBType.STRING.getCode()).getAsString();
            if (name.equals(candidateKey)) {
                return position + byteBuffer.getInt(refPositions + 8 * i + 4);
            }
        }
        return -1;
    }
}

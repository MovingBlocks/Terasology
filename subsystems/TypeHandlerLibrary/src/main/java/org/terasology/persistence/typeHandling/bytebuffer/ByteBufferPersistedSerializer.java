// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * TODO make direct serializer factory.
 */
public class ByteBufferPersistedSerializer implements PersistedDataSerializer {

    @Override
    public PersistedData serialize(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1 + 4);
        buffer.put(BBType.STRING.getCode());
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(String... values) {
        return serializeStrings(Arrays.asList(values));
    }

    @Override
    public PersistedData serializeStrings(Iterable<String> value) {
        List<ByteBufferPersistedData> buffers = Lists.newArrayList();
        int byteSize = 0;
        int size = 0;
        for (String str : value) {
            ByteBufferPersistedData serialize = (ByteBufferPersistedData) serialize(str);
            byteSize += serialize.getData().array().length;
            size++;
            buffers.add(serialize);
        }
        if (size == 1) {
            return buffers.get(0);
        }
        ByteBuffer buffer = ByteBuffer.allocate(byteSize + 4);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.STRING.getCode());
        buffer.putInt(size);
        for (ByteBufferPersistedData data : buffers) {
            data.getData().position(1); // remove header of string.
            buffer.put(data.getData());
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);
    }

    @Override
    public PersistedData serialize(float value) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(BBType.FLOAT.getCode());
        buffer.putFloat(value);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(float... values) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + 4 + values.length * 4);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.FLOAT.getCode());
        buffer.putInt(values.length);
        for (float value : values) {
            buffer.putFloat(value);
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);
    }

    @Override
    public PersistedData serialize(TFloatIterator value) {
        TFloatArrayList data = new TFloatArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return serialize(data.toArray());
    }

    @Override
    public PersistedData serialize(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(BBType.INTEGER.getCode());
        buffer.putInt(value);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(int... values) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + 4 + values.length * 4);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.INTEGER.getCode());
        buffer.putInt(values.length);
        for (int value : values) {
            buffer.putInt(value);
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);
    }

    @Override
    public PersistedData serialize(TIntIterator value) {
        TIntArrayList data = new TIntArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return serialize(data.toArray());
    }

    @Override
    public PersistedData serialize(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put(BBType.LONG.getCode());
        buffer.putLong(value);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(long... values) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + 4 + values.length * 8);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.LONG.getCode());
        buffer.putInt(values.length);
        for (long value : values) {
            buffer.putLong(value);
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);
    }

    @Override
    public PersistedData serialize(TLongIterator value) {
        TLongArrayList data = new TLongArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return serialize(data.toArray());
    }

    @Override
    public PersistedData serialize(boolean value) {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put(BBType.BOOLEAN.getCode());
        buffer.put(value ? (byte) 1 : (byte) 0);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(boolean... values) {
        int size = values.length;
        int sizeInBytes = size % 8 + 1;
        ByteBuffer buffer = ByteBuffer.allocate(6 + sizeInBytes);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.BOOLEAN.getCode());
        buffer.putInt(size);
        for (int i = 0; i < sizeInBytes; i++) {
            byte valueByte = 0;
            for (int bi = 0; bi < 8; bi++) {
                if (i * 8 + bi >= size) {
                    break;
                }
                boolean b = values[i * 8 + bi];
                valueByte |= (b ? 1 : 0) << bi;
            }
            buffer.put(valueByte);
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);

    }

    @Override
    public PersistedData serialize(double value) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(BBType.DOUBLE.getCode());
        buffer.putDouble(value);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(double... values) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + 4 + values.length * 8);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.DOUBLE.getCode());
        buffer.putInt(values.length);
        for (double value : values) {
            buffer.putDouble(value);
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);
    }

    @Override
    public PersistedData serialize(TDoubleIterator value) {
        TDoubleArrayList data = new TDoubleArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return serialize(data.toArray());
    }

    @Override
    public PersistedData serialize(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(value.length + 1 + 4);
        buffer.put(BBType.BYTES.getCode());
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(ByteBuffer value) {
        int size = value.array().length;
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + size);
        buffer.put(BBType.BYTEBUFFER.getCode());
        buffer.putInt(size);
        buffer.put(value);
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }

    @Override
    public PersistedData serialize(PersistedData... values) {
        int bytes = 0;
        int[] sizes = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            PersistedData data = values[i];
            ByteBufferPersistedData dataBytes = (ByteBufferPersistedData) data;
            int length = dataBytes.byteBuffer.array().length;
            bytes += length;
            sizes[i] = length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(6 + values.length * 4 + bytes);
        buffer.put(BBType.ARRAY.getCode());
        buffer.put(BBArrayType.VALUE.getCode());
        buffer.putInt(values.length);
        // Write data sizes
        for (int size : sizes) {
            buffer.putInt(size);
        }
        for (PersistedData data : values) {
            ByteBufferPersistedData dataBytes = (ByteBufferPersistedData) data;
            dataBytes.byteBuffer.rewind();
            buffer.put(dataBytes.byteBuffer);
        }
        buffer.rewind();
        return new ByteBufferPersistedDataArray(buffer);
    }

    @Override
    public PersistedData serialize(Iterable<PersistedData> data) {
        return serialize(Iterables.toArray(data, PersistedData.class));
    }

    @Override
    public PersistedData serialize(Map<String, PersistedData> data) {
        int entryCount = data.size();
        int size = 0;
        List<PersistedData> keys = new ArrayList<>(entryCount);
        List<PersistedData> values = new ArrayList<>(entryCount);
        for (Map.Entry<String, PersistedData> entry : data.entrySet()) {
            PersistedData serialize = serialize(entry.getKey());
            ByteBuffer keyBuffer = ((ByteBufferPersistedData) serialize).byteBuffer;
            keyBuffer.position(1); // skip header
            size += keyBuffer.remaining();
            keys.add(serialize);
            size += ((ByteBufferPersistedData) entry.getValue()).byteBuffer.array().length;
            values.add(entry.getValue());
        }
        int refsSize = 8 * entryCount;
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + refsSize + size);
        buffer.put(BBType.VALUEMAP.getCode());
        buffer.putInt(entryCount);
        int dataPosition = 1 + 4 + refsSize;
        buffer.position(dataPosition);
        for (int i = 0; i < keys.size(); i++) {
            ByteBufferPersistedData key = (ByteBufferPersistedData) keys.get(i);
            int keySize = key.byteBuffer.remaining();
            buffer.put(key.byteBuffer);
            buffer.putInt(1 + 4 + i * 8, dataPosition);
            dataPosition += keySize;
        }
        for (int i = 0; i < values.size(); i++) {
            ByteBufferPersistedData value = (ByteBufferPersistedData) values.get(i);
            int keySize = value.byteBuffer.array().length;
            buffer.put(value.byteBuffer.array());
            buffer.putInt(1 + 4 + i * 8 + 4, dataPosition);
            dataPosition += keySize;
        }
        buffer.rewind();
        return new ByteBufferPersistedDataMap(buffer);
    }

    @Override
    public PersistedData serializeNull() {
        ByteBuffer buffer = ByteBuffer.allocate(1)
                .put(BBType.NULL.getCode());
        buffer.rewind();
        return new ByteBufferPersistedData(buffer);
    }
}

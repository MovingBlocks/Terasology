// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * ByteBuffer-backed persisted data array representation.
 * <pre>
 * 1 byte - BBType
 * 4 bytes - size
 * 0..n bytes - data
 * </pre>
 */
public class ByteBufferPersistedDataArray extends ByteBufferPersistedData implements PersistedDataArray {

    private final int size;

    public ByteBufferPersistedDataArray(ByteBuffer byteBuffer) {
        super(byteBuffer);
        size = byteBuffer.getInt();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public PersistedData getArrayItem(int index) {
        BBType primitiveType = type.getPrimitiveType();
        if (primitiveType != null) {
            return new ByteBufferPersistedData(byteBuffer, calculateIndex(index), primitiveType.getCode());
        } else {
            return new ByteBufferPersistedData(byteBuffer, calculateIndex(index));
        }
    }

    @Override
    public boolean isNumberArray() {
        return type == BBType.FLOAT_ARRAY
                || type == BBType.DOUBLE_ARRAY
                || type == BBType.INTEGER_ARRAY
                || type == BBType.LONG_ARRAY;
    }

    @Override
    public boolean isBooleanArray() {
        return type == BBType.BOOLEAN_ARRAY;
    }

    @Override
    public boolean isStringArray() {
        return type == BBType.STRING_ARRAY;
    }

    @Override
    public List<String> getAsStringArray() {
        List<String> list = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            list.add(getArrayItem(i).getAsString());
        }
        return list;
    }

    @Override
    public String getAsString() {
        if (isStringArray()) {
            if (size() == 1) {
                return getArrayItem(0).getAsString();
            } else {
                throw new IllegalStateException("String array have size != 1");
            }
        } else {
            if (size() == 1) {
                PersistedData data = getArrayItem(0);
                if (data.isString()) {
                    return data.getAsString();
                }
            }
            throw new ClassCastException(String.format("Source is not of type string array: %s", type.name()));
        }
    }


    @Override
    public double getAsDouble() {
        if (isNumberArray()) {
            if (size() == 1) {
                return getArrayItem(0).getAsDouble();
            } else {
                throw new IllegalStateException("Number array have size != 1");
            }
        } else {
            if (size() == 1) {
                PersistedData data = getArrayItem(0);
                if (data.isNumber()) {
                    return data.getAsDouble();
                }
            }
            throw new ClassCastException(String.format("Source is not of type number array: %s", type.name()));
        }
    }

    @Override
    public float getAsFloat() {
        if (isNumberArray()) {
            if (size() == 1) {
                return getArrayItem(0).getAsFloat();
            } else {
                throw new IllegalStateException("Number array have size != 1");
            }
        } else {
            if (size() == 1) {
                PersistedData data = getArrayItem(0);
                if (data.isNumber()) {
                    return data.getAsFloat();
                }
            }
            throw new ClassCastException(String.format("Source is not of type number array: %s", type.name()));
        }
    }

    @Override
    public int getAsInteger() {
        if (isNumberArray()) {
            if (size() == 1) {
                return getArrayItem(0).getAsInteger();
            } else {
                throw new IllegalStateException("Number array have size != 1");
            }
        } else {
            if (size() == 1) {
                PersistedData data = getArrayItem(0);
                if (data.isNumber()) {
                    return data.getAsInteger();
                }
            }
            throw new ClassCastException(String.format("Source is not of type number array: %s", type.name()));
        }
    }

    @Override
    public long getAsLong() {
        if (isNumberArray()) {
            if (size() == 1) {
                return getArrayItem(0).getAsLong();
            } else {
                throw new IllegalStateException("Number array have size != 1");
            }
        } else {
            if (size() == 1) {
                PersistedData data = getArrayItem(0);
                if (data.isNumber()) {
                    return data.getAsLong();
                }
            }
            throw new ClassCastException(String.format("Source is not of type number array: %s", type.name()));
        }
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        byteBuffer.position(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER);
        TDoubleList list = new TDoubleArrayList(size());
        Iterator<PersistedData> iter = typedIterator(type.getPrimitiveType());
        while (iter.hasNext()) {
            list.add(iter.next().getAsDouble());
        }
        return list;
    }

    @Override
    public TFloatList getAsFloatArray() {
        byteBuffer.position(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER);
        TFloatList list = new TFloatArrayList(size());
        Iterator<PersistedData> iter = typedIterator(type.getPrimitiveType());
        while (iter.hasNext()) {
            list.add(iter.next().getAsFloat());
        }
        return list;
    }

    @Override
    public TIntList getAsIntegerArray() {
        byteBuffer.position(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER);
        TIntList list = new TIntArrayList(size());
        Iterator<PersistedData> iter = typedIterator(type.getPrimitiveType());
        while (iter.hasNext()) {
            list.add(iter.next().getAsInteger());
        }
        return list;
    }

    @Override
    public TLongList getAsLongArray() {
        byteBuffer.position(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER);
        TLongList list = new TLongArrayList(size());
        Iterator<PersistedData> iter = typedIterator(type.getPrimitiveType());
        while (iter.hasNext()) {
            list.add(iter.next().getAsLong());
        }
        return list;
    }

    @Override
    public boolean[] getAsBooleanArray() {
        byteBuffer.position(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER);
        int sizeInBytes = size() % 8 + 1;
        byte[] bytes = new byte[sizeInBytes];
        byteBuffer.get(bytes);
        boolean[] booleans = new boolean[size()];
        for (int i = 0; i < sizeInBytes; i++) {
            for (int bi = 0; bi < 8; bi++) {
                if (i * 8 + bi >= size()) {
                    break;
                }
                booleans[i * 8 + bi] = ((bytes[i] >> bi) & 1) == 1;
            }
        }
        return booleans;
    }

    @Override
    public List<PersistedData> getAsValueArray() {
        byteBuffer.position(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER);
        List<PersistedData> data = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            data.add(new ByteBufferPersistedData(byteBuffer, calculateIndex(i)));
        }
        return data;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public PersistedData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("iterator haven't something.");
                }
                PersistedData data = new ByteBufferPersistedData(byteBuffer, calculateIndex(index));
                index++;
                return data;
            }
        };
    }

    private Iterator<PersistedData> typedIterator(BBType type) {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public PersistedData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("iterator haven't something.");
                }
                PersistedData data = new ByteBufferPersistedData(byteBuffer, calculateIndex(index), type.getCode());
                index++;
                return data;
            }
        };
    }

    private int calculateIndex(int index) {
        switch (type) {
            case BOOLEAN_ARRAY:
                return BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER + index % 8 + 1;
            case FLOAT_ARRAY:
            case INTEGER_ARRAY:
                return BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER + index * 4;
            case DOUBLE_ARRAY:
            case LONG_ARRAY:
                return BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER + index * 8;
            case STRING_ARRAY: {
                int pos = position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER;
                for (int i = 0; i < index; i++) {
                    pos += byteBuffer.getInt(pos) + BBConsts.SIZE_HEADER;
                }
                return pos;
            }
            case VALUE_ARRAY: {
                int pos = 0;
                for (int i = 0; i < index; i++) {
                    pos += byteBuffer.getInt(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER + i *  BBConsts.SIZE_HEADER);
                }
                int sizeArraySize = size() * 4;
                return pos + position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER + sizeArraySize;
            }

        }
        throw new UnsupportedOperationException("IDK how it to do");
    }

    @Override
    public boolean getAsBoolean() {
        if (isBooleanArray()) {
            if (size() == 1) {
                return (byteBuffer.get(position + BBConsts.TYPE_HEADER + BBConsts.SIZE_HEADER) & 1) == 1;
            } else {
                throw new IllegalStateException("boolean array have size != 1");
            }
        } else {
            if (size() == 1) {
                PersistedData data = getArrayItem(0);
                if (data.isBoolean()) {
                    return data.getAsBoolean();
                }
            }
            throw new ClassCastException(String.format("Source is not of type boolean array: %s", type.name()));
        }
    }

    @Override
    public byte[] getAsBytes() {
        if (size() == 1) {
            PersistedData data = getArrayItem(0);
            if (data.isBytes()) {
                return data.getAsBytes();
            }
        }
        throw new DeserializationException(String.format("Source is not of type bytes array: %s", type.name()));
    }

    @Override
    public ByteBuffer getAsByteBuffer() {
        if (size() == 1) {
            PersistedData data = getArrayItem(0);
            if (data.isBytes()) {
                return data.getAsByteBuffer();
            }
        }
        throw new DeserializationException(String.format("Source is not of type bytes array: %s", type.name()));
    }
}

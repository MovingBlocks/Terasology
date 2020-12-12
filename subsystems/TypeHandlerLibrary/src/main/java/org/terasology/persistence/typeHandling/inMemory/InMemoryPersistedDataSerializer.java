// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory;

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
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedBooleanArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedDoubleArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedFloatArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedIntegerArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedLongArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedStringArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedValueArray;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class InMemoryPersistedDataSerializer implements PersistedDataSerializer {

    public static final PersistedData NULL = new AbstractPersistedData() {
        @Override
        public boolean isNull() {
            return true;
        }
    };

    @Override
    public PersistedData serialize(String value) {
        return new PersistedString(value);
    }

    @Override
    public PersistedData serialize(String... values) {
        return serializeStrings(Arrays.asList(values));
    }

    @Override
    public PersistedData serializeStrings(Iterable<String> value) {
        return new PersistedStringArray(Lists.newArrayList(value));
    }

    @Override
    public PersistedData serialize(float value) {
        return new PersistedFloat(value);
    }

    @Override
    public PersistedData serialize(float... values) {
        return new PersistedFloatArray(TFloatArrayList.wrap(values));
    }

    @Override
    public PersistedData serialize(TFloatIterator value) {
        TFloatArrayList data = new TFloatArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return new PersistedFloatArray(data);
    }

    @Override
    public PersistedData serialize(int value) {
        return new PersistedInteger(value);
    }

    @Override
    public PersistedData serialize(int... values) {
        return new PersistedIntegerArray(TIntArrayList.wrap(values));
    }

    @Override
    public PersistedData serialize(TIntIterator value) {
        TIntArrayList data = new TIntArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return new PersistedIntegerArray(data);
    }

    @Override
    public PersistedData serialize(long value) {
        return new PersistedLong(value);
    }

    @Override
    public PersistedData serialize(long... values) {
        return new PersistedLongArray(TLongArrayList.wrap(values));
    }

    @Override
    public PersistedData serialize(TLongIterator value) {
        TLongArrayList data = new TLongArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return new PersistedLongArray(data);
    }

    @Override
    public PersistedData serialize(boolean value) {
        return new PersistedBoolean(value);
    }

    @Override
    public PersistedData serialize(boolean... values) {
        return new PersistedBooleanArray(values);
    }

    @Override
    public PersistedData serialize(double value) {
        return new PersistedDouble(value);
    }

    @Override
    public PersistedData serialize(double... values) {
        return new PersistedDoubleArray(TDoubleArrayList.wrap(values));
    }

    @Override
    public PersistedData serialize(TDoubleIterator value) {
        TDoubleArrayList data = new TDoubleArrayList();
        while (value.hasNext()) {
            data.add(value.next());
        }
        return new PersistedDoubleArray(data);
    }

    @Override
    public PersistedData serialize(byte[] value) {
        return new PersistedBytes(value);
    }

    @Override
    public PersistedData serialize(ByteBuffer value) {
        return serialize(value.array());
    }

    @Override
    public PersistedData serialize(PersistedData... values) {
        return new PersistedValueArray(Arrays.asList(values));
    }

    @Override
    public PersistedData serialize(Iterable<PersistedData> data) {
        return new PersistedValueArray(Lists.newArrayList(data));
    }

    @Override
    public PersistedData serialize(Map<String, PersistedData> data) {
        return new PersistedMap(data);
    }

    @Override
    public PersistedData serializeNull() {
        return NULL;
    }
}

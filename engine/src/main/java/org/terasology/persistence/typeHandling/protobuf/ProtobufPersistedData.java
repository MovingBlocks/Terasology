/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.protobuf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
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
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.inMemory.PersistedBoolean;
import org.terasology.persistence.typeHandling.inMemory.PersistedDouble;
import org.terasology.persistence.typeHandling.inMemory.PersistedFloat;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.persistence.typeHandling.inMemory.PersistedLong;
import org.terasology.persistence.typeHandling.inMemory.PersistedMap;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;
import org.terasology.protobuf.EntityData;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 */
public class ProtobufPersistedData implements PersistedData, PersistedDataArray {
    private EntityData.Value data;

    public ProtobufPersistedData(EntityData.Value data) {
        this.data = data;
    }

    public EntityData.Value getValue() {
        return data;
    }

    @Override
    public String getAsString() {
        if (data.getStringCount() == 1) {
            return data.getString(0);
        } else if (data.getStringCount() > 1) {
            throw new IllegalStateException("Data is an array of size != 1");
        } else if (!isNull()) {
            throw new ClassCastException("Data is not a String");
        }
        return null;
    }

    @Override
    public double getAsDouble() {
        if (data.getDoubleCount() == 1) {
            return data.getDouble(0);
        } else if (data.getFloatCount() == 1) {
            return data.getFloat(0);
        } else if (data.getLongCount() == 1) {
            return data.getLong(0);
        } else if (data.getIntegerCount() == 1) {
            return data.getInteger(0);
        } else if (data.getDoubleCount() + data.getFloatCount() + data.getIntegerCount() + data.getLongCount() > 1) {
            throw new IllegalStateException("Data is an array of size != 1");
        } else {
            throw new ClassCastException("Data is not a number");
        }
    }

    @Override
    public float getAsFloat() {
        if (data.getFloatCount() == 1) {
            return data.getFloat(0);
        } else if (data.getDoubleCount() == 1) {
            return (float) data.getDouble(0);
        } else if (data.getLongCount() == 1) {
            return data.getLong(0);
        } else if (data.getIntegerCount() == 1) {
            return data.getInteger(0);
        } else if (data.getDoubleCount() + data.getFloatCount() + data.getIntegerCount() + data.getLongCount() > 1) {
            throw new IllegalStateException("Data is an array of size != 1");
        } else {
            throw new ClassCastException("Data is not a number");
        }
    }

    @Override
    public int getAsInteger() {
        if (data.getIntegerCount() == 1) {
            return data.getInteger(0);
        } else if (data.getDoubleCount() == 1) {
            return (int) data.getDouble(0);
        } else if (data.getFloatCount() == 1) {
            return (int) data.getFloat(0);
        } else if (data.getLongCount() == 1) {
            return (int) data.getLong(0);
        } else if (data.getDoubleCount() + data.getFloatCount() + data.getIntegerCount() + data.getLongCount() > 1) {
            throw new IllegalStateException("Data is an array of size != 1");
        } else {
            throw new ClassCastException("Data is not a number");
        }
    }

    @Override
    public long getAsLong() {
        if (data.getLongCount() == 1) {
            return (int) data.getLong(0);
        } else if (data.getIntegerCount() == 1) {
            return data.getInteger(0);
        } else if (data.getDoubleCount() == 1) {
            return (int) data.getDouble(0);
        } else if (data.getFloatCount() == 1) {
            return (int) data.getFloat(0);
        } else if (data.getDoubleCount() + data.getFloatCount() + data.getIntegerCount() + data.getLongCount() > 1) {
            throw new IllegalStateException("Data is an array of size != 1");
        } else {
            throw new ClassCastException("Data is not a number");
        }
    }

    @Override
    public boolean getAsBoolean() {
        if (data.getBooleanCount() == 1) {
            return data.getBoolean(0);
        } else if (data.getBooleanCount() > 1) {
            throw new IllegalStateException("Data is an array of size != 1");
        } else {
            throw new ClassCastException("Data is not a boolean");
        }
    }

    @Override
    public byte[] getAsBytes() {
        if (data.hasBytes()) {
            return data.getBytes().toByteArray();
        } else if (!isNull()) {
            throw new DeserializationException("Data is not bytes");
        } else {
            return new byte[0];
        }
    }

    @Override
    public ByteBuffer getAsByteBuffer() {
        if (data.hasBytes()) {
            return data.getBytes().asReadOnlyByteBuffer();
        } else if (!isNull()) {
            throw new DeserializationException("Data is not bytes");
        } else {
            return ByteBuffer.wrap(new byte[0]);
        }
    }

    @Override
    public PersistedDataArray getAsArray() {
        if (isArray()) {
            return this;
        }
        throw new IllegalStateException("Data is not an array");
    }

    @Override
    public PersistedDataMap getAsValueMap() {
        Map<String, PersistedData> result = Maps.newLinkedHashMap();
        if (data.getNameValueCount() > 0) {
            for (int i = 0; i < data.getNameValueCount(); ++i) {
                result.put(data.getNameValue(i).getName(), new ProtobufPersistedData(data.getNameValue(i).getValue()));
            }
        } else if (!isNull()) {
            throw new IllegalStateException("Data is not a value map");
        }
        return new PersistedMap(result);
    }

    @Override
    public boolean isString() {
        return data.getStringCount() == 1 || isNull();
    }

    @Override
    public boolean isNumber() {
        return data.getIntegerCount() == 1 || data.getFloatCount() == 1 || data.getLongCount() == 1 || data.getDoubleCount() == 1;
    }

    @Override
    public boolean isBoolean() {
        return data.getBooleanCount() == 1;
    }

    @Override
    public boolean isBytes() {
        return data.hasBytes() || isNull();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isValueMap() {
        return data.getNameValueCount() != 0 || isNull();
    }

    @Override
    public boolean isNull() {
        return !data.hasBytes() && data.getBooleanCount() + data.getFloatCount() + data.getDoubleCount() + data.getIntegerCount() + data.getLongCount()
                + data.getStringCount() + data.getValueCount() + data.getNameValueCount() == 0;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public int size() {
        return Math.max(Math.max(Math.max(data.getBooleanCount(), data.getFloatCount()), Math.max(data.getDoubleCount(), data.getIntegerCount())),
                Math.max(Math.max(data.getLongCount(), data.getStringCount()), data.getValueCount()));
    }

    @Override
    public PersistedData getArrayItem(int index) {
        if (data.getValueCount() > 0) {
            return new ProtobufPersistedData(data.getValue(index));
        } else if (data.getFloatCount() > 0) {
            return new PersistedFloat(data.getFloat(index));
        } else if (data.getIntegerCount() > 0) {
            return new PersistedInteger(data.getInteger(index));
        } else if (data.getDoubleCount() > 0) {
            return new PersistedDouble(data.getDouble(index));
        } else if (data.getBooleanCount() > 0) {
            return new PersistedBoolean(data.getBoolean(index));
        } else if (data.getLongCount() > 0) {
            return new PersistedLong(data.getLong(index));
        } else if (data.getStringCount() > 0) {
            return new PersistedString(data.getString(index));
        } else if (data.hasBytes()) {
            throw new IllegalStateException("Data is not an array");
        }
        throw new IndexOutOfBoundsException(index + " exceeds size of array data");
    }

    @Override
    public boolean isNumberArray() {
        return data.getIntegerCount() + data.getFloatCount() + data.getDoubleCount() + data.getLongCount() > 0 || isNull();
    }

    @Override
    public boolean isBooleanArray() {
        return data.getBooleanCount() > 0 || isNull();
    }

    @Override
    public boolean isStringArray() {
        return data.getStringCount() > 0 || isNull();
    }

    @Override
    public List<String> getAsStringArray() {
        return data.getStringList();
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        if (data.getDoubleCount() != 0) {
            TDoubleList result = new TDoubleArrayList(data.getDoubleCount());
            for (int i = 0; i < data.getDoubleCount(); ++i) {
                result.add(data.getDouble(i));
            }
            return result;
        } else {
            TDoubleList result = new TDoubleArrayList(data.getFloatCount());
            for (int i = 0; i < data.getFloatCount(); ++i) {
                result.add(data.getFloat(i));
            }
            return result;
        }
    }

    @Override
    public TFloatList getAsFloatArray() {
        if (data.getFloatCount() != 0) {
            TFloatList result = new TFloatArrayList(data.getFloatCount());
            for (int i = 0; i < data.getFloatCount(); ++i) {
                result.add(data.getFloat(i));
            }
            return result;
        } else {
            TFloatList result = new TFloatArrayList(data.getDoubleCount());
            for (int i = 0; i < data.getDoubleCount(); ++i) {
                result.add((float) data.getDouble(i));
            }
            return result;
        }
    }

    @Override
    public TIntList getAsIntegerArray() {
        if (data.getIntegerCount() > 0) {
            TIntList result = new TIntArrayList(data.getIntegerCount());
            for (int i = 0; i < data.getIntegerCount(); ++i) {
                result.add(data.getInteger(i));
            }
            return result;
        } else {
            TIntList result = new TIntArrayList(data.getLongCount());
            for (int i = 0; i < data.getLongCount(); ++i) {
                result.add(Ints.saturatedCast(data.getLong(i)));
            }
            return result;
        }
    }

    @Override
    public TLongList getAsLongArray() {
        if (data.getLongCount() > 0) {
            TLongList result = new TLongArrayList(data.getLongCount());
            for (int i = 0; i < data.getLongCount(); ++i) {
                result.add(data.getLong(i));
            }
            return result;
        } else {
            TLongList result = new TLongArrayList(data.getIntegerCount());
            for (int i = 0; i < data.getIntegerCount(); ++i) {
                result.add(data.getInteger(i));
            }
            return result;
        }
    }

    @Override
    public boolean[] getAsBooleanArray() {
        boolean[] result = new boolean[data.getBooleanCount()];
        for (int i = 0; i < data.getBooleanCount(); ++i) {
            result[i] = data.getBoolean(i);
        }
        return result;
    }

    @Override
    public List<PersistedData> getAsValueArray() {
        List<PersistedData> result = Lists.newArrayList();
        if (data.getValueCount() > 0) {
            for (EntityData.Value val : data.getValueList()) {
                result.add(new ProtobufPersistedData(val));
            }
        } else if (data.getFloatCount() > 0) {
            for (int i = 0; i < data.getFloatCount(); ++i) {
                result.add(new PersistedFloat(data.getFloat(i)));
            }
        } else if (data.getIntegerCount() > 0) {
            for (int i = 0; i < data.getIntegerCount(); ++i) {
                result.add(new PersistedInteger(data.getInteger(i)));
            }
        } else if (data.getDoubleCount() > 0) {
            for (int i = 0; i < data.getDoubleCount(); ++i) {
                result.add(new PersistedDouble(data.getDouble(i)));
            }
        } else if (data.getBooleanCount() > 0) {
            for (int i = 0; i < data.getBooleanCount(); ++i) {
                result.add(new PersistedBoolean(data.getBoolean(i)));
            }
        } else if (data.getLongCount() > 0) {
            for (int i = 0; i < data.getLongCount(); ++i) {
                result.add(new PersistedLong(data.getLong(i)));
            }
        } else if (data.getStringCount() > 0) {
            for (int i = 0; i < data.getStringCount(); ++i) {
                result.add(new PersistedString(data.getString(i)));
            }
        } else if (data.getNameValueCount() > 0) {
            result.add(new ProtobufPersistedData(data));
        } else if (data.hasBytes()) {
            throw new IllegalStateException("Data is not an array");
        }
        return result;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return getAsValueArray().iterator();
    }
}

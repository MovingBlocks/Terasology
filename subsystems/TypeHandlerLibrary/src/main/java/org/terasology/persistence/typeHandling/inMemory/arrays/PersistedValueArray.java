// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public class PersistedValueArray extends AbstractPersistedArray {

    private final List<PersistedData> data;

    public PersistedValueArray(List<PersistedData> data) {
        this.data = data;
    }

    @Override
    public String getAsString() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isString()) {
                return persistedData.getAsString();
            } else if (!isNull()) {
                throw new ClassCastException("Data is not a String: " + persistedData.toString());
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
        return null;
    }

    @Override
    public double getAsDouble() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isNumber()) {
                return persistedData.getAsDouble();
            } else {
                throw new ClassCastException("Data is not a Double");
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public float getAsFloat() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isNumber()) {
                return persistedData.getAsFloat();
            } else {
                throw new ClassCastException("Data is not a Float");
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public int getAsInteger() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isNumber()) {
                return persistedData.getAsInteger();
            } else {
                throw new ClassCastException("Data is not a Integer" );
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public long getAsLong() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isNumber()) {
                return persistedData.getAsLong();
            } else {
                throw new ClassCastException("Data is not a Long");
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public boolean getAsBoolean() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isBoolean()) {
                return persistedData.getAsBoolean();
            } else  {
                throw new ClassCastException("Data is not a Boolean");
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public byte[] getAsBytes() {
        if (size() == 1) {
            PersistedData persistedData = getArrayItem(0);
            if (persistedData.isBytes()) {
                return persistedData.getAsBytes();
            } else  {
                throw new DeserializationException("Data is not a Bytes");
            }
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public ByteBuffer getAsByteBuffer() {
        return ByteBuffer.wrap(getAsBytes());
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return data.get(index);
    }

    @Override
    public List<PersistedData> getAsValueArray() {
        return data;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return data.iterator();
    }
}

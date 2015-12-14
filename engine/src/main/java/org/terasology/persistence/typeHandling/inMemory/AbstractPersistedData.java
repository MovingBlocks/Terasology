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
package org.terasology.persistence.typeHandling.inMemory;

import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;

import java.nio.ByteBuffer;

/**
 */
public abstract class AbstractPersistedData implements PersistedData {

    @Override
    public String getAsString() {
        throw new ClassCastException("Data is not a string");
    }

    @Override
    public double getAsDouble() {
        throw new ClassCastException("Data is not a number");
    }

    @Override
    public float getAsFloat() {
        throw new ClassCastException("Data is not a number");
    }

    @Override
    public int getAsInteger() {
        throw new ClassCastException("Data is not a number");
    }

    @Override
    public long getAsLong() {
        throw new ClassCastException("Data is not a number");
    }

    @Override
    public boolean getAsBoolean() {
        throw new ClassCastException("Data is not a boolean");
    }

    @Override
    public byte[] getAsBytes() {
        throw new DeserializationException("Data is not bytes");
    }

    @Override
    public ByteBuffer getAsByteBuffer() {
        throw new DeserializationException("Data is not bytes");
    }

    @Override
    public PersistedDataArray getAsArray() {
        throw new IllegalStateException("Data is not an array");
    }

    @Override
    public PersistedDataMap getAsValueMap() {
        throw new IllegalStateException("Data is not a value map");
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isBytes() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isValueMap() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }
}

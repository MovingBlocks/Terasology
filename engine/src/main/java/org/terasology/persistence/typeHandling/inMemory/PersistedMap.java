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

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;

import java.util.Map;
import java.util.Set;

/**
 */
public class PersistedMap extends AbstractPersistedData implements PersistedDataMap {
    private Map<String, PersistedData> map;

    public PersistedMap(Map<String, PersistedData> map) {
        this.map = map;
    }

    @Override
    public PersistedDataMap getAsValueMap() {
        return this;
    }

    @Override
    public boolean isValueMap() {
        return true;
    }

    @Override
    public boolean has(String name) {
        return map.containsKey(name);
    }

    @Override
    public PersistedData get(String name) {
        return map.get(name);
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
        return map.entrySet();
    }
}

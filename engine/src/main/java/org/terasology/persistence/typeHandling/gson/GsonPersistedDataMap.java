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
package org.terasology.persistence.typeHandling.gson;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class GsonPersistedDataMap extends AbstractGsonPersistedData implements PersistedDataMap {

    private JsonObject map;

    public GsonPersistedDataMap(JsonObject map) {
        this.map = map;
    }

    @Override
    public JsonElement getElement() {
        return map;
    }

    @Override
    public PersistedDataArray getAsArray() {
        throw new IllegalStateException("Data is not an array");
    }

    @Override
    public boolean has(String name) {
        return map.has(name);
    }

    @Override
    public PersistedData get(String name) {
        return new GsonPersistedData(map.get(name));
    }

    @Override
    public float getAsFloat(String name) {
        return map.getAsJsonPrimitive(name).getAsFloat();
    }

    @Override
    public int getAsInteger(String name) {
        return map.getAsJsonPrimitive(name).getAsInt();
    }

    @Override
    public double getAsDouble(String name) {
        return map.getAsJsonPrimitive(name).getAsDouble();
    }

    @Override
    public long getAsLong(String name) {
        return map.getAsJsonPrimitive(name).getAsLong();
    }

    @Override
    public String getAsString(String name) {
        return map.getAsJsonPrimitive(name).getAsString();
    }

    @Override
    public boolean getAsBoolean(String name) {
        return map.getAsJsonPrimitive(name).getAsBoolean();
    }

    @Override
    public PersistedDataMap getAsMap(String name) {
        return new GsonPersistedDataMap(map.getAsJsonObject(name));
    }

    @Override
    public PersistedDataArray getAsArray(String name) {
        return new GsonPersistedDataArray(map.getAsJsonArray(name));
    }

    @Override
    public Set<Map.Entry<String, PersistedData>> entrySet() {
        Set<Map.Entry<String, PersistedData>> entries = Sets.newLinkedHashSet();
        for (Map.Entry<String, JsonElement> element : map.entrySet()) {
            entries.add(new AbstractMap.SimpleEntry<String, PersistedData>(element.getKey(), new GsonPersistedData(element.getValue())));
        }
        return entries;
    }
}

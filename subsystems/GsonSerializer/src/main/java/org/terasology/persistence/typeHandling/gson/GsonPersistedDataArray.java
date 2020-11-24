// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.gson;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;

import java.util.Iterator;
import java.util.List;

/**
 */
public class GsonPersistedDataArray extends AbstractGsonPersistedData implements PersistedDataArray {

    private JsonArray array;

    public GsonPersistedDataArray(JsonArray array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return new GsonPersistedData(array.get(index));
    }

    @Override
    public boolean isNumberArray() {
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isBooleanArray() {
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isStringArray() {
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getAsStringArray() {
        List<String> result = Lists.newArrayListWithCapacity(size());
        for (JsonElement element : array) {
            result.add(element.getAsString());
        }
        return result;
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        TDoubleList result = new TDoubleArrayList(size());
        for (JsonElement element : array) {
            result.add(element.getAsDouble());
        }
        return result;
    }

    @Override
    public TFloatList getAsFloatArray() {
        TFloatList result = new TFloatArrayList(size());
        for (JsonElement element : array) {
            result.add(element.getAsFloat());
        }
        return result;
    }

    @Override
    public TIntList getAsIntegerArray() {
        TIntList result = new TIntArrayList(size());
        for (JsonElement element : array) {
            result.add(element.getAsInt());
        }
        return result;
    }

    @Override
    public TLongList getAsLongArray() {
        TLongList result = new TLongArrayList(size());
        for (JsonElement element : array) {
            result.add(element.getAsLong());
        }
        return result;
    }

    @Override
    public boolean[] getAsBooleanArray() {
        boolean[] result = new boolean[size()];
        for (int i = 0; i < size(); ++i) {
            result[i] = array.get(i).getAsBoolean();
        }
        return result;
    }

    @Override
    public List<PersistedData> getAsValueArray() {
        List<PersistedData> result = Lists.newArrayListWithCapacity(array.size());
        for (JsonElement childElement : array) {
            result.add(new GsonPersistedData(childElement));
        }
        return result;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return getAsValueArray().iterator();
    }

    @Override
    public JsonElement getElement() {
        return array;
    }

    @Override
    public PersistedDataArray getAsArray() {
        return this;
    }
}

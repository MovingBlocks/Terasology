// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedDouble;

import java.util.Iterator;

public class PersistedDoubleArray extends PersistedNumberArray {

    private final TDoubleList data;

    public PersistedDoubleArray(TDoubleList data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        return data;
    }

    @Override
    public TFloatList getAsFloatArray() {
        return TroveUtils.doubleToFloat(data);
    }

    @Override
    public TIntList getAsIntegerArray() {
        return TroveUtils.doubleToInt(data);
    }

    @Override
    public TLongList getAsLongArray() {
        return TroveUtils.doubleToLong(data);
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return new PersistedDouble(data.get(index));
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return TroveUtils.iteratorFrom(data);
    }

}

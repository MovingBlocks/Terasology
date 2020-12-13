// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;

import java.util.Iterator;

public class PersistedIntegerArray extends PersistedNumberArray {

    private final TIntList data;

    public PersistedIntegerArray(TIntList data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        return TroveUtils.intToDouble(data);
    }

    @Override
    public TFloatList getAsFloatArray() {
        return TroveUtils.intToFloat(data);
    }

    @Override
    public TIntList getAsIntegerArray() {
        return data;
    }

    @Override
    public TLongList getAsLongArray() {
        return TroveUtils.intToLong(data);
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return new PersistedInteger(data.get(index));
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

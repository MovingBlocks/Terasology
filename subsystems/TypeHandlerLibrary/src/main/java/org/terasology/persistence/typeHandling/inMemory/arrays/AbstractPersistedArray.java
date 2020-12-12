// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import com.google.common.collect.Lists;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.inMemory.AbstractPersistedData;

import java.util.List;

public abstract class AbstractPersistedArray extends AbstractPersistedData implements PersistedDataArray {

    @Override
    public PersistedDataArray getAsArray() {
        return this;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isNumberArray() {
        return false;
    }

    @Override
    public boolean isBooleanArray() {
        return false;
    }

    @Override
    public boolean isStringArray() {
        return false;
    }

    @Override
    public List<String> getAsStringArray() {
        throw new ClassCastException("Data Array is not a String Array");
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        throw new ClassCastException("Data Array is not a Double Array");
    }

    @Override
    public TFloatList getAsFloatArray() {
        throw new ClassCastException("Data Array is not a Float Array");
    }

    @Override
    public TIntList getAsIntegerArray() {
        throw new ClassCastException("Data Array is not a Integer Array");
    }

    @Override
    public TLongList getAsLongArray() {
        throw new ClassCastException("Data Array is not a Long Array");
    }

    @Override
    public List<PersistedData> getAsValueArray() {
        return Lists.newArrayList(iterator());
    }

    @Override
    public boolean[] getAsBooleanArray() {
        throw new ClassCastException("Data Array is not a Boolean Array");
    }
}

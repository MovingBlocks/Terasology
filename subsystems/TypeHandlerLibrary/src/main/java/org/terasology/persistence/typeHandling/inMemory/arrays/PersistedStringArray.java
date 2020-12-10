// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import com.google.common.collect.Lists;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

import java.util.Iterator;
import java.util.List;

public class PersistedStringArray extends AbstractPersistedArray {

    private final List<String> data;

    public PersistedStringArray(List<String> data) {
        this.data = data;
    }

    @Override
    public String getAsString() {
        if (size() == 1) {
            return data.get(0);
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return new PersistedString(data.get(index));
    }

    @Override
    public List<String> getAsStringArray() {
        return Lists.newArrayList(data);
    }


    @Override
    public boolean isStringArray() {
        return true;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return data.stream().map(PersistedString::new).map(ps -> (PersistedData) ps).iterator();
    }
}

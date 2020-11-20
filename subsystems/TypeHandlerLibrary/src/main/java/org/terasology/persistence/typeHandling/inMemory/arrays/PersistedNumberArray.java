// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

public abstract class PersistedNumberArray extends AbstractPersistedArray {
    @Override
    public double getAsDouble() {
        if (size() == 1) {
            return getArrayItem(0).getAsDouble();
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public float getAsFloat() {
        if (size() == 1) {
            return getArrayItem(0).getAsFloat();
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public int getAsInteger() {
        if (size() == 1) {
            return getArrayItem(0).getAsInteger();
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public long getAsLong() {
        if (size() == 1) {
            return getArrayItem(0).getAsLong();
        } else {
            throw new IllegalStateException("Data is an array of size != 1");
        }
    }

    @Override
    public boolean isNumberArray() {
        return true;
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.inMemory;

/**
 */
public class PersistedInteger extends PersistedNumber {
    private int data;

    public PersistedInteger(int data) {
        this.data = data;
    }

    @Override
    public double getAsDouble() {
        return data;
    }

    @Override
    public float getAsFloat() {
        return data;
    }

    @Override
    public int getAsInteger() {
        return data;
    }

    @Override
    public long getAsLong() {
        return data;
    }
}

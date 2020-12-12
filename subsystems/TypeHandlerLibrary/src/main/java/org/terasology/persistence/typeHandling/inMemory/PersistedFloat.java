// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.inMemory;

/**
 */
public class PersistedFloat extends PersistedNumber {

    private float data;

    public PersistedFloat(float data) {
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
        return (int) data;
    }

    @Override
    public long getAsLong() {
        return (long) data;
    }
}

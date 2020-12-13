// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.inMemory;

/**
 */
public class PersistedDouble extends PersistedNumber {
    private double data;

    public PersistedDouble(double data) {
        this.data = data;
    }

    @Override
    public double getAsDouble() {
        return data;
    }

    @Override
    public float getAsFloat() {
        return (float) data;
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

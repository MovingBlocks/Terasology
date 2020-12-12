// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.inMemory;

/**
 */
public class PersistedLong extends PersistedNumber {
    private long data;

    public PersistedLong(long data) {
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
        return data;
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.inMemory;

/**
 */
public abstract class PersistedNumber extends AbstractPersistedData {

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public abstract double getAsDouble();

    @Override
    public abstract float getAsFloat();

    @Override
    public abstract int getAsInteger();

    @Override
    public abstract long getAsLong();
}

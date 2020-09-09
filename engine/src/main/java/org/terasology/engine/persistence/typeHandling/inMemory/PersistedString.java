// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.inMemory;

/**
 */
public class PersistedString extends AbstractPersistedData {

    private final String data;

    public PersistedString(String data) {
        this.data = data;
    }

    @Override
    public String getAsString() {
        return data;
    }

    @Override
    public boolean isString() {
        return true;
    }

}

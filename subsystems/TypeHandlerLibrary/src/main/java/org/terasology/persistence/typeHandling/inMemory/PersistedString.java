// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.inMemory;

import com.google.common.base.MoreObjects;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(data)
                .toString();
    }
}

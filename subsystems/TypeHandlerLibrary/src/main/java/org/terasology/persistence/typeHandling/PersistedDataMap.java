// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.terasology.persistence.typeHandling.inMemory.PersistedMap;

import java.util.Map;
import java.util.Set;

/**
 */
public interface PersistedDataMap extends PersistedData {

    boolean has(String name);

    PersistedData get(String name);

    float getAsFloat(String name);

    int getAsInteger(String name);

    double getAsDouble(String name);

    long getAsLong(String name);

    String getAsString(String name);

    boolean getAsBoolean(String name);

    PersistedDataMap getAsMap(String name);

    PersistedDataArray getAsArray(String name);

    Set<Map.Entry<String, PersistedData>> entrySet();

    static PersistedDataMap of(Map<String, PersistedData> map) {
        return new PersistedMap(map);
    }
}

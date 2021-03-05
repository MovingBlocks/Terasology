// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.collection;

import java.util.EnumSet;

/**
 * EnumMap for storing primitive booleans against each enum value.
 * Values default to false
 *
 */
public class EnumBooleanMap<ENUM extends Enum<ENUM>> {
    private EnumSet<ENUM> store;

    public EnumBooleanMap(Class<ENUM> enumClass) {
        store = EnumSet.noneOf(enumClass);
    }

    public int size() {
        return store.size();
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsKey(ENUM key) {
        return true;
    }

    public boolean get(ENUM key) {
        return store.contains(key);
    }

    public boolean put(ENUM key, boolean value) {
        if (value) {
            return !store.add(key);
        } else {
            return store.remove(key);
        }
    }

    public void putAll(EnumBooleanMap<ENUM> other) {
        store.addAll(other.store);
    }

    /**
     * Sets all values to false
     */
    public void clear() {
        store.clear();
    }
}

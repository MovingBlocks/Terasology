/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities.collection;

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

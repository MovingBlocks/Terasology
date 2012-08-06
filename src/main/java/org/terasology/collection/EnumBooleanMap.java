/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.collection;

/**
 * EnumMap for storing primitive booleans against each enum value.
 * Values default to false
 *
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Remove, use EnumSet instead?
public class EnumBooleanMap<ENUM extends Enum> {
    private boolean[] store;

    public EnumBooleanMap(Class<ENUM> enumClass) {
        store = new boolean[enumClass.getEnumConstants().length];
    }

    public int size() {
        return store.length;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsKey(ENUM key) {
        return true;
    }

    public boolean get(ENUM key) {
        return store[key.ordinal()];
    }

    public boolean put(ENUM key, boolean value) {
        boolean old = store[key.ordinal()];
        store[key.ordinal()] = value;
        return old;
    }

    public void putAll(EnumBooleanMap<ENUM> other) {
        assert other.store.length == store.length;
        for (int i = 0; i < store.length; ++i) {
            store[i] = other.store[i];
        }
    }

    /**
     * Sets all values to false
     */
    public void clear() {
        for (int i = 0; i < store.length; ++i) {
            store[i] = false;
        }
    }
}

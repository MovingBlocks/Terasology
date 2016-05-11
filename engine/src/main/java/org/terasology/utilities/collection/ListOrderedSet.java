/*
 * Copyright 2016 MovingBlocks
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

import java.util.ArrayList;
import java.util.HashMap;

public final class ListOrderedSet<T> {

    private HashMap<T, Integer> map = new HashMap<T, Integer>();
    private ArrayList<T> array = new ArrayList<T>();

    public static <T> ListOrderedSet<T> create() {
        return new ListOrderedSet<>();
    }

    public boolean add(T object) {
        if (!map.containsKey(object)) {
            array.add(object);
            map.put(object, array.size() - 1);
            return true;
        }
        return false;
    }

    public T get(int index) {
        return array.get(index);
    }

    public boolean contains(T object) {
        return map.containsKey(object);
    }

    public boolean remove(T object) {
        Integer index = map.remove(object);
        if (index != null) {
            array.remove(index.intValue());
            return true;
        }

        return false;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
        array.clear();
    }

    public T getLast() {
        return array.get(array.size() - 1);
    }
}

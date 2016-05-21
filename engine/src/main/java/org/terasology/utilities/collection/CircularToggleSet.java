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

import java.util.HashMap;
import java.util.Map;

/**
 * CircularToggleSet is a "toggleable" doubly-linked list, provides uniqueness with the help of HashMap.
 * <p>
 * CircularToggleSet's {@link #toggle()} provides iteration through out given elements. Whenever, {@link #toggle()} is called
 * first-inserted element is returned. Therefore, iteration starts from beginning again.
 *
 * @param <T>
 */
public class CircularToggleSet<T> {

    private Element<T> head;
    private Element<T> next;
    private Element<T> cursor;
    private Map<T, Element<T>> hashMap;


    public CircularToggleSet() {
        hashMap = new HashMap<T, Element<T>>();
        clear();
    }

    public void clear() {
        head = null;
        next = new Element<T>(null, null, null);
        hashMap.clear();
        cursor = null;
    }

    public T toggle() {
        if (head == null) {
            return null;
        }

        if (cursor == null) {
            cursor = head;
        } else {
            do {
                cursor = cursor.next;
            } while (cursor.value == null);
        }

        return cursor.value;
    }

    public boolean add(T item) {
        if (hashMap.containsKey(item)) {
            return false;
        }

        if (head == null) {
            head = new Element<>(item, null, null);
            next.previous = head;
            head.previous = next;
            head.next = next;
            hashMap.put(item, head);
            next.next = head;
        } else {
            next.value = item;
            hashMap.put(item, next);
            next.next = new Element<>(null, null, next);
            next = next.next;
            next.next = head;
            head.previous = next;
        }
        return true;
    }

    public boolean remove(T item) {
        if (!hashMap.containsKey(item)) {
            return false;
        }

        Element<T> element = hashMap.get(item);
        Element<T> prev = element.previous;
        Element<T> subsequent = element.next;

        if (prev == subsequent) {
            clear();
        } else {
            prev.next = subsequent;
            subsequent.previous = prev;

            if (cursor == element) {
                cursor = subsequent;
            }
            if (head == element) {
                head = subsequent;
            }

        }
        hashMap.remove(item);
        return true;
    }

    public int size() {
        return hashMap.size();
    }

    private class Element<V> {
        V value;
        Element<V> next;
        Element<V> previous;

        Element(V value, Element<V> next, Element<V> previous) {
            this.value = value;
            this.next = next;
            this.previous = previous;
        }

    }

}

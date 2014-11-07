/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.utilities.tree;

import java.util.Collection;

/**
 * Data structure that allows to quickly find values nearest to the specified position.
 *
 * The data structure is populated with add() method and values can be removed with remove() method.
 *
 * This data structure allows to store only one value for each position, where position duplication is defined on
 * the implementation level of this interface.
 *
 * @param <T> Type of objects stored in the data structure.
 */
public interface DimensionalMap<T> {
    /**
     * Adds new value for the specified position.
     *
     * @param position Position to add at.
     * @param value Value to add at the position.
     * @return Previous value (if any) that was stored at that position.
     */
    T add(float[] position, T value);

    /**
     * Removes the value at the specified position.
     *
     * @param position The position to remove a value for.
     * @return A value (if any) that was stored at that position.
     */
    T remove(float[] position);

    /**
     * Finds nearest stored value for the defined position.
     *
     * @param position Position to search for.
     * @return An entry describing the value and distance from the specified position.
     */
    Entry<T> findNearest(float[] position);

    /**
     * Finds nearest stored value for the defined position within the defined maximum distance.
     * @param position Position to search for.
     * @param within A maximum distance from the given position to search for.
     * @return An entry describing the value and distance from the specified position.
     */
    Entry<T> findNearest(float[] position, float within);

    /**
     * Finds nearest "count" stored values for the defined position.
     *
     * @param position Position to search for.
     * @param count An amount of values to return (is possible).
     * @return A collection of entries describing the value and distance from the specified position. The collection
     * is sorted by distance.
     */
    Collection<Entry<T>> findNearest(float[] position, int count);

    /**
     * Finds nearest "count" stored values for the defined position within the defined maximum distance.
     *
     * @param position Position to search for.
     * @param count An amount of values to return (is possible).
     * @param within A maximum distance from the given position to search for.
     * @return A collection of entries describing the value and distance from the specified position. The collection
     * is sorted by distance.
     */
    Collection<Entry<T>> findNearest(float[] position, int count, float within);

    public class Entry<T> {
        public final float distance;
        public final T value;

        public Entry(float distance, T value) {
            this.distance = distance;
            this.value = value;
        }
    }
}

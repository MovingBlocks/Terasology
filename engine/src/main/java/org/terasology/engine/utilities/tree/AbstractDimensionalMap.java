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

public abstract class AbstractDimensionalMap<T> implements DimensionalMap<T> {
    @Override
    public Entry<T> findNearest(float[] position) {
        return findNearest(position, Float.MAX_VALUE);
    }

    @Override
    public Entry<T> findNearest(float[] position, float within) {
        Collection<Entry<T>> nearest = findNearest(position, 1, within);
        if (nearest.size() == 0) {
            return null;
        }
        return nearest.iterator().next();
    }

    @Override
    public Collection<Entry<T>> findNearest(float[] position, int count) {
        return findNearest(position, count, Float.MAX_VALUE);
    }
}

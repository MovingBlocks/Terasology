// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.tree;

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

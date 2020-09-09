// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.picker;

import org.terasology.math.geom.BaseVector2f;

import java.util.Set;

/**
 * Retrieves a set of circular objects in the proximity of a given anchor point.
 *
 * @param <T> the object type
 */
public interface CirclePicker<T> {

    void offer(float locX, float locY, T object);

    default void offer(BaseVector2f location, T object) {
        offer(location.getX(), location.getY(), object);
    }

    Set<T> getAll();
}

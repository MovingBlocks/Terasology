// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.viewer.picker;

import java.util.Set;

import org.joml.Vector2fc;
import org.terasology.math.geom.BaseVector2f;

/**
 * Retrieves a set of circular objects in the proximity of a given anchor point.
 * @param <T> the object type
 */
public interface CirclePicker<T> {

    void offer(float locX, float locY, T object);

    /** @deprecated use {@link #offer(Vector2fc, Object)} instead. */
    @Deprecated
    default void offer(BaseVector2f location, T object) {
        offer(location.getX(), location.getY(), object);
    }
    
    default void offer(Vector2fc location, T object) {
        offer(location.x(), location.y(), object);
    }

    Set<T> getAll();
}

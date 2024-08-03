// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.picker;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * Retrieves the closest (circular) object from a collection of tested elements.
 * @param <T> the object type
 */
public class CirclePickerClosest<T> implements CirclePicker<T> {

    private final Vector2fc cursor;
    private final Function<? super T, ? extends Number> radiusFunc;

    private double minDistSq = Double.POSITIVE_INFINITY;
    private T closest;

    /**
     * No minimum distance to the target is required
     * @param target the target location
     */
    public CirclePickerClosest(Vector2fc target) {
        this(target, ignored -> Double.POSITIVE_INFINITY);
    }

    /**
     * Matches all elements that lie within a given radius
     * @param target the target location
     * @param radiusFunc the radius function for each of the tested elements
     */
    public CirclePickerClosest(Vector2fc target, Function<? super T, ? extends Number> radiusFunc) {
        this.cursor = new Vector2f(target);
        this.radiusFunc = radiusFunc;
    }

    @Override
    public void offer(float locX, float locY, T object) {
        float dx = cursor.x() - locX;
        float dy = cursor.y() - locY;
        float distSq = dx * dx + dy * dy;
        float rad = radiusFunc.apply(object).floatValue();

        if (distSq < rad * rad && distSq < minDistSq) {
            minDistSq = distSq;
            closest = object;
        }
    }

    /**
     * @return the closest element, or <code>null</code>.
     */
    public T getClosest() {
        return closest;
    }

    @Override
    public Set<T> getAll() {
        if (closest != null) {
            return Collections.singleton(closest);
        } else {
            return Collections.emptySet();
        }
    }
}


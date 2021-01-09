// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.viewer.picker;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.joml.Vector2fc;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;

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
    public CirclePickerClosest(BaseVector2f target) {
        this(target, ignored -> Double.POSITIVE_INFINITY);
    }

    /**
     * Matches all elements that lie within a given radius
     * @param target the target location
     * @param radiusFunc the radius function for each of the tested elements
     *
     * @deprecated use {@link CirclePickerClosest#CirclePickerClosest(Vector2fc, Function)} instead
     */
    @Deprecated
    public CirclePickerClosest(BaseVector2f target, Function<? super T, ? extends Number> radiusFunc) {
        this(JomlUtil.from(target), radiusFunc);
    }

    /**
     * Matches all elements that lie within a given radius
     * @param target the target location
     * @param radiusFunc the radius function for each of the tested elements
     */
    public CirclePickerClosest(Vector2fc target, Function<? super T, ? extends Number> radiusFunc) {
        this.cursor = new org.joml.Vector2f(target);
        this.radiusFunc = radiusFunc;
    }

    @Override
    public void offer(float locX, float locY, T object) {
        float dx = cursor.x() - locX;
        float dy = cursor.y() - locY;
        float distSq = dx * dx + dy * dy;
        float rad = radiusFunc.apply(object).floatValue();

        if (distSq < rad * rad) {
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = object;
            }
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


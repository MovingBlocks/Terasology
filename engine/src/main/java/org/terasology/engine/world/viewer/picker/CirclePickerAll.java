// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.viewer.picker;

import org.joml.Vector2fc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class CirclePickerAll<T> implements CirclePicker<T> {
    private final Set<T> hits = new HashSet<>();

    private final Vector2fc cursor;
    private final Function<? super T, ? extends Number> radiusFunc;

    public CirclePickerAll(Vector2fc cursor, Function<? super T, ? extends Number> radiusFunc) {
        this.cursor = new org.joml.Vector2f(cursor);
        this.radiusFunc = radiusFunc;
    }

    @Override
    public void offer(float locX, float locY, T object) {
        float dx = cursor.x() - locX;
        float dy = cursor.y() - locY;
        float distSq = dx * dx + dy * dy;

        float rad = radiusFunc.apply(object).floatValue();
        if (distSq <= rad * rad) {
            hits.add(object);
        }
    }

    @Override
    public Set<T> getAll() {
        return Collections.unmodifiableSet(hits);
    }
}

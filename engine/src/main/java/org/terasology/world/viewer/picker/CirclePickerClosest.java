/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"){ }
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.viewer.picker;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.terasology.math.geom.BaseVector2f;

/**
 * Retrieves the closest (circular) object.
 * @param <T> the object type
 * @author Martin Steiger
 */
public class CirclePickerClosest<T> implements CirclePicker<T> {

    private final BaseVector2f cursor;
    private final Function<? super T, ? extends Number> radiusFunc;

    private double minDistSq = Double.MAX_VALUE;
    private T closest;

    public CirclePickerClosest(BaseVector2f cursor, Function<? super T, ? extends Number> radiusFunc) {
        this.cursor = cursor;
        this.radiusFunc = radiusFunc;
    }

    @Override
    public void offer(float locX, float locY, T object) {
        float dx = cursor.getX() - locX;
        float dy = cursor.getY() - locY;
        float distSq = dx * dx + dy * dy;
        float rad = radiusFunc.apply(object).floatValue();

        if (distSq < rad * rad) {
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = object;
            }
        }
    }

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


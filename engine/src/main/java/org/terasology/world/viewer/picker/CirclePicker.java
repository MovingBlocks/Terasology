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

import java.util.Set;

import org.terasology.math.geom.BaseVector2f;

/**
 * Retrieves a set of circular objects in the proximity of a given anchor point.
 * @param <T> the object type
 */
public interface CirclePicker<T> {

    void offer(float locX, float locY, T object);

    default void offer(BaseVector2f location, T object) {
        offer(location.getX(), location.getY(), object);
    }

    Set<T> getAll();
}

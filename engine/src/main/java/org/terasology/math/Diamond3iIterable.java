/*
 * Copyright 2020 MovingBlocks
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

package org.terasology.math;

import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Iterator;

/**
 * Diamond3Iterable iterates in a diamond shape hull where the manhattan distance is the same for that level.
 * for each group of iterations the manhattan distance decreases. inner and outer radiuses can be set where
 * the shape can be an hull with distance 1 to a full solid.
 */
public final class Diamond3iIterable implements Iterable<Vector3ic> {

    private final Vector3i origin = new Vector3i();
    private final int endDistance;
    private int startDistance = 0;

    /**
     *
     * @param origin center point of Diamond3iIterator
     * @param maxDistance maxDistance away
     */
    private Diamond3iIterable(Vector3ic origin, int maxDistance) {
        Preconditions.checkArgument(maxDistance >= 0, "maxDistance must be >= 0");
        this.origin.set(origin);
        this.endDistance = maxDistance;
    }

    /**
     *
     * @param origin center point of Diamond3iIterator
     * @param startDistance inner distance outwards
     * @param maxDistance maxDistance away
     */
    private Diamond3iIterable(Vector3ic origin, int startDistance, int maxDistance) {
        Preconditions.checkArgument(startDistance < maxDistance, "startDistance must be < maxDistance");
        Preconditions.checkArgument(maxDistance >= 0, "maxDistance must be >= 0");
        Preconditions.checkArgument(startDistance >= 0, "startDistance must be >= 0");

        this.origin.set(origin);
        this.endDistance = maxDistance;
        this.startDistance = startDistance;
    }

    /**
     * solid diamond shape from the center to a radius
     * @param origin center of diamond iterator
     * @param radius distance to iterate out to
     */
    public static Diamond3iIterable.Builder region(Vector3ic origin, int radius) {
        return new Diamond3iIterable.Builder(origin, radius);
    }

    /**
     * hollow shape with an inner and outer distance to iterate over
     * @param origin  center of diamond iterator
     * @param start distance to start iterating from
     * @param end distance to iterate out to
     */
    public static Diamond3iIterable.Builder hollow(Vector3ic origin, int start, int end) {
        return new Diamond3iIterable.Builder(origin, end).start(start);
    }

    /**
     * iterates in a shell that is 1 block wide
     * @param origin center of shell
     * @param radius distance for 1 block wide shell
     */
    public static Diamond3iIterable.Builder shell(Vector3ic origin, int radius) {
        return new Diamond3iIterable.Builder(origin, radius).start(radius - 1);
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        Vector3i pos = new Vector3i();
        final int[] level = {this.startDistance};
        final int[] offset = {-this.startDistance,0,0};

        return new Iterator<Vector3ic>() {
            @Override
            public boolean hasNext() {
                return level[0] < endDistance;
            }

            @Override
            public Vector3ic next() {
                pos.set(origin.x + offset[0], origin.y + offset[1], origin.z + offset[2]);
                if (offset[2] < 0) {
                    offset[2] *= -1;
                } else if (offset[1] < 0) {
                    offset[1] *= -1;
                    offset[2] = -(level[0] - TeraMath.fastAbs(offset[0]) - TeraMath.fastAbs(offset[1]));
                } else {
                    offset[1] = -offset[1] + 1;
                    if (offset[1] > 0) {
                        if (++offset[0] <= level[0]) {
                            offset[1] = TeraMath.fastAbs(offset[0]) - level[0];
                            offset[2] = 0;
                        } else {
                            level[0]++;
                            offset[0] = -level[0];
                            offset[1] = 0;
                            offset[2] = 0;
                        }
                    } else {
                        offset[2] = -(level[0] - TeraMath.fastAbs(offset[0]) - TeraMath.fastAbs(offset[1]));
                    }
                }
                return pos;
            }
        };
    }

    public static final class Builder {
        private final Vector3ic origin;
        private final int endDistance;
        private int startDistance = 0;

        /**
         * @param origin center region for iterator
         * @param endDistance maximums radius
         */
        private Builder(Vector3ic origin, int endDistance){
            this.origin = origin;
            this.endDistance = endDistance;
        }

        /**
         * Default start distance is 0
         * @param start  the minimum radius
         */
        public Diamond3iIterable.Builder start(int start) {
            this.startDistance = start;
            return this;
        }

        public Diamond3iIterable build() {
            return new Diamond3iIterable(origin,startDistance,endDistance);
        }

    }

}

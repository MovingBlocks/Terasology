// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.math;

import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.TeraMath;

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
        return new Diamond3iIterable.Builder(origin, radius).start(radius);
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        Vector3i pos = new Vector3i();
        final int[] level = {this.startDistance};
        final Vector3i offset = new Vector3i(-this.startDistance, 0, 0);

        return new Iterator<Vector3ic>() {
            @Override
            public boolean hasNext() {
                return level[0] < endDistance;
            }

            @Override
            public Vector3ic next() {
                origin.add(offset, pos);
                if (offset.z < 0) {
                    offset.z *= -1;
                } else if (offset.y() < 0) {
                    offset.y *= -1;
                    offset.z = -(level[0] - TeraMath.fastAbs(offset.x()) - TeraMath.fastAbs(offset.y()));
                } else {
                    offset.y = -offset.y() + 1;
                    if (offset.y > 0) {
                        if (++offset.x <= level[0]) {
                            offset.y = TeraMath.fastAbs(offset.x) - level[0];
                            offset.z = 0;
                        } else {
                            level[0]++;
                            offset.x = -level[0];
                            offset.y = 0;
                            offset.z = 0;
                        }
                    } else {
                        offset.z = -(level[0] - TeraMath.fastAbs(offset.x) - TeraMath.fastAbs(offset.y));
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
        private Builder(Vector3ic origin, int endDistance) {
            this.origin = origin;
            this.endDistance = endDistance + 1;
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
            return new Diamond3iIterable(origin, startDistance, endDistance);
        }

    }

}

/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.math.geom.Vector3i;

import java.util.Iterator;

/**
 */
public final class Diamond3iIterator implements Iterator<Vector3i> {

    private final Vector3i origin;
    private int maxDistance;

    private int x;
    private int y;
    private int z;
    private int level;

    private Diamond3iIterator(Vector3i origin, int maxDistance) {
        this.origin = origin;
        this.maxDistance = maxDistance + 1;
    }

    private Diamond3iIterator(Vector3i origin, int maxDistance, int startDistance) {
        this(origin, maxDistance);
        this.level = startDistance + 1;
        x = -level;
    }

    public static Iterable<Vector3i> iterate(final Vector3i origin, final int distance) {
        return () -> new Diamond3iIterator(origin, distance);
    }

    public static Iterable<Vector3i> iterate(final Vector3i origin, final int distance, final int startDistance) {
        return () -> new Diamond3iIterator(origin, distance, startDistance);
    }

    public static Iterable<Vector3i> iterateAtDistance(final Vector3i origin, final int distance) {
        return () -> new Diamond3iIterator(origin, distance, distance - 1);
    }

    @Override
    public boolean hasNext() {
        return level < maxDistance;
    }

    @Override
    public Vector3i next() {
        Vector3i result = new Vector3i(origin.x + x, origin.y + y, origin.z + z);
        if (z < 0) {
            z *= -1;
        } else if (y < 0) {
            y *= -1;
            z = -(level - TeraMath.fastAbs(x) - TeraMath.fastAbs(y));
        } else {
            y = -y + 1;
            if (y > 0) {
                if (++x <= level) {
                    y = TeraMath.fastAbs(x) - level;
                    z = 0;
                } else {
                    level++;
                    x = -level;
                    y = 0;
                    z = 0;
                }
            } else {
                z = -(level - TeraMath.fastAbs(x) - TeraMath.fastAbs(y));
            }
        }

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }


}

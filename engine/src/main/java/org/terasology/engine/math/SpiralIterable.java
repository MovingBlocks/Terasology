// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import com.google.common.base.Preconditions;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterable} that iterates in
 * a square-shapes spiral around the central point (inclusive).
 * <br><br>
 * The iteration starts in positive x direction.
 * <br><br>
 * The iterating vector is reused. <b>Do not attempt to store the instance</b> e.g. in a collection.
 */
public class SpiralIterable implements Iterable<Vector2ic> {

    /**
     * (MAX_SIDELEN * 2 + 1) ^2 must be < Integer.MAX_VALUE
     */
    private static final int MAX_RADIUS = 23169;

    private final Vector2ic center;
    private final boolean clockwise;
    private final int maxArea;
    private final int scale;

    /**
     * @param center the spiral center
     * @param clockwise true for clockwise iteration, false for counter-clockwise
     * @param scale the scale of the iteration (positive integer)
     * @param maxRadius the maximum radius of the spiral [0..23169] (inclusive)
     */
    private SpiralIterable(Vector2ic center, boolean clockwise, int scale, int maxRadius) {
        Preconditions.checkArgument(scale > 0, "scale must be > 0");
        Preconditions.checkArgument(maxRadius >= 0, "maxRadius must be >= 0");
        Preconditions.checkArgument(maxRadius <= MAX_RADIUS, "maxRadius must be <= " + MAX_RADIUS);

        int sideLen = maxRadius * 2 + 1;

        this.scale = scale;
        this.center = center;
        this.maxArea = sideLen * sideLen;
        this.clockwise = clockwise;
    }

    /**
     * Iterates in clock-wise orientation around the given point.
     * The point will be the first iterated point.
     * @param center the spiral center
     */
    public static SpiralIterable.Builder clockwise(Vector2ic center) {
        return new SpiralIterable.Builder(center, true);
    }

    /**
     * Iterates in clock-wise orientation around the given point.
     * The point will be the first iterated point.
     * @param center the spiral center
     */
    public static SpiralIterable.Builder counterClockwise(Vector2ic center) {
        return new SpiralIterable.Builder(center, false);
    }

    @Override
    public Iterator<Vector2ic> iterator() {

        return new Iterator<Vector2ic>() {
            private int radius = 1;
            private int leg;
            private int x = -1;
            private int y;
            private int index;

            private final Vector2i pos = new Vector2i();

            @Override
            public Vector2ic next() {
                if (index >= maxArea) {
                    throw new NoSuchElementException("radius has been reached");
                }

                switch (leg) {
                    case 0:
                        ++x;
                        if (x == radius) {
                            ++leg;
                        }
                        break;
                    case 1:
                        ++y;
                        if (y == radius) {
                            ++leg;
                        }
                        break;
                    case 2:
                        --x;
                        if (-x == radius) {
                            ++leg;
                        }
                        break;
                    case 3:
                        --y;
                        if (-y == radius) {
                            leg = 0;
                            ++radius;
                        }
                        break;
                }

                index++;
                int finalX = center.x() + x * scale;
                int finalY = center.y() + (clockwise ? y : -y) * scale;
                pos.set(finalX, finalY);
                return pos;
            }

            @Override
            public boolean hasNext() {
                return index < maxArea;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    public static final class Builder {

        private final Vector2ic center;
        private final boolean clockwise;
        private int maxRadius = MAX_RADIUS;
        private int scale = 1;

        private Builder(Vector2ic center, boolean clockwise) {
            this.clockwise = clockwise;
            this.center = center;
        }

        /**
         * Default value is 23169.
         * @param newRadius the maximum radius of the spiral [0..23169] (inclusive)
         * @return this
         */
        public SpiralIterable.Builder maxRadius(int newRadius) {
            this.maxRadius = newRadius;
            return this;
        }

        /**
         * Default value is 1.
         * @param newScale the scale of the iteration (positive integer)
         * @return this
         */
        public SpiralIterable.Builder scale(int newScale) {
            this.scale = newScale;
            return this;
        }

        public SpiralIterable build() {
            return new SpiralIterable(center, clockwise, scale, maxRadius);
        }
    }
}

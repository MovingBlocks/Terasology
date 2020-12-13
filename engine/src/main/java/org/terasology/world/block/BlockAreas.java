// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Math;
import org.joml.Rectanglef;
import org.joml.RoundingMode;
import org.joml.Vector2ic;

public final class BlockAreas {
    private BlockAreas() {
    }

    public static BlockArea fromMinAndMax(Vector2ic min, Vector2ic max) {
        return new BlockArea().setMin(min).setMax(max);
    }

    public static BlockArea fromMinAndMax(int minX, int minY, int maxX, int maxY) {
        return new BlockArea().setMin(minX, minY).setMax(maxX, maxY);
    }

    public static BlockArea fromWorldArea(Rectanglef rect) {
        return BlockAreas.fromMinAndMax(
                Math.roundUsing(rect.minX, RoundingMode.CEILING),
                Math.roundUsing(rect.minY, RoundingMode.CEILING),
                Math.roundUsing(rect.maxX, RoundingMode.FLOOR),
                Math.roundUsing(rect.maxY, RoundingMode.FLOOR));
    }

    public static final class Intersections {
        private Intersections() {
        }

        /**
         * Test whether the block area and the axis-aligned rectangle in world coordinates intersect.
         *
         * @param area the block area to test
         * @param rectangle the rectangle to test against in world coordinates
         * @return {@code true} iff the given rectangle intersects with the world area of the block area; {@code false}
         *         otherwise
         */
        public static boolean intersectsRectangle(BlockArea area, Rectanglef rectangle) {
            return area.getWorldArea().intersectsRectangle(rectangle);
        }
    }
}

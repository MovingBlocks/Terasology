/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.terasology.core.world.generator.facetProviders;

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Predicate;

/**
 * A collection of filters that restrict the placement of objects
 * @author Martin Steiger
 */
public final class PositionFilters {

    private PositionFilters() {
        // no instances
    }

    /**
     * Filters based on the vector's y value
     * @return a predicate that returns true only if (y > height)
     */
    public static Predicate<Vector3i> minHeight(final int height) {
        return new Predicate<Vector3i>() {

            @Override
            public boolean apply(Vector3i input) {
                return input.getY() > height;
            }
        };
    }

    /**
     * Filters based on the density
     * @param density the density facet that contains all tested coords.
     * @return a predicate that returns true if (density >= 0) and (density < 0) for the block at (y - 1)
     */
    public static Predicate<Vector3i> density(final DensityFacet density) {
        return new Predicate<Vector3i>() {

            @Override
            public boolean apply(Vector3i input) {
                // pass if the block on the surface is dense enough
                float densBelow = density.getWorld(input.getX(), input.getY() - 1, input.getZ());
                float densThis = density.getWorld(input);
                return (densBelow >= 0 && densThis < 0);
            }
        };
    }

    /**
     * Filters based on surface flatness
     * @param surface the surface height facet that contains all tested coords.
     * @return a predicate that returns true only if there is a level surface in adjacent directions
     */
    public static Predicate<Vector3i> flatness(final SurfaceHeightFacet surface) {
        return new Predicate<Vector3i>() {

            @Override
            public boolean apply(Vector3i input) {
                int x = input.getX();
                int y = input.getY();
                int z = input.getZ();

                return (TeraMath.ceilToInt(surface.getWorld(x - 1, z)) == y)
                    && (TeraMath.ceilToInt(surface.getWorld(x + 1, z)) == y)
                    && (TeraMath.ceilToInt(surface.getWorld(x, z - 1)) == y)
                    && (TeraMath.ceilToInt(surface.getWorld(x, z + 1)) == y);
            }
        };
    }

    /**
     * Filters based on a random noise
     * @param treeNoise the noise table
     * @param density the threshold in [0..1]
     * @return true if the noise value is below the threshold
     */
    public static Predicate<Vector3i> probability(NoiseTable treeNoise, float density) {
        return new Predicate<Vector3i>() {

            @Override
            public boolean apply(Vector3i input) {
                return treeNoise.noise(input.getX(), input.getZ()) / 255f < density;
            }
        };
    }

}

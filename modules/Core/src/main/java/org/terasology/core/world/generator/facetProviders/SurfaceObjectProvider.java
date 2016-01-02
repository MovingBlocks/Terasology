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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.ObjectFacet2D;
import org.terasology.world.generation.facets.base.ObjectFacet3D;

import java.util.List;
import java.util.Map;

/**
 * Places objects on the surface based on population densities
 * for a environmental variable (e.g. biome).
 *
 */
@Requires(@Facet(SurfaceHeightFacet.class))
public abstract class SurfaceObjectProvider<B, T> implements FacetProvider {

    private Noise typeNoiseGen;

    private final Table<B, T, Float> probsTable = HashBasedTable.create();

    @Override
    public void setSeed(long seed) {
        typeNoiseGen = new WhiteNoise(seed + 1);
    }

    /**
     * Populates a given facet based on filters and population densities
     *
     * @param facet        the facet to populate
     * @param surfaceFacet the surface height facet
     * @param typeFacet    the facet that provides the environment
     * @param filters      a set of filters
     */
    protected void populateFacet(ObjectFacet3D<T> facet, SurfaceHeightFacet surfaceFacet, ObjectFacet2D<? extends B> typeFacet, List<Predicate<Vector3i>> filters) {

        Region3i worldRegion = facet.getWorldRegion();

        int minY = worldRegion.minY();
        int maxY = worldRegion.maxY();

        Vector3i pos = new Vector3i();

        for (int z = worldRegion.minZ(); z <= worldRegion.maxZ(); z++) {
            for (int x = worldRegion.minX(); x <= worldRegion.maxX(); x++) {
                int height = TeraMath.floorToInt(surfaceFacet.getWorld(x, z)) + 1;

                // if the surface is in range
                if (height >= minY && height <= maxY) {

                    pos.set(x, height, z);

                    // if all predicates match
                    if (applyAll(filters, pos)) {
                        B biome = typeFacet.getWorld(x, z);
                        Map<T, Float> plantProb = probsTable.row(biome);
                        T type = getType(x, z, plantProb);
                        if (type != null) {
                            facet.setWorld(x, height, z, type);
                        }
                    }
                }
            }
        }

    }

    private boolean applyAll(List<Predicate<Vector3i>> components, Vector3i pos) {
        // Similar to guava's implementation of Predicates#all
        // According to google, using indices is superior to using an Iterator
        // This implementation also avoids duplicating the list
        for (Predicate<Vector3i> component : components) {
            if (!component.apply(pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Registers an object type with a certain population density based on an environmental variable
     *
     * @param biome       the environment type (e.g. biome)
     * @param tree        the object type
     * @param probability the population density in [0..1]
     * @throws IllegalArgumentException if probability is not in [0..1]
     */
    protected void register(B biome, T tree, float probability) {
        Preconditions.checkArgument(probability >= 0, "probability must be >= 0");
        Preconditions.checkArgument(probability <= 1, "probability must be <= 1");

        probsTable.put(biome, tree, probability);
    }

    /**
     * Clears all registered population densities
     */
    protected void clearProbabilities() {
        probsTable.clear();
    }

    /**
     * @param x    the x coordinate
     * @param z    the z coordinate
     * @param objs a map (objType to probability)
     * @return a random pick from the map or <code>null</code>
     */
    protected T getType(int x, int z, Map<T, Float> objs) {
        float random = Math.abs(typeNoiseGen.noise(x, z));

        for (T generator : objs.keySet()) {
            Float threshold = objs.get(generator);
            if (threshold != null) {
                if (random < threshold) {
                    return generator;
                } else {
                    random -= threshold;
                }
            }
        }
        return null;
    }

}

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

import java.util.List;
import java.util.Map;

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.FastNoise;
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.ObjectFacet2D;
import org.terasology.world.generation.facets.base.ObjectFacet3D;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Requires(@Facet(SurfaceHeightFacet.class))
public abstract class SurfaceObjectProvider<B, T> implements FacetProvider {

    private Noise2D typeNoiseGen;

    private final Table<B, T, Float> probsTable = HashBasedTable.create();


    @Override
    public void setSeed(long seed) {
        typeNoiseGen = new FastNoise(seed + 1);
    }

    protected void populateFacet(ObjectFacet3D<T> facet, SurfaceHeightFacet surfaceFacet, ObjectFacet2D<? extends B> typeFacet, List<Predicate<Vector3i>> filters) {

        Region3i worldRegion = facet.getWorldRegion();

        int minY = worldRegion.minY();
        int maxY = worldRegion.maxY();

        for (int z = worldRegion.minZ(); z <= worldRegion.maxZ(); z++) {
            for (int x = worldRegion.minX(); x <= worldRegion.maxX(); x++) {
                int height = TeraMath.floorToInt(surfaceFacet.getWorld(x, z)) + 1;

                // if the surface is in range
                if (height >= minY && height <= maxY) {

                    Vector3i pos = new Vector3i(x, height, z);

                    // if all predicates match
                    if (Predicates.and(filters).apply(pos)) {
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

    protected void register(B biome, T tree, float probability) {
        Preconditions.checkArgument(probability >= 0, "probability must be >= 0");
        Preconditions.checkArgument(probability <= 1, "probability must be <= 1");

        probsTable.put(biome, tree, Float.valueOf(probability));
    }

    protected T getType(int x, int z, Map<T, Float> gens) {
        float random = typeNoiseGen.noise(x, z);

        for (T generator : gens.keySet()) {
            float threshold = gens.get(generator).floatValue();
            if (random < threshold) {
                return generator;
            } else {
                random -= threshold;
            }
        }
        return null;
    }

}

/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.core.world.generator.facetProviders;

import java.util.List;
import java.util.Map;

import org.terasology.core.world.generator.facets.FloraFacet;
import org.terasology.core.world.generator.rasterizers.FloraType;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.biomes.Biome;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.ObjectFacet2D;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Determines where plants can be placed.  Will put plants one block above the surface if it is in the correct biome.
 */
@Produces(FloraFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
public abstract class AbstractFloraProvider implements FacetProvider {

    private NoiseTable noiseTable;
    private NoiseTable noiseTypeTable;

    private final Table<Biome, FloraType, Float> probsTable = HashBasedTable.create();

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
        noiseTypeTable = new NoiseTable(seed + 1);
    }

    protected void register(Biome biome, FloraType tree, float probability) {
        Preconditions.checkArgument(probability >= 0, "probability must be >= 0");
        probsTable.put(biome, tree, Float.valueOf(probability));
    }

    protected FloraFacet createFacet(GeneratingRegion region, List<Predicate<Vector3i>> filters, ObjectFacet2D<? extends Biome> biomeFacet) {
        FloraFacet facet = new FloraFacet(region.getRegion(), region.getBorderForFacet(FloraFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();

        for (int z = facet.getWorldRegion().minZ(); z <= facet.getWorldRegion().maxZ(); z++) {
            for (int x = facet.getWorldRegion().minX(); x <= facet.getWorldRegion().maxX(); x++) {
                int height = TeraMath.floorToInt(surface.getWorld(x, z)) + 1;

                // if the surface is in range
                if (height >= minY && height <= maxY) {

                    Vector3i pos = new Vector3i(x, height, z);

                    // if all predicates match
                    if (Predicates.and(filters).apply(pos)) {
                        Biome biome = biomeFacet.getWorld(x, z);
                        Map<FloraType, Float> plantProb = probsTable.row(biome);
                        FloraType type = getType(x, z, plantProb);
                        if (type != null) {
                            facet.setWorld(x, height, z, type);
                        }
                    }
                }
            }
        }

        return facet;
    }

    protected FloraType getType(int x, int z, Map<FloraType, Float> probs) {
        float random = noiseTypeTable.noise(x, z) / 255.0f;

        for (FloraType generator : probs.keySet()) {
            float threshold = probs.get(generator).floatValue();
            if (random <= threshold) {
                return generator;
            } else {
                random -= threshold;
            }
        }

        return null;
    }
}

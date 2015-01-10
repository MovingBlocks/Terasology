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

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.chunkGenerators.TreeGenerator;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.biomes.Biome;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
public abstract class AbstractTreeProvider implements FacetProvider {

    private NoiseTable treeSeedNoise;

    private final Table<Biome, TreeGenerator, Float> treeGeneratorLookup = HashBasedTable.create();

    @Override
    public void setSeed(long seed) {
        treeSeedNoise = new NoiseTable(seed + 1);
    }

    protected void registerTree(CoreBiome biome, TreeGenerator tree, float probability) {
        Preconditions.checkArgument(probability > 0, "probability must be > 0");
        treeGeneratorLookup.put(biome, tree, Float.valueOf(probability));
    }

    protected TreeFacet createFacet(GeneratingRegion region, List<Predicate<Vector3i>> filters, BiomeFacet biomeFacet) {
        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, 15, 10));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();

        for (int z = facet.getWorldRegion().minZ(); z <= facet.getWorldRegion().maxZ(); z++) {
            for (int x = facet.getWorldRegion().minX(); x <= facet.getWorldRegion().maxX(); x++) {
                int height = TeraMath.ceilToInt(surface.getWorld(x, z));

                // if the surface is in range, check filters
                if (height >= minY && height <= maxY) {

                    Vector3i pos = new Vector3i(x, height, z);

                    if (Predicates.and(filters).apply(pos)) {
                        CoreBiome biome = biomeFacet.getWorld(pos.x, pos.z);
                        float facetValue = treeSeedNoise.noise(x, z) / 255f;
                        for (TreeGenerator generator : treeGeneratorLookup.row(biome).keySet()) {
                            if (treeGeneratorLookup.get(biome, generator) > facetValue) {

                                facet.setWorld(x, height, z, generator);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return facet;
    }
}


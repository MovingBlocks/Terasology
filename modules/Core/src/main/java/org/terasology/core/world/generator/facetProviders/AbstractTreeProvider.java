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

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.core.world.generator.trees.TreeGenerator;
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
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.ObjectFacet2D;

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

    protected TreeFacet createFacet(int maxRad, int maxHeight, GeneratingRegion region,
                                    List<Predicate<Vector3i>> filters, ObjectFacet2D<? extends Biome> biomeFacet) {

        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, maxHeight, maxRad));

        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();

        for (int z = facet.getWorldRegion().minZ(); z <= facet.getWorldRegion().maxZ(); z++) {
            for (int x = facet.getWorldRegion().minX(); x <= facet.getWorldRegion().maxX(); x++) {
                int height = TeraMath.ceilToInt(surface.getWorld(x, z));

                // if the surface is in range
                if (height >= minY && height <= maxY) {

                    Vector3i pos = new Vector3i(x, height, z);

                    // if all predicates match
                    if (Predicates.and(filters).apply(pos)) {
                        Biome biome = biomeFacet.getWorld(pos.x, pos.z);
                        Map<TreeGenerator, Float> gens = treeGeneratorLookup.row(biome);
                        putTree(facet, pos, gens);
                    }
                }
            }
        }

        return facet;
    }

    protected void putTree(TreeFacet facet, Vector3i pos, Map<TreeGenerator, Float> gens) {
        float random = treeSeedNoise.noise(pos.x, pos.z) / 255f;

        for (TreeGenerator generator : gens.keySet()) {
            float threshold = gens.get(generator).floatValue();
            if (random < threshold) {
                facet.setWorld(pos, generator);
                break;
            } else {
                random -= threshold;
            }
        }
    }

    protected static class SeaLevelFilter implements Predicate<Vector3i> {

        private int seaLevel;

        public SeaLevelFilter(int seaLevel) {
            this.seaLevel = seaLevel;
        }

        @Override
        public boolean apply(Vector3i input) {
            return input.getY() > seaLevel;
        }
    }

    protected static class DensityFilter implements Predicate<Vector3i> {

        private DensityFacet density;

        public DensityFilter(DensityFacet density) {
            this.density = density;
        }

        @Override
        public boolean apply(Vector3i input) {
            // pass if the block on the surface is dense enough
            float densBelow = density.getWorld(input.getX(), input.getY() - 1, input.getZ());
            float densThis = density.getWorld(input);
            return (densBelow >= 0 && densThis < 0);
        }
    }

    protected static class FlatnessFilter implements Predicate<Vector3i> {
        private SurfaceHeightFacet surface;

        public FlatnessFilter(SurfaceHeightFacet surface) {
            this.surface = surface;
        }

        @Override
        public boolean apply(Vector3i input) {
            // pass if there is a level surface in adjacent directions
            int x = input.getX();
            int z = input.getZ();
            int height = input.getY();

            return (TeraMath.ceilToInt(surface.getWorld(x - 1, z)) == height)
                && (TeraMath.ceilToInt(surface.getWorld(x + 1, z)) == height)
                && (TeraMath.ceilToInt(surface.getWorld(x, z - 1)) == height)
                && (TeraMath.ceilToInt(surface.getWorld(x, z + 1)) == height);
        }
    }

    protected static class ProbabilityFilter implements Predicate<Vector3i> {

        private NoiseTable treeNoise;
        private float density;

        public ProbabilityFilter(NoiseTable treeNoise, float density) {
            this.treeNoise = treeNoise;
            this.density = density;
        }

        @Override
        public boolean apply(Vector3i input) {
            return treeNoise.noise(input.getX(), input.getZ()) / 255f < density;
        }
    }
}

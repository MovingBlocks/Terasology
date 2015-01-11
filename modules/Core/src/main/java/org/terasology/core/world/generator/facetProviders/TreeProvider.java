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
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.core.world.generator.trees.Trees;
import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires({
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = 10)),
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 10 + 1)),
//        @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 25 + 1, sides = 10)),
        @Facet(value = BiomeFacet.class, border = @FacetBorder(sides = 10))
})
public class TreeProvider extends AbstractTreeProvider implements ConfigurableFacetProvider {

    private NoiseTable treeNoise;
    private TreeProviderConfiguration configuration = new TreeProviderConfiguration();

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        treeNoise = new NoiseTable(seed);

        // Add the trees to the generator lists
        registerTree(CoreBiome.MOUNTAINS, Trees.oakTree(), 0.04f);
        registerTree(CoreBiome.MOUNTAINS, Trees.pineTree(), 0.02f);

        registerTree(CoreBiome.FOREST, Trees.oakTree(), 0.08f);
        registerTree(CoreBiome.FOREST, Trees.pineTree(), 0.05f);
        registerTree(CoreBiome.FOREST, Trees.oakVariationTree(), 0.08f);

        registerTree(CoreBiome.SNOW, Trees.birkTree(), 0.02f);

        registerTree(CoreBiome.PLAINS, Trees.redTree(), 0.01f);
        registerTree(CoreBiome.PLAINS, Trees.oakTree(), 0.02f);

        registerTree(CoreBiome.DESERT, Trees.cactus(), 0.04f);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
//        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        BiomeFacet biome = region.getRegionFacet(BiomeFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, 25, 10));

        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        filters.add(new SeaLevelFilter(seaLevel.getSeaLevel()));
        filters.add(new ProbabilityFilter(treeNoise, configuration.density * 0.1f));
//        filters.add(new DensityFilter(density));
        filters.add(new FlatnessFilter(surface));
        filters.add(new MinDistanceFilter(facet, 4.0f));

        fillFacet(facet, region, filters, biome);

        region.setRegionFacet(TreeFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Trees";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (TreeProviderConfiguration) configuration;
    }

    private static class TreeProviderConfiguration implements Component {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall tree density")
        private float density = 0.4f;

    }
}

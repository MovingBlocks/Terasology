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

import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
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
import org.terasology.world.generation.facets.DensityFacet;
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
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(bottom = 15, sides = 10 + 1)),
        @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 15 + 1, sides = 10)),
        @Facet(value = BiomeFacet.class, border = @FacetBorder(bottom = 15, sides = 10))
})
public class TreeProvider extends AbstractTreeProvider implements ConfigurableFacetProvider {

    private NoiseTable treeNoise;
    private TreeProviderConfiguration configuration = new TreeProviderConfiguration();

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        treeNoise = new NoiseTable(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        // TODO: JAVA8: Use lambda expressions instead
        filters.add(new Predicate<Vector3i>() {
            @Override
            public boolean apply(Vector3i input) {
                return input.getY() > seaLevel.getSeaLevel();
            }
        });

        // TODO: JAVA8: Use lambda expressions instead
        filters.add(new Predicate<Vector3i>() {
            @Override
            public boolean apply(Vector3i input) {
                // if the block on the surface is dense enough
                float densBelow = density.getWorld(input.getX(), input.getY() - 1, input.getZ());
                float densThis = density.getWorld(input);
                return (densBelow >= 0 && densThis < 0);
            }
        });

        // TODO: JAVA8: Use lambda expressions instead
        filters.add(new Predicate<Vector3i>() {
            @Override
            public boolean apply(Vector3i input) {
                // and if there is a level surface in adjacent directions
                int x = input.getX();
                int z = input.getZ();
                int height = input.getY();

                return (TeraMath.ceilToInt(surface.getWorld(x - 1, z)) == height)
                    && (TeraMath.ceilToInt(surface.getWorld(x + 1, z)) == height)
                    && (TeraMath.ceilToInt(surface.getWorld(x, z - 1)) == height)
                    && (TeraMath.ceilToInt(surface.getWorld(x, z + 1)) == height);
            }
        });


        // TODO: JAVA8: Use lambda expressions instead
        filters.add(new Predicate<Vector3i>() {
            @Override
            public boolean apply(Vector3i input) {
                // and if it selects a % of them
                return treeNoise.noise(input.getX(), input.getZ()) / 255f < configuration.density;
            }
        });

        TreeFacet facet = createFacet(region, filters);

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
        @Range(min = 0, max = 0.3f, increment = 0.01f, precision = 2, description = "Define the tree density for forests")
        private float density = 0.12f;

    }
}

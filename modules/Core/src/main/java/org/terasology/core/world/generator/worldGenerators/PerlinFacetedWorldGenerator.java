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
package org.terasology.core.world.generator.worldGenerators;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.terasology.core.world.generator.facetProviders.BiomeProvider;
import org.terasology.core.world.generator.facetProviders.EnsureSpawnableChunkZeroProvider;
import org.terasology.core.world.generator.facetProviders.FloraProvider;
import org.terasology.core.world.generator.facetProviders.PerlinBaseSurfaceProvider;
import org.terasology.core.world.generator.facetProviders.PerlinHillsAndMountainsProvider;
import org.terasology.core.world.generator.facetProviders.PerlinHumidityProvider;
import org.terasology.core.world.generator.facetProviders.PerlinOceanProvider;
import org.terasology.core.world.generator.facetProviders.PerlinRiverProvider;
import org.terasology.core.world.generator.facetProviders.PerlinSurfaceTemperatureProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.facetProviders.TreeProvider;
import org.terasology.core.world.generator.facetProviders.World2dPreviewProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.SolidRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Color;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.base.ColorSummaryFacet;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.WorldGenerator2DPreview;

import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
@RegisterWorldGenerator(id = "facetedperlin", displayName = "Perlin", description = "Faceted world generator")
public class PerlinFacetedWorldGenerator implements WorldGenerator, WorldGenerator2DPreview {

    private final SimpleUri uri;
    private String worldSeed;
    private World world;

    public PerlinFacetedWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        world = new WorldBuilder(worldSeed.hashCode())
                .addProvider(new World2dPreviewProvider())
                .addProvider(new SeaLevelProvider())
                .addProvider(new PerlinHumidityProvider())
                .addProvider(new PerlinSurfaceTemperatureProvider())
                .addProvider(new PerlinBaseSurfaceProvider())
                .addProvider(new PerlinRiverProvider())
                .addProvider(new PerlinOceanProvider())
                .addProvider(new PerlinHillsAndMountainsProvider())
                .addProvider(new BiomeProvider())
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new FloraProvider())
                .addProvider(new TreeProvider())
                .addProvider(new EnsureSpawnableChunkZeroProvider())
                        //.addRasterizer(new GroundRasterizer(blockManager))
                .addRasterizer(new FloraRasterizer())
                .addRasterizer(new TreeRasterizer())
                .addRasterizer(new SolidRasterizer())
                .build();

    }

    @Override
    public void initialize() {
        world.initialize();
    }

    @Override
    public void createChunk(CoreChunk chunk) {
        world.rasterizeChunk(chunk);
    }

    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        return Optional.absent();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Color get(String layerName, Rect2i area) {
        Map<String, Class<? extends WorldFacet>> namedFacets = getWorld().getNamedFacets();
        Class<? extends WorldFacet> facetType = namedFacets.get(layerName);
        Region3i area3d = Region3i.createFromMinAndSize(new Vector3i(area.minX(), 0, area.minY()), new Vector3i(area.sizeX(), 1, area.sizeY()));
        Region region = getWorld().getWorldData(area3d);
        ColorSummaryFacet colorSummaryFacet = (ColorSummaryFacet) region.getFacet(facetType);
        return colorSummaryFacet.getColor();
    }

    @Override
    public Iterable<String> getLayers() {
        Set<String> layerNames = Sets.newHashSet();
        for (Map.Entry<String, Class<? extends WorldFacet>> namedFacet : getWorld().getNamedFacets().entrySet()) {
            if (ColorSummaryFacet.class.isAssignableFrom(namedFacet.getValue())) {
                layerNames.add(namedFacet.getKey());
            }
        }
        return layerNames;
    }

}

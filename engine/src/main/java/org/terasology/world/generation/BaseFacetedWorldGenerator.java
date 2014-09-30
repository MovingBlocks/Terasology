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
package org.terasology.world.generation;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.rendering.nui.Color;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.facets.base.ColorSummaryFacet;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.WorldGenerator2DPreview;

import java.util.Map;
import java.util.Set;

public abstract class BaseFacetedWorldGenerator implements WorldGenerator, WorldGenerator2DPreview {

    private final SimpleUri uri;
    private String worldSeed;
    private WorldBuilder worldBuilder;
    private World world;

    public BaseFacetedWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
        worldBuilder = createWorld(worldSeed.hashCode());
        // reset the world to lazy load it again later
        world = null;
    }

    protected abstract WorldBuilder createWorld(long seed);

    @Override
    public void initialize() {
        getWorld().initialize();
    }

    @Override
    public void createChunk(CoreChunk chunk) {
        world.rasterizeChunk(chunk);
    }

    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        FacetedWorldConfigurator worldConfigurator = worldBuilder.createConfigurator();
        addConfiguration(worldConfigurator);
        return Optional.of((WorldConfigurator) worldConfigurator);
    }

    protected void addConfiguration(FacetedWorldConfigurator worldConfigurator) {
    }

    @Override
    public void setConfigurator(WorldConfigurator newConfigurator) {
        if (newConfigurator instanceof FacetedWorldConfigurator) {
            worldBuilder.setConfigurator((FacetedWorldConfigurator) newConfigurator);
        }
    }


    @Override
    public World getWorld() {
        // build the world as late as possible so that we can do configuration and 2d previews
        if (world == null) {
            world = worldBuilder.build();
        }
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

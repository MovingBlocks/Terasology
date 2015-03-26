/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.core.world.generator;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.Color;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.base.ColorSummaryFacet;
import org.terasology.world.generator.ChunkGenerationPass;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.WorldGenerator2DPreview;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public abstract class AbstractBaseWorldGenerator implements WorldGenerator, WorldGenerator2DPreview {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseWorldGenerator.class);

    private String worldSeed;
    private final List<ChunkGenerationPass> generationPasses = Lists.newArrayList();
    private final SimpleUri uri;

    public AbstractBaseWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    public abstract void initialize();

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public String getWorldSeed() {
        return worldSeed;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
        for (final ChunkGenerationPass generator : generationPasses) {
            generator.setWorldSeed(seed);
        }
    }

    protected final void register(final ChunkGenerationPass generator) {
        registerPass(generator);
        generationPasses.add(generator);
    }

    private void registerPass(final ChunkGenerationPass generator) {
        generator.setWorldSeed(worldSeed);
    }

    @Override
    public void createChunk(final CoreChunk chunk) {
        for (final ChunkGenerationPass generator : generationPasses) {
            try {
                generator.generateChunk(chunk);
            } catch (Throwable e) {
                logger.error("Error during generation pass {}", generator, e);
            }
        }
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

    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        return Optional.absent();
    }

    @Override
    public void setConfigurator(WorldConfigurator worldConfigurator) {
    }

    @Override
    public World getWorld() {
        return null;
    }
}

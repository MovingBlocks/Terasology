/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.zones;

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.viewer.layers.FacetLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A region in the world with its own rasterization and world preview properties.
 *
 * Each top-level zone (those directly added to the {@link WorldBuilder}) with one or more preview layers added to it
 * will show up in the drop-down list of zones on the world preview screen, allowing it to be previewed separately.
 *
 * A zone affects a certain region, given by the {@link #regionFunction}. Only blocks that are present in this region
 * will be rasterized, so zones can be combined together and, unless their regions overlap, they will not be able to
 * affect each other.
 *
 * Zones can also be nested, with the inner zones only being able to affect areas that are also included in their parent
 * zone. Preview layers on the inner zones also get added to their parent zone.
 */
@API
public class Zone extends ProviderStore implements WorldRasterizer {

    private final List<FacetProvider> facetProviders = new ArrayList<>();
    private final List<WorldRasterizer> rasterizers = new ArrayList<>();
    //TODO: add entity providers
    private final List<FacetLayer> facetLayers = new ArrayList<>();

    private ProviderStore parent;

    private final ZoneRegionFunction regionFunction;
    private final String name;


    public Zone(String name, Function<BaseVector3i, Boolean> regionFunction) {
        this(name, (pos, region) -> regionFunction.apply(pos));
    }

    public Zone(String name, BiFunction<BaseVector3i, Region, Boolean> regionFunction) {
        this.regionFunction = new StandardZoneRegionFunction(this, regionFunction);
        this.name = name;
    }

    public Zone(String name, ConfigurableZoneRegionFunction regionFunction) {
        this.regionFunction = regionFunction;
        this.name = name;
        regionFunction.initialize(this);
    }

    /* WorldRasterizer methods */


    @Override
    public void initialize() {
        rasterizers.forEach(WorldRasterizer::initialize);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        //Save the blocks that aren't meant to be changed
        Map<BaseVector3i, Block> savedBlocks = new HashMap<>();
        ChunkBlockIterator iterator = chunk.getBlockIterator();
        while (iterator.next()) {
            if (!containsBlock(iterator.getBlockPos(), chunkRegion)) {
                Block block = chunk.getBlock(ChunkMath.calcBlockPos(iterator.getBlockPos()));
                savedBlocks.put(ChunkMath.calcBlockPos(iterator.getBlockPos()), block);
            }
        }
        if (savedBlocks.size() != ChunkConstants.SIZE_X * ChunkConstants.SIZE_Y * ChunkConstants.SIZE_Z) {
            //Do the rasterization
            rasterizers.forEach(r -> r.generateChunk(chunk, chunkRegion));
            //Restore the saved blocks
            savedBlocks.forEach(chunk::setBlock);
        }
    }

    /* Preview features */


    public Zone addPreviewLayer(FacetLayer facetLayer) {
        facetLayers.add(facetLayer);
        return this;
    }

    public List<FacetLayer> getPreviewLayers() {
        return facetLayers;
    }

    public PreviewGenerator preview(WorldGenerator generator) {
        return new FacetLayerPreview(generator, facetLayers);
    }


    /* General utility */


    public boolean containsBlock(BaseVector3i worldPos, Region chunkRegion) {
        return regionFunction.apply(worldPos, chunkRegion);
    }

    public ZoneRegionFunction getRegionFunction() {
        return regionFunction;
    }

    public List<FacetProvider> getFacetProviders() {
        return facetProviders;
    }

    public List<WorldRasterizer> getRasterizers() {
        return rasterizers;
    }

    public void setParent(ProviderStore parent) {
        this.parent = parent;
    }

    public ProviderStore getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }


    /* BuilderZone methods */


    @Override
    public Zone addProvider(FacetProvider facet) {
        facetProviders.add(facet);
        return this;
    }

    @Override
    public Zone addRasterizer(WorldRasterizer rasterizer) {
        rasterizers.add(rasterizer);
        return this;
    }

    @Override
    public Zone addZone(Zone zone) {
        super.addZone(zone);
        zone.getPreviewLayers().forEach(this::addPreviewLayer);
        return this;
    }

}

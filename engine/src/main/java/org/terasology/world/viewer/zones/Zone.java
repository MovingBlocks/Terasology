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
package org.terasology.world.viewer.zones;

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.viewer.layers.FacetLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@API
public class Zone implements WorldRasterizer {

    private final List<FacetProvider> facetProviders = new ArrayList<>();
    private final List<WorldRasterizer> rasterizers = new ArrayList<>();
    //TODO: add entity providers
    private final List<FacetLayer> facetLayers = new ArrayList<>();

    private final BiFunction<BaseVector3i, Region, Boolean> regionFunction;
    private final String name;

    public Zone(String name, BiFunction<BaseVector3i, Region, Boolean> regionFunction) {
        this.regionFunction = regionFunction;
        this.name = name;
    }

    public Zone(String name, Function<BaseVector3i, Boolean> regionFunction) {
        this(name, (pos, region) -> regionFunction.apply(pos));
    }

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
        //Do the rasterization
        rasterizers.forEach(r -> r.generateChunk(chunk, chunkRegion));
        //Restore the saved blocks
        savedBlocks.forEach(chunk::setBlock);
    }

    public Zone addZone(Zone zone) {
        facetProviders.addAll(zone.getFacetProviders());
        facetLayers.addAll(zone.getPreviewLayers());
        rasterizers.add(zone);
        return this;
    }

    public Zone addProvider(FacetProvider facet) {
        facetProviders.add(facet);
        return this;
    }

    public List<FacetProvider> getFacetProviders() {
        return facetProviders;
    }

    public Zone addRasterizer(WorldRasterizer rasterizer) {
        rasterizers.add(rasterizer);
        return this;
    }

    public List<WorldRasterizer> getRasterizers() {
        return rasterizers;
    }

    public Zone addPreviewLayer(FacetLayer facetLayer) {
        facetLayers.add(facetLayer);
        return this;
    }

    public List<FacetLayer> getPreviewLayers() {
        return facetLayers;
    }

    public boolean containsBlock(BaseVector3i worldPos, Region chunkRegion) {
        return regionFunction.apply(worldPos, chunkRegion);
    }

    public String getName() {
        return name;
    }

    public PreviewGenerator preview(WorldGenerator generator) {
        return new FacetLayerPreview(generator, facetLayers);
    }

    @Override
    public String toString() {
        return getName();
    }
}

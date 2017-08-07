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
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@API
public class Zone implements FacetProvider, WorldRasterizer {

    private final List<FacetProvider> facetProviders = new ArrayList<>();
    private final List<WorldRasterizer> rasterizers = new ArrayList<>();
    //TODO: add entity providers

    Function<BaseVector3i, Boolean> regionFunction;

    public Zone(Function<BaseVector3i, Boolean> regionFunction) {
        this.regionFunction = regionFunction;
    }

    @Override
    public void setSeed(long seed) {
        facetProviders.forEach(fp -> fp.setSeed(seed));
    }

    @Override
    public void initialize() {
        rasterizers.forEach(WorldRasterizer::initialize);
    }

    @Override
    public void process(GeneratingRegion region) {
        facetProviders.forEach(fp -> fp.process(region));
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        //Save the blocks that aren't meant to be changed
        Map<BaseVector3i, Block> savedBlocks = new HashMap<>();
        ChunkBlockIterator iterator = chunk.getBlockIterator();
        while (iterator.next()) {
            if (!containsBlock(iterator.getBlockPos())) {
                Block block = chunk.getBlock(ChunkMath.calcBlockPos(iterator.getBlockPos()));
                savedBlocks.put(ChunkMath.calcBlockPos(iterator.getBlockPos()), block);
            }
        }
        //Do the rasterization
        rasterizers.forEach(r -> r.generateChunk(chunk, chunkRegion));
        //Restore the saved blocks
        savedBlocks.forEach(chunk::setBlock);
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

    public boolean containsBlock(BaseVector3i worldPos) {
        return regionFunction.apply(worldPos);
    }
}

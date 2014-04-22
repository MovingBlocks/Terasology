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
package org.terasology.world.generation2;

import com.google.common.collect.ListMultimap;
import org.terasology.math.Region3i;
import org.terasology.world.chunks.CoreChunk;

import java.util.List;

/**
 * @author Immortius
 */
public class WorldImpl implements World {
    private long seed;
    private ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains;
    private List<WorldRasterizer> worldRasterizers;

    public WorldImpl(long seed, ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains, List<WorldRasterizer> worldRasterizers) {
        this.seed = seed;
        this.facetProviderChains = facetProviderChains;
        this.worldRasterizers = worldRasterizers;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public Region getWorldData(Region3i region) {
        return new RegionImpl(region, facetProviderChains);
    }

    @Override
    public void rasterizeChunk(CoreChunk chunk) {
        Region chunkRegion = getWorldData(chunk.getRegion());
        for (WorldRasterizer rasterizer : worldRasterizers) {
            rasterizer.generateChunk(chunk, chunkRegion);
        }
    }
}

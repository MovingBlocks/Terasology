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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.terasology.math.Region3i;
import org.terasology.world.chunks.CoreChunk;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Immortius
 */
public class WorldImpl implements World {
    private final ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains;
    private final List<WorldRasterizer> worldRasterizers;
    private final Map<Class<? extends WorldFacet>, Border3D> borders;
    private final int seaLevel;

    public WorldImpl(ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains,
                     List<WorldRasterizer> worldRasterizers,
                     Map<Class<? extends WorldFacet>, Border3D> borders,
                     int seaLevel) {
        this.facetProviderChains = facetProviderChains;
        this.worldRasterizers = worldRasterizers;
        this.borders = borders;
        this.seaLevel = seaLevel;
    }

    @Override
    public Region getWorldData(Region3i region) {
        return new RegionImpl(region, facetProviderChains, borders);
    }

    @Override
    public int getSeaLevel() {
        return seaLevel;
    }

    @Override
    public void rasterizeChunk(CoreChunk chunk) {
        Region chunkRegion = getWorldData(chunk.getRegion());
        for (WorldRasterizer rasterizer : worldRasterizers) {
            rasterizer.generateChunk(chunk, chunkRegion);
        }
    }

    @Override
    public Map<String, Class<? extends WorldFacet>> getNamedFacets() {
        Map<String, Class<? extends WorldFacet>> facets = Maps.newHashMap();

        for (Class<? extends WorldFacet> facetClass : facetProviderChains.keySet()) {
            FacetName facetName = facetClass.getAnnotation(FacetName.class);
            if (facetName != null && !facets.containsKey(facetName.value())) {
                facets.put(facetName.value(), facetClass);
            }
        }

        return facets;
    }

    @Override
    public Set<Class<? extends WorldFacet>> getAllFacets() {
        return Sets.newHashSet(facetProviderChains.keySet());
    }

    @Override
    public void initialize() {
        for (WorldRasterizer rasterizer : worldRasterizers) {
            rasterizer.initialize();
        }
    }
}

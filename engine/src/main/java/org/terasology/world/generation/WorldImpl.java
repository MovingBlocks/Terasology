// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generation;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.CoreChunk;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class WorldImpl implements World {
    private final ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains;
    private final ListMultimap<Class<? extends WorldFacet>, FacetProvider> scalableFacetProviderChains;
    private final List<WorldRasterizer> worldRasterizers;
    private final List<WorldRasterizer> scalableWorldRasterizers;
    private final List<EntityProvider> entityProviders;
    private final Map<Class<? extends WorldFacet>, Border3D> borders;
    private final int seaLevel;

    public WorldImpl(ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains,
                     ListMultimap<Class<? extends WorldFacet>, FacetProvider> scalableFacetProviderChains,
                     List<WorldRasterizer> worldRasterizers,
                     List<WorldRasterizer> scalableWorldRasterizers,
                     List<EntityProvider> entityProviders,
                     Map<Class<? extends WorldFacet>, Border3D> borders,
                     int seaLevel) {
        this.facetProviderChains = facetProviderChains;
        this.scalableFacetProviderChains = scalableFacetProviderChains;
        this.worldRasterizers = worldRasterizers;
        this.scalableWorldRasterizers = scalableWorldRasterizers;
        this.entityProviders = entityProviders;
        this.borders = borders;
        this.seaLevel = seaLevel;
    }

    @Override
    public Region getWorldData(BlockRegion region, float scale) {
        return new RegionImpl(region, scale == 1 ? facetProviderChains : scalableFacetProviderChains, borders, scale);
    }

    @Override
    public int getSeaLevel() {
        return seaLevel;
    }

    @Override
    public void rasterizeChunk(CoreChunk chunk, EntityBuffer buffer) {
        Region chunkRegion = getWorldData(chunk.getRegion(), 1);
        for (WorldRasterizer rasterizer : worldRasterizers) {
            rasterizer.generateChunk(chunk, chunkRegion);
        }
        for (EntityProvider entityProvider : entityProviders) {
            entityProvider.process(chunkRegion, buffer);
        }
    }

    @Override
    public void rasterizeChunk(CoreChunk chunk, EntityBuffer buffer, float scale) {
        Region chunkRegion = getWorldData(chunk.getRegion(), scale);
        for (WorldRasterizer rasterizer : scalableWorldRasterizers) {
            ((ScalableWorldRasterizer) rasterizer).generateChunk(chunk, chunkRegion, scale);
        }
        for (EntityProvider entityProvider : entityProviders) {
            entityProvider.process(chunkRegion, buffer);
        }
    }

    @Override
    public Set<Class<? extends WorldFacet>> getAllFacets() {
        return Sets.newHashSet(facetProviderChains.keySet());
    }

    @Override
    public void initialize() {
        // throw them all in a set to remove duplicates
        Collection<FacetProvider> facetProviders = new LinkedHashSet<>(facetProviderChains.values());

        facetProviders.forEach(FacetProvider::initialize);

        worldRasterizers.forEach(WorldRasterizer::initialize);

        entityProviders.forEach(EntityProvider::initialize);
    }
}

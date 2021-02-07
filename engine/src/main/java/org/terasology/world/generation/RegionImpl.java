// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generation;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import org.terasology.utilities.collection.TypeMap;
import org.terasology.world.block.BlockRegion;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 */
public class RegionImpl implements Region, GeneratingRegion {

    private final BlockRegion region;
    private final ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains;
    private final Map<Class<? extends WorldFacet>, Border3D> borders;
    private final float scale;

    private final Set<FacetProvider> processedProviders = Sets.newHashSet();
    private final TypeMap<WorldFacet> generatedFacets = TypeMap.create();
    private final TypeMap<WorldFacet> generatingFacets = TypeMap.create();
    private final TypeMap<WorldFacet> cachedFacets;


    public RegionImpl(TypeMap<WorldFacet> cached, BlockRegion region, ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains, Map<Class<? extends WorldFacet>, Border3D> borders, float scale) {
        this.region = region;
        this.facetProviderChains = facetProviderChains;
        this.borders = borders;
        this.scale = scale;
        this.cachedFacets = cached;
    }

    @Override
    public <T extends WorldFacet> T getFacet(Class<T> dataType) {
        T facet = generatedFacets.get(dataType);
        if (facet == null) {
            facetProviderChains.get(dataType).stream().filter(provider -> !processedProviders.contains(provider)).forEach(provider -> {
                if (scale == 1) {
                    provider.process(this);
                } else {
                    ((ScalableFacetProvider) provider).process(this, scale);
                }
                processedProviders.add(provider);
            });
            facet = generatingFacets.get(dataType);
            generatedFacets.put(dataType, facet);
        }
        return facet;
    }

    @Override
    public BlockRegion getRegion() {
        return region;
    }

    @Override
    public <T extends WorldFacet> T getRegionFacet(Class<T> type) {
        return generatingFacets.get(type);
    }

    @Override
    public <T extends WorldFacet> void setRegionFacet(Class<T> type, T facet) {
        generatingFacets.put(type, facet);
    }

    /**
     * retrieves a thread local facet else create a new instance and store it in the cache for later retrieval
     * @param type the type of facet
     * @param supplier the supplier to provide the facet
     * @param <T> the type of facet
     * @return the instanced facet
     */
    public <T extends WorldFacet> T cachedFacet(Class<T> type, Supplier<T> supplier) {
        if (this.cachedFacets.containsKey(type)) {
            return this.cachedFacets.get(type);
        }
        T facet = supplier.get();
        this.cachedFacets.put(type, facet);
        return facet;
    }

    @Override
    public Border3D getBorderForFacet(Class<? extends WorldFacet> type) {
        if (borders.containsKey(type)) {
            return borders.get(type);
        } else {
            return new Border3D(0, 0, 0);
        }
    }
}

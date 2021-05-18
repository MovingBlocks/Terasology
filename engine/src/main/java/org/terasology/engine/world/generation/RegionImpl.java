// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.collect.Sets;
import org.terasology.engine.world.block.BlockRegion;

import java.util.Map;
import java.util.Set;

/**
 */
public class RegionImpl implements Region, GeneratingRegion {

    private final BlockRegion region;
    private final ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains;
    private final Map<Class<? extends WorldFacet>, Border3D> borders;
    private final float scale;

    private final ClassToInstanceMap<WorldFacet> generatingFacets = MutableClassToInstanceMap.create();
    private final Set<FacetProvider> processedProviders = Sets.newHashSet();
    private final ClassToInstanceMap<WorldFacet> generatedFacets = MutableClassToInstanceMap.create();

    public RegionImpl(BlockRegion region, ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains, Map<Class<? extends WorldFacet>, Border3D> borders, float scale) {
        this.region = region;
        this.facetProviderChains = facetProviderChains;
        this.borders = borders;
        this.scale = scale;
    }

    @Override
    public <T extends WorldFacet> T getFacet(Class<T> dataType) {
        T facet = generatedFacets.getInstance(dataType);
        if (facet == null) {
            facetProviderChains.get(dataType).stream().filter(provider -> !processedProviders.contains(provider)).forEach(provider -> {
                if (scale == 1) {
                    provider.process(this);
                } else {
                    ((ScalableFacetProvider) provider).process(this, scale);
                }
                processedProviders.add(provider);
            });
            facet = generatingFacets.getInstance(dataType);
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
        return generatingFacets.getInstance(type);
    }

    @Override
    public <T extends WorldFacet> void setRegionFacet(Class<T> type, T facet) {
        generatingFacets.putInstance(type, facet);
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

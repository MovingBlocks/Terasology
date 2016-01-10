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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import org.terasology.math.Region3i;
import org.terasology.utilities.collection.TypeMap;

import java.util.Map;
import java.util.Set;

/**
 */
public class RegionImpl implements Region, GeneratingRegion {

    private final Region3i region;
    private final ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains;
    private final Map<Class<? extends WorldFacet>, Border3D> borders;

    private final TypeMap<WorldFacet> generatingFacets = TypeMap.create();
    private final Set<FacetProvider> processedProviders = Sets.newHashSet();
    private final TypeMap<WorldFacet> generatedFacets = TypeMap.create();

    public RegionImpl(Region3i region, ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains, Map<Class<? extends WorldFacet>, Border3D> borders) {
        this.region = region;
        this.facetProviderChains = facetProviderChains;
        this.borders = borders;
    }

    @Override
    public <T extends WorldFacet> T getFacet(Class<T> dataType) {
        T facet = generatedFacets.get(dataType);
        if (facet == null) {
            facetProviderChains.get(dataType).stream().filter(provider -> !processedProviders.contains(provider)).forEach(provider -> {
                provider.process(this);
                processedProviders.add(provider);
            });
            facet = generatingFacets.get(dataType);
            generatedFacets.put(dataType, facet);
        }
        return facet;
    }

    @Override
    public Region3i getRegion() {
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


    @Override
    public Border3D getBorderForFacet(Class<? extends WorldFacet> type) {
        if (borders.containsKey(type)) {
            return borders.get(type);
        } else {
            return new Border3D(0, 0, 0);
        }
    }
}

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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class WorldBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorldBuilder.class);

    private long seed;
    private List<FacetProvider> providersList = Lists.newArrayList();
    private Set<Class<? extends WorldFacet>> facetCalculationInProgress = Sets.newHashSet();
    private List<WorldRasterizer> rasterizers = Lists.newArrayList();

    public WorldBuilder(long seed) {
        this.seed = seed;
    }

    public WorldBuilder addProvider(FacetProvider provider) {
        provider.setSeed(seed);
        providersList.add(provider);
        return this;
    }

    public WorldBuilder addRasterizer(WorldRasterizer rasterizer) {
        rasterizers.add(rasterizer);
        return this;
    }

    public World build() {
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains = determineProviderChains();
        return new WorldImpl(seed, providerChains, rasterizers, determineBorders(providerChains));
    }

    private Map<Class<? extends WorldFacet>, Border3D> determineBorders(ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains) {
        Map<Class<? extends WorldFacet>, Border3D> borders = Maps.newHashMap();

        Border3D largestBorder = new Border3D(0, 0, 0);

        // loop through the providers and find all the max sized borders for each facet
        for (FacetProvider provider : providersList) {
            Requires requirements = provider.getClass().getAnnotation(Requires.class);
            if (requirements != null) {
                for (Facet requirement : requirements.value()) {
                    largestBorder = new Border3D(
                            Math.max(requirement.border().top(), largestBorder.getTop()),
                            Math.max(requirement.border().bottom(), largestBorder.getBottom()),
                            Math.max(requirement.border().sides(), largestBorder.getSides()));
                }
            }
        }

        for (Class<? extends WorldFacet> type : providerChains.keySet()) {
            borders.put(type, largestBorder);
        }

        return borders;
    }

    private ListMultimap<Class<? extends WorldFacet>, FacetProvider> determineProviderChains() {
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> result = ArrayListMultimap.create();
        Set<Class<? extends WorldFacet>> facets = Sets.newHashSet();
        for (FacetProvider provider : providersList) {
            Produces produces = provider.getClass().getAnnotation(Produces.class);
            if (produces != null) {
                facets.addAll(Arrays.asList(produces.value()));
            }
            Updates updates = provider.getClass().getAnnotation(Updates.class);
            if (updates != null) {
                for (Facet facet : updates.value()) {
                    facets.add(facet.value());
                }
            }
        }
        for (Class<? extends WorldFacet> facet : facets) {
            determineProviderChainFor(facet, result);
        }

        return result;
    }

    private void determineProviderChainFor(Class<? extends WorldFacet> facet, ListMultimap<Class<? extends WorldFacet>, FacetProvider> result) {
        if (result.containsKey(facet)) {
            return;
        }
        if (!facetCalculationInProgress.add(facet)) {
            throw new RuntimeException("Circular dependency detected when calculating facet provider ordering for " + facet);
        }
        Set<FacetProvider> orderedProviders = Sets.newLinkedHashSet();
        for (FacetProvider provider : providersList) {
            if (producesFacet(provider, facet)) {
                Requires requirements = provider.getClass().getAnnotation(Requires.class);
                if (requirements != null) {
                    for (Facet requirement : requirements.value()) {
                        determineProviderChainFor(requirement.value(), result);
                        orderedProviders.addAll(result.get(requirement.value()));
                    }
                }
                orderedProviders.add(provider);
            }
            result.putAll(facet, orderedProviders);
            facetCalculationInProgress.remove(facet);
        }
    }

    private boolean producesFacet(FacetProvider provider, Class<? extends WorldFacet> facet) {
        Produces produces = provider.getClass().getAnnotation(Produces.class);
        if (produces != null && Arrays.asList(produces.value()).contains(facet)) {
            return true;
        }

        Updates updates = provider.getClass().getAnnotation(Updates.class);
        if (updates != null) {
            for (Facet updatedFacet : updates.value()) {
                if (updatedFacet.value() == facet) {
                    return true;
                }
            }
        }
        return false;
    }
}

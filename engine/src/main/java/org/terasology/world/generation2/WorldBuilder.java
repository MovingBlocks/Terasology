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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
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
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> facetProviderChains = determineProviderChains();
        return new WorldImpl(seed, determineProviderChains(), rasterizers);
    }

    private ListMultimap<Class<? extends WorldFacet>, FacetProvider> determineProviderChains() {
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> result = ArrayListMultimap.create();
        Set<Class<? extends WorldFacet>> facets = Sets.newHashSet();
        for (FacetProvider provider : providersList) {
            Produces produces = provider.getClass().getAnnotation(Produces.class);
            if (produces != null) {
                facets.addAll(Arrays.asList(produces.value()));
            } else {
                logger.warn("FacetProvider {} does not produce any data", provider.getClass());
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
            Produces produces = provider.getClass().getAnnotation(Produces.class);
            if (produces != null && Arrays.asList(produces.value()).contains(facet)) {
                Requires requirements = provider.getClass().getAnnotation(Requires.class);
                if (requirements != null) {
                    for (Class<? extends WorldFacet> requirement : requirements.value()) {
                        if (requirement != facet) {
                            determineProviderChainFor(requirement, result);
                            orderedProviders.addAll(result.get(requirement));
                        }
                    }
                }
                orderedProviders.add(provider);
            }
        }
        result.putAll(facet, orderedProviders);
        facetCalculationInProgress.remove(facet);
    }

}

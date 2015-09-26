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
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class WorldBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorldBuilder.class);

    private final List<FacetProvider> providersList = Lists.newArrayList();
    private final Set<Class<? extends WorldFacet>> facetCalculationInProgress = Sets.newHashSet();
    private final List<WorldRasterizer> rasterizers = Lists.newArrayList();
    private final List<EntityProvider> entityProviders = new ArrayList<>();
    private int seaLevel = 32;
    private Long seed;

    private WorldGeneratorPluginLibrary pluginLibrary;

    public WorldBuilder(WorldGeneratorPluginLibrary pluginLibrary) {
        this.pluginLibrary = pluginLibrary;
    }

    public WorldBuilder addProvider(FacetProvider provider) {
        providersList.add(provider);
        return this;
    }

    public WorldBuilder addRasterizer(WorldRasterizer rasterizer) {
        rasterizers.add(rasterizer);
        return this;
    }

    public WorldBuilder addEntities(EntityProvider entityProvider) {
        entityProviders.add(entityProvider);
        return this;
    }

    public WorldBuilder addPlugins() {
        pluginLibrary.instantiateAllOfType(FacetProviderPlugin.class).forEach(this::addProvider);
        pluginLibrary.instantiateAllOfType(WorldRasterizerPlugin.class).forEach(this::addRasterizer);

        return this;
    }

    /**
     * @param level the sea level, measured in blocks
     * @return this
     */
    public WorldBuilder setSeaLevel(int level) {
        this.seaLevel = level;
        return this;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public World build() {
        // TODO: ensure the required providers are present

        if (seed == null) {
            throw new IllegalStateException("Seed has not been set");
        }
        for (FacetProvider provider : providersList) {
            provider.setSeed(seed);
        }
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains = determineProviderChains();
        return new WorldImpl(providerChains, rasterizers, entityProviders, determineBorders(providerChains), seaLevel);
    }

    private Map<Class<? extends WorldFacet>, Border3D> determineBorders(ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains) {
        Map<Class<? extends WorldFacet>, Border3D> borders = Maps.newHashMap();

        for (Class<? extends WorldFacet> facet : providerChains.keySet()) {
            ensureBorderCalculatedForFacet(facet, providerChains, borders);
        }

        return borders;
    }

    private void ensureBorderCalculatedForFacet(Class<? extends WorldFacet> facet, ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
                                                Map<Class<? extends WorldFacet>, Border3D> borders) {
        if (!borders.containsKey(facet)) {
            Border3D border = new Border3D(0, 0, 0);

            for (FacetProvider facetProvider : providerChains.values()) {
                // Find all facets that require it
                Requires requires = facetProvider.getClass().getAnnotation(Requires.class);
                if (requires != null) {
                    for (Facet requiredFacet : requires.value()) {
                        if (requiredFacet.value() == facet) {
                            Produces produces = facetProvider.getClass().getAnnotation(Produces.class);
                            Updates updates = facetProvider.getClass().getAnnotation(Updates.class);

                            FacetBorder requiredBorder = requiredFacet.border();

                            if (produces != null) {
                                for (Class<? extends WorldFacet> producedFacet : produces.value()) {
                                    ensureBorderCalculatedForFacet(producedFacet, providerChains, borders);
                                    Border3D borderForProducedFacet = borders.get(producedFacet);
                                    border = border.maxWith(
                                            borderForProducedFacet.getTop() + requiredBorder.top(),
                                            borderForProducedFacet.getBottom() + requiredBorder.bottom(),
                                            borderForProducedFacet.getSides() + requiredBorder.sides());
                                }
                            }
                            if (updates != null) {
                                for (Facet producedFacetAnnotation : updates.value()) {
                                    Class<? extends WorldFacet> producedFacet = producedFacetAnnotation.value();
                                    FacetBorder borderForFacetAnnotation = producedFacetAnnotation.border();
                                    ensureBorderCalculatedForFacet(producedFacet, providerChains, borders);
                                    Border3D borderForProducedFacet = borders.get(producedFacet);
                                    border = border.maxWith(
                                            borderForProducedFacet.getTop() + requiredBorder.top() + borderForFacetAnnotation.top(),
                                            borderForProducedFacet.getBottom() + requiredBorder.bottom() + borderForFacetAnnotation.bottom(),
                                            borderForProducedFacet.getSides() + requiredBorder.sides() + borderForFacetAnnotation.sides());
                                }
                            }
                        }
                    }
                }
            }
            borders.put(facet, border);
        }
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

        // first add all @Produces facet providers
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
        }
        // then add all @Updates facet providers
        for (FacetProvider provider : providersList) {
            if (updatesFacet(provider, facet)) {
                Requires requirements = provider.getClass().getAnnotation(Requires.class);
                if (requirements != null) {
                    for (Facet requirement : requirements.value()) {
                        determineProviderChainFor(requirement.value(), result);
                        orderedProviders.addAll(result.get(requirement.value()));
                    }
                }
                orderedProviders.add(provider);
            }
        }
        result.putAll(facet, orderedProviders);
        facetCalculationInProgress.remove(facet);
    }

    private boolean producesFacet(FacetProvider provider, Class<? extends WorldFacet> facet) {
        Produces produces = provider.getClass().getAnnotation(Produces.class);
        if (produces != null && Arrays.asList(produces.value()).contains(facet)) {
            return true;
        }
        return false;
    }

    private boolean updatesFacet(FacetProvider provider, Class<? extends WorldFacet> facet) {
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

    public FacetedWorldConfigurator createConfigurator() {
        List<ConfigurableFacetProvider> configurables = new ArrayList<>();
        for (FacetProvider facetProvider : providersList) {
            if (facetProvider instanceof ConfigurableFacetProvider) {
                configurables.add((ConfigurableFacetProvider) facetProvider);
            }
        }
        FacetedWorldConfigurator worldConfigurator = new FacetedWorldConfigurator(configurables);
        return worldConfigurator;
    }
}

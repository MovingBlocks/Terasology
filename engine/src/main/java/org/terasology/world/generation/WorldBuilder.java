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
import org.terasology.entitySystem.Component;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class WorldBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorldBuilder.class);

    private final long seed;
    private final List<FacetProvider> providersList = Lists.newArrayList();
    private final Set<Class<? extends WorldFacet>> facetCalculationInProgress = Sets.newHashSet();
    private final List<WorldRasterizer> rasterizers = Lists.newArrayList();
    private int seaLevel = 32;

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

    public WorldBuilder addPlugins() {
        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        for (FacetProvider facetProvider : pluginLibrary.instantiateAllOfType(FacetProviderPlugin.class)) {
            addProvider(facetProvider);
        }

        for (WorldRasterizer worldRasterizer : pluginLibrary.instantiateAllOfType(WorldRasterizerPlugin.class)) {
            addRasterizer(worldRasterizer);
        }

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

    public World build() {
        // TODO: ensure the required providers are present

        ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains = determineProviderChains();
        return new WorldImpl(providerChains, rasterizers, determineBorders(providerChains), seaLevel);
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

    public FacetedWorldConfigurator createConfigurator() {
        FacetedWorldConfigurator worldConfigurator = new FacetedWorldConfigurator();
        for (FacetProvider facetProvider : providersList) {
            if (facetProvider instanceof ConfigurableFacetProvider) {
                ConfigurableFacetProvider configurableFacetProvider = (ConfigurableFacetProvider) facetProvider;
                worldConfigurator.addProperty(configurableFacetProvider.getConfigurationName(), configurableFacetProvider.getConfiguration());
            }
        }
        return worldConfigurator;
    }

    public void setConfigurator(FacetedWorldConfigurator worldConfigurator) {
        Map<String, Component> configurationMap = worldConfigurator.getProperties();
        for (FacetProvider facetProvider : providersList) {
            if (facetProvider instanceof ConfigurableFacetProvider) {
                ConfigurableFacetProvider configurableFacetProvider = (ConfigurableFacetProvider) facetProvider;
                Component configuration = configurationMap.get(configurableFacetProvider.getConfigurationName());
                if (configuration != null) {
                    configurableFacetProvider.setConfiguration(configuration);
                }
            }
        }
    }
}

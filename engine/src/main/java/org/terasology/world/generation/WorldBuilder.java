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
    
    class WorldBorderCalculator{
        
    }
    
    private static final Logger logger = LoggerFactory.getLogger(WorldBuilder.class);

    private final long seed;
    private final List<FacetProvider> providersList = Lists.newArrayList();
    private final Set<Class<? extends WorldFacet>> facetCalculationInProgress = Sets.newHashSet();
    private final List<WorldRasterizer> rasterizers = Lists.newArrayList();
    private int seaLevel = 32;

    public WorldBuilder(long seed) {
        this.seed = seed;
    }

    /**
     * Adds the provided provider to this world builder instance.
     * @param provider FacecProvider
     * @return This world builder instance
     */
    public WorldBuilder addProvider(FacetProvider provider) {
        provider.setSeed(seed);
        providersList.add(provider);
        return this;
    }

    /**
     * Adds the provided rasterizer to this world builder instance.
     * @param rasterizer
     * @return
     */
    public WorldBuilder addRasterizer(WorldRasterizer rasterizer) {
        rasterizers.add(rasterizer);
        return this;
    }

    /**
     * Adds the provided plugin to this world builder instance.
     * @return
     */
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

    /**
     * Builds the world.
     * @return World builded.
     */
    public World build() {
        // TODO: ensure the required providers are present

        ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains = determineProviderChains();
        return new WorldImpl(providerChains, rasterizers, determineBorders(providerChains), seaLevel);
    }

    /**
     * Determines the border for every facet in the world.
     * @param providerChains Facet list
     * @return A border for every facet
     */
    private Map<Class<? extends WorldFacet>, Border3D> determineBorders(ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains) {
        Map<Class<? extends WorldFacet>, Border3D> borders = Maps.newHashMap();

        for (Class<? extends WorldFacet> facet : providerChains.keySet()) {
            ensureBorderCalculatedForFacet(facet, providerChains, borders);
        }

        return borders;
    }

    /**
     * Determines the border needed for an specific facet.
     * @param facet
     * @param providerChains
     * @param borders
     */
    private void ensureBorderCalculatedForFacet(Class<? extends WorldFacet> facet, ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
                                                Map<Class<? extends WorldFacet>, Border3D> borders) {
        //For already calculated facets
        if (borders.containsKey(facet))
            return;

        Border3D border = new Border3D(0, 0, 0);
        for (FacetProvider facetProvider : providerChains.values()) {
            border = getBorderFromProvider(facet, providerChains, borders,
                    border, facetProvider);
        }
        borders.put(facet, border);
    
    }

    /**
     * Determines the border for an facet's specific provider.
     * @param facet Facet to determines its border
     * @param providerChains
     * @param borders Map of all borders
     * @param border Current border for this facet
     * @param facetProvider Facet's provider
     * @return
     */
    private Border3D getBorderFromProvider(
            Class<? extends WorldFacet> facet,
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
            Map<Class<? extends WorldFacet>, Border3D> borders,
            Border3D border, FacetProvider facetProvider) {
        Requires requires = facetProvider.getClass().getAnnotation(Requires.class);
        
        if (requires == null) 
            return border;
        
        for (Facet requiredFacet : requires.value()) {
            border = getBorderFromRequiredFacet(facet,
                    providerChains, borders, border, facetProvider,
                    requiredFacet);
        }
        return border;
    }

    /**
     * Get the border from the facets required from another one.
     * @param facet Facet to obtain its border
     * @param providerChains Providers
     * @param borders Border map
     * @param border Current border of the facet
     * @param facetProvider Provider of the facet
     * @param requiredFacet A facet required
     * @return New border for the facer
     */
    private Border3D getBorderFromRequiredFacet(
            Class<? extends WorldFacet> facet,
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
            Map<Class<? extends WorldFacet>, Border3D> borders,
            Border3D border, FacetProvider facetProvider, Facet requiredFacet) {
        if (requiredFacet.value() == facet) {
            Produces produces = facetProvider.getClass().getAnnotation(Produces.class);
            Updates updates = facetProvider.getClass().getAnnotation(Updates.class);

            FacetBorder requiredBorder = requiredFacet.border();

            border = getBorderFromProduces(providerChains,
                    borders, border, produces, requiredBorder);
            border = getBorderFromUpdates(providerChains,
                    borders, border, updates, requiredBorder);
        }
        return border;
    }

    /**
     * Get the border from the facets produced by a provider
     * @param providerChains Providers
     * @param borders Map of borders
     * @param border Current border
     * @param produces Facets produced
     * @param requiredBorder Border from the required facet
     * @return
     */
    private Border3D getBorderFromProduces(
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
            Map<Class<? extends WorldFacet>, Border3D> borders,
            Border3D border, Produces produces, FacetBorder requiredBorder) {
        if (produces != null) {
            for (Class<? extends WorldFacet> producedFacet : produces.value()) {
                border = getBorderFromProducedFacet(
                        providerChains, borders, border,
                        requiredBorder, producedFacet);
            }
        }
        return border;
    }

    /**
     * 
     * @param providerChains
     * @param borders
     * @param border
     * @param updates
     * @param requiredBorder
     * @return
     */
    private Border3D getBorderFromUpdates(
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
            Map<Class<? extends WorldFacet>, Border3D> borders,
            Border3D border, Updates updates, FacetBorder requiredBorder) {
        if (updates != null) {
            for (Facet producedFacetAnnotation : updates.value()) {
                border = getBorderFromFacetAnnotation(
                        providerChains, borders, border,
                        requiredBorder,
                        producedFacetAnnotation);
            }
        }
        return border;
    }

    /**
     * 
     * @param providerChains
     * @param borders
     * @param border
     * @param requiredBorder
     * @param producedFacetAnnotation
     * @return
     */
    private Border3D getBorderFromFacetAnnotation(
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
            Map<Class<? extends WorldFacet>, Border3D> borders,
            Border3D border, FacetBorder requiredBorder,
            Facet producedFacetAnnotation) {
        Class<? extends WorldFacet> producedFacet = producedFacetAnnotation.value();
        FacetBorder borderForFacetAnnotation = producedFacetAnnotation.border();
        ensureBorderCalculatedForFacet(producedFacet, providerChains, borders);
        Border3D borderForProducedFacet = borders.get(producedFacet);
        border = border.maxWith(
                borderForProducedFacet.getTop() + requiredBorder.top() + borderForFacetAnnotation.top(),
                borderForProducedFacet.getBottom() + requiredBorder.bottom() + borderForFacetAnnotation.bottom(),
                borderForProducedFacet.getSides() + requiredBorder.sides() + borderForFacetAnnotation.sides());
        return border;
    }

    /**
     * Get the border for an specific produced facet.
     * @param providerChains
     * @param borders
     * @param border
     * @param requiredBorder
     * @param producedFacet
     * @return
     */
    private Border3D getBorderFromProducedFacet(
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
            Map<Class<? extends WorldFacet>, Border3D> borders,
            Border3D border, FacetBorder requiredBorder,
            Class<? extends WorldFacet> producedFacet) {
        ensureBorderCalculatedForFacet(producedFacet, providerChains, borders);
        Border3D borderForProducedFacet = borders.get(producedFacet);
        border = border.maxWith(
                borderForProducedFacet.getTop() + requiredBorder.top(),
                borderForProducedFacet.getBottom() + requiredBorder.bottom(),
                borderForProducedFacet.getSides() + requiredBorder.sides());
        return border;
    }

    /**
     * Generates the whole list of Facets corresponding to its FacetProvider for this world builder.
     * @return List of Facets related to its FacetProvider
     */
    private ListMultimap<Class<? extends WorldFacet>, FacetProvider> determineProviderChains() {
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> result = ArrayListMultimap.create();
        Set<Class<? extends WorldFacet>> facets = Sets.newHashSet();
        for (FacetProvider provider : providersList) {
            determineProviderChainsFromProvider(facets, provider);
        }
        for (Class<? extends WorldFacet> facet : facets) {
            determineProviderChainFor(facet, result);
        }

        return result;
    }

    /**
     * Generates the provider chains for an specific facet provider.
     * @param facets
     * @param provider
     */
    private void determineProviderChainsFromProvider(
            Set<Class<? extends WorldFacet>> facets, FacetProvider provider) {
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

    /**
     * 
     * @param facet
     * @param result
     */
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
                getProviderChainForRequirements(result, orderedProviders,
                        provider);
            }
            result.putAll(facet, orderedProviders);
            facetCalculationInProgress.remove(facet);
        }
    }

    /**
     * Get the provider chain for a set of required facets.
     * @param result
     * @param orderedProviders
     * @param provider
     */
    private void getProviderChainForRequirements(
            ListMultimap<Class<? extends WorldFacet>, FacetProvider> result,
            Set<FacetProvider> orderedProviders, FacetProvider provider) {
        Requires requirements = provider.getClass().getAnnotation(Requires.class);
        if (requirements != null) {
            for (Facet requirement : requirements.value()) {
                determineProviderChainFor(requirement.value(), result);
                orderedProviders.addAll(result.get(requirement.value()));
            }
        }
        orderedProviders.add(provider);
    }

    /**
     * 
     * @param provider
     * @param facet
     * @return
     */
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

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @param worldConfigurator
     */
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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.zones.ProviderStore;
import org.terasology.engine.world.zones.Zone;
import org.terasology.engine.world.zones.ZonePlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldBuilder extends ProviderStore {

    private static final Logger logger = LoggerFactory.getLogger(WorldBuilder.class);

    private final List<FacetProvider> providersList = Lists.newArrayList();
    private final List<WorldRasterizer> rasterizers = Lists.newArrayList();
    private final List<EntityProvider> entityProviders = new ArrayList<>();
    private int seaLevel = 32;

    // Used for detecting circular dependencies
    private final Map<Class<? extends WorldFacet>, FacetProvider> requiredBy = new HashMap<>();
    private final Map<Class<? extends WorldFacet>, FacetProvider> providedBy = new HashMap<>();

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

    public WorldBuilder addZone(Zone zone) {
        super.addZone(zone);
        return this;
    }

    public WorldBuilder addPlugins() {
        pluginLibrary.instantiateAllOfType(FacetProviderPlugin.class).forEach(this::addProvider);
        pluginLibrary.instantiateAllOfType(WorldRasterizerPlugin.class).forEach(this::addRasterizer);
        pluginLibrary.instantiateAllOfType(EntityProviderPlugin.class).forEach(this::addEntities);
        pluginLibrary.instantiateAllOfType(ZonePlugin.class).forEach(this::addZone);

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

        if (seed == null) {
            throw new IllegalStateException("Seed has not been set");
        }
        for (FacetProvider provider : providersList) {
            provider.setSeed(seed);
        }
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains = determineProviderChains(false);
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> scalableProviderChains = determineProviderChains(true);
        List<WorldRasterizer> orderedRasterizers = ensureRasterizerOrdering(providerChains, false);
        List<WorldRasterizer> scalableRasterizers = ensureRasterizerOrdering(scalableProviderChains, true);
        return new WorldImpl(
                providerChains,
                scalableProviderChains,
                orderedRasterizers,
                scalableRasterizers,
                entityProviders,
                determineBorders(providerChains, orderedRasterizers),
                seaLevel
        );
    }

    private Map<Class<? extends WorldFacet>, Border3D> determineBorders(ListMultimap<Class<? extends WorldFacet>,
            FacetProvider> providerChains, List<WorldRasterizer> worldRasterizers) {
        List<FacetProvider> orderedProviders = new ArrayList<>();
        for (Class<? extends WorldFacet> facet : providerChains.keySet()) {
            for (FacetProvider provider : providerChains.get(facet)) {
                if (!orderedProviders.contains(provider)) {
                    orderedProviders.add(provider);
                }
            }
        }
        Map<Class<? extends WorldFacet>, Border3D> borders = Maps.newHashMap();

        for (WorldRasterizer rasterizer : worldRasterizers) {
            Requires requires = rasterizer.getClass().getAnnotation(Requires.class);
            if (requires != null) {
                for (Facet facet : requires.value()) {
                    borders.put(facet.value(), new Border3D(facet.border()).maxWith(borders.get(facet.value())));
                }
            }
        }

        for (int i = orderedProviders.size() - 1; i >= 0; i--) {
            FacetProvider provider = orderedProviders.get(i);
            Border3D requiredBorder = new Border3D(0, 0, 0);
            Requires requires = provider.getClass().getAnnotation(Requires.class);
            Produces produces = provider.getClass().getAnnotation(Produces.class);
            Updates updates = provider.getClass().getAnnotation(Updates.class);

            // Calculate how large a region needs to be correct in the output
            if (produces != null) {
                for (Class<? extends WorldFacet> facet : produces.value()) {
                    Border3D facetBorder = borders.get(facet);
                    if (facetBorder != null) {
                        requiredBorder = requiredBorder.maxWith(facetBorder);
                    }
                }
            }
            if (updates != null) {
                for (Facet facet : updates.value()) {
                    Border3D facetBorder = borders.get(facet.value());
                    if (facetBorder != null) {
                        requiredBorder = requiredBorder.maxWith(facetBorder);
                    }
                }
            }

            // Convert that to how large a region needs to be correct in the input.
            if (updates != null) {
                for (Facet facet : updates.value()) {
                    Border3D facetBorder = requiredBorder.extendBy(new Border3D(facet.border()));
                    borders.put(facet.value(), facetBorder.maxWith(borders.get(facet.value())));
                }
            }
            if (requires != null) {
                for (Facet facet : requires.value()) {
                    Border3D facetBorder = requiredBorder.extendBy(new Border3D(facet.border()));
                    borders.put(facet.value(), facetBorder.maxWith(borders.get(facet.value())));
                }
            }
        }

        return borders;
    }

    private ListMultimap<Class<? extends WorldFacet>, FacetProvider> determineProviderChains(boolean scalable) {
        ListMultimap<Class<? extends WorldFacet>, FacetProvider> result = ArrayListMultimap.create();
        Set<Class<? extends WorldFacet>> facets = new LinkedHashSet<>();
        for (FacetProvider provider : providersList) {
            Class<? extends FacetProvider> providerClass = provider.getClass();
            Produces produces = providerClass.getAnnotation(Produces.class);
            if (produces != null) {
                facets.addAll(Arrays.asList(produces.value()));
            }

            Requires requires = providerClass.getAnnotation(Requires.class);
            if (requires != null) {
                for (Facet facet : requires.value()) {
                    Class<? extends WorldFacet> facetValue = facet.value();
                    if (!facets.contains(facetValue)) {
                        logger.error("Facet provider for {} is missing. It is required by {}", facetValue, providerClass);
                        throw new IllegalStateException("Missing facet provider");
                    }
                }
            }

            Updates updates = providerClass.getAnnotation(Updates.class);
            if (updates != null) {
                for (Facet facet : updates.value()) {
                    facets.add(facet.value());
                }
            }
        }

        for (Class<? extends WorldFacet> facet : facets) {
            if (!result.containsKey(facet)) {
                Set<FacetProvider> orderedProviders = Sets.newLinkedHashSet();
                addProviderChain(facet, scalable, Integer.MIN_VALUE, orderedProviders);
                result.putAll(facet, orderedProviders);
            }
            if (logger.isDebugEnabled()) {
                StringBuilder text = new StringBuilder(facet.getSimpleName());
                text.append(" --> ");
                Iterator<FacetProvider> it = result.get(facet).iterator();
                while (it.hasNext()) {
                    text.append(it.next().getClass().getSimpleName());
                    if (it.hasNext()) {
                        text.append(", ");
                    }
                }
                logger.debug(text.toString());
            }
        }

        return result;
    }

    /**
     * Adds all facet providers and updaters for {@code facet} which have priority greater than {@code minPriority} to {@code orderedProviders}.
     */
    private void addProviderChain(Class<? extends WorldFacet> facet, boolean scalable, int minPriority,
                                  Set<FacetProvider> orderedProviders) {
        FacetProvider producer = null;
        for (FacetProvider provider : providersList) {
            if (producesFacet(provider, facet) && (!scalable || provider instanceof ScalableFacetProvider)) {
                if (producer != null) {
                    logger.warn("Facet already produced by {} and overwritten by {}", producer, provider);
                }
                providedBy.put(facet, provider);
                addRequirements(facet, provider, scalable, orderedProviders);
                producer = provider;
            }
        }

        for (FacetProvider provider : providersList) {
            if (updatesFacet(provider, facet) && (!scalable || provider instanceof ScalableFacetProvider)
                    && updatePriority(provider, facet) > minPriority) {
                providedBy.put(facet, provider);
                addRequirements(facet, provider, scalable, orderedProviders);
            }
        }
    }

    /**
     * Adds {@code provider} and all its dependencies (calculated by calling {@link #addProviderChain}
     * to {@code orderedProviders} in the proper order.
     * Doesn't consider dependencies through {@code providedFacet}, because that's already required by something else.
     */
    private void addRequirements(Class<? extends WorldFacet> providedFacet, FacetProvider provider, boolean scalable,
                                 Set<FacetProvider> orderedProviders) {
        if (orderedProviders.contains(provider)) {
            return;
        }

        Stream.of(updatedFacets(provider), requiredFacets(provider))
                .flatMap(Arrays::stream)
                .filter(r -> r.value() != providedFacet)
                .forEachOrdered(r -> {
                    FacetProvider last = requiredBy.put(r.value(), provider);

                    // Detect circular dependencies
                    if (last != null && updatePriority(last, r.value()) <= updatePriority(provider, r.value())) {
                        FacetProvider other = providedBy.get(r.value());
                        String help = "";
                        if (updatesFacet(other, r.value())) {
                            help = "\nMaybe the priority of " + other.getClass().getSimpleName() + " could be adjusted below "
                                    + UpdatePriority.priorityString(updatePriority(provider, r.value()));
                        } else if (updatesFacet(provider, providedFacet)) {
                            help = "\nMaybe the priority of " + provider.getClass().getSimpleName() + " could be adjusted below "
                                    + UpdatePriority.priorityString(updatePriority(other, providedFacet));
                        }
                        throw new RuntimeException("Circular dependency detected:\n- " + provider.getClass().getSimpleName() + " provides "
                                + providedFacet.getSimpleName() + " and requires " + r.value().getSimpleName()
                                + "\n- " + other.getClass().getSimpleName() + " provides " + r.value().getSimpleName() + " and requires "
                                + providedFacet.getSimpleName() + help);
                    }

                    addProviderChain(r.value(), scalable, updatePriority(provider, r.value()), orderedProviders);

                    if (last != null) {
                        requiredBy.put(r.value(), last);
                    } else {
                        requiredBy.remove(r.value());
                    }
                });

        orderedProviders.add(provider);
    }

    private Facet[] requiredFacets(FacetProvider provider) {
        Requires requirements = provider.getClass().getAnnotation(Requires.class);
        if (requirements != null) {
            return requirements.value();
        }
        return new Facet[0];
    }

    private Facet[] updatedFacets(FacetProvider provider) {
        Updates updates = provider.getClass().getAnnotation(Updates.class);
        if (updates != null) {
            return updates.value();
        }
        return new Facet[0];
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

    /**
     * @return the priority in which {@code provider} reads {@code facet}. If {@code provider} updates {@code facet}, it's the update priority;
     * if {@code provider} requires {@code facet}, it's {@link UpdatePriority#PRIORITY_REQUIRES};
     * otherwise, it's {@link UpdatePriority#PRIORITY_PRODUCES}.
     */
    private int updatePriority(FacetProvider provider, Class<? extends WorldFacet> facet) {
        Updates updates = provider.getClass().getAnnotation(Updates.class);
        if (updates != null) {
            return updates.priority();
        } else {
            Requires requires = provider.getClass().getAnnotation(Requires.class);
            if (requires != null) {
                for (Facet f : requires.value()) {
                    if (f.value() == facet) {
                        return UpdatePriority.PRIORITY_REQUIRES;
                    }
                }
            }
            return UpdatePriority.PRIORITY_PRODUCES;
        }
    }

    // Ensure that rasterizers that must run after others are in the correct order. This ensures that blocks from
    // the dependent raterizer are not being overwritten by any antecedent rasterizer.
    // TODO: This will only handle first-order dependencies and does not check for circular dependencies
    private List<WorldRasterizer> ensureRasterizerOrdering(ListMultimap<Class<? extends WorldFacet>,
            FacetProvider> providerChains, boolean scalable) {
        List<WorldRasterizer> orderedRasterizers = Lists.newArrayList();

        Set<Class<? extends WorldRasterizer>> addedRasterizers = new HashSet<>();

        for (WorldRasterizer rasterizer : rasterizers) {
            // Does this have dependencies on other rasterizers
            RequiresRasterizer requiresRasterizer = rasterizer.getClass().getAnnotation(RequiresRasterizer.class);
            if (requiresRasterizer != null) {
                List<Class<? extends WorldRasterizer>> antecedentClassList = Arrays.asList(requiresRasterizer.value());
                List<WorldRasterizer> antecedents = rasterizers.stream()
                        .filter(r -> antecedentClassList.contains(r.getClass()))
                        .collect(Collectors.toList());

                // Add all antecedents to the list first
                antecedents.forEach(dependency -> {
                    if (!addedRasterizers.contains(dependency.getClass())) {
                        tryAddRasterizer(orderedRasterizers, dependency, providerChains, scalable);
                        addedRasterizers.add(dependency.getClass());
                    }
                });

                // Then add this one
                tryAddRasterizer(orderedRasterizers, rasterizer, providerChains, scalable);
            } else {
                tryAddRasterizer(orderedRasterizers, rasterizer, providerChains, scalable);
                addedRasterizers.add(rasterizer.getClass());
            }
        }
        return orderedRasterizers;
    }

    private void tryAddRasterizer(List<WorldRasterizer> orderedRasterizers, WorldRasterizer rasterizer,
                                  ListMultimap<Class<? extends WorldFacet>, FacetProvider> providerChains,
                                  boolean scalable) {
        if (scalable && !(rasterizer instanceof ScalableWorldRasterizer)) {
            return;
        }
        Requires requires = rasterizer.getClass().getAnnotation(Requires.class);
        if (requires != null) {
            for (Facet facet : requires.value()) {
                if (!providerChains.containsKey(facet.value())) {
                    return;
                }
            }
        }
        orderedRasterizers.add(rasterizer);
    }


    public FacetedWorldConfigurator createConfigurator() {
        List<ConfigurableFacetProvider> configurables = new ArrayList<>();
        for (FacetProvider facetProvider : providersList) {
            if (facetProvider instanceof ConfigurableFacetProvider) {
                configurables.add((ConfigurableFacetProvider) facetProvider);
            }
        }
        return new FacetedWorldConfigurator(configurables);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generation;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.world.generator.WorldConfigurator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FacetedWorldConfigurator implements WorldConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(FacetedWorldConfigurator.class);

    private final Map<String, Component> properties = Maps.newHashMap();

    private final List<ConfigurableFacetProvider> providers;

    public FacetedWorldConfigurator(List<ConfigurableFacetProvider> providersList) {
        for (ConfigurableFacetProvider provider : providersList) {
            Component old = properties.put(provider.getConfigurationName(), provider.getConfiguration());
            if (old != null) {
                logger.warn("Duplicate property key: {}", provider.getConfigurationName());
            }
        }
        this.providers = providersList;
    }

    @Override
    public Map<String, Component> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public void setProperty(String key, Component comp) {
        for (ConfigurableFacetProvider facetProvider : providers) {
            if (key.equals(facetProvider.getConfigurationName())) {
                facetProvider.setConfiguration(comp);
                properties.put(key, comp);
                return;
            }
        }

        logger.warn("No property {} found", key);
    }
}

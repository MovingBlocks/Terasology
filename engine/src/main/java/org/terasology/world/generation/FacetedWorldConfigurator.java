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

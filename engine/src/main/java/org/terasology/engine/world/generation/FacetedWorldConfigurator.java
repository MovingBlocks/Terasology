// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Observer;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.gestalt.entitysystem.component.Component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FacetedWorldConfigurator implements WorldConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(FacetedWorldConfigurator.class);

    private final Map<String, Component> properties = Maps.newHashMap();

    private final List<ConfigurableFacetProvider> providers;
    private HashMap<String, Set<PropertyChangeListener>> listeners = new HashMap<>();

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
                Component oldComp = properties.get(key);
                properties.put(key, comp);

                PropertyChangeEvent event = new PropertyChangeEvent(this, key, oldComp, comp);
                Set<PropertyChangeListener> allPropListeners = listeners.get("*");
                if (allPropListeners != null) {
                    allPropListeners.forEach(listener -> {
                        listener.propertyChange(event);
                    });
                }
                Set<PropertyChangeListener> specPropListeners = listeners.get(key);
                if (specPropListeners != null) {
                    specPropListeners.forEach(listener -> {
                        listener.propertyChange(event);
                    });
                }
                return;
            }
        }

        logger.warn("No property {} found", key);
    }

    @Override
    public void subscribe(PropertyChangeListener changeListener) {
        Set<PropertyChangeListener> ls = listeners.computeIfAbsent("*", k -> new HashSet<>());
        ls.add(changeListener);
    }

    @Override
    public void unsubscribe(PropertyChangeListener changeListener) {
        listeners.computeIfPresent("*", (k, v) -> {
            v.remove(changeListener);
            return v;
        });
    }

    @Override
    public void subscribe(String propertyName, PropertyChangeListener changeListener) {
        Set<PropertyChangeListener> ls = listeners.computeIfAbsent(propertyName, k -> new HashSet<>());
        ls.add(changeListener);
    }

    @Override
    public void unsubscribe(String propertyName, PropertyChangeListener changeListener) {
        listeners.computeIfPresent(propertyName, (k, v) -> {
            v.remove(changeListener);
            return v;
        });
    }
}

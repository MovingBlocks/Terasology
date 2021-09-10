// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator;

import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

/**
 * Allows for configuration of world generators.
 */
public interface WorldConfigurator  {

    /**
     * The values are supposed to be annotated with {@link org.terasology.rendering.nui.properties.Property}
     * @return a map (label to object)
     */
    Map<String, Component> getProperties();

    /**
     * @param key the name of the configuration
     * @param comp the configuration component
     */
    void setProperty(String key, Component comp);
}

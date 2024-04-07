// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator;

import org.terasology.engine.utilities.subscribables.Subscribable;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

/**
 * Allows for configuration of world generators.
 * Interested parties can subscribe to all or only specific properties and receive change notifications.
 */
public interface WorldConfigurator extends Subscribable {

    /**
     * The values are supposed to be annotated with {@link org.terasology.nui.properties.Property}
     *
     * @return a map (label to object)
     */
    Map<String, Component> getProperties();

    /**
     * @param key the name of the configuration
     * @param comp the configuration component
     */
    void setProperty(String key, Component comp);
}

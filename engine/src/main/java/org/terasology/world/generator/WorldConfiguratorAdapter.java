// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator;

import org.terasology.engine.entitySystem.Component;

import java.util.Collections;
import java.util.Map;

/**
 * A dummy implementation of {@link WorldConfigurator} that does nothing.
 */
public class WorldConfiguratorAdapter implements WorldConfigurator {

    @Override
    public Map<String, Component> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public void setProperty(String key, Component comp) {
        // simply ignore
    }
}

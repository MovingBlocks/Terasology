// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.entitySystem.Component;

public interface ConfigurableFacetProvider extends FacetProvider {
    String getConfigurationName();

    Component getConfiguration();

    void setConfiguration(Component configuration);
}

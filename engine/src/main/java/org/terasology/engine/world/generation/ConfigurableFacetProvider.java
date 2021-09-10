// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.gestalt.entitysystem.component.Component;

public interface ConfigurableFacetProvider extends FacetProvider {
    String getConfigurationName();

    Component getConfiguration();

    void setConfiguration(Component configuration);
}

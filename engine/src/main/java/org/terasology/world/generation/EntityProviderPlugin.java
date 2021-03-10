// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation;

import org.terasology.engine.world.generator.plugin.WorldGeneratorPlugin;

/**
 * A marker interface that combines {@link EntityProvider} and {@link WorldGeneratorPlugin}.
 */
public interface EntityProviderPlugin extends EntityProvider, WorldGeneratorPlugin {
    // marker interface
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator.plugin;

import java.util.List;

@FunctionalInterface
public interface WorldGeneratorPluginLibrary {

    <U extends WorldGeneratorPlugin> List<U> instantiateAllOfType(Class<U> ofType);
}

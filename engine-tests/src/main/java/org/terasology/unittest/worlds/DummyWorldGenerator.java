// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.worlds;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;

@RegisterWorldGenerator(id = "dummy", displayName = "dummy")
public class DummyWorldGenerator extends BaseFacetedWorldGenerator {
    public static final int SURFACE_HEIGHT = 40;

    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;

    public DummyWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld() {
        return new WorldBuilder(worldGeneratorPluginLibrary)
                .addProvider(new FlatSurfaceHeightProvider(SURFACE_HEIGHT))
                .addPlugins();
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.world.WorldRenderer;

public class WorldRendererMode extends MetricsMode {

    public WorldRendererMode() {
        super("\n- World Rendering -");
    }

    @Override
    public String getMetrics() {
        return getName() + "\n" + CoreRegistry.get(WorldRenderer.class).getMetrics();
    }

    @Override
    public boolean isAvailable() {
        return CoreRegistry.get(WorldRenderer.class) != null;
    }
}

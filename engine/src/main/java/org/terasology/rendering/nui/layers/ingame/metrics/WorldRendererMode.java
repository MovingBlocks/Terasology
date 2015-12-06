/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame.metrics;

import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.world.WorldRenderer;

/**
 */
public class WorldRendererMode extends MetricsMode {

    public WorldRendererMode() {
        super("World Rendering");
    }

    @Override
    public String getMetrics() {
        return getName() + "\n" + CoreRegistry.get(WorldRenderer.class).getMetrics();
    }

    @Override
    public boolean isAvailable() {
        return CoreRegistry.get(WorldRenderer.class) != null;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return false;
    }
}

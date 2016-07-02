/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag;

import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import static org.terasology.rendering.opengl.OpenGLUtils.disableWireframeIf;
import static org.terasology.rendering.opengl.OpenGLUtils.enableWireframeIf;

/**
 * TODO: Diagram of this node
 */
public class ObjectsOpaqueNode implements Node {
    @In
    private Config config;

    @In
    private ComponentSystemManager componentSystemManager;

    private RenderingDebugConfig renderingDebugConfig;

    @Override
    public void initialise() {
        renderingDebugConfig = config.getRendering().getDebug();
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/objectsOpaque");
        enableWireframeIf(renderingDebugConfig.isWireframe());

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOpaque();
        }

        disableWireframeIf(renderingDebugConfig.isWireframe());
        PerformanceMonitor.endActivity();
    }
}

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
package org.terasology.rendering.dag.nodes;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.WireframeCapableNode;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

/**
 * This nodes renders overlays, i.e. the black lines highlighting a nearby block the user can interact with.
 *
 * Objects to be rendered as overlays must be registered as implementing the interface RenderSystem and
 * must take advantage of the RenderSystem.renderOverlay() method, which is called in process().
 */
public class OverlaysNode extends WireframeCapableNode {

    @In
    private ComponentSystemManager componentSystemManager;

    /**
     * Initialises the node. -Must- be called once after instantiation.
     */
    @Override
    public void initialise() {
        super.initialise();
        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER));
    }

    /**
     * Iterates over any registered RenderSystem instance and calls its renderOverlay() method.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/overlays");

        READ_ONLY_GBUFFER.bind();

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOverlay();
        }

        PerformanceMonitor.endActivity();
    }
}

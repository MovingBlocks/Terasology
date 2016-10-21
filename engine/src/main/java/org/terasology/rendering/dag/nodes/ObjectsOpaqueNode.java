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
 * TODO: Diagram of this node
 */
public class ObjectsOpaqueNode extends WireframeCapableNode {

    @In
    private ComponentSystemManager componentSystemManager;

    @Override
    public void initialise() {
        super.initialise();
        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/objectsOpaque");

        READ_ONLY_GBUFFER.bind();

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOpaque();
        }

        PerformanceMonitor.endActivity();
    }
}

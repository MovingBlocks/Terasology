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

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

/**
 * This nodes renders overlays, i.e. the black lines highlighting a nearby block the user can interact with.
 *
 * Objects to be rendered as overlays must be registered as implementing the interface RenderSystem and
 * must take advantage of the RenderSystem.renderOverlay() method, which is called in process().
 */
public class OverlaysNode extends AbstractNode implements WireframeCapable {

    private static final ResourceUrn DEFAULT_TEXTURED_MATERIAL = new ResourceUrn("engine:prog.defaultTextured");

    @In
    private ComponentSystemManager componentSystemManager;

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    private Camera playerCamera;
    private SetWireframe wireframeStateChange;

    /**
     * Initialises the node. -Must- be called once after instantiation.
     */
    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();

        wireframeStateChange = new SetWireframe(true);
        RenderingDebugConfig renderingDebugConfig = config.getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER));
        addDesiredStateChange(new EnableMaterial(DEFAULT_TEXTURED_MATERIAL.toString()));
    }

    /**
     * Enables wireframe.
     *
     * Notice that this is just a request and wireframe gets enabled only after the
     * rendering task list has been refreshed. This occurs before the beginning
     * of next frame or earlier.
     */
    public void enableWireframe() {
        if (!getDesiredStateChanges().contains(wireframeStateChange)) {
            addDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    /**
     * Disables wireframe.
     *
     * Notice that this is just a request and wireframe gets disabled only after the
     * rendering task list has been refreshed. This occurs before the beginning
     * of next frame or earlier.
     */
    public void disableWireframe() {
        if (getDesiredStateChanges().contains(wireframeStateChange)) {
            removeDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    /**
     * Iterates over any registered RenderSystem instance and calls its renderOverlay() method.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/overlays");

        playerCamera.lookThrough(); // TODO: remove. Placed here to make the dependency explicit.

        READ_ONLY_GBUFFER.bind(); // TODO: remove when we can bind this via a StateChange

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOverlay();
        }

        PerformanceMonitor.endActivity();
    }
}

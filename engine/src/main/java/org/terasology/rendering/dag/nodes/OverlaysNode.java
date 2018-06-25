/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

/**
 * This nodes renders overlays, i.e. the black lines highlighting a nearby block the user can interact with.
 *
 * Objects to be rendered as overlays must be registered as implementing the interface RenderSystem and
 * must take advantage of the RenderSystem.renderOverlay() method, which is called in process().
 */
public class OverlaysNode extends AbstractNode implements WireframeCapable {
    private static final ResourceUrn DEFAULT_TEXTURED_MATERIAL_URN = new ResourceUrn("engine:prog.defaultTextured");

    private ComponentSystemManager componentSystemManager;
    private WorldRenderer worldRenderer;

    private SetWireframe wireframeStateChange;

    public OverlaysNode(String nodeUri, Context context) {
        super(nodeUri, context);

        componentSystemManager = context.get(ComponentSystemManager.class);

        worldRenderer = context.get(WorldRenderer.class);
        SubmersibleCamera playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThrough(playerCamera));

        wireframeStateChange = new SetWireframe(true);
        RenderingDebugConfig renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        addDesiredStateChange(new BindFbo(context.get(DisplayResolutionDependentFBOs.class).getGBufferPair().getLastUpdatedFbo()));

        addDesiredStateChange(new EnableMaterial(DEFAULT_TEXTURED_MATERIAL_URN));
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
        PerformanceMonitor.startActivity("rendering/" + getUri());

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOverlay();
        }

        PerformanceMonitor.endActivity();
    }
}

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

import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

/**
 * This node renders the opaque (as opposed to semi-transparent)
 * objects present in the world. This node -does not- render the landscape.
 *
 * Objects to be rendered must be registered as implementing the interface RenderSystem and
 * take advantage of the RenderSystem.renderOpaque() method, which is called in process().
 */
public class OpaqueObjectsNode extends AbstractNode implements WireframeCapable {
    private ComponentSystemManager componentSystemManager;
    private WorldRenderer worldRenderer;

    private SetWireframe wireframeStateChange;
    private EnableFaceCulling faceCullingStateChange;

    public OpaqueObjectsNode(String nodeUri, Context context) {
        super(nodeUri, context);

        componentSystemManager = context.get(ComponentSystemManager.class);

        worldRenderer = context.get(WorldRenderer.class);
        Camera playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThrough(playerCamera));

        // IF wireframe is enabled the WireframeTrigger will remove the face culling state change
        // from the set of desired state changes.
        // The alternative would have been to check here first if wireframe mode is enabled and *if not*
        // add the face culling state change. However, if wireframe *is* enabled, the WireframeTrigger
        // would attempt to remove the face culling state even though it isn't there, relying on the
        // quiet behaviour of Set.remove(nonExistentItem). We therefore favored the first solution.
        faceCullingStateChange = new EnableFaceCulling();
        addDesiredStateChange(faceCullingStateChange);

        wireframeStateChange = new SetWireframe(true);
        RenderingDebugConfig renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        addDesiredStateChange(new BindFbo(context.get(DisplayResolutionDependentFBOs.class).getGBufferPair().getLastUpdatedFbo()));
    }

    public void enableWireframe() {
        if (!getDesiredStateChanges().contains(wireframeStateChange)) {
            removeDesiredStateChange(faceCullingStateChange);
            addDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    public void disableWireframe() {
        if (getDesiredStateChanges().contains(wireframeStateChange)) {
            addDesiredStateChange(faceCullingStateChange);
            removeDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    /**
     * Iterates over any registered RenderSystem instance and calls its renderOpaque() method.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOpaque();
        }

        PerformanceMonitor.endActivity();
    }
}

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

import org.lwjgl.opengl.GL11;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.SetDepthFunction;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * TODO: explain what does this node do, really, as right now it's not clear and it's being discussed for removal.
 */
public class FirstPersonViewNode extends ConditionDependentNode implements WireframeCapable {
    private ComponentSystemManager componentSystemManager;
    private WorldRenderer worldRenderer;

    private Camera playerCamera;
    private RenderingDebugConfig renderingDebugConfig;
    private SetWireframe wireframeStateChange;

    public FirstPersonViewNode(Context context) {
        super(context);

        componentSystemManager = context.get(ComponentSystemManager.class);

        worldRenderer = context.get(WorldRenderer.class);
        playerCamera = worldRenderer.getActiveCamera();

        wireframeStateChange = new SetWireframe(true);
        renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        requiresCondition(() -> !renderingDebugConfig.isFirstPersonElementsHidden());
        renderingDebugConfig.subscribe(RenderingDebugConfig.FIRST_PERSON_ELEMENTS_HIDDEN, this);

        addDesiredStateChange(new BindFBO(READONLY_GBUFFER, context.get(DisplayResolutionDependentFBOs.class)));

        // this guarantee the objects drawn by this node are always drawn in front of everything else
        addDesiredStateChange(new SetDepthFunction(GL_ALWAYS));
    }

    public void enableWireframe() {
        if (!getDesiredStateChanges().contains(wireframeStateChange)) {
            addDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    public void disableWireframe() {
        if (getDesiredStateChanges().contains(wireframeStateChange)) {
            removeDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    @Override
    public void process() {
            PerformanceMonitor.startActivity("rendering/firstPersonView");

            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            //GL11.glDepthFunc(GL11.GL_ALWAYS); // TODO: remove commented out lines when we know what to do with this node

            playerCamera.updateMatrices(90f);
            playerCamera.loadProjectionMatrix();

            for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
                renderer.renderFirstPerson();
            }

            playerCamera.updateMatrices();
            playerCamera.loadProjectionMatrix();

            //GL11.glDepthFunc(GL_LEQUAL);
            GL11.glPopMatrix();

            PerformanceMonitor.endActivity();
    }
}

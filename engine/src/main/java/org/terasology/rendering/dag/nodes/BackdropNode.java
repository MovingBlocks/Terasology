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
import org.lwjgl.util.glu.Sphere;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;

import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.WRITEONLY_GBUFFER;

import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.DisableDepthWriting;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThroughNormalized;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Renders the backdrop.
 *
 * In this implementation the backdrop consists of a spherical mesh (a skysphere)
 * on which two sky textures are projected, one for the day and one for the night.
 * The two textures cross-fade as the day turns to night and viceversa.
 *
 * The shader also procedurally adds a main light (sun/moon) in the form of a blurred disc.
 */
public class BackdropNode extends AbstractNode implements WireframeCapable {
    private static final int SLICES = 16;
    private static final int STACKS = 128;
    private static final int RADIUS = 1024;

    @In
    private Config config;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;


    private Camera playerCamera;
    private int skySphere = -1;
    private SetWireframe wireframeStateChange;
    private FBO sceneOpaqueFbo;

    /**
     * This method must be called once shortly after instantiation to fully initialize the node
     * and make it ready for rendering.
     */
    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThroughNormalized(playerCamera));

        initSkysphere(playerCamera.getzFar() < RADIUS ? playerCamera.getzFar() : RADIUS);

        wireframeStateChange = new SetWireframe(true);
        RenderingDebugConfig renderingDebugConfig = config.getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        addDesiredStateChange(new SetViewportToSizeOf(WRITEONLY_GBUFFER, displayResolutionDependentFBOs));
        sceneOpaqueFbo = displayResolutionDependentFBOs.get(READONLY_GBUFFER);

        // We do not call requireFBO as we can count this default buffer is there.
        //addDesiredStateChange(new BindFBO(READ_ONLY_GBUFFER)); // TODO: enable FBO this way when default FBOs are standard FBOs.
        addDesiredStateChange(new EnableMaterial("engine:prog.sky"));

        // By disabling the writing to the depth buffer the sky will always have a depth value
        // set by the latest glClear statement.
        addDesiredStateChange(new DisableDepthWriting());

        // Note: culling GL_FRONT polygons is necessary as we are inside the sphere and
        //       due to vertex ordering the polygons we do see are the GL_BACK ones.
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));
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

    /**
     * Renders the backdrop of the scene - in this implementation: the skysphere.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/backdrop");

        sceneOpaqueFbo.bind(); // TODO: remove when we can bind this via a StateChange
        sceneOpaqueFbo.setRenderBufferMask(true, false, false);

        glCallList(skySphere); // Draws the skysphere

        sceneOpaqueFbo.setRenderBufferMask(true, true, true); // TODO: handle these via new StateChange to be created

        PerformanceMonitor.endActivity();
    }

    private void initSkysphere(float sphereRadius) {
        Sphere sphere = new Sphere();
        sphere.setTextureFlag(true);

        skySphere = glGenLists(1);

        glNewList(skySphere, GL11.GL_COMPILE);
        sphere.draw(sphereRadius, SLICES, STACKS);
        glEndList();
    }
}

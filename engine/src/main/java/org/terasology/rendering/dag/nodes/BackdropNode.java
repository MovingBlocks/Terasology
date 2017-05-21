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
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;

import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.DisableDepthWriting;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThroughNormalized;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.dag.stateChanges.SetFboWriteMask;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

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
    private final static ResourceUrn SKY_MATERIAL = new ResourceUrn("engine:prog.sky");
    private static final int SLICES = 16;
    private static final int STACKS = 128;
    private static final int RADIUS = 1024;

    private WorldRenderer worldRenderer;

    private int skySphere = -1;
    private SetWireframe wireframeStateChange;

    public BackdropNode(Context context) {
        worldRenderer = context.get(WorldRenderer.class);
        Camera playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThroughNormalized(playerCamera));

        initSkysphere(playerCamera.getzFar() < RADIUS ? playerCamera.getzFar() : RADIUS);

        wireframeStateChange = new SetWireframe(true);
        RenderingDebugConfig renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        addDesiredStateChange(new BindFbo(READONLY_GBUFFER, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetFboWriteMask(true, false, false, READONLY_GBUFFER, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(SKY_MATERIAL));

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

        glCallList(skySphere); // Draws the skysphere

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

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

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.WireframeCapableNode;

import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

import org.terasology.rendering.dag.stateChanges.DisableDepthMask;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;


/**
 * TODO: Diagram of this node
 */
public class BackdropNode extends WireframeCapableNode {

    public static final int RADIUS = 1024;
    public static final int SLICES = 16;
    public static final int STACKS = 128;

    @In
    private BackdropRenderer backdropRenderer;

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Camera playerCamera;
    private int skySphere = -1;

    @Override
    public void initialise() {
        super.initialise();
        playerCamera = worldRenderer.getActiveCamera();
        initSkysphere();

        // We do not call requireFBO as we can count this default buffer is there.
        //addDesiredStateChange(new BindFBO(READ_ONLY_GBUFFER)); // TODO: enable FBO this way when default FBOs are standard FBOs.
        addDesiredStateChange(new EnableMaterial("engine:prog.sky"));

        // By disabling the writing to the depth buffer the sky will always have a depth value
        // set by the latest glClear statement.
        addDesiredStateChange(new DisableDepthMask());

        // Note: culling GL_FRONT polygons is necessary as we are inside the sphere and
        //       due to vertex ordering the polygons we do see are the GL_BACK ones.
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/backdrop");
        READ_ONLY_GBUFFER.bind(); // remove this when default FBOs become standard FBOs.

        playerCamera.lookThroughNormalized();
        READ_ONLY_GBUFFER.setRenderBufferMask(true, false, false);

        glCallList(skySphere); // Draws the skysphere

        READ_ONLY_GBUFFER.setRenderBufferMask(true, true, true); // TODO: handle these via new StateChange to be created
        playerCamera.lookThrough();

        PerformanceMonitor.endActivity();
    }

    private void initSkysphere() {
        Sphere sphere = new Sphere();
        sphere.setTextureFlag(true);

        skySphere = glGenLists(1);

        glNewList(skySphere, GL11.GL_COMPILE);
        sphere.draw(RADIUS, SLICES, STACKS);
        glEndList();
    }
}

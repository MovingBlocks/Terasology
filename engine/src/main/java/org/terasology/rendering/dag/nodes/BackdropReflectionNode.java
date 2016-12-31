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
import org.terasology.assets.ResourceUrn;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.DisableDepthWriting;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;

/**
 * An instance of this class is responsible for rendering a reflected backdrop (usually the sky) into the
 * "engine:sceneReflected" buffer. The content of the buffer is later used to render the reflections
 * on the water surface.
 *
 * This class could potentially be used also for other reflecting surfaces, i.e. metal, but it only works
 * for horizontal surfaces.
 *
 * Instances of this class are not dependent on the Video Settings or any other conditions. They can be disabled
 * by using method Node.setEnabled(boolean) or by removing the instance from the Render Graph.
 *
 */
public class BackdropReflectionNode extends AbstractNode {

    public static final ResourceUrn REFLECTED = new ResourceUrn("engine:sceneReflected");
    private static final int RADIUS = 1024;
    private static final int SLICES = 16;
    private static final int STACKS = 128;

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Camera playerCamera;
    private int skySphere = -1;

    /**
     * Node initialization.
     *
     * Internally requires the "engine:sceneReflected" buffer, stored in the (display) resolution-dependent FBO manager.
     * This is a default, half-scale buffer inclusive of a depth buffer FBO. See FBOConfig and ScalingFactors for details
     * on possible FBO configurations.
     *
     * This method also requests the material using the "sky" shaders (vertex, fragment) to be enabled.
     */
    @Override
    public void initialise() {
        this.playerCamera = worldRenderer.getActiveCamera();
        initSkysphere();

        requiresFBO(new FBOConfig(REFLECTED, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFLECTED, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(REFLECTED, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new DisableDepthWriting());
        addDesiredStateChange(new EnableMaterial("engine:prog.sky"));
    }

    /**
     * Renders the sky, reflected, into the buffers attached to the "engine:sceneReflected" FBO. It is used later,
     * to render horizontal reflective surfaces, i.e. water.
     *
     * Notice that this method clears the FBO, both its color and depth attachments. Earlier nodes using the
     * same buffers beware.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/reflectedBackdropNode");

        playerCamera.setReflected(true);
        playerCamera.lookThroughNormalized(); // we don't want the reflected scene to be bobbing or moving with the player

        glCallList(skySphere); // Draws the skysphere

        // TODO: initially avoid the next line by having a LookThroughNormalized state change
        // TODO: eventually, when cameras are components, this node would simply use a different camera.
        playerCamera.lookThrough();
        playerCamera.setReflected(false);

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

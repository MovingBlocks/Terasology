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
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.DisableDepthWriting;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThroughNormalized;
import org.terasology.rendering.dag.stateChanges.ReflectedCamera;
import org.terasology.rendering.dag.stateChanges.SetInputTexture2D;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.terasology.rendering.dag.nodes.BackdropNode.getAllWeatherZenith;
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
    public static final SimpleUri REFLECTED_FBO_URI = new SimpleUri("engine:fbo.sceneReflected");
    private static final ResourceUrn SKY_MATERIAL_URN = new ResourceUrn("engine:prog.sky");
    private static final int RADIUS = 1024;
    private static final int SLICES = 16;
    private static final int STACKS = 128;

    private BackdropProvider backdropProvider;

    private int skySphere = -1;

    private Material skyMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 1.0f, max = 8192.0f)
    private float sunExponent = 512.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 1.0f, max = 8192.0f)
    private float moonExponent = 256.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float skyDaylightBrightness = 0.6f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float skyNightBrightness = 1.0f;

    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private float turbidity;

    /**
     * Internally requires the "engine:sceneReflected" buffer, stored in the (display) resolution-dependent FBO manager.
     * This is a default, half-scale buffer inclusive of a depth buffer FBO. See FBOConfig and ScalingFactors for details
     * on possible FBO configurations.
     *
     * This method also requests the material using the "sky" shaders (vertex, fragment) to be enabled.
     */
    public BackdropReflectionNode(String nodeUri, Context context) {
        super(nodeUri, context);

        backdropProvider = context.get(BackdropProvider.class);

        SubmersibleCamera activeCamera = context.get(WorldRenderer.class).getActiveCamera();
        addDesiredStateChange(new ReflectedCamera(activeCamera));
        addDesiredStateChange(new LookThroughNormalized(activeCamera));
        initSkysphere();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        FBO reflectedFbo = requiresFBO(new FBOConfig(REFLECTED_FBO_URI, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(reflectedFbo));
        addDesiredStateChange(new SetViewportToSizeOf(reflectedFbo));
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new DisableDepthWriting());
        addDesiredStateChange(new EnableMaterial(SKY_MATERIAL_URN));

        skyMaterial = getMaterial(SKY_MATERIAL_URN);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:sky90", SKY_MATERIAL_URN, "texSky90"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot, "engine:sky180", SKY_MATERIAL_URN, "texSky180"));
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
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Common Shader Parameters

        sunDirection = backdropProvider.getSunDirection(false);
        turbidity = backdropProvider.getTurbidity();

        skyMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        skyMaterial.setFloat3("sunVec", sunDirection, true);

        // Specific Shader Parameters

        skyMaterial.setFloat3("zenith", getAllWeatherZenith(sunDirection.y, turbidity), true);
        skyMaterial.setFloat("turbidity", turbidity, true);
        skyMaterial.setFloat("colorExp", backdropProvider.getColorExp(), true);
        skyMaterial.setFloat4("skySettings", sunExponent, moonExponent, skyDaylightBrightness, skyNightBrightness, true);

        // Actual Node Processing

        glCallList(skySphere); // Draws the skysphere

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

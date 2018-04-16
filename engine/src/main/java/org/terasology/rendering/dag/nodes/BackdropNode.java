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
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
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
import org.terasology.rendering.dag.stateChanges.SetInputTexture2D;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

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
    private static final ResourceUrn SKY_MATERIAL_URN = new ResourceUrn("engine:prog.sky");
    private static final int SLICES = 16;
    private static final int STACKS = 128;
    private static final int RADIUS = 1024;

    private WorldRenderer worldRenderer;
    private BackdropProvider backdropProvider;

    private int skySphere = -1;
    private SetWireframe wireframeStateChange;

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

    public BackdropNode(String nodeUri, Context context) {
        super(nodeUri, context);

        backdropProvider = context.get(BackdropProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        SubmersibleCamera activeCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThroughNormalized(activeCamera));

        initSkysphere(activeCamera.getzFar() < RADIUS ? activeCamera.getzFar() : RADIUS);

        wireframeStateChange = new SetWireframe(true);
        RenderingDebugConfig renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        FBO lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();
        addDesiredStateChange(new BindFbo(lastUpdatedGBuffer));
        addDesiredStateChange(new SetFboWriteMask(lastUpdatedGBuffer, true, false, false));

        addDesiredStateChange(new EnableMaterial(SKY_MATERIAL_URN));

        // By disabling the writing to the depth buffer the sky will always have a depth value
        // set by the latest glClear statement.
        addDesiredStateChange(new DisableDepthWriting());

        // Note: culling GL_FRONT polygons is necessary as we are inside the sphere and
        //       due to vertex ordering the polygons we do see are the GL_BACK ones.
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));

        skyMaterial = getMaterial(SKY_MATERIAL_URN);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:sky90", SKY_MATERIAL_URN, "texSky90"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot, "engine:sky180", SKY_MATERIAL_URN, "texSky180"));
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
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Common Shader Parameters

        sunDirection = backdropProvider.getSunDirection(false);
        turbidity = backdropProvider.getTurbidity();

        skyMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        skyMaterial.setFloat3("sunVec", sunDirection, true);

        // Shader Parameters

        skyMaterial.setFloat3("zenith", getAllWeatherZenith(backdropProvider.getSunDirection(false).y, turbidity), true);
        skyMaterial.setFloat("turbidity", turbidity, true);
        skyMaterial.setFloat("colorExp", backdropProvider.getColorExp(), true);
        skyMaterial.setFloat4("skySettings", sunExponent, moonExponent, skyDaylightBrightness, skyNightBrightness, true);

        // Actual Node Processing

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

    static Vector3f getAllWeatherZenith(float thetaSunAngle, float turbidity) {
        float thetaSun = (float) Math.acos(thetaSunAngle);
        Vector4f cx1 = new Vector4f(0.0f, 0.00209f, -0.00375f, 0.00165f);
        Vector4f cx2 = new Vector4f(0.00394f, -0.03202f, 0.06377f, -0.02903f);
        Vector4f cx3 = new Vector4f(0.25886f, 0.06052f, -0.21196f, 0.11693f);
        Vector4f cy1 = new Vector4f(0.0f, 0.00317f, -0.00610f, 0.00275f);
        Vector4f cy2 = new Vector4f(0.00516f, -0.04153f, 0.08970f, -0.04214f);
        Vector4f cy3 = new Vector4f(0.26688f, 0.06670f, -0.26756f, 0.15346f);

        float t2 = turbidity * turbidity;
        float chi = (4.0f / 9.0f - turbidity / 120.0f) * ((float) Math.PI - 2.0f * thetaSun);

        Vector4f theta = new Vector4f(1, thetaSun, thetaSun * thetaSun, thetaSun * thetaSun * thetaSun);

        float why = (4.0453f * turbidity - 4.9710f) * (float) Math.tan(chi) - 0.2155f * turbidity + 2.4192f;
        float x = t2 * cx1.dot(theta) + turbidity * cx2.dot(theta) + cx3.dot(theta);
        float y = t2 * cy1.dot(theta) + turbidity * cy2.dot(theta) + cy3.dot(theta);

        return new Vector3f(why, x, y);
    }
}

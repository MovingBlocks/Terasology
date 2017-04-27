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
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * An instance of this class takes advantage of the color and depth buffers attached to the read-only gbuffer
 * and produces light shafts from the main light (sun/moon). It is therefore a relatively inexpensive
 * 2D effect rendered on a full screen quad - no 3D geometry involved.
 *
 * Trivia: the more correct term would be Crepuscular Rays [1], an atmospheric effect. One day we might
 * be able to provide indoor light shafts through other means and it might be appropriate to rename
 * this node accordingly.
 *
 * [1] https://en.wikipedia.org/wiki/Crepuscular_rays
 */
public class LightShaftsNode extends ConditionDependentNode {
    public static final ResourceUrn LIGHT_SHAFTS_FBO = new ResourceUrn("engine:fbo.lightShafts");
    public static final ResourceUrn LIGHT_SHAFTS_MATERIAL = new ResourceUrn("engine:prog.lightShafts");

    private BackdropProvider backdropProvider;
    private WorldRenderer worldRenderer;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;

    private Material lightShaftsMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float density = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.01f)
    private float exposure = 0.0075f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float weight = 8.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.99f)
    private float decay = 0.95f;

    private SubmersibleCamera activeCamera;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraDir;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraPosition;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector4f sunPositionWorldSpace4 = new Vector4f();
    @SuppressWarnings("FieldCanBeLocal")
    private Vector4f sunPositionScreenSpace = new Vector4f();

    public LightShaftsNode(Context context) {
        super(context);

        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);
        worldRenderer = context.get(WorldRenderer.class);

        activeCamera = worldRenderer.getActiveCamera();

        renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.LIGHT_SHAFTS, this);
        requiresCondition(renderingConfig::isLightShafts);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(LIGHT_SHAFTS_FBO, HALF_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(LIGHT_SHAFTS_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(LIGHT_SHAFTS_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(LIGHT_SHAFTS_MATERIAL));

        lightShaftsMaterial = getMaterial(LIGHT_SHAFTS_MATERIAL);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, LIGHT_SHAFTS_MATERIAL, "texScene"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, DepthStencilTexture, displayResolutionDependentFBOs, LIGHT_SHAFTS_MATERIAL, "texDepth"));
    }

    /**
     * Renders light shafts, taking advantage of the information provided
     * by the color buffer and especially the depth buffer attached to the FBO
     * currently set as read-only.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/lightShafts");

        // Common Shader Parameters

        lightShaftsMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);

        lightShaftsMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        lightShaftsMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
        lightShaftsMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

        cameraDir = activeCamera.getViewingDirection();
        cameraPosition = activeCamera.getPosition();

        lightShaftsMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        lightShaftsMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
        lightShaftsMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
        lightShaftsMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        sunDirection = backdropProvider.getSunDirection(false);
        lightShaftsMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);

        lightShaftsMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        lightShaftsMaterial.setFloat("density", density, true);
        lightShaftsMaterial.setFloat("exposure", exposure, true);
        lightShaftsMaterial.setFloat("weight", weight, true);
        lightShaftsMaterial.setFloat("decay", decay, true);

        sunPositionWorldSpace4.set(sunDirection.x * 10000.0f, sunDirection.y * 10000.0f, sunDirection.z * 10000.0f, 1.0f);
        sunPositionScreenSpace.set(sunPositionWorldSpace4);
        activeCamera.getViewProjectionMatrix().transform(sunPositionScreenSpace);

        sunPositionScreenSpace.x /= sunPositionScreenSpace.w;
        sunPositionScreenSpace.y /= sunPositionScreenSpace.w;
        sunPositionScreenSpace.z /= sunPositionScreenSpace.w;
        sunPositionScreenSpace.w = 1.0f;

        lightShaftsMaterial.setFloat("lightDirDotViewDir", activeCamera.getViewingDirection().dot(sunDirection), true);
        lightShaftsMaterial.setFloat2("lightScreenPos", (sunPositionScreenSpace.x + 1.0f) / 2.0f, (sunPositionScreenSpace.y + 1.0f) / 2.0f, true);

        // Actual Node Processing

        // The source code for this method is quite short because everything happens in the shader and its setup.
        // In particular see the class ShaderParametersLightShafts and resource lightShafts_frag.glsl
        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}

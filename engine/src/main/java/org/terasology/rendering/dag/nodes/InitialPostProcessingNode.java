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
import org.terasology.math.geom.Vector3d;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;

import static org.terasology.rendering.dag.nodes.BloomBlurNode.ONE_8TH_SCALE_FBO;
import static org.terasology.rendering.dag.nodes.LightShaftsNode.LIGHT_SHAFTS_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * An instance of this node adds chromatic aberration (currently non-functional), light shafts,
 * 1/8th resolution bloom and vignette onto the rendering achieved so far, stored in the gbuffer.
 * Stores the result into the InitialPostProcessingNode.INITIAL_POST_FBO, to be used at a later stage.
 */
public class InitialPostProcessingNode extends AbstractNode {
    public static final ResourceUrn INITIAL_POST_FBO = new ResourceUrn("engine:fbo.initialPost");
    public static final ResourceUrn INITIAL_POST_MATERIAL = new ResourceUrn("engine:prog.initialPost");

    private WorldRenderer worldRenderer;
    private BackdropProvider backdropProvider;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;

    private Material initialPostMaterial;

    // TODO: abberationOffsets are never assigned. Why?
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float aberrationOffsetX;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float aberrationOffsetY;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float bloomFactor = 0.5f;

    private SubmersibleCamera activeCamera;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraDir;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraPosition;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f tint;

    public InitialPostProcessingNode(Context context) {
        backdropProvider = context.get(BackdropProvider.class);
        renderingConfig = context.get(Config.class).getRendering();
        worldProvider = context.get(WorldProvider.class);
        worldRenderer = context.get(WorldRenderer.class);

        activeCamera = worldRenderer.getActiveCamera();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        // TODO: see if we could write this straight into a GBUFFER - notice this FBO is used in ShaderParametersHdr
        requiresFBO(new FBOConfig(INITIAL_POST_FBO, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(INITIAL_POST_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(INITIAL_POST_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(INITIAL_POST_MATERIAL));

        initialPostMaterial = getMaterial(INITIAL_POST_MATERIAL);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL, "texScene"));
        // TODO: monitor config parameter by subscribing to it
        if (renderingConfig.isBloom()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, ONE_8TH_SCALE_FBO, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL, "texBloom"));
        }
        // TODO: monitor config parameter by subscribing to it
        if (renderingConfig.isLightShafts()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, LIGHT_SHAFTS_FBO, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL, "texLightShafts"));
        }
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:vignette").get().getId(), INITIAL_POST_MATERIAL, "texVignette"));
    }

    /**
     * Renders a quad, in turn filling the InitialPostProcessingNode.INITIAL_POST_FBO.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/initialPostProcessing");

        // Common Shader Parameters

        initialPostMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);

        initialPostMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        initialPostMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
        initialPostMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

        cameraDir = activeCamera.getViewingDirection();
        cameraPosition = activeCamera.getPosition();

        initialPostMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        initialPostMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
        initialPostMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
        initialPostMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        sunDirection = backdropProvider.getSunDirection(false);
        initialPostMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);

        initialPostMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        tint = worldProvider.getBlock(activeCamera.getPosition()).getTint();
        initialPostMaterial.setFloat3("inLiquidTint", tint.x, tint.y, tint.z, true);

        // TODO: monitor config parameter by subscribing to it
        if (renderingConfig.isBloom()) {
            initialPostMaterial.setFloat("bloomFactor", bloomFactor, true);
        }

        initialPostMaterial.setFloat2("aberrationOffset", aberrationOffsetX, aberrationOffsetY, true);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}

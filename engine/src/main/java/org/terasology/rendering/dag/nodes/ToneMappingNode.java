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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;

import static org.terasology.rendering.dag.nodes.InitialPostProcessingNode.INITIAL_POST_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * The exposure calculated earlier in the rendering process is used by an instance
 * of this node to remap the colors of the image rendered so far, brightening otherwise
 * undetailed dark areas or dimming otherwise burnt bright areas, depending on the circumstances.
 *
 * For more details on the specific algorithm used see shader resource toneMapping_frag.glsl.
 *
 * This node stores its output in TONE_MAPPED_FBO.
 */
public class ToneMappingNode extends AbstractNode {
    public static final ResourceUrn TONE_MAPPING_FBO = new ResourceUrn("engine:fbo.toneMapping");
    public static final ResourceUrn TONE_MAPPING_MATERIAL = new ResourceUrn("engine:prog.toneMapping");

    private BackdropProvider backdropProvider;
    private WorldRenderer worldRenderer;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;
    private ScreenGrabber screenGrabber;

    private Material toneMappingMaterial;

    private SubmersibleCamera activeCamera;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraDir;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraPosition;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float exposureBias = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 100.0f)
    private float whitePoint = 9f;

    public ToneMappingNode(Context context) {
        renderingConfig = context.get(Config.class).getRendering();
        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);
        worldRenderer = context.get(WorldRenderer.class);
        screenGrabber = context.get(ScreenGrabber.class);

        activeCamera = worldRenderer.getActiveCamera();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(TONE_MAPPING_FBO, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(TONE_MAPPING_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(TONE_MAPPING_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(TONE_MAPPING_MATERIAL));

        toneMappingMaterial = getMaterial(TONE_MAPPING_MATERIAL);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot, INITIAL_POST_FBO, ColorTexture, displayResolutionDependentFBOs, TONE_MAPPING_MATERIAL, "texScene"));
    }

    /**
     * Renders a full screen quad with the opengl state defined by the initialise() method,
     * using the GBUFFER as input and filling the TONE_MAPPED_FBO with the output of
     * the shader operations. As such, this method performs purely 2D operations.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/toneMapping");

        // Common Shader Parameters

        toneMappingMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);

        toneMappingMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        toneMappingMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
        toneMappingMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

        cameraDir = activeCamera.getViewingDirection();
        cameraPosition = activeCamera.getPosition();

        toneMappingMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        toneMappingMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
        toneMappingMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
        toneMappingMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        sunDirection = backdropProvider.getSunDirection(false);
        toneMappingMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);

        toneMappingMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters
        toneMappingMaterial.setFloat("exposure", screenGrabber.getExposure() * exposureBias, true);
        toneMappingMaterial.setFloat("whitePoint", whitePoint, true);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}

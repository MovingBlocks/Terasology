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
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.DisableDepthTest;
import org.terasology.rendering.dag.stateChanges.EnableBlending;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetBlendFunction;
import org.terasology.rendering.dag.stateChanges.SetFboWriteMask;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.terasology.rendering.dag.nodes.ShadowMapNode.SHADOW_MAP_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.NormalsTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

// TODO: have this node and the shadowmap node handle multiple directional lights

/**
 * This class is integral to the deferred rendering process.
 * It renders the main light (sun/moon) as a directional light, a type of light emitting parallel rays as is
 * appropriate for astronomical light sources.
 *
 * This achieved by blending a single color into each pixel of the light accumulation buffer, the single
 * color being dependent only on the angle between the camera and the light direction.
 *
 * Eventually the content of the light accumulation buffer is combined with other buffers to correctly
 * light up the 3d scene.
 */
public class DeferredMainLightNode extends AbstractNode {
    private static final ResourceUrn LIGHT_GEOMETRY_MATERIAL = new ResourceUrn("engine:prog.lightGeometryPass");

    private BackdropProvider backdropProvider;
    private WorldRenderer worldRenderer;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;

    private LightComponent mainLightComponent = new LightComponent();

    private Material lightGeometryMaterial;

    private SubmersibleCamera activeCamera;
    private Camera lightCamera;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraDir;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraPosition;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f activeCameraToLightSpace = new Vector3f();
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f mainLightInViewSpace = new Vector3f();

    public DeferredMainLightNode(Context context) {
        backdropProvider = context.get(BackdropProvider.class);
        renderingConfig = context.get(Config.class).getRendering();
        worldProvider = context.get(WorldProvider.class);
        worldRenderer = context.get(WorldRenderer.class);

        activeCamera = worldRenderer.getActiveCamera();
        lightCamera = worldRenderer.getLightCamera();

        addDesiredStateChange(new EnableMaterial(LIGHT_GEOMETRY_MATERIAL));
        lightGeometryMaterial = getMaterial(LIGHT_GEOMETRY_MATERIAL);

        addDesiredStateChange(new DisableDepthTest());

        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new SetBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR));

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        addDesiredStateChange(new BindFBO(READONLY_GBUFFER, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetFboWriteMask(false, false, true, READONLY_GBUFFER, displayResolutionDependentFBOs));

        initMainDirectionalLight();

        ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs = context.get(ShadowMapResolutionDependentFBOs.class);
        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, DepthStencilTexture, displayResolutionDependentFBOs, LIGHT_GEOMETRY_MATERIAL, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, NormalsTexture, displayResolutionDependentFBOs, LIGHT_GEOMETRY_MATERIAL, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, LightAccumulationTexture, displayResolutionDependentFBOs, LIGHT_GEOMETRY_MATERIAL, "texSceneOpaqueLightBuffer"));
        if (renderingConfig.isDynamicShadows()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, SHADOW_MAP_FBO, DepthStencilTexture, shadowMapResolutionDependentFBOs, LIGHT_GEOMETRY_MATERIAL, "texSceneShadowMap"));

            if (renderingConfig.isCloudShadows()) {
                addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:perlinNoiseTileable").get().getId(), LIGHT_GEOMETRY_MATERIAL, "texSceneClouds"));
            }
        }
    }

    // TODO: one day the main light (sun/moon) should be just another light in the scene.
    private void initMainDirectionalLight() {
        mainLightComponent.lightType = LightComponent.LightType.DIRECTIONAL;
        mainLightComponent.lightAmbientIntensity = 0.75f;
        mainLightComponent.lightDiffuseIntensity = 0.75f;
        mainLightComponent.lightSpecularPower = 100f;
    }

    /**
     * Renders the main light (sun/moon) as a uniformly colored full-screen quad.
     * This gets blended into the existing data stored in the light accumulation buffer.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/mainLightGeometry");

        lightGeometryMaterial.activateFeature(ShaderProgramFeature.FEATURE_LIGHT_DIRECTIONAL);

        // Common Shader Parameters

        lightGeometryMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);

        lightGeometryMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        lightGeometryMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
        lightGeometryMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

        cameraDir = activeCamera.getViewingDirection();
        cameraPosition = activeCamera.getPosition();

        lightGeometryMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        lightGeometryMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
        lightGeometryMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
        lightGeometryMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        sunDirection = backdropProvider.getSunDirection(false);
        lightGeometryMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);

        lightGeometryMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        if (renderingConfig.isDynamicShadows()) {
            lightGeometryMaterial.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix(), true);
            lightGeometryMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);

            activeCameraToLightSpace.sub(activeCamera.getPosition(), lightCamera.getPosition());
            lightGeometryMaterial.setFloat3("activeCameraToLightSpace", activeCameraToLightSpace.x, activeCameraToLightSpace.y, activeCameraToLightSpace.z, true);
        }

        // Note: no need to set a camera here: the render takes place
        // with a default opengl camera and the quad is in front of it.

        lightGeometryMaterial.setFloat3("lightColorDiffuse", mainLightComponent.lightColorDiffuse.x,
                mainLightComponent.lightColorDiffuse.y, mainLightComponent.lightColorDiffuse.z, true);
        lightGeometryMaterial.setFloat3("lightColorAmbient", mainLightComponent.lightColorAmbient.x,
                mainLightComponent.lightColorAmbient.y, mainLightComponent.lightColorAmbient.z, true);
        lightGeometryMaterial.setFloat3("lightProperties", mainLightComponent.lightAmbientIntensity,
                mainLightComponent.lightDiffuseIntensity, mainLightComponent.lightSpecularPower, true);

        mainLightInViewSpace = backdropProvider.getSunDirection(true);
        activeCamera.getViewMatrix().transformPoint(mainLightInViewSpace);
        lightGeometryMaterial.setFloat3("lightViewPos", mainLightInViewSpace.x, mainLightInViewSpace.y, mainLightInViewSpace.z, true);

        // Actual Node Processing

        renderFullscreenQuad(); // renders the light.

        lightGeometryMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_LIGHT_DIRECTIONAL);

        PerformanceMonitor.endActivity();
    }
}

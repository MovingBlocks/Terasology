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
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Matrix4f;
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
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetBlendFunction;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.dag.stateChanges.SetFboWriteMask;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;

import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.terasology.rendering.dag.nodes.ShadowMapNode.SHADOW_MAP_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.NormalsTexture;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * Instances of this class are integral to the deferred rendering process.
 * They render point lights as spheres, into the light accumulation buffer
 * (the spheres have a radius proportional to each light's attenuation radius).
 * Data from the light accumulation buffer is eventually combined with the
 * content of other buffers to correctly light up the scene.
 */
public class DeferredPointLightsNode extends AbstractNode {
    private static final ResourceUrn LIGHT_GEOMETRY_MATERIAL = new ResourceUrn("engine:prog.lightGeometryPass");
    private static int lightSphereDisplayList = -1;

    private EntityManager entityManager;
    private BackdropProvider backdropProvider;
    private WorldRenderer worldRenderer;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;

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

    public DeferredPointLightsNode(Context context) {
        backdropProvider = context.get(BackdropProvider.class);
        renderingConfig = context.get(Config.class).getRendering();
        worldProvider = context.get(WorldProvider.class);
        worldRenderer = context.get(WorldRenderer.class);
        entityManager = context.get(EntityManager.class);
        entityManager = context.get(EntityManager.class);

        activeCamera = worldRenderer.getActiveCamera();
        lightCamera = worldRenderer.getLightCamera();

        addDesiredStateChange(new LookThrough(activeCamera));

        lightGeometryMaterial = getMaterial(LIGHT_GEOMETRY_MATERIAL);
        addDesiredStateChange(new EnableMaterial(LIGHT_GEOMETRY_MATERIAL));

        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));

        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new SetBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR));

        addDesiredStateChange(new DisableDepthTest());

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        addDesiredStateChange(new BindFBO(READONLY_GBUFFER, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetFboWriteMask(false, false, true, READONLY_GBUFFER, displayResolutionDependentFBOs));

        initLightSphereDisplayList();

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

    private void initLightSphereDisplayList() {
        lightSphereDisplayList = glGenLists(1);
        Sphere sphere = new Sphere();

        glNewList(lightSphereDisplayList, GL11.GL_COMPILE);
        sphere.draw(1, 8, 8);
        glEndList();
    }

    private boolean lightIsRenderable(LightComponent lightComponent, Vector3f lightPositionRelativeToCamera) {
        // if lightRenderingDistance is 0.0, the light is always considered, no matter the distance.
        boolean lightIsRenderable = lightComponent.lightRenderingDistance == 0.0f
                || lightPositionRelativeToCamera.lengthSquared() < (lightComponent.lightRenderingDistance * lightComponent.lightRenderingDistance);
        // above: rendering distance must be higher than distance from the camera or the light is ignored

        // No matter what, we ignore lights that are not in the camera frustrum
        lightIsRenderable &= activeCamera.getViewFrustum().intersects(lightPositionRelativeToCamera, lightComponent.lightAttenuationRange);
        // TODO: (above) what about lights just off-frame? They might light up in-frame surfaces.

        return lightIsRenderable;
    }

    /**
     * Iterates over all available point lights and renders them as spheres into the light accumulation buffer.
     *
     * Furthermore, lights that are further from the camera than their set rendering distance are ignored,
     * while lights with a rendering distance set to 0.0 are always considered. However, only lights within
     * the camera's field of view (frustrum) are rendered.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/pointLightsGeometry");

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

        // Actual Node Processing

        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            if (lightComponent.lightType == LightComponent.LightType.POINT) {
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
                final Vector3f lightPositionInTeraCoords = locationComponent.getWorldPosition();

                Vector3f lightPositionRelativeToCamera = new Vector3f();
                lightPositionRelativeToCamera.sub(lightPositionInTeraCoords, activeCamera.getPosition());

                if (lightIsRenderable(lightComponent, lightPositionRelativeToCamera)) {
                    lightGeometryMaterial.activateFeature(ShaderProgramFeature.FEATURE_LIGHT_POINT);

                    lightGeometryMaterial.setCamera(activeCamera);

                    // setting shader parameters regarding the light's properties
                    lightGeometryMaterial.setFloat3("lightColorDiffuse",
                            lightComponent.lightColorDiffuse.x, lightComponent.lightColorDiffuse.y, lightComponent.lightColorDiffuse.z, true);
                    lightGeometryMaterial.setFloat3("lightColorAmbient",
                            lightComponent.lightColorAmbient.x, lightComponent.lightColorAmbient.y, lightComponent.lightColorAmbient.z, true);
                    lightGeometryMaterial.setFloat3("lightProperties",
                            lightComponent.lightAmbientIntensity, lightComponent.lightDiffuseIntensity, lightComponent.lightSpecularPower, true);
                    lightGeometryMaterial.setFloat4("lightExtendedProperties",
                            lightComponent.lightAttenuationRange, lightComponent.lightAttenuationFalloff, 0.0f, 0.0f, true);

                    // setting shader parameters for the light position in camera space
                    Vector3f lightPositionInViewSpace = new Vector3f(lightPositionRelativeToCamera);
                    activeCamera.getViewMatrix().transformPoint(lightPositionInViewSpace);
                    lightGeometryMaterial.setFloat3("lightViewPos", lightPositionInViewSpace.x, lightPositionInViewSpace.y, lightPositionInViewSpace.z, true);

                    // set the size and location of the sphere to be rendered via shader parameters
                    Matrix4f modelMatrix = new Matrix4f();
                    modelMatrix.set(lightComponent.lightAttenuationRange); // scales the modelview matrix, effectively scales the light sphere
                    modelMatrix.setTranslation(lightPositionRelativeToCamera); // effectively moves the light sphere in the right position relative to camera
                    lightGeometryMaterial.setMatrix4("modelMatrix", modelMatrix, true);

                    glCallList(lightSphereDisplayList); // draws the light sphere

                    lightGeometryMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_LIGHT_POINT);
                }
            }
        }

        PerformanceMonitor.endActivity();
    }
}

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
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;

import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;

import static org.terasology.rendering.dag.nodes.BackdropReflectionNode.REFLECTED_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.REFRACTIVE;

import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.RenderableChunk;

/**
 * This node renders refractive/reflective blocks, i.e. water blocks.
 *
 * Reflections always include the sky but may or may not include the landscape,
 * depending on the "Reflections" video setting. Any other object currently
 * reflected is an artifact.
 *
 * Refractions distort the blocks behind the refracting surface, i.e. the bottom
 * of a lake seen from above water or the landscape above water when the player is underwater.
 * Refractions are currently always enabled.
 *
 * Note: a third "Reflections" video setting enables Screen-space Reflections (SSR),
 * an experimental feature. It produces initially appealing reflections but rotating the
 * camera partially spoils the effect showing its limits.
 */
public class RefractiveReflectiveBlocksNode extends AbstractNode implements FBOManagerSubscriber {
    public static final ResourceUrn REFRACTIVE_REFLECTIVE_FBO = new ResourceUrn("engine:sceneReflectiveRefractive");
    private static final ResourceUrn CHUNK_MATERIAL = new ResourceUrn("engine:prog.chunk");

    private RenderQueuesHelper renderQueues;
    private WorldRenderer worldRenderer;
    private BackdropProvider backdropProvider;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Material chunkMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    private FBO readOnlyGBufferFbo;
    @SuppressWarnings("FieldCanBeLocal")
    private FBO refractiveReflectiveFbo;

    private SubmersibleCamera activeCamera;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraDir;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraPosition;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector4f lightingSettingsFrag = new Vector4f();
    @SuppressWarnings("FieldCanBeLocal")
    private Vector4f waterSettingsFrag = new Vector4f();
    @SuppressWarnings("FieldCanBeLocal")
    private Vector4f alternativeWaterSettingsFrag = new Vector4f();

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveIntens = 2.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveIntensFalloff = 0.85f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSize = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSizeFalloff = 1.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSpeed = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSpeedFalloff = 0.95f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 5.0f)
    public static float waterOffsetY;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public float waveOverallScale = 1.0f;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    float waterRefraction = 0.04f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    float waterFresnelBias = 0.01f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    float waterFresnelPow = 2.5f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 1.0f, max = 100.0f)
    float waterNormalBias = 10.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    float waterTint = 0.24f;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1024.0f)
    float waterSpecExp = 200.0f;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.5f)
    float parallaxBias = 0.05f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.50f)
    float parallaxScale = 0.05f;

    public RefractiveReflectiveBlocksNode(Context context) {
        renderQueues = context.get(RenderQueuesHelper.class);
        renderingConfig = context.get(Config.class).getRendering();
        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThrough(activeCamera));

        displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(REFRACTIVE_REFLECTIVE_FBO, FULL_SCALE, FBO.Type.HDR).useNormalBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFRACTIVE_REFLECTIVE_FBO, displayResolutionDependentFBOs));
        update(); // Cheeky way to initialise readOnlyGBufferFbo, refractiveReflectiveFbo
        displayResolutionDependentFBOs.subscribe(this);

        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL));

        chunkMaterial = getMaterial(CHUNK_MATERIAL);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:terrain").get().getId(), CHUNK_MATERIAL, "textureAtlas"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:waterStill").get().getId(), CHUNK_MATERIAL, "textureWater"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:lavaStill").get().getId(), CHUNK_MATERIAL, "textureLava"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:waterNormal").get().getId(), CHUNK_MATERIAL, "textureWaterNormal"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:waterNormalAlt").get().getId(), CHUNK_MATERIAL, "textureWaterNormalAlt"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:effects").get().getId(), CHUNK_MATERIAL, "textureEffects"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, REFLECTED_FBO, ColorTexture, displayResolutionDependentFBOs, CHUNK_MATERIAL, "textureWaterReflection"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, CHUNK_MATERIAL, "texSceneOpaque"));
        // TODO: monitor the renderingConfig for changes rather than check every frame
        if (renderingConfig.isNormalMapping()) {
            addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:terrainNormal").get().getId(), CHUNK_MATERIAL, "textureAtlasNormal"));

            if (renderingConfig.isParallaxMapping()) {
                addDesiredStateChange(new SetInputTexture(textureSlot++, Assets.getTexture("engine:terrainHeight").get().getId(), CHUNK_MATERIAL, "textureAtlasHeight"));
            }
        }
    }

    /**
     * This method is where the actual rendering of refractive/reflective blocks takes place.
     *
     * Also takes advantage of the two methods
     *
     * - WorldRenderer.increaseTrianglesCount(int)
     * - WorldRenderer.increaseNotReadyChunkCount(int)
     *
     * to publish some statistics over its own activity.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/RefractiveReflectiveBlocks");

        chunkMaterial.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);

        // Common Shader Parameters

        chunkMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);

        chunkMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        chunkMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
        chunkMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

        cameraDir = activeCamera.getViewingDirection();
        cameraPosition = activeCamera.getPosition();

        chunkMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        chunkMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
        chunkMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
        chunkMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        sunDirection = backdropProvider.getSunDirection(false);
        chunkMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);

        chunkMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        // TODO: This is necessary right now because activateFeature removes all material parameters.
        // TODO: Remove this explicit binding once we get rid of activateFeature, or find a way to retain parameters through it.
        chunkMaterial.setInt("textureAtlas", 0, true);
        chunkMaterial.setInt("textureWater", 1, true);
        chunkMaterial.setInt("textureLava", 2, true);
        chunkMaterial.setInt("textureWaterNormal", 3, true);
        chunkMaterial.setInt("textureWaterNormalAlt", 4, true);
        chunkMaterial.setInt("textureEffects", 5, true);
        chunkMaterial.setInt("textureWaterReflection", 6, true);
        chunkMaterial.setInt("texSceneOpaque", 7, true);

        if (renderingConfig.isNormalMapping()) {
            if (renderingConfig.isParallaxMapping()) {
                chunkMaterial.setFloat4("parallaxProperties", parallaxBias, parallaxScale, 0.0f, 0.0f, true);
            }
        }

        lightingSettingsFrag.set(0, 0, waterSpecExp, 0);
        chunkMaterial.setFloat4("lightingSettingsFrag", lightingSettingsFrag, true);

        waterSettingsFrag.set(waterNormalBias, waterRefraction, waterFresnelBias, waterFresnelPow);
        chunkMaterial.setFloat4("waterSettingsFrag", waterSettingsFrag, true);

        alternativeWaterSettingsFrag.set(waterTint, 0, 0, 0);
        chunkMaterial.setFloat4("alternativeWaterSettingsFrag", alternativeWaterSettingsFrag, true);

        // TODO: monitor the renderingConfig for changes rather than check every frame
        if (renderingConfig.isAnimateWater()) {
            chunkMaterial.setFloat("waveIntensFalloff", waveIntensFalloff, true);
            chunkMaterial.setFloat("waveSizeFalloff", waveSizeFalloff, true);
            chunkMaterial.setFloat("waveSize", waveSize, true);
            chunkMaterial.setFloat("waveSpeedFalloff", waveSpeedFalloff, true);
            chunkMaterial.setFloat("waveSpeed", waveSpeed, true);
            chunkMaterial.setFloat("waveIntens", waveIntens, true);
            chunkMaterial.setFloat("waterOffsetY", waterOffsetY, true);
            chunkMaterial.setFloat("waveOverallScale", waveOverallScale, true);
        }

        chunkMaterial.setFloat("clip", 0.0f, true);

        // Actual Node Processing

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        final Vector3f cameraPosition = activeCamera.getPosition();

        while (renderQueues.chunksAlphaBlend.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksAlphaBlend.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkMaterial, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(REFRACTIVE, chunkPosition, cameraPosition);

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        chunkMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        readOnlyGBufferFbo = displayResolutionDependentFBOs.get(READONLY_GBUFFER);
        refractiveReflectiveFbo = displayResolutionDependentFBOs.get(REFRACTIVE_REFLECTIVE_FBO);

        readOnlyGBufferFbo.attachDepthBufferTo(refractiveReflectiveFbo);
    }
}

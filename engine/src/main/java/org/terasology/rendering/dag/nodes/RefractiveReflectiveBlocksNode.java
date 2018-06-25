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
import org.terasology.engine.SimpleUri;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetInputTexture2D;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.RenderableChunk;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.dag.nodes.BackdropReflectionNode.REFLECTED_FBO_URI;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.POST_FBO_REGENERATION;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.PRE_FBO_REGENERATION;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.REFRACTIVE;

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
public class RefractiveReflectiveBlocksNode extends AbstractNode implements PropertyChangeListener {
    public static final SimpleUri REFRACTIVE_REFLECTIVE_FBO_URI = new SimpleUri("engine:fbo.sceneReflectiveRefractive");

    // TODO: rename to more meaningful/precise variable names, like waveAmplitude or waveHeight.
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveIntensity = 2.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveIntensityFalloff = 0.85f;
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

    private static final ResourceUrn CHUNK_MATERIAL_URN = new ResourceUrn("engine:prog.chunk");
    
    private RenderQueuesHelper renderQueues;
    private WorldRenderer worldRenderer;
    private BackdropProvider backdropProvider;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;

    private Material chunkMaterial;

    private FBO lastUpdatedGBuffer;
    private FBO refractiveReflectiveFbo;

    private SubmersibleCamera activeCamera;

    private boolean normalMappingIsEnabled;
    private boolean parallaxMappingIsEnabled;
    private boolean animatedWaterIsEnabled;

    private StateChange setTerrainNormalsInputTexture;
    private StateChange setTerrainHeightInputTexture;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveOverallScale = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float waterRefraction = 0.04f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float waterFresnelBias = 0.01f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float waterFresnelPow = 2.5f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 1.0f, max = 100.0f)
    private float waterNormalBias = 10.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float waterTint = 0.24f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1024.0f)
    private float waterSpecExp = 200.0f;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.5f)
    private float parallaxBias = 0.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.50f)
    private float parallaxScale = 0.5f;

    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;

    public RefractiveReflectiveBlocksNode(String nodeUri, Context context) {
        super(nodeUri, context);

        renderQueues = context.get(RenderQueuesHelper.class);
        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThrough(activeCamera));

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();
        refractiveReflectiveFbo = requiresFBO(new FBOConfig(REFRACTIVE_REFLECTIVE_FBO_URI, FULL_SCALE, FBO.Type.HDR).useNormalBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(refractiveReflectiveFbo));
        lastUpdatedGBuffer.attachDepthBufferTo(refractiveReflectiveFbo);
        displayResolutionDependentFBOs.subscribe(PRE_FBO_REGENERATION, this);
        displayResolutionDependentFBOs.subscribe(POST_FBO_REGENERATION, this);

        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL_URN));

        chunkMaterial = getMaterial(CHUNK_MATERIAL_URN);

        renderingConfig = context.get(Config.class).getRendering();
        normalMappingIsEnabled = renderingConfig.isNormalMapping();
        renderingConfig.subscribe(RenderingConfig.NORMAL_MAPPING, this);
        parallaxMappingIsEnabled = renderingConfig.isParallaxMapping();
        renderingConfig.subscribe(RenderingConfig.PARALLAX_MAPPING, this);
        animatedWaterIsEnabled = renderingConfig.isAnimateWater();
        renderingConfig.subscribe(RenderingConfig.ANIMATE_WATER, this);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:terrain", CHUNK_MATERIAL_URN, "textureAtlas"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:effects", CHUNK_MATERIAL_URN, "textureEffects"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:waterStill", CHUNK_MATERIAL_URN, "textureWater"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:waterNormal", CHUNK_MATERIAL_URN, "textureWaterNormal"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:waterNormalAlt", CHUNK_MATERIAL_URN, "textureWaterNormalAlt"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, REFLECTED_FBO_URI, ColorTexture, displayResolutionDependentFBOs, CHUNK_MATERIAL_URN, "textureWaterReflection"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, ColorTexture, displayResolutionDependentFBOs, CHUNK_MATERIAL_URN, "texSceneOpaque"));
        setTerrainNormalsInputTexture = new SetInputTexture2D(textureSlot++, "engine:terrainNormal", CHUNK_MATERIAL_URN, "textureAtlasNormal");
        setTerrainHeightInputTexture = new SetInputTexture2D(textureSlot, "engine:terrainHeight", CHUNK_MATERIAL_URN, "textureAtlasHeight");

        if (normalMappingIsEnabled) {
            addDesiredStateChange(setTerrainNormalsInputTexture);
        }

        if (parallaxMappingIsEnabled) {
            addDesiredStateChange(setTerrainHeightInputTexture);
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
        PerformanceMonitor.startActivity("rendering/" + getUri());

        chunkMaterial.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);

        // Common Shader Parameters

        sunDirection = backdropProvider.getSunDirection(false);

        chunkMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        chunkMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        chunkMaterial.setFloat("time", worldProvider.getTime().getDays(), true);
        chunkMaterial.setFloat3("sunVec", sunDirection, true);

        // Specific Shader Parameters

        // TODO: This is necessary right now because activateFeature removes all material parameters.
        // TODO: Remove this explicit binding once we get rid of activateFeature, or find a way to retain parameters through it.
        chunkMaterial.setInt("textureAtlas", 0, true);
        chunkMaterial.setInt("textureEffects", 1, true);
        chunkMaterial.setInt("textureWater", 2, true);
        chunkMaterial.setInt("textureWaterNormal", 3, true);
        chunkMaterial.setInt("textureWaterNormalAlt", 4, true);
        chunkMaterial.setInt("textureWaterReflection", 5, true);
        chunkMaterial.setInt("texSceneOpaque", 6, true);
        if (normalMappingIsEnabled) {
            chunkMaterial.setInt("textureAtlasNormal", 7, true);
        }
        if (parallaxMappingIsEnabled) {
            chunkMaterial.setInt("textureAtlasHeight", 8, true);
            chunkMaterial.setFloat4("parallaxProperties", parallaxBias, parallaxScale, 0.0f, 0.0f, true);
        }

        chunkMaterial.setFloat4("lightingSettingsFrag", 0, 0, waterSpecExp, 0, true);
        chunkMaterial.setFloat4("waterSettingsFrag", waterNormalBias, waterRefraction, waterFresnelBias, waterFresnelPow, true);
        chunkMaterial.setFloat4("alternativeWaterSettingsFrag", waterTint, 0, 0, 0, true);

        if (animatedWaterIsEnabled) {
            chunkMaterial.setFloat("waveIntensityFalloff", waveIntensityFalloff, true);
            chunkMaterial.setFloat("waveSizeFalloff", waveSizeFalloff, true);
            chunkMaterial.setFloat("waveSize", waveSize, true);
            chunkMaterial.setFloat("waveSpeedFalloff", waveSpeedFalloff, true);
            chunkMaterial.setFloat("waveSpeed", waveSpeed, true);
            chunkMaterial.setFloat("waveIntensity", waveIntensity, true);
            chunkMaterial.setFloat("waterOffsetY", waterOffsetY, true);
            chunkMaterial.setFloat("waveOverallScale", waveOverallScale, true);
        }

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

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        chunkMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case PRE_FBO_REGENERATION:
                refractiveReflectiveFbo.detachDepthBuffer();
                return;

            case POST_FBO_REGENERATION:
                lastUpdatedGBuffer.attachDepthBufferTo(refractiveReflectiveFbo);
                return;

            case RenderingConfig.NORMAL_MAPPING:
                normalMappingIsEnabled = renderingConfig.isNormalMapping();
                if (normalMappingIsEnabled) {
                    addDesiredStateChange(setTerrainNormalsInputTexture);
                } else {
                    removeDesiredStateChange(setTerrainNormalsInputTexture);
                }
                break;

            case RenderingConfig.PARALLAX_MAPPING:
                parallaxMappingIsEnabled = renderingConfig.isParallaxMapping();
                if (parallaxMappingIsEnabled) {
                    addDesiredStateChange(setTerrainHeightInputTexture);
                } else {
                    removeDesiredStateChange(setTerrainHeightInputTexture);
                }
                break;

            case RenderingConfig.ANIMATE_WATER:
                animatedWaterIsEnabled = renderingConfig.isAnimateWater();
                break;

            // default: no other cases are possible - see subscribe operations in initialize().
        }

        worldRenderer.requestTaskListRefresh();
    }
}

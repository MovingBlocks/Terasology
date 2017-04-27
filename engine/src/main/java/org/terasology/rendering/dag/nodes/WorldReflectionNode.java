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
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.ReflectedCamera;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.RenderableChunk;

import static org.terasology.rendering.dag.nodes.BackdropReflectionNode.REFLECTED_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.OPAQUE;

/**
 * An instance of this class is responsible for rendering a reflected landscape into the
 * "engine:sceneReflected" buffer. This buffer is then used to produce the reflection
 * of the landscape on the water surface.
 *
 * It could potentially be used also for other reflecting surfaces, i.e. metal, but it only works
 * for horizontal surfaces.
 *
 * An instance of this class is enabled or disabled depending on the reflections setting in the rendering config.
 *
 * Diagram of this node can be viewed from:
 * TODO: move diagram to the wiki when this part of the code is stable
 * - https://docs.google.com/drawings/d/1Iz7MA8Y5q7yjxxcgZW-0antv5kgx6NYkvoInielbwGU/edit?usp=sharing
 */
public class WorldReflectionNode extends ConditionDependentNode {
    public static final ResourceUrn REFLECTED_FBO = new ResourceUrn("engine:sceneReflected");
    private static final ResourceUrn CHUNK_MATERIAL = new ResourceUrn("engine:prog.chunk");

    private RenderQueuesHelper renderQueues;
    private WorldRenderer worldRenderer;
    private BackdropProvider backdropProvider;
    private WorldProvider worldProvider;

    private Material chunkMaterial;
    private RenderingConfig renderingConfig;

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
    public float waveIntens = 2.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public float waveIntensFalloff = 0.85f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public float waveSize = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public float waveSizeFalloff = 1.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public float waveSpeed = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public float waveSpeedFalloff = 0.95f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 5.0f)
    public float waterOffsetY;

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

    /**
     * Constructs an instance of this class.
     *
     * Internally requires the "engine:sceneReflected" buffer, stored in the (display) resolution-dependent FBO manager.
     * This is a default, half-scale buffer inclusive of a depth buffer FBO. See FBOConfig and ScalingFactors for details
     * on possible FBO configurations.
     *
     * This method also requests the material using the "chunk" shaders (vertex, fragment) to be enabled.
     */
    public WorldReflectionNode(Context context) {
        super(context);

        renderQueues = context.get(RenderQueuesHelper.class);
        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new ReflectedCamera(activeCamera)); // this has to go before the LookThrough state change
        addDesiredStateChange(new LookThrough(activeCamera));

        renderingConfig = context.get(Config.class).getRendering();
        requiresCondition(() -> renderingConfig.isReflectiveWater());
        renderingConfig.subscribe(RenderingConfig.REFLECTIVE_WATER, this);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(REFLECTED_FBO, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFLECTED_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(REFLECTED_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));
        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL));

        // TODO: improve EnableMaterial to take advantage of shader feature bitmasks.
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
     * Renders the landscape, reflected, into the buffers attached to the "engine:sceneReflected" FBO. It is used later,
     * to render horizontal reflective surfaces, i.e. water.
     *
     * Notice that this method -does not- clear the FBO. The rendering takes advantage of the depth buffer to decide
     * which pixel is in front of the one already stored in the buffer.
     *
     * See: https://en.wikipedia.org/wiki/Deep_image_compositing
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/worldReflection");

        chunkMaterial.activateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

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

        lightingSettingsFrag.set(0, 0, 0, waterSpecExp);
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

        while (renderQueues.chunksOpaqueReflection.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksOpaqueReflection.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkMaterial, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(OPAQUE, chunkPosition, cameraPosition);

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        chunkMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }
}

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
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;

import static org.terasology.rendering.dag.nodes.BackdropReflectionNode.REFLECTED_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.OPAQUE;

/**
 * This node renders the opaque blocks in the world.
 *
 * In a typical world this is the majority of the world's landscape.
 */
public class OpaqueBlocksNode extends AbstractNode implements WireframeCapable {
    private static final ResourceUrn CHUNK_MATERIAL = new ResourceUrn("engine:prog.chunk");

    private WorldRenderer worldRenderer;
    private RenderQueuesHelper renderQueues;
    private RenderingConfig renderingConfig;
    private BackdropProvider backdropProvider;
    private WorldProvider worldProvider;

    private Material chunkMaterial;
    private SetWireframe wireframeStateChange;
    private RenderingDebugConfig renderingDebugConfig;

    private SubmersibleCamera activeCamera;

    // TODO: rename to more meaningful/precise variable names, like waveAmplitude or waveHeight.
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveIntensity = 2.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveIntensityFalloff = 0.85f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveSize = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveSizeFalloff = 1.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveSpeed = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    private float waveSpeedFalloff = 0.95f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 5.0f)
    private float waterOffsetY = 0.0f;

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
    private float parallaxBias = 0.05f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.50f)
    private float parallaxScale = 0.05f;

    public OpaqueBlocksNode(Context context) {
        renderQueues = context.get(RenderQueuesHelper.class);
        renderingConfig = context.get(Config.class).getRendering();
        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);

        wireframeStateChange = new SetWireframe(true);
        renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThrough(activeCamera));

        addDesiredStateChange(new BindFbo(READONLY_GBUFFER, context.get(DisplayResolutionDependentFBOs.class)));

        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL));

        chunkMaterial = getMaterial(CHUNK_MATERIAL);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:terrain", CHUNK_MATERIAL, "textureAtlas"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:waterStill", CHUNK_MATERIAL, "textureWater"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:lavaStill", CHUNK_MATERIAL, "textureLava"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:waterNormal", CHUNK_MATERIAL, "textureWaterNormal"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:waterNormalAlt", CHUNK_MATERIAL, "textureWaterNormalAlt"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:effects", CHUNK_MATERIAL, "textureEffects"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, REFLECTED_FBO, ColorTexture, displayResolutionDependentFBOs, CHUNK_MATERIAL, "textureWaterReflection"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, CHUNK_MATERIAL, "texSceneOpaque"));
        // TODO: monitor the renderingConfig for changes rather than check every frame
        if (renderingConfig.isNormalMapping()) {
            addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:terrainNormal", CHUNK_MATERIAL, "textureAtlasNormal"));

            if (renderingConfig.isParallaxMapping()) {
                addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:terrainHeight", CHUNK_MATERIAL, "textureAtlasHeight"));
            }
        }
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
     * Renders the world's opaque blocks, effectively, the world's landscape.
     * Does not render semi-transparent blocks, i.e. semi-transparent vegetation.
     *
     * If RenderingDebugConfig.isRenderChunkBoundingBoxes() returns true
     * this method also draws wireframe boxes around chunks, displaying
     * their boundaries.
     *
     * Finally, takes advantage of the two methods
     *
     * - WorldRenderer.increaseTrianglesCount(int)
     * - WorldRenderer.increaseNotReadyChunkCount(int)
     *
     * to publish some statistics over its own activity.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/opaqueChunks");

        // Common Shader Parameters

        chunkMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        chunkMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        chunkMaterial.setFloat3("sunVec", backdropProvider.getSunDirection(false), true);
        chunkMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        if (renderingConfig.isNormalMapping()) {
            if (renderingConfig.isParallaxMapping()) {
                chunkMaterial.setFloat4("parallaxProperties", parallaxBias, parallaxScale, 0.0f, 0.0f, true);
            }
        }

        chunkMaterial.setFloat4("lightingSettingsFrag", 0, 0, 0, waterSpecExp, true);
        chunkMaterial.setFloat4("waterSettingsFrag", waterNormalBias, waterRefraction, waterFresnelBias, waterFresnelPow, true);
        chunkMaterial.setFloat4("alternativeWaterSettingsFrag", waterTint, 0, 0, 0, true);

        // TODO: monitor the renderingConfig for changes rather than check every frame
        if (renderingConfig.isAnimateWater()) {
            chunkMaterial.setFloat("waveIntensityFalloff", waveIntensityFalloff, true);
            chunkMaterial.setFloat("waveSizeFalloff", waveSizeFalloff, true);
            chunkMaterial.setFloat("waveSize", waveSize, true);
            chunkMaterial.setFloat("waveSpeedFalloff", waveSpeedFalloff, true);
            chunkMaterial.setFloat("waveSpeed", waveSpeed, true);
            chunkMaterial.setFloat("waveIntensity", waveIntensity, true);
            chunkMaterial.setFloat("waterOffsetY", waterOffsetY, true);
            chunkMaterial.setFloat("waveOverallScale", waveOverallScale, true);
        }

        chunkMaterial.setFloat("clip", 0.0f, true);

        // Actual Node Processing

        final Vector3f cameraPosition = activeCamera.getPosition();

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        while (renderQueues.chunksOpaque.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksOpaque.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkMaterial, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(OPAQUE, chunkPosition, cameraPosition);

                if (renderingDebugConfig.isRenderChunkBoundingBoxes()) {
                    renderChunkBoundingBox(chunk, chunkPosition, cameraPosition);
                }

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }

    private void renderChunkBoundingBox(RenderableChunk chunk, Vector3f chunkPosition, Vector3f cameraPosition) {
        GL11.glPushMatrix();

        // chunkPositionRelativeToCamera = chunkCoordinates * chunkDimensions - cameraCoordinate
        final Vector3f chunkPositionRelativeToCamera =
                new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                        chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                        chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);

        new AABBRenderer(chunk.getAABB()).renderLocally(1f);

        GL11.glPopMatrix(); // Resets the matrix stack after the rendering of a chunk.
    }
}

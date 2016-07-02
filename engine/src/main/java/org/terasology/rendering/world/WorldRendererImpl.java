/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.world;

import com.google.api.client.util.Lists;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.RenderHelper;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OculusStereoCamera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.dag.AmbientOcclusionPassesNode;
import org.terasology.rendering.dag.BackdropNode;
import org.terasology.rendering.dag.BloomPassesNode;
import org.terasology.rendering.dag.BlurPassesNode;
import org.terasology.rendering.dag.ChunksAlphaRejectNode;
import org.terasology.rendering.dag.ChunksOpaqueNode;
import org.terasology.rendering.dag.ChunksRefractiveReflectiveNode;
import org.terasology.rendering.dag.DirectionalLightsNode;
import org.terasology.rendering.dag.DownSampleSceneAndUpdateExposureNode;
import org.terasology.rendering.dag.FinalPostProcessingNode;
import org.terasology.rendering.dag.FirstPersonViewNode;
import org.terasology.rendering.dag.InitialPostProcessingNode;
import org.terasology.rendering.dag.LightGeometryNode;
import org.terasology.rendering.dag.LightShaftsNode;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.ObjectsOpaqueNode;
import org.terasology.rendering.dag.OutlineNode;
import org.terasology.rendering.dag.OverlaysNode;
import org.terasology.rendering.dag.PrePostCompositeNode;
import org.terasology.rendering.dag.ShadowMapNode;
import org.terasology.rendering.dag.SimpleBlendMaterialsNode;
import org.terasology.rendering.dag.SkyBandsNode;
import org.terasology.rendering.dag.ToneMappingNode;
import org.terasology.rendering.dag.WorldReflectionNode;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.opengl.GraphicState;
import org.terasology.rendering.opengl.PostProcessor;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.LightGeometryHelper;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.RenderableChunk;
import java.util.List;
import java.util.PriorityQueue;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * Renders the 3D world, including background, overlays and first person/in hand objects. 2D UI elements are dealt with elsewhere.
 *
 * This implementation includes support for Oculus VR, a head mounted display. No other stereoscopic displays are
 * supported at this stage: see https://github.com/MovingBlocks/Terasology/issues/2111 for updates.
 *
 * This implementation works closely with a number of support objects, in particular:
 *
 * - a FrameBuffersManager instance, holding handles to GPU buffers used as input and output of rendering steps<br/>
 * - a GraphicState instance, providing a number of methods to affect the OpenGL state<br/>
 * - a PostProcessor instance, taking care of (mostly) 2D processing on the content of the GPU buffers<br/>
 * - a RenderableWorld instance, providing acceleration structures caching blocks requiring different rendering treatments<br/>
 *
 */
public final class WorldRendererImpl implements WorldRenderer {
    private boolean isFirstRenderingStageForCurrentFrame;
    private final RenderQueuesHelper renderQueues;
    private final Context context;
    private final BackdropProvider backdropProvider;
    private final WorldProvider worldProvider;
    private final RenderableWorld renderableWorld;
    private final ShaderManager shaderManager;
    private final Camera playerCamera;

    private float timeSmoothedMainLightIntensity;
    private RenderingStage currentRenderingStage;
    private Material chunkShader;
    // private Material simpleShader; // in use by the currently commented out light stencil pass

    private float millisecondsSinceRenderingStart;
    private float secondsSinceLastFrame;
    private int statChunkMeshEmpty;
    private int statChunkNotReady;
    private int statRenderedTriangles;

    // TODO: to be documented (when understood in more detail)
    public enum ChunkRenderMode {
        DEFAULT,
        REFLECTION,
        SHADOW_MAP,
        Z_PRE_PASS
    }

    private final RenderingConfig renderingConfig;
    private final RenderingDebugConfig renderingDebugConfig;
    private FrameBuffersManager buffersManager;
    private GraphicState graphicState;
    private PostProcessor postProcessor;
    private List<Node> renderGraph; // TODO: will be replaced by a DirectedAcyclicGraph data structure
    private ShadowMapNode shadowMapNode;

    /**
     * Instantiates a WorldRenderer implementation.
     *
     * This particular implementation works as deferred shader. The scene is rendered multiple times per frame
     * in a number of separate passes (each stored in GPU buffers) and the passes are combined throughout the
     * rendering pipeline to calculate per-pixel lighting and other effects.
     *
     * Transparencies are handled through alpha rejection (i.e. ground plants) and alpha-based blending.
     * An exception to this is water, which is handled separately to allow for reflections and refractions, if enabled.
     *
     * By the time it is fully instantiated this implementation is already connected to all the support objects
     * it requires and is ready to render via the render(RenderingStage) method.
     *
     * @param context a context object, to obtain instances of classes such as the rendering config.
     * @param bufferPool a GLBufferPool, to be passed to the RenderableWorld instance used by this implementation.
     */
    public WorldRendererImpl(Context context, GLBufferPool bufferPool) {
        this.context = context;
        this.worldProvider = context.get(WorldProvider.class);
        this.backdropProvider = context.get(BackdropProvider.class);
        this.renderingConfig = context.get(Config.class).getRendering();
        this.renderingDebugConfig = renderingConfig.getDebug();
        this.shaderManager = context.get(ShaderManager.class);

        if (renderingConfig.isOculusVrSupport()) {
            playerCamera = new OculusStereoCamera();
            currentRenderingStage = RenderingStage.LEFT_EYE;

        } else {
            playerCamera = new PerspectiveCamera(renderingConfig.getCameraSettings());
            currentRenderingStage = RenderingStage.MONO;
        }

        // TODO: won't need localPlayerSystem here once camera is in the ES proper
        LocalPlayerSystem localPlayerSystem = context.get(LocalPlayerSystem.class);
        localPlayerSystem.setPlayerCamera(playerCamera);

        renderableWorld = new RenderableWorldImpl(worldProvider, context.get(ChunkProvider.class), bufferPool, playerCamera);
        renderQueues = renderableWorld.getRenderQueues();

        initRenderingSupport();
    }

    private void initRenderingSupport() {
        buffersManager = new FrameBuffersManager();
        context.put(FrameBuffersManager.class, buffersManager);

        graphicState = new GraphicState(buffersManager);
        postProcessor = new PostProcessor(buffersManager);
        context.put(PostProcessor.class, postProcessor);

        buffersManager.setGraphicState(graphicState);
        buffersManager.setPostProcessor(postProcessor);
        buffersManager.initialize();

        shaderManager.initShaders();
        postProcessor.initializeMaterials();
        initMaterials();

        context.put(WorldRenderer.class, this);
        context.put(RenderQueuesHelper.class, renderQueues);
        context.put(RenderableWorld.class, renderableWorld);
        initRenderGraph();
    }

    private void initMaterials() {
        chunkShader = getMaterial("engine:prog.chunk");
        //simpleShader = getMaterial("engine:prog.simple");  // in use by the currently commented out light stencil pass
    }

    private void initRenderGraph() {
        // FIXME: init pipeline without specifying them as a field in this class
        shadowMapNode = createInstance(ShadowMapNode.class, context);
        Node worldReflectionNode = createInstance(WorldReflectionNode.class, context);
        Node backdropNode = createInstance(BackdropNode.class, context);
        Node skybandsNode = createInstance(SkyBandsNode.class, context);
        Node objectOpaqueNode = createInstance(ObjectsOpaqueNode.class, context);
        Node chunksOpaqueNode = createInstance(ChunksOpaqueNode.class, context);
        Node chunksAlphaRejectNode = createInstance(ChunksAlphaRejectNode.class, context);
        Node overlaysNode = createInstance(OverlaysNode.class, context);
        Node firstPersonViewNode = createInstance(FirstPersonViewNode.class, context);
        Node lightGeometryNode = createInstance(LightGeometryNode.class, context);
        Node directionalLightsNode = createInstance(DirectionalLightsNode.class, context);
        Node chunksRefractiveReflectiveNode = createInstance(ChunksRefractiveReflectiveNode.class, context);
        Node outlineNode = createInstance(OutlineNode.class, context);
        Node ambientOcclusionPassesNode = createInstance(AmbientOcclusionPassesNode.class, context);
        Node prePostCompositeNode = createInstance(PrePostCompositeNode.class, context);
        Node simpleBlendMaterialsNode = createInstance(SimpleBlendMaterialsNode.class, context);
        Node lightShaftsNode = createInstance(LightShaftsNode.class, context);
        Node initialPostProcessingNode = createInstance(InitialPostProcessingNode.class, context);
        Node downSampleSceneAndUpdateExposure = createInstance(DownSampleSceneAndUpdateExposureNode.class, context);
        Node toneMappedSceneNode = createInstance(ToneMappingNode.class, context);
        Node bloomPassesNode = createInstance(BloomPassesNode.class, context);
        Node blurPassesNode = createInstance(BlurPassesNode.class, context);
        Node finalPostProcessingNode = createInstance(FinalPostProcessingNode.class, context);

        renderGraph = Lists.newArrayList();
        renderGraph.add(shadowMapNode);
        renderGraph.add(worldReflectionNode);
        renderGraph.add(backdropNode);
        renderGraph.add(skybandsNode);
        renderGraph.add(objectOpaqueNode);
        renderGraph.add(chunksOpaqueNode);
        renderGraph.add(chunksAlphaRejectNode);
        renderGraph.add(overlaysNode);
        renderGraph.add(firstPersonViewNode);
        renderGraph.add(lightGeometryNode);
        renderGraph.add(directionalLightsNode);
        renderGraph.add(chunksRefractiveReflectiveNode);
        renderGraph.add(outlineNode);
        renderGraph.add(ambientOcclusionPassesNode);
        renderGraph.add(prePostCompositeNode);
        renderGraph.add(simpleBlendMaterialsNode);
        // Post-Processing proper: tone mapping, bloom and blur passes // TODO: verify if the order of operations around here is correct
        renderGraph.add(lightShaftsNode);
        renderGraph.add(initialPostProcessingNode);
        renderGraph.add(downSampleSceneAndUpdateExposure);
        renderGraph.add(toneMappedSceneNode);
        renderGraph.add(bloomPassesNode);
        renderGraph.add(blurPassesNode);
        renderGraph.add(finalPostProcessingNode);
    }

    private static <T extends Node> T createInstance(Class<T> type, Context context) {
        // Attempt constructor-based injection first
        T node = InjectionHelper.createWithConstructorInjection(type, context);
        // Then fill @In fields
        InjectionHelper.inject(node, context);
        node.initialise();
        return type.cast(node);
    }

    @Override
    public float getSecondsSinceLastFrame() {
        return secondsSinceLastFrame;
    }

    @Override
    public Material getMaterial(String assetId) {
        return Assets.getMaterial(assetId).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + assetId + "'"));
    }

    @Override
    public void onChunkLoaded(Vector3i pos) {
        renderableWorld.onChunkLoaded(pos);
    }

    @Override
    public void onChunkUnloaded(Vector3i pos) {
        renderableWorld.onChunkUnloaded(pos);
    }

    @Override
    public boolean pregenerateChunks() {
        return renderableWorld.pregenerateChunks();
    }

    @Override
    public void update(float deltaInSeconds) {
        secondsSinceLastFrame += deltaInSeconds;
    }


    private void resetStats() {
        statChunkMeshEmpty = 0;
        statChunkNotReady = 0;
        statRenderedTriangles = 0;
    }

    private void preRenderUpdate(RenderingStage renderingStage) {
        resetStats();

        currentRenderingStage = renderingStage;
        if ((currentRenderingStage == RenderingStage.MONO) || (currentRenderingStage == RenderingStage.LEFT_EYE)) {
            isFirstRenderingStageForCurrentFrame = true;
        } else {
            isFirstRenderingStageForCurrentFrame = false;
        }

        // this is done to execute this code block only once per frame
        // instead of once per eye in a stereo setup.
        if (isFirstRenderingStageForCurrentFrame) {
            timeSmoothedMainLightIntensity = TeraMath.lerp(timeSmoothedMainLightIntensity, getMainLightIntensityAt(playerCamera.getPosition()), secondsSinceLastFrame);

            playerCamera.update(secondsSinceLastFrame);


            renderableWorld.update();
            renderableWorld.generateVBOs();
            secondsSinceLastFrame = 0;

            buffersManager.preRenderUpdate();

            millisecondsSinceRenderingStart += secondsSinceLastFrame * 1000;  // updates the variable animations are based on.
        }

        if (currentRenderingStage != RenderingStage.MONO) {
            playerCamera.updateFrustum();
        }

        // this line needs to be here as deep down it relies on the camera's frustrum, updated just above.
        renderableWorld.queueVisibleChunks(isFirstRenderingStageForCurrentFrame);
    }

    /**
     * TODO: update javadocs
     * This method triggers the execution of the rendering pipeline and, eventually, sends the output to the display
     * or to a file, when grabbing a screenshot.
     *
     * In this particular implementation this method can be called once per frame, when rendering to a standard display,
     * or twice, each time with a different rendering stage, when rendering to the OculusVR head mounted display.
     *
     * PerformanceMonitor.startActivity/endActivity statements are used in this method and in those it executes,
     * to provide statistics regarding the ongoing rendering and its individual steps (i.e. rendering shadows,
     * reflections, 2D filters...).
     *
     * @param renderingStage "MONO" for standard rendering and "LEFT_EYE" or "RIGHT_EYE" for stereoscopic displays.
     */
    @Override
    public void render(RenderingStage renderingStage) {
        preRenderUpdate(renderingStage);

        renderGraph.forEach(Node::process);

        playerCamera.updatePrevViewProjectionMatrix();
    }

    @Override
    public boolean renderLightComponent(LightComponent lightComponent, Vector3f lightWorldPosition, Material program, boolean geometryOnly) {
        Vector3f positionViewSpace = new Vector3f();
        positionViewSpace.sub(lightWorldPosition, playerCamera.getPosition());

        boolean doRenderLight = lightComponent.lightType == LightComponent.LightType.DIRECTIONAL
                || lightComponent.lightRenderingDistance == 0.0f
                || positionViewSpace.lengthSquared() < (lightComponent.lightRenderingDistance * lightComponent.lightRenderingDistance);

        doRenderLight &= isLightVisible(positionViewSpace, lightComponent);

        if (!doRenderLight) {
            return false;
        }

        if (!geometryOnly) {
            if (lightComponent.lightType == LightComponent.LightType.POINT) {
                program.activateFeature(ShaderProgramFeature.FEATURE_LIGHT_POINT);
            } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
                program.activateFeature(ShaderProgramFeature.FEATURE_LIGHT_DIRECTIONAL);
            }
        }
        program.enable();
        program.setCamera(playerCamera);

        Vector3f worldPosition = new Vector3f();
        worldPosition.sub(lightWorldPosition, playerCamera.getPosition());

        Vector3f lightViewPosition = new Vector3f(worldPosition);
        playerCamera.getViewMatrix().transformPoint(lightViewPosition);

        program.setFloat3("lightViewPos", lightViewPosition.x, lightViewPosition.y, lightViewPosition.z, true);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.set(lightComponent.lightAttenuationRange);

        modelMatrix.setTranslation(worldPosition);
        program.setMatrix4("modelMatrix", modelMatrix, true);

        if (!geometryOnly) {
            program.setFloat3("lightColorDiffuse", lightComponent.lightColorDiffuse.x, lightComponent.lightColorDiffuse.y, lightComponent.lightColorDiffuse.z, true);
            program.setFloat3("lightColorAmbient", lightComponent.lightColorAmbient.x, lightComponent.lightColorAmbient.y, lightComponent.lightColorAmbient.z, true);
            program.setFloat3("lightProperties", lightComponent.lightAmbientIntensity, lightComponent.lightDiffuseIntensity, lightComponent.lightSpecularPower, true);
        }

        if (lightComponent.lightType == LightComponent.LightType.POINT) {
            if (!geometryOnly) {
                program.setFloat4("lightExtendedProperties", lightComponent.lightAttenuationRange, lightComponent.lightAttenuationFalloff, 0.0f, 0.0f, true);
            }

            LightGeometryHelper.renderSphereGeometry();
        } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
            // Directional lights cover all pixels on the screen
            renderFullscreenQuad();
        }

        if (!geometryOnly) {
            if (lightComponent.lightType == LightComponent.LightType.POINT) {
                program.deactivateFeature(ShaderProgramFeature.FEATURE_LIGHT_POINT);
            } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
                program.deactivateFeature(ShaderProgramFeature.FEATURE_LIGHT_DIRECTIONAL);
            }
        }

        return true;
    }

    @Override
    public boolean isFirstRenderingStageForCurrentFrame() {
        return isFirstRenderingStageForCurrentFrame;
    }

    @Override
    public void renderChunks(PriorityQueue<RenderableChunk> chunks, ChunkMesh.RenderPhase phase, Camera camera, ChunkRenderMode mode) {
        final Vector3f cameraPosition = camera.getPosition();
        if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
            if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                chunkShader.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
            } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                chunkShader.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
            }

            if (mode == ChunkRenderMode.REFLECTION) {
                chunkShader.setFloat("clip", camera.getClipHeight(), true);
            } else {
                chunkShader.setFloat("clip", 0.0f, true);
            }

            chunkShader.enable();

        } else if (mode == ChunkRenderMode.Z_PRE_PASS) {
            shaderManager.disableShader();
        }

        // render all the chunks in the queue
        while (chunks.size() > 0) {
            RenderableChunk chunk = chunks.poll();
            if (chunk.hasMesh()) {
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();
                final Vector3f chunkPositionRelativeToCamera =
                        new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                                chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                                chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);

                if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
                    chunkShader.setFloat3("chunkPositionWorld",
                            chunkPosition.x * ChunkConstants.SIZE_X,
                            chunkPosition.y * ChunkConstants.SIZE_Y,
                            chunkPosition.z * ChunkConstants.SIZE_Z,
                            true);
                    chunkShader.setFloat("animated", chunk.isAnimated() ? 1.0f : 0.0f, true);
                }

                graphicState.preRenderSetupChunk(chunkPositionRelativeToCamera);

                if (renderingDebugConfig.isRenderChunkBoundingBoxes()) {
                    AABBRenderer aabbRenderer = new AABBRenderer(chunk.getAABB());
                    aabbRenderer.renderLocally(1f);
                    statRenderedTriangles += 12;
                }

                chunk.getMesh().render(phase);
                statRenderedTriangles += chunk.getMesh().triangleCount();

                graphicState.postRenderCleanupChunk();

            } else {
                statChunkNotReady++;
            }
        }

        if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
            if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
            } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
            }
        }
    }

    /**
     * Disposes of support objects used by this implementation.
     */
    @Override
    public void dispose() {
        renderableWorld.dispose();
        worldProvider.dispose();
        graphicState.dispose();
        postProcessor.dispose();
    }

    @Override
    public void setViewDistance(ViewDistance viewDistance) {
        renderableWorld.updateChunksInProximity(viewDistance);
    }

    private boolean isLightVisible(Vector3f positionViewSpace, LightComponent component) {
        return component.lightType == LightComponent.LightType.DIRECTIONAL
                || playerCamera.getViewFrustum().intersects(positionViewSpace, component.lightAttenuationRange);

    }

    @Override
    public boolean isHeadUnderWater() {
        Vector3f cameraPosition = new Vector3f(playerCamera.getPosition());

        // Compensate for waves
        if (renderingConfig.isAnimateWater()) {
            cameraPosition.y -= RenderHelper.evaluateOceanHeightAtPosition(cameraPosition, worldProvider.getTime().getDays());
        }

        if (worldProvider.isBlockRelevant(cameraPosition)) {
            return worldProvider.getBlock(cameraPosition).isLiquid();
        }
        return false;
    }

    @Override
    public float getTimeSmoothedMainLightIntensity() {
        return timeSmoothedMainLightIntensity;
    }

    @Override
    public float getRenderingLightIntensityAt(Vector3f pos) {
        float rawLightValueSun = worldProvider.getSunlight(pos) / 15.0f;
        float rawLightValueBlock = worldProvider.getLight(pos) / 15.0f;

        float lightValueSun = (float) Math.pow(BLOCK_LIGHT_SUN_POW, (1.0f - rawLightValueSun) * 16.0f) * rawLightValueSun;
        lightValueSun *= backdropProvider.getDaylight();
        // TODO: Hardcoded factor and value to compensate for daylight tint and night brightness
        lightValueSun *= 0.9f;
        lightValueSun += 0.05f;

        float lightValueBlock = (float) Math.pow(BLOCK_LIGHT_POW, (1.0f - rawLightValueBlock) * 16.0f) * rawLightValueBlock * BLOCK_INTENSITY_FACTOR;

        return Math.max(lightValueBlock, lightValueSun);
    }

    @Override
    public float getMainLightIntensityAt(Vector3f position) {
        return backdropProvider.getDaylight() * worldProvider.getSunlight(position) / 15.0f;
    }

    @Override
    public float getBlockLightIntensityAt(Vector3f position) {
        return worldProvider.getLight(position) / 15.0f;
    }

    @Override
    public String getMetrics() {
        StringBuilder builder = new StringBuilder();
        builder.append(renderableWorld.getMetrics());
        builder.append("Empty Mesh Chunks: ");
        builder.append(statChunkMeshEmpty);
        builder.append("\n");
        builder.append("Unready Chunks: ");
        builder.append(statChunkNotReady);
        builder.append("\n");
        builder.append("Rendered Triangles: ");
        builder.append(statRenderedTriangles);
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public float getMillisecondsSinceRenderingStart() {
        return millisecondsSinceRenderingStart;
    }

    @Override
    public Camera getActiveCamera() {
        return playerCamera;
    }

    @Override
    public Camera getLightCamera() {
        //FIXME: remove this method
        return shadowMapNode.shadowMapCamera;
    }

    @Override
    public RenderingStage getCurrentRenderStage() {
        return currentRenderingStage;
    }
}

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

import org.lwjgl.opengl.GL11;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.RenderHelper;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OculusStereoCamera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.NodeFactory;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.RenderTaskListGenerator;
import org.terasology.rendering.dag.nodes.AmbientOcclusionPassesNode;
import org.terasology.rendering.dag.nodes.BackdropNode;
import org.terasology.rendering.dag.nodes.BindReadOnlyFBONode;
import org.terasology.rendering.dag.nodes.BloomPassesNode;
import org.terasology.rendering.dag.nodes.BlurPassesNode;
import org.terasology.rendering.dag.nodes.BufferClearingNode;
import org.terasology.rendering.dag.nodes.ChunksAlphaRejectNode;
import org.terasology.rendering.dag.nodes.ChunksOpaqueNode;
import org.terasology.rendering.dag.nodes.ChunksRefractiveReflectiveNode;
import org.terasology.rendering.dag.nodes.DirectionalLightsNode;
import org.terasology.rendering.dag.nodes.DownSampleSceneAndUpdateExposureNode;
import org.terasology.rendering.dag.nodes.FinalPostProcessingNode;
import org.terasology.rendering.dag.nodes.FirstPersonViewNode;
import org.terasology.rendering.dag.nodes.InitialPostProcessingNode;
import org.terasology.rendering.dag.nodes.LightGeometryNode;
import org.terasology.rendering.dag.nodes.LightShaftsNode;
import org.terasology.rendering.dag.nodes.ObjectsOpaqueNode;
import org.terasology.rendering.dag.nodes.OutlineNode;
import org.terasology.rendering.dag.nodes.OverlaysNode;
import org.terasology.rendering.dag.nodes.PrePostCompositeNode;
import org.terasology.rendering.dag.nodes.BackdropReflectionNode;
import org.terasology.rendering.dag.nodes.ShadowMapNode;
import org.terasology.rendering.dag.nodes.SimpleBlendMaterialsNode;
import org.terasology.rendering.dag.nodes.HazeNode;
import org.terasology.rendering.dag.nodes.ToneMappingNode;
import org.terasology.rendering.dag.nodes.WorldReflectionNode;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;
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

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_16TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_32TH_SCALE;

/**
 * Renders the 3D world, including background, overlays and first person/in hand objects. 2D UI elements are dealt with elsewhere.
 *
 * This implementation includes support for Oculus VR, a head mounted display. No other stereoscopic displays are
 * supported at this stage: see https://github.com/MovingBlocks/Terasology/issues/2111 for updates.
 *
 * This implementation works closely with a number of support objects, in particular:
 *
 * TODO: update this section to include new, relevant objects
 * - a RenderableWorld instance, providing acceleration structures caching blocks requiring different rendering treatments<br/>
 *
 */
public final class WorldRendererImpl implements WorldRenderer {

    private static final boolean DELAY_INIT = true;

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
    private ScreenGrabber screenGrabber;
    private List<RenderPipelineTask> renderPipelineTaskList;
    private ShadowMapNode shadowMapNode;

    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;
    private ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs;
    private ImmutableFBOs immutableFBOs;

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
        screenGrabber = new ScreenGrabber(context);
        context.put(ScreenGrabber.class, screenGrabber);

        displayResolutionDependentFBOs = new DisplayResolutionDependentFBOs(context);
        immutableFBOs = new ImmutableFBOs();
        shadowMapResolutionDependentFBOs = new ShadowMapResolutionDependentFBOs();

        context.put(DisplayResolutionDependentFBOs.class, displayResolutionDependentFBOs);
        context.put(ImmutableFBOs.class, immutableFBOs);
        context.put(ShadowMapResolutionDependentFBOs.class, shadowMapResolutionDependentFBOs);

        shaderManager.initShaders();
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
        NodeFactory nodeFactory = new NodeFactory(context);
        RenderGraph renderGraph = new RenderGraph();

        // ShadowMap generation
        FBOConfig shadowMapConfig =
                new FBOConfig(ShadowMapNode.SHADOW_MAP, FBO.Type.NO_COLOR).useDepthBuffer();
        BufferClearingNode shadowMapClearingNode = nodeFactory.createInstance(BufferClearingNode.class, DELAY_INIT);
        shadowMapClearingNode.initialise(shadowMapConfig, shadowMapResolutionDependentFBOs, GL_DEPTH_BUFFER_BIT);
        renderGraph.addNode(shadowMapClearingNode, "shadowMapClearingNode");

        shadowMapNode = nodeFactory.createInstance(ShadowMapNode.class);
        renderGraph.addNode(shadowMapNode, "shadowMapNode");

        // (i.e. water) reflection generation
        FBOConfig reflectedBufferConfig =
                new FBOConfig(BackdropReflectionNode.REFLECTED, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer();
        BufferClearingNode reflectedBufferClearingNode = nodeFactory.createInstance(BufferClearingNode.class, DELAY_INIT);
        reflectedBufferClearingNode.initialise(reflectedBufferConfig, displayResolutionDependentFBOs, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderGraph.addNode(reflectedBufferClearingNode, "reflectedBufferClearingNode"); // TODO: verify this is necessary

        Node reflectedBackdropNode = nodeFactory.createInstance(BackdropReflectionNode.class);
        renderGraph.addNode(reflectedBackdropNode, "reflectedBackdropNode");

        Node worldReflectionNode = nodeFactory.createInstance(WorldReflectionNode.class);
        renderGraph.addNode(worldReflectionNode, "worldReflectionNode");

        // TODO: write snippets and shaders to inspect content of a color/depth buffer

        // sky generation
        FBOConfig reflectedRefractedBufferConfig = new FBOConfig(new ResourceUrn("engine:sceneReflectiveRefractive"), FULL_SCALE, FBO.Type.HDR).useNormalBuffer();
        BufferClearingNode reflectedRefractedClearingNode = nodeFactory.createInstance(BufferClearingNode.class, DELAY_INIT);
        reflectedRefractedClearingNode.initialise(reflectedRefractedBufferConfig, displayResolutionDependentFBOs, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderGraph.addNode(reflectedRefractedClearingNode, "reflectedRefractedClearingNode");

        BufferClearingNode readBufferClearingNode = nodeFactory.createInstance(BufferClearingNode.class, DELAY_INIT);
        readBufferClearingNode.initialise(READ_ONLY_GBUFFER.getConfig(), displayResolutionDependentFBOs,
                GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        renderGraph.addNode(readBufferClearingNode, "readBufferClearingNode");

        Node backdropNode = nodeFactory.createInstance(BackdropNode.class);
        renderGraph.addNode(backdropNode, "backdropNode");

        String aLabel = "hazeIntermediateNode";
        FBOConfig hazeIntermediateConfig = new FBOConfig(HazeNode.INTERMEDIATE_HAZE, ONE_16TH_SCALE, FBO.Type.DEFAULT);
        HazeNode hazeIntermediateNode = nodeFactory.createInstance(HazeNode.class, DELAY_INIT);
        hazeIntermediateNode.initialise(READ_ONLY_GBUFFER.getConfig(), hazeIntermediateConfig, aLabel);
        renderGraph.addNode(hazeIntermediateNode, aLabel);

        aLabel = "hazeFinalNode";
        FBOConfig hazeFinalConfig = new FBOConfig(HazeNode.FINAL_HAZE, ONE_32TH_SCALE, FBO.Type.DEFAULT);
        HazeNode hazeFinalNode = nodeFactory.createInstance(HazeNode.class, DELAY_INIT);
        hazeFinalNode.initialise(hazeIntermediateConfig, hazeFinalConfig, aLabel);
        renderGraph.addNode(hazeFinalNode, aLabel);

        // TODO: eventually eliminate this node. The operations in its process() method will move to the following nodes.
        Node bindReadOnlyFBONode = nodeFactory.createInstance(BindReadOnlyFBONode.class);
        renderGraph.addNode(bindReadOnlyFBONode, "bindReadOnlyFBONode");

        // TODO: node instantiation and node addition to the graph should be handled as above, for easy deactivation of nodes during the debug.
        Node objectOpaqueNode = nodeFactory.createInstance(ObjectsOpaqueNode.class);
        Node chunksOpaqueNode = nodeFactory.createInstance(ChunksOpaqueNode.class);
        Node chunksAlphaRejectNode = nodeFactory.createInstance(ChunksAlphaRejectNode.class);
        Node overlaysNode = nodeFactory.createInstance(OverlaysNode.class);
        Node firstPersonViewNode = nodeFactory.createInstance(FirstPersonViewNode.class);
        Node lightGeometryNode = nodeFactory.createInstance(LightGeometryNode.class);
        Node directionalLightsNode = nodeFactory.createInstance(DirectionalLightsNode.class);
        // TODO: consider having a none-rendering node for FBO.attachDepthBufferTo() methods
        Node chunksRefractiveReflectiveNode = nodeFactory.createInstance(ChunksRefractiveReflectiveNode.class);
        Node outlineNode = nodeFactory.createInstance(OutlineNode.class);
        Node ambientOcclusionPassesNode = nodeFactory.createInstance(AmbientOcclusionPassesNode.class);
        Node prePostCompositeNode = nodeFactory.createInstance(PrePostCompositeNode.class);
        Node simpleBlendMaterialsNode = nodeFactory.createInstance(SimpleBlendMaterialsNode.class);
        Node lightShaftsNode = nodeFactory.createInstance(LightShaftsNode.class);
        Node initialPostProcessingNode = nodeFactory.createInstance(InitialPostProcessingNode.class);
        Node downSampleSceneAndUpdateExposure = nodeFactory.createInstance(DownSampleSceneAndUpdateExposureNode.class);
        Node toneMappingNode = nodeFactory.createInstance(ToneMappingNode.class);
        Node bloomPassesNode = nodeFactory.createInstance(BloomPassesNode.class);
        Node blurPassesNode = nodeFactory.createInstance(BlurPassesNode.class);
        Node finalPostProcessingNode = nodeFactory.createInstance(FinalPostProcessingNode.class);

        renderGraph.addNode(objectOpaqueNode, "objectOpaqueNode");
        renderGraph.addNode(chunksOpaqueNode, "chunksOpaqueNode");
        renderGraph.addNode(chunksAlphaRejectNode, "chunksAlphaRejectNode");
        renderGraph.addNode(overlaysNode, "overlaysNode");
        renderGraph.addNode(firstPersonViewNode, "firstPersonViewNode");
        renderGraph.addNode(lightGeometryNode, "lightGeometryNode");
        renderGraph.addNode(directionalLightsNode, "directionalLightsNode");
        renderGraph.addNode(chunksRefractiveReflectiveNode, "chunksRefractiveReflectiveNode");
        renderGraph.addNode(outlineNode, "outlineNode");
        renderGraph.addNode(ambientOcclusionPassesNode, "ambientOcclusionPassesNode");
        renderGraph.addNode(prePostCompositeNode, "prePostCompositeNode");
        renderGraph.addNode(simpleBlendMaterialsNode, "simpleBlendMaterialsNode");
        // Post-Processing proper: tone mapping, bloom and blur passes // TODO: verify if the order of operations around here is correct
        renderGraph.addNode(lightShaftsNode, "lightShaftsNode");
        renderGraph.addNode(initialPostProcessingNode, "initialPostProcessingNode");
        renderGraph.addNode(downSampleSceneAndUpdateExposure, "downSampleSceneAndUpdateExposure");
        renderGraph.addNode(toneMappingNode, "toneMappingNode");
        renderGraph.addNode(bloomPassesNode, "bloomPassesNode");
        renderGraph.addNode(blurPassesNode, "blurPassesNode");
        renderGraph.addNode(finalPostProcessingNode, "finalPostProcessingNode");

        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = renderGraph.getNodesInTopologicalOrder();

        renderPipelineTaskList = renderTaskListGenerator.generateFrom(orderedNodes);
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

            displayResolutionDependentFBOs.update();

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

        // TODO: Add a method here to check wireframe configuration and regenerate "renderPipelineTask" accordingly.

        // The following line re-establish OpenGL defaults, so that the nodes/tasks can rely on them.
        // A place where Terasology overrides the defaults is LwjglGraphics.initOpenGLParams(), but
        // there could be potentially other places, i.e. in the UI code. In the rendering engine we'd like
        // to eventually rely on a default OpenGL state.
        glDisable(GL_CULL_FACE);
        //glDisable(GL_DEPTH_TEST);
        //glDisable(GL_NORMALIZE); // currently keeping these as they are, until we find where they are used.
        //glDepthFunc(GL_LESS);

        renderPipelineTaskList.forEach(RenderPipelineTask::execute);

        // this line re-establish Terasology defaults, so that the rest of the application can rely on them.
        LwjglGraphics.initOpenGLParams();

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

    // TODO: review - break this method and move it into the individual nodes using it?
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

                // Effectively this just positions the chunk appropriately, relative to the camera.
                // chunkPositionRelativeToCamera = chunkCoordinates * chunkDimensions - cameraCoordinate
                GL11.glPushMatrix();
                GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);

                // TODO: review - if this is enabled it probably happens multiple times per frame, overdrawing objects.
                if (renderingDebugConfig.isRenderChunkBoundingBoxes()) {
                    AABBRenderer aabbRenderer = new AABBRenderer(chunk.getAABB());
                    aabbRenderer.renderLocally(1f);
                    statRenderedTriangles += 12;
                }

                chunk.getMesh().render(phase);
                statRenderedTriangles += chunk.getMesh().triangleCount();

                GL11.glPopMatrix(); // Resets the matrix stack after the rendering of a chunk.

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
        // TODO: Making this as a subscribable value especially for node "ChunksRefractiveReflectiveNode",
        // TODO: glDisable and glEnable state changes on that node will be dynamically added/removed based on this value.
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
        String stringToReturn = "";
        stringToReturn += renderableWorld.getMetrics();
        stringToReturn += "Empty Mesh Chunks: ";
        stringToReturn += statChunkMeshEmpty;
        stringToReturn += "\n";
        stringToReturn += "Unready Chunks: ";
        stringToReturn += statChunkNotReady;
        stringToReturn += "\n";
        stringToReturn += "Rendered Triangles: ";
        stringToReturn += statRenderedTriangles;
        stringToReturn += "\n";
        return stringToReturn;
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

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

import org.terasology.rendering.dag.nodes.ApplyDeferredLightingNode;
import org.terasology.rendering.dag.nodes.CopyImageToScreenNode;
import org.terasology.rendering.dag.nodes.DeferredMainLightNode;
import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.RenderHelper;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OpenVRStereoCamera;
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
import org.terasology.rendering.dag.nodes.AlphaRejectBlocksNode;
import org.terasology.rendering.dag.nodes.OpaqueBlocksNode;
import org.terasology.rendering.dag.nodes.RefractiveReflectiveBlocksNode;
import org.terasology.rendering.dag.nodes.DownSampleSceneAndUpdateExposureNode;
import org.terasology.rendering.dag.nodes.FinalPostProcessingNode;
import org.terasology.rendering.dag.nodes.CopyImageToHMDNode;
import org.terasology.rendering.dag.nodes.FirstPersonViewNode;
import org.terasology.rendering.dag.nodes.InitialPostProcessingNode;
import org.terasology.rendering.dag.nodes.DeferredPointLightsNode;
import org.terasology.rendering.dag.nodes.LightShaftsNode;
import org.terasology.rendering.dag.nodes.OpaqueObjectsNode;
import org.terasology.rendering.dag.nodes.OutlineNode;
import org.terasology.rendering.dag.nodes.OverlaysNode;
import org.terasology.rendering.dag.nodes.PrePostCompositeNode;
import org.terasology.rendering.dag.nodes.BackdropReflectionNode;
import org.terasology.rendering.dag.nodes.ShadowMapNode;
import org.terasology.rendering.dag.nodes.SimpleBlendMaterialsNode;
import org.terasology.rendering.dag.nodes.HazeNode;
import org.terasology.rendering.dag.nodes.ToneMappingNode;
import org.terasology.rendering.dag.nodes.WorldReflectionNode;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkProvider;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.terasology.rendering.dag.NodeFactory.DELAY_INIT;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_16TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_32TH_SCALE;

/**
 * Renders the 3D world, including background, overlays and first person/in hand objects. 2D UI elements are dealt with elsewhere.
 *
 * This implementation includes support for OpenVR, through which HTC Vive and Oculus Rift is supported.
 *
 * This implementation works closely with a number of support objects, in particular:
 *
 * TODO: update this section to include new, relevant objects
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

    private float millisecondsSinceRenderingStart;
    private float secondsSinceLastFrame;
    private int statChunkMeshEmpty;
    private int statChunkNotReady;
    private int statRenderedTriangles;

    private final RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    private RenderTaskListGenerator renderTaskListGenerator;
    private boolean requestedTaskListRefresh;
    private List<RenderPipelineTask> renderPipelineTaskList;
    private ShadowMapNode shadowMapNode;

    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;
    private ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs;
    private ImmutableFBOs immutableFBOs;
    private OpenVRProvider vrProvider;

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
        this.shaderManager = context.get(ShaderManager.class);
        if (renderingConfig.isVrSupport()) {
            this.vrProvider = new OpenVRProvider();
            context.put(OpenVRProvider.class, vrProvider);
            if (this.vrProvider.init()) {
                playerCamera = new OpenVRStereoCamera(this.vrProvider);
                currentRenderingStage = RenderingStage.LEFT_EYE;
            } else {
                playerCamera = new PerspectiveCamera(renderingConfig.getCameraSettings());
                currentRenderingStage = RenderingStage.MONO;
            }
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

        // TODO: write snippets and shaders to inspect content of a color/depth buffer - debug mode

        // sky rendering
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

        // world rendering
        Node opaqueObjectsNode = nodeFactory.createInstance(OpaqueObjectsNode.class);
        renderGraph.addNode(opaqueObjectsNode, "opaqueObjectsNode");

        Node opaqueBlocksNode = nodeFactory.createInstance(OpaqueBlocksNode.class);
        renderGraph.addNode(opaqueBlocksNode, "opaqueBlocksNode");

        Node alphaRejectBlocksNode = nodeFactory.createInstance(AlphaRejectBlocksNode.class);
        renderGraph.addNode(alphaRejectBlocksNode, "alphaRejectBlocksNode");

        Node overlaysNode = nodeFactory.createInstance(OverlaysNode.class);
        renderGraph.addNode(overlaysNode, "overlaysNode");

        // TODO: remove this, including associated method in the RenderSystem interface
        Node firstPersonViewNode = nodeFactory.createInstance(FirstPersonViewNode.class);
        renderGraph.addNode(firstPersonViewNode, "firstPersonViewNode");

        // lighting
        Node deferredPointLightsNode = nodeFactory.createInstance(DeferredPointLightsNode.class);
        renderGraph.addNode(deferredPointLightsNode, "DeferredPointLightsNode");

        // TODO next!
        Node deferredMainLightNode = nodeFactory.createInstance(DeferredMainLightNode.class);
        renderGraph.addNode(deferredMainLightNode, "deferredMainLightNode");

        Node applyDeferredLightingNode = nodeFactory.createInstance(ApplyDeferredLightingNode.class);
        renderGraph.addNode(applyDeferredLightingNode, "applyDeferredLightingNode");

        // END OF THE SECOND REFACTORING PASS TO SWITCH NODES TO THE NEW ARCHITECTURE - each PR moves this line down.
        // TODO: node instantiation and node addition to the graph should be handled as above, for easy deactivation of nodes during the debug.

        // TODO: eventually eliminate this node. The operations in its process() method will move to the following nodes.
        Node bindReadOnlyFBONode = nodeFactory.createInstance(BindReadOnlyFBONode.class);
        renderGraph.addNode(bindReadOnlyFBONode, "bindReadOnlyFBONode");

        // TODO: consider having a none-rendering node for FBO.attachDepthBufferTo() methods
        Node chunksRefractiveReflectiveNode = nodeFactory.createInstance(RefractiveReflectiveBlocksNode.class);
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
        Node copyToVRFrameBufferNode = nodeFactory.createInstance(CopyImageToHMDNode.class);
        Node copyImageToScreenNode = nodeFactory.createInstance(CopyImageToScreenNode.class);
        

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
        renderGraph.addNode(copyToVRFrameBufferNode, "copyToVRFrameBufferNode");
        renderGraph.addNode(copyImageToScreenNode, "copyImageToScreenNode");

        renderTaskListGenerator = new RenderTaskListGenerator();
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

    @Override
    public void increaseTrianglesCount(int increase) {
        statRenderedTriangles += increase;
    }

    @Override
    public void increaseNotReadyChunkCount(int increase) {
        statChunkNotReady += increase;
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

        if (requestedTaskListRefresh) {
            renderTaskListGenerator.refresh();
            requestedTaskListRefresh = false;
        }
    }

    /**
     * TODO: update javadocs
     * This method triggers the execution of the rendering pipeline and, eventually, sends the output to the display
     * or to a file, when grabbing a screenshot.
     *
     * In this particular implementation this method can be called once per frame, when rendering to a standard display,
     * or twice, each time with a different rendering stage, when rendering to the head mounted display.
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
    public void requestTaskListRefresh() {
        requestedTaskListRefresh = true;
    }

    @Override
    public boolean isFirstRenderingStageForCurrentFrame() {
        return isFirstRenderingStageForCurrentFrame;
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

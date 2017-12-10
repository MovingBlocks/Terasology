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
package org.terasology.rendering.world;

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OpenVRStereoCamera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.RenderTaskListGenerator;
import org.terasology.rendering.dag.nodes.AlphaRejectBlocksNode;
import org.terasology.rendering.dag.nodes.AmbientOcclusionNode;
import org.terasology.rendering.dag.nodes.ApplyDeferredLightingNode;
import org.terasology.rendering.dag.nodes.BackdropNode;
import org.terasology.rendering.dag.nodes.BackdropReflectionNode;
import org.terasology.rendering.dag.nodes.BloomBlurNode;
import org.terasology.rendering.dag.nodes.BlurredAmbientOcclusionNode;
import org.terasology.rendering.dag.nodes.BufferClearingNode;
import org.terasology.rendering.dag.nodes.DeferredMainLightNode;
import org.terasology.rendering.dag.nodes.DeferredPointLightsNode;
import org.terasology.rendering.dag.nodes.DownSamplerForExposureNode;
import org.terasology.rendering.dag.nodes.FinalPostProcessingNode;
import org.terasology.rendering.dag.nodes.FirstPersonViewNode;
import org.terasology.rendering.dag.nodes.HazeNode;
import org.terasology.rendering.dag.nodes.HighPassNode;
import org.terasology.rendering.dag.nodes.InitialPostProcessingNode;
import org.terasology.rendering.dag.nodes.LateBlurNode;
import org.terasology.rendering.dag.nodes.LightShaftsNode;
import org.terasology.rendering.dag.nodes.OpaqueBlocksNode;
import org.terasology.rendering.dag.nodes.OpaqueObjectsNode;
import org.terasology.rendering.dag.nodes.OutlineNode;
import org.terasology.rendering.dag.nodes.OutputToHMDNode;
import org.terasology.rendering.dag.nodes.OutputToScreenNode;
import org.terasology.rendering.dag.nodes.OverlaysNode;
import org.terasology.rendering.dag.nodes.PrePostCompositeNode;
import org.terasology.rendering.dag.nodes.RefractiveReflectiveBlocksNode;
import org.terasology.rendering.dag.nodes.ShadowMapNode;
import org.terasology.rendering.dag.nodes.SimpleBlendMaterialsNode;
import org.terasology.rendering.dag.nodes.ToneMappingNode;
import org.terasology.rendering.dag.nodes.UpdateExposureNode;
import org.terasology.rendering.dag.nodes.WorldReflectionNode;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkProvider;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.terasology.rendering.dag.nodes.DownSamplerForExposureNode.FBO_16X16_CONFIG;
import static org.terasology.rendering.dag.nodes.DownSamplerForExposureNode.FBO_1X1_CONFIG;
import static org.terasology.rendering.dag.nodes.DownSamplerForExposureNode.FBO_2X2_CONFIG;
import static org.terasology.rendering.dag.nodes.DownSamplerForExposureNode.FBO_4X4_CONFIG;
import static org.terasology.rendering.dag.nodes.DownSamplerForExposureNode.FBO_8X8_CONFIG;
import static org.terasology.rendering.dag.nodes.LateBlurNode.FIRST_LATE_BLUR_FBO_URI;
import static org.terasology.rendering.dag.nodes.LateBlurNode.SECOND_LATE_BLUR_FBO_URI;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_16TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_32TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_8TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.QUARTER_SCALE;

/**
 * Renders the 3D world, including background, overlays and first person/in hand objects. 2D UI elements are dealt with elsewhere.
 * <p>
 * This implementation includes support for OpenVR, through which HTC Vive and Oculus Rift is supported.
 * <p>
 * This implementation works closely with a number of support objects, in particular:
 * <p>
 * TODO: update this section to include new, relevant objects
 * - a RenderableWorld instance, providing acceleration structures caching blocks requiring different rendering treatments<br/>
 */
@RegisterSystem
public final class WorldRendererImpl implements WorldRenderer, ComponentSystem {

    /*
     * presumably, the eye height should be context.get(Config.class).getPlayer().getEyeHeight() above the ground plane.
     * It's not, so for now, we use this factor to adjust for the disparity.
     */
    private static final float GROUND_PLANE_HEIGHT_DISPARITY = -0.7f;
    private static RenderGraph renderGraph = new RenderGraph(); // TODO: Try making this non-static

    private boolean isFirstRenderingStageForCurrentFrame;
    private final RenderQueuesHelper renderQueues;
    private final Context context;
    private final BackdropProvider backdropProvider;
    private final WorldProvider worldProvider;
    private final RenderableWorld renderableWorld;
    private final ShaderManager shaderManager;
    private final SubmersibleCamera playerCamera;

    // TODO: @In
    private final OpenVRProvider vrProvider;

    private float timeSmoothedMainLightIntensity;
    private RenderingStage currentRenderingStage;

    private float millisecondsSinceRenderingStart;
    private float secondsSinceLastFrame;
    private int statChunkMeshEmpty;
    private int statChunkNotReady;
    private int statRenderedTriangles;

    private final RenderingConfig renderingConfig;

    private RenderTaskListGenerator renderTaskListGenerator;
    private boolean requestedTaskListRefresh;
    private List<RenderPipelineTask> renderPipelineTaskList;
    private ShadowMapNode shadowMapNode;

    private ImmutableFBOs immutableFBOs;
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;
    private ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs;

    // Required for ComponentSystem to register the system (via @RegisterSystem).
    // @RegisterSystem requires a default constructor, and since we have final variables in the class,
    // it was essential to set them to some value (in this case, null) in this constructor.
    // Note that this constructor shouldn't be actually used normally anywhere in code.
    public WorldRendererImpl() {
        renderingConfig = null;
        vrProvider = null;
        renderQueues = null;
        context = null;
        backdropProvider = null;
        worldProvider = null;
        renderableWorld = null;
        shaderManager = null;
        playerCamera = null;
    }

    /**
     * Instantiates a WorldRenderer implementation.
     * <p>
     * This particular implementation works as deferred shader. The scene is rendered multiple times per frame
     * in a number of separate passes (each stored in GPU buffers) and the passes are combined throughout the
     * rendering pipeline to calculate per-pixel lighting and other effects.
     * <p>
     * Transparencies are handled through alpha rejection (i.e. ground plants) and alpha-based blending.
     * An exception to this is water, which is handled separately to allow for reflections and refractions, if enabled.
     * <p>
     * By the time it is fully instantiated this implementation is already connected to all the support objects
     * it requires and is ready to render via the render(RenderingStage) method.
     *
     * @param context    a context object, to obtain instances of classes such as the rendering config.
     * @param bufferPool a GLBufferPool, to be passed to the RenderableWorld instance used by this implementation.
     */
    public WorldRendererImpl(Context context, GLBufferPool bufferPool) {
        this.context = context;
        this.worldProvider = context.get(WorldProvider.class);
        this.backdropProvider = context.get(BackdropProvider.class);
        this.renderingConfig = context.get(Config.class).getRendering();
        this.shaderManager = context.get(ShaderManager.class);
        vrProvider = OpenVRProvider.getInstance();
        if (renderingConfig.isVrSupport()) {
            context.put(OpenVRProvider.class, vrProvider);
            // If vrProvider.init() returns false, this means that we are unable to initialize VR hardware for some
            // reason (for example, no HMD is connected). In that case, even though the configuration requests
            // vrSupport, we fall back on rendering to the main display. The reason for init failure can be read from
            // the log.
            if (vrProvider.init()) {
                playerCamera = new OpenVRStereoCamera(vrProvider, worldProvider, renderingConfig);
                /*
                * The origin of OpenVR's coordinate system lies on the ground of the user. We have to move this origin
                * such that the ground plane of the rendering system and the ground plane of the room the VR user is
                * in match.
                 */
                vrProvider.getState().setGroundPlaneYOffset(
                        GROUND_PLANE_HEIGHT_DISPARITY - context.get(Config.class).getPlayer().getEyeHeight());
                currentRenderingStage = RenderingStage.LEFT_EYE;
            } else {
                playerCamera = new PerspectiveCamera(worldProvider, renderingConfig, context.get(DisplayDevice.class));
                currentRenderingStage = RenderingStage.MONO;
            }
        } else {
            playerCamera = new PerspectiveCamera(worldProvider, renderingConfig, context.get(DisplayDevice.class));
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
        ScreenGrabber screenGrabber = new ScreenGrabber(context);
        context.put(ScreenGrabber.class, screenGrabber);

        immutableFBOs = new ImmutableFBOs();
        displayResolutionDependentFBOs = new DisplayResolutionDependentFBOs(context.get(Config.class).getRendering(), screenGrabber, context.get(DisplayDevice.class));
        shadowMapResolutionDependentFBOs = new ShadowMapResolutionDependentFBOs();

        context.put(DisplayResolutionDependentFBOs.class, displayResolutionDependentFBOs);
        context.put(ImmutableFBOs.class, immutableFBOs);
        context.put(ShadowMapResolutionDependentFBOs.class, shadowMapResolutionDependentFBOs);

        shaderManager.initShaders();

        context.put(WorldRenderer.class, this);
        context.put(RenderQueuesHelper.class, renderQueues);
        context.put(RenderableWorld.class, renderableWorld);
        initRenderGraph();
    }

    private void initRenderGraph() {
        addShadowMapNodes();

        addReflectionNodes();

        addSkyNodes();

        addWorldRenderingNodes();

        addLightingNodes();

        addRefractiveReflectiveNode();

        add3dDecorationNodes();

        addPrePostProcessingNodes();

        addPostProcessingNodes();

        addOutputNodes();

        renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = renderGraph.getNodesInTopologicalOrder();
        renderPipelineTaskList = renderTaskListGenerator.generateFrom(orderedNodes);
    }

    private void addShadowMapNodes() {
        FBOConfig shadowMapConfig = new FBOConfig(ShadowMapNode.SHADOW_MAP_FBO_URI, FBO.Type.NO_COLOR).useDepthBuffer();
        BufferClearingNode shadowMapClearingNode = new BufferClearingNode(shadowMapConfig, shadowMapResolutionDependentFBOs, GL_DEPTH_BUFFER_BIT);
        renderGraph.addNode(shadowMapClearingNode, "shadowMapClearingNode");

        shadowMapNode = new ShadowMapNode(context);
        renderGraph.addNode(shadowMapNode, "shadowMapNode");
    }

    private void addReflectionNodes() {
        FBOConfig reflectedBufferConfig = new FBOConfig(BackdropReflectionNode.REFLECTED_FBO_URI, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer();
        BufferClearingNode reflectedBufferClearingNode =
                new BufferClearingNode(reflectedBufferConfig, displayResolutionDependentFBOs, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderGraph.addNode(reflectedBufferClearingNode, "reflectedBufferClearingNode");

        Node reflectedBackdropNode = new BackdropReflectionNode(context);
        renderGraph.addNode(reflectedBackdropNode, "reflectedBackdropNode");

        Node worldReflectionNode = new WorldReflectionNode(context);
        renderGraph.addNode(worldReflectionNode, "worldReflectionNode");
    }

    private void addSkyNodes() {
        FBOConfig reflectedRefractedBufferConfig = new FBOConfig(RefractiveReflectiveBlocksNode.REFRACTIVE_REFLECTIVE_FBO_URI, FULL_SCALE, FBO.Type.HDR).useNormalBuffer();
        BufferClearingNode reflectedRefractedClearingNode =
                new BufferClearingNode(reflectedRefractedBufferConfig, displayResolutionDependentFBOs, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderGraph.addNode(reflectedRefractedClearingNode, "reflectedRefractedClearingNode");

        FBOConfig gBuffer1Config = displayResolutionDependentFBOs.getFboConfig(new SimpleUri("engine:fbo.gBuffer1")); // TODO: Remove the hard coded value here
        FBOConfig gBuffer2Config = displayResolutionDependentFBOs.getFboConfig(new SimpleUri("engine:fbo.gBuffer2")); // TODO: Remove the hard coded value here

        BufferClearingNode lastUpdatedGBufferClearingNode =
                new BufferClearingNode(gBuffer1Config, displayResolutionDependentFBOs, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        renderGraph.addNode(lastUpdatedGBufferClearingNode, "gBuffer1ClearingNode");

        BufferClearingNode staleGBufferClearingNode =
                new BufferClearingNode(gBuffer2Config, displayResolutionDependentFBOs, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        renderGraph.addNode(staleGBufferClearingNode, "gBuffer2ClearingNode");

        Node backdropNode = new BackdropNode(context);
        renderGraph.addNode(backdropNode, "backdropNode");

        FBOConfig hazeIntermediateConfig = new FBOConfig(HazeNode.INTERMEDIATE_HAZE_FBO_URI, ONE_16TH_SCALE, FBO.Type.DEFAULT);
        FBO hazeIntermediateFbo = displayResolutionDependentFBOs.request(hazeIntermediateConfig);

        String label = "hazeIntermediate";
        HazeNode hazeIntermediateNode = new HazeNode(context, displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo(), hazeIntermediateFbo, label);
        renderGraph.addNode(hazeIntermediateNode, label + "Node");

        FBOConfig hazeFinalConfig = new FBOConfig(HazeNode.FINAL_HAZE_FBO_URI, ONE_32TH_SCALE, FBO.Type.DEFAULT);
        FBO hazeFinalFbo = displayResolutionDependentFBOs.request(hazeFinalConfig);

        label = "hazeFinal";
        HazeNode hazeFinalNode = new HazeNode(context, hazeIntermediateFbo, hazeFinalFbo, label);
        renderGraph.addNode(hazeFinalNode, label + "Node");
    }

    private void addWorldRenderingNodes() {
        Node opaqueObjectsNode = new OpaqueObjectsNode(context);
        renderGraph.addNode(opaqueObjectsNode, "opaqueObjectsNode");

        Node opaqueBlocksNode = new OpaqueBlocksNode(context);
        renderGraph.addNode(opaqueBlocksNode, "opaqueBlocksNode");

        Node alphaRejectBlocksNode = new AlphaRejectBlocksNode(context);
        renderGraph.addNode(alphaRejectBlocksNode, "alphaRejectBlocksNode");

        Node overlaysNode = new OverlaysNode(context);
        renderGraph.addNode(overlaysNode, "overlaysNode");

        // TODO: remove this, including associated method in the RenderSystem interface
        Node firstPersonViewNode = new FirstPersonViewNode(context);
        renderGraph.addNode(firstPersonViewNode, "firstPersonViewNode");
    }

    private void addLightingNodes() {
        Node deferredPointLightsNode = new DeferredPointLightsNode(context);
        renderGraph.addNode(deferredPointLightsNode, "DeferredPointLightsNode");

        Node deferredMainLightNode = new DeferredMainLightNode(context);
        renderGraph.addNode(deferredMainLightNode, "deferredMainLightNode");

        Node applyDeferredLightingNode = new ApplyDeferredLightingNode(context);
        renderGraph.addNode(applyDeferredLightingNode, "applyDeferredLightingNode");
    }

    private void addRefractiveReflectiveNode() {
        Node chunksRefractiveReflectiveNode = new RefractiveReflectiveBlocksNode(context);
        renderGraph.addNode(chunksRefractiveReflectiveNode, "chunksRefractiveReflectiveNode");
        // TODO: consider having a non-rendering node for FBO.attachDepthBufferTo() methods
    }

    private void add3dDecorationNodes() {
        Node outlineNode = new OutlineNode(context);
        renderGraph.addNode(outlineNode, "outlineNode");

        Node ambientOcclusionNode = new AmbientOcclusionNode(context);
        renderGraph.addNode(ambientOcclusionNode, "ambientOcclusionNode");

        Node blurredAmbientOcclusionNode = new BlurredAmbientOcclusionNode(context);
        renderGraph.addNode(blurredAmbientOcclusionNode, "blurredAmbientOcclusionNode");
    }

    private void addPrePostProcessingNodes() {
        // Pre-post-processing, just one more interaction with 3D data (semi-transparent objects, in SimpleBlendMaterialsNode)
        // and then it's 2D post-processing all the way to the image shown on the display.
        Node prePostCompositeNode = new PrePostCompositeNode(context);
        renderGraph.addNode(prePostCompositeNode, "prePostCompositeNode");

        Node simpleBlendMaterialsNode = new SimpleBlendMaterialsNode(context);
        renderGraph.addNode(simpleBlendMaterialsNode, "simpleBlendMaterialsNode");
    }

    private void addPostProcessingNodes() {
        // Post-Processing proper: tone mapping, light shafts, bloom and blur passes
        Node lightShaftsNode = new LightShaftsNode(context);
        renderGraph.addNode(lightShaftsNode, "lightShaftsNode");

        Node initialPostProcessingNode = new InitialPostProcessingNode(context);
        renderGraph.addNode(initialPostProcessingNode, "initialPostProcessingNode");

        FBOConfig gBuffer2Config = displayResolutionDependentFBOs.getFboConfig(new SimpleUri("engine:fbo.gBuffer2")); // TODO: Remove the hard coded value here
        String label = "downSampling_gBuffer_to_16x16px_forExposure";
        DownSamplerForExposureNode exposureDownSamplerTo16pixels =
                new DownSamplerForExposureNode(context, gBuffer2Config, displayResolutionDependentFBOs, FBO_16X16_CONFIG, immutableFBOs, label);
        renderGraph.addNode(exposureDownSamplerTo16pixels, label + "Node");

        label = "downSampling_16x16px_to_8x8px_forExposure";
        DownSamplerForExposureNode exposureDownSamplerTo8pixels =
                new DownSamplerForExposureNode(context, FBO_16X16_CONFIG, immutableFBOs, FBO_8X8_CONFIG, immutableFBOs, label);
        renderGraph.addNode(exposureDownSamplerTo8pixels, label + "Node");

        label = "downSampling_8x8px_to_4x4px_forExposure";
        DownSamplerForExposureNode exposureDownSamplerTo4pixels = new DownSamplerForExposureNode(context, FBO_8X8_CONFIG, immutableFBOs, FBO_4X4_CONFIG, immutableFBOs, label);
        renderGraph.addNode(exposureDownSamplerTo4pixels, label + "Node");

        label = "downSampling_4x4px_to_2x2px_forExposure";
        DownSamplerForExposureNode exposureDownSamplerTo2pixels = new DownSamplerForExposureNode(context, FBO_4X4_CONFIG, immutableFBOs, FBO_2X2_CONFIG, immutableFBOs, label);
        renderGraph.addNode(exposureDownSamplerTo2pixels, label + "Node");

        label = "downSampling_2x2px_to_1x1px_forExposure";
        DownSamplerForExposureNode exposureDownSamplerTo1pixel = new DownSamplerForExposureNode(context, FBO_2X2_CONFIG, immutableFBOs, FBO_1X1_CONFIG, immutableFBOs, label);
        renderGraph.addNode(exposureDownSamplerTo1pixel, label + "Node");

        Node updateExposureNode = new UpdateExposureNode(context);
        renderGraph.addNode(updateExposureNode, "updateExposureNode");

        Node toneMappingNode = new ToneMappingNode(context);
        renderGraph.addNode(toneMappingNode, "toneMappingNode");

        // Bloom Effect: one high-pass filter and three blur passes
        Node highPassNode = new HighPassNode(context);
        renderGraph.addNode(highPassNode, "highPassNode");

        FBOConfig halfScaleBloomConfig = new FBOConfig(BloomBlurNode.HALF_SCALE_FBO_URI, HALF_SCALE, FBO.Type.DEFAULT);
        FBO halfScaleBloomFbo = displayResolutionDependentFBOs.request(halfScaleBloomConfig);

        label = "halfScaleBlurredBloom";
        BloomBlurNode halfScaleBlurredBloom = new BloomBlurNode(context, displayResolutionDependentFBOs.get(HighPassNode.HIGH_PASS_FBO_URI), halfScaleBloomFbo, label);
        renderGraph.addNode(halfScaleBlurredBloom, label + "Node");

        FBOConfig quarterScaleBloomConfig = new FBOConfig(BloomBlurNode.QUARTER_SCALE_FBO_URI, QUARTER_SCALE, FBO.Type.DEFAULT);
        FBO quarterScaleBloomFbo = displayResolutionDependentFBOs.request(quarterScaleBloomConfig);

        label = "quarterScaleBlurredBloom";
        BloomBlurNode quarterScaleBlurredBloom = new BloomBlurNode(context, halfScaleBloomFbo, quarterScaleBloomFbo, label);
        renderGraph.addNode(quarterScaleBlurredBloom, label + "Node");

        FBOConfig one8thScaleBloomConfig = new FBOConfig(BloomBlurNode.ONE_8TH_SCALE_FBO_URI, ONE_8TH_SCALE, FBO.Type.DEFAULT);
        FBO one8thScaleBloomFbo = displayResolutionDependentFBOs.request(one8thScaleBloomConfig);

        label = "one8thScaleBlurredBloom";
        BloomBlurNode one8thScaleBlurredBloom = new BloomBlurNode(context, quarterScaleBloomFbo, one8thScaleBloomFbo, label);
        renderGraph.addNode(one8thScaleBlurredBloom, label + "Node");

        // Late Blur nodes: assisting Motion Blur and Depth-of-Field effects
        FBOConfig firstLateBlurConfig = new FBOConfig(FIRST_LATE_BLUR_FBO_URI, HALF_SCALE, FBO.Type.DEFAULT);
        FBO firstLateBlurFbo = displayResolutionDependentFBOs.request(firstLateBlurConfig);

        label = "firstLateBlur";
        LateBlurNode firstLateBlurNode = new LateBlurNode(context, displayResolutionDependentFBOs.get(ToneMappingNode.TONE_MAPPING_FBO_URI), firstLateBlurFbo, label);
        renderGraph.addNode(firstLateBlurNode, label + "Node");

        FBOConfig secondLateBlurConfig = new FBOConfig(SECOND_LATE_BLUR_FBO_URI, HALF_SCALE, FBO.Type.DEFAULT);
        FBO secondLateBlurFbo = displayResolutionDependentFBOs.request(secondLateBlurConfig);

        label = "secondLateBlur";
        LateBlurNode secondLateBlurNode = new LateBlurNode(context, firstLateBlurFbo, secondLateBlurFbo, label);
        renderGraph.addNode(secondLateBlurNode, label + "Node");

        Node finalPostProcessingNode = new FinalPostProcessingNode(context);
        renderGraph.addNode(finalPostProcessingNode, "finalPostProcessingNode");
    }

    private void addOutputNodes() {
        Node copyToVRFrameBufferNode = new OutputToHMDNode(context);
        renderGraph.addNode(copyToVRFrameBufferNode, "outputToVRFrameBufferNode");

        Node copyImageToScreenNode = new OutputToScreenNode(context);
        renderGraph.addNode(copyImageToScreenNode, "outputToScreenNode");
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

        if (currentRenderingStage == RenderingStage.MONO || currentRenderingStage == RenderingStage.LEFT_EYE) {
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
     * <p>
     * In this particular implementation this method can be called once per frame, when rendering to a standard display,
     * or twice, each time with a different rendering stage, when rendering to the head mounted display.
     * <p>
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

        renderPipelineTaskList.forEach(RenderPipelineTask::process);

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
        // TODO: Shift this to a better place, after a RenderGraph class has been implemented.
        SetViewportToSizeOf.disposeDefaultInstance();
    }

    @Override
    public void setViewDistance(ViewDistance viewDistance) {
        renderableWorld.updateChunksInProximity(viewDistance);
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
    public SubmersibleCamera getActiveCamera() {
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

    public void recompileShaders() {
        shaderManager.recompileAllShaders();
    }

    @Override
    public void initialise() {
    }

    @Override
    public void preBegin() {
    }

    @Override
    public void postBegin() {
    }

    @Override
    public void preSave() {
    }

    @Override
    public void postSave() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Debugging command for DAG.", requiredPermission = PermissionManager.NO_PERMISSION)
    public void dagNodeCommand(@CommandParam("nodeUri") final String nodeUri, @CommandParam("command") final String command,
                               @CommandParam(value = "arguments") final String... arguments) {
        Node node = renderGraph.findNode(new SimpleUri(nodeUri));
        if (node == null) {
            throw new RuntimeException(("No node is associated with URI '" + nodeUri + "'"));
        }
        node.handleCommand(command, arguments);
    }
}

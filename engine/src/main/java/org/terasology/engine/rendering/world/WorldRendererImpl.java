// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.rendering.RenderingModuleRegistry;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsUtil;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.MethodCommand;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.LocalPlayerSystem;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.cameras.PerspectiveCamera;
import org.terasology.engine.rendering.dag.ModuleRendering;
import org.terasology.engine.rendering.dag.Node;
import org.terasology.engine.rendering.dag.RenderGraph;
import org.terasology.engine.rendering.dag.RenderPipelineTask;
import org.terasology.engine.rendering.dag.RenderTaskListGenerator;
import org.terasology.engine.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.opengl.ScreenGrabber;
import org.terasology.engine.rendering.opengl.fbms.DisplayResolutionDependentFbo;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.LodChunkProvider;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.generator.ScalableWorldGenerator;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.math.TeraMath;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glViewport;


/**
 * Renders the 3D world, including background, overlays and first person/in hand objects. 2D UI elements are dealt with
 * elsewhere.
 * <p>
 * This implementation works closely with a number of support objects, in particular:
 * <p>
 * TODO: update this section to include new, relevant objects - a RenderableWorld instance, providing acceleration
 * structures caching blocks requiring different rendering treatments<br>
 */
public final class WorldRendererImpl implements WorldRenderer {
    /*
     * Presumably, the eye height should be context.get(Config.class).getPlayer().getEyeHeight() above the ground plane.
     * It's not, so for now, we use this factor to adjust for the disparity.
     */
    private static final Logger logger = LoggerFactory.getLogger(WorldRendererImpl.class);
    private static final float GROUND_PLANE_HEIGHT_DISPARITY = -0.7f;
    private RenderGraph renderGraph;
    private RenderingModuleRegistry renderingModuleRegistry;

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
    private final Console console;

    private RenderTaskListGenerator renderTaskListGenerator;
    private boolean requestedTaskListRefresh;
    private List<RenderPipelineTask> renderPipelineTaskList;

    private DisplayResolutionDependentFbo displayResolutionDependentFbo;

    /**
     * Instantiates a WorldRenderer implementation.
     * <p>
     * This particular implementation works as deferred shader. The scene is rendered multiple times per frame in a
     * number of separate passes (each stored in GPU buffers) and the passes are combined throughout the rendering
     * pipeline to calculate per-pixel lighting and other effects.
     * <p>
     * Transparencies are handled through alpha rejection (i.e. ground plants) and alpha-based blending. An exception to
     * this is water, which is handled separately to allow for reflections and refractions, if enabled.
     * <p>
     * By the time it is fully instantiated this implementation is already connected to all the support objects it
     * requires and is ready to render via the render(RenderingStage) method.
     *
     * @param context a context object, to obtain instances of classes such as the rendering config.
     */
    public WorldRendererImpl(Context context) {
        this.context = context;
        renderGraph = new RenderGraph(context);

        this.worldProvider = context.get(WorldProvider.class);
        this.backdropProvider = context.get(BackdropProvider.class);
        this.renderingConfig = context.get(Config.class).getRendering();
        this.shaderManager = context.get(ShaderManager.class);
        playerCamera = new PerspectiveCamera(renderingConfig, context.get(DisplayDevice.class));
        currentRenderingStage = RenderingStage.MONO;
        // TODO: won't need localPlayerSystem here once camera is in the ES proper
        LocalPlayerSystem localPlayerSystem = context.get(LocalPlayerSystem.class);
        localPlayerSystem.setPlayerCamera(playerCamera);

        context.put(ChunkTessellator.class, new ChunkTessellator());

        ChunkProvider chunkProvider = context.get(ChunkProvider.class);
        ChunkTessellator chunkTessellator = context.get(ChunkTessellator.class);
        BlockManager blockManager = context.get(BlockManager.class);
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);
        Config config = context.get(Config.class);


        WorldGenerator worldGenerator = context.get(WorldGenerator.class);
        LodChunkProvider lodChunkProvider = null;
        if (worldGenerator instanceof ScalableWorldGenerator) {
            lodChunkProvider = new LodChunkProvider(chunkProvider, blockManager, extraDataManager,
                    (ScalableWorldGenerator) worldGenerator, chunkTessellator);
        }
        this.renderableWorld = new RenderableWorldImpl(this, lodChunkProvider, chunkProvider, chunkTessellator, worldProvider, config, playerCamera);
        renderQueues = renderableWorld.getRenderQueues();

        initRenderingSupport();

        initRenderGraph();

        initRenderingModules();

        console = context.get(Console.class);
        MethodCommand.registerAvailable(this, console, context);
    }

    private void initRenderingSupport() {
        ScreenGrabber screenGrabber = new ScreenGrabber(context);
        context.put(ScreenGrabber.class, screenGrabber);

        displayResolutionDependentFbo = new DisplayResolutionDependentFbo(
                context.get(Config.class).getRendering(), screenGrabber, context.get(DisplayDevice.class));
        context.put(DisplayResolutionDependentFbo.class, displayResolutionDependentFbo);

        shaderManager.initShaders();

        context.put(WorldRenderer.class, this);
        context.put(RenderQueuesHelper.class, renderQueues);
        context.put(RenderableWorld.class, renderableWorld);
    }

    private void initRenderGraph() {
        context.put(RenderGraph.class, renderGraph);

        renderTaskListGenerator = new RenderTaskListGenerator();
        context.put(RenderTaskListGenerator.class, renderTaskListGenerator);
    }

    private void initRenderingModules() {
        renderingModuleRegistry = context.get(RenderingModuleRegistry.class);

        // registry not populated by new ModuleRendering instances in UI, populate now
        if (renderingModuleRegistry.getOrderedRenderingModules().isEmpty()) {
            List<ModuleRendering> renderingModules = renderingModuleRegistry.updateRenderingModulesOrder(
                    context.get(ModuleManager.class).getEnvironment(), context);
            if (renderingModules.isEmpty()) {
                GameEngine gameEngine = context.get(GameEngine.class);
                gameEngine.changeState(new StateMainMenu("No rendering module loaded, unable to render. Try enabling " +
                        "CoreRendering."));
            }
        } else { // registry populated by new ModuleRendering instances in UI
            // Switch module's context from gamecreation subcontext to gamerunning context
            renderingModuleRegistry.updateModulesContext(context);
        }
        /*
        TODO: work out where to put this.

        renderGraph.connect(opaqueObjectsNode, overlaysNode);
        renderGraph.connect(opaqueBlocksNode, overlaysNode);
        renderGraph.connect(alphaRejectBlocksNode, overlaysNode);
        */

        for (ModuleRendering moduleRenderingInstance : renderingModuleRegistry.getOrderedRenderingModules()) {
            if (moduleRenderingInstance.isEnabled()) {
                logger.atInfo().log("\nInitialising rendering class {} from {} module.\n",
                        moduleRenderingInstance.getClass().getSimpleName(),
                        moduleRenderingInstance.getProvidingModule());
                moduleRenderingInstance.initialise();
            }
        }

        requestTaskListRefresh();
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
    public void onChunkLoaded(Vector3ic pos) {
        renderableWorld.onChunkLoaded(pos);
    }

    @Override
    public void onChunkUnloaded(Vector3ic pos) {
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
            timeSmoothedMainLightIntensity = TeraMath.lerp(timeSmoothedMainLightIntensity,
                    getMainLightIntensityAt(playerCamera.getPosition()), secondsSinceLastFrame);

            playerCamera.update(secondsSinceLastFrame);

            renderableWorld.update();
            secondsSinceLastFrame = 0;

            displayResolutionDependentFbo.update();

            millisecondsSinceRenderingStart += secondsSinceLastFrame * 1000;  // updates the variable animations are
            // based on.
        }

        if (currentRenderingStage != RenderingStage.MONO) {
            playerCamera.updateFrustum();
        }

        // this line needs to be here as deep down it relies on the camera's frustrum, updated just above.
        renderableWorld.queueVisibleChunks(isFirstRenderingStageForCurrentFrame);

        if (requestedTaskListRefresh) {
            List<Node> orderedNodes = renderGraph.getNodesInTopologicalOrder();
            renderPipelineTaskList = renderTaskListGenerator.generateFrom(orderedNodes);
            requestedTaskListRefresh = false;
        }
    }

    /**
     * TODO: update javadocs This method triggers the execution of the rendering pipeline and, eventually, sends the
     * output to the display or to a file, when grabbing a screenshot.
     * <p>
     * In this particular implementation this method can be called once per frame, when rendering to a standard display,
     * or twice, each time with a different rendering stage, when rendering to the head mounted display.
     * <p>
     * PerformanceMonitor.startActivity/endActivity statements are used in this method and in those it executes, to
     * provide statistics regarding the ongoing rendering and its individual steps (i.e. rendering shadows, reflections,
     * 2D filters...).
     *
     * @param renderingStage "MONO" for standard rendering and "LEFT_EYE" or "RIGHT_EYE" for stereoscopic
     *         displays.
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
        FBO lastUpdatedGBuffer = displayResolutionDependentFbo.getGBufferPair().getLastUpdatedFbo();
        glViewport(0, 0, lastUpdatedGBuffer.width(), lastUpdatedGBuffer.height());

        renderPipelineTaskList.forEach(RenderPipelineTask::process);

        // this line re-establish Terasology defaults, so that the rest of the application can rely on them.
        LwjglGraphicsUtil.initOpenGLParams();

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
        renderGraph.dispose();
        // TODO: Shift this to a better place, after a RenderGraph class has been implemented.
        SetViewportToSizeOf.disposeDefaultInstance();
    }

    @Override
    public void setViewDistance(ViewDistance viewDistance, int chunkLods) {
        renderableWorld.updateChunksInProximity(viewDistance, chunkLods);
    }

    @Override
    public float getTimeSmoothedMainLightIntensity() {
        return timeSmoothedMainLightIntensity;
    }

    @Override
    public float getRenderingLightIntensityAt(Vector3f pos) {
        float rawLightValueSun = worldProvider.getSunlight(pos) / 15.0f;
        float rawLightValueBlock = worldProvider.getLight(pos) / 15.0f;

        float lightValueSun =
                (float) Math.pow(BLOCK_LIGHT_SUN_POW, (1.0f - rawLightValueSun) * 16.0) * rawLightValueSun;
        lightValueSun *= backdropProvider.getDaylight();
        // TODO: Hardcoded factor and value to compensate for daylight tint and night brightness
        lightValueSun *= 0.9f;
        lightValueSun += 0.05f;

        float lightValueBlock =
                (float) Math.pow(BLOCK_LIGHT_POW, (1.0f - (double) rawLightValueBlock) * 16.0f) * rawLightValueBlock * BLOCK_INTENSITY_FACTOR;

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
    public RenderingStage getCurrentRenderStage() {
        return currentRenderingStage;
    }

    @Override
    public RenderGraph getRenderGraph() {
        return renderGraph;
    }

    /**
     * Forces a recompilation of all shaders. This command, backed by Gestalt's monitoring feature, allows developers to
     * hot-swap shaders for easy development.
     * <p>
     * To run the command simply type "recompileShaders" and then press Enter in the console.
     */
    @Command(shortDescription = "Forces a recompilation of shaders.", requiredPermission =
            PermissionManager.NO_PERMISSION)
    public void recompileShaders() {
        console.addMessage("Recompiling shaders... ", false);
        shaderManager.recompileAllShaders();
        console.addMessage("done!");
    }

    /**
     * Acts as an interface between the console and the Nodes. All parameters passed to command are redirected to the
     * concerned Nodes, which in turn take care of executing them.
     * <p>
     * Usage: {@code dagNodeCommand <nodeUri> <command> <parameters>}
     * <p>
     * Example: dagNodeCommand engine:outputToScreenNode setFbo engine:fbo.ssao
     */
    @Command(shortDescription = "Debugging command for DAG.", requiredPermission = PermissionManager.NO_PERMISSION)
    public void dagNodeCommand(@CommandParam("nodeUri") final String nodeUri,
                               @CommandParam("command") final String command,
                               @CommandParam(value = "arguments") final String... arguments) {
        Node node = renderGraph.findNode(nodeUri);
        if (node == null) {
            node = renderGraph.findAka(nodeUri);
            if (node == null) {
                throw new RuntimeException(("No node is associated with URI '" + nodeUri + "'"));
            }
        }
        node.handleCommand(command, arguments);
    }

    /**
     * Redirect output FBO from one node to another's input
     * <p>
     * Usage: {@code dagRedirect <connectionTypeString> <fromNodeUri> <outputFboId> <toNodeUri> <inputFboId>}
     * <p>
     * Example: dagRedirect fbo blurredAmbientOcclusion 1 BasicRendering:outputToScreenNode 1 dagRedirect bufferpair
     * backdrop 1 AdvancedRendering:intermediateHazeNode 1
     */
    @Command(shortDescription = "Debugging command for DAG.", requiredPermission = PermissionManager.NO_PERMISSION)
    public void dagRedirect(@CommandParam("fromNodeUri") final String connectionTypeString, @CommandParam(
            "fromNodeUri") final String fromNodeUri, @CommandParam("outputFboId") final int outputFboId,
                            @CommandParam("toNodeUri") final String toNodeUri,
                            @CommandParam(value = "inputFboId") final int inputFboId) {
        RenderGraph.ConnectionType connectionType;
        if (connectionTypeString.equalsIgnoreCase("fbo")) {
            connectionType = RenderGraph.ConnectionType.FBO;
        } else if (connectionTypeString.equalsIgnoreCase("bufferpair")) {
            connectionType = RenderGraph.ConnectionType.BUFFER_PAIR;
        } else {
            throw new RuntimeException(("Unsupported connection type: '" + connectionTypeString + "'. Expected 'fbo' " +
                    "or 'bufferpair'.\n"));
        }

        Node toNode = renderGraph.findNode(toNodeUri);
        if (toNode == null) {
            toNode = renderGraph.findAka(toNodeUri);
            if (toNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + toNodeUri + "'"));
            }
        }

        Node fromNode = renderGraph.findNode(fromNodeUri);
        if (fromNode == null) {
            fromNode = renderGraph.findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }
        renderGraph.reconnectInputToOutput(fromNode, outputFboId, toNode, inputFboId, connectionType, true);
        toNode.clearDesiredStateChanges();
        requestTaskListRefresh();

    }
}

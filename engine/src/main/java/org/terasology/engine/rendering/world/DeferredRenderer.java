// Copyright 2023 The Terasology Foundation
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
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.MethodCommand;
import org.terasology.engine.logic.players.LocalPlayerSystem;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.cameras.PerspectiveCamera;
import org.terasology.engine.rendering.dag.ModuleRendering;
import org.terasology.engine.rendering.dag.RenderGraph;
import org.terasology.engine.rendering.opengl.ScreenGrabber;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.rust.EngineKernel;
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

public class DeferredRenderer implements WorldRenderer {
    /*
     * Presumably, the eye height should be context.get(Config.class).getPlayer().getEyeHeight() above the ground plane.
     * It's not, so for now, we use this factor to adjust for the disparity.
     */
    private static final Logger logger = LoggerFactory.getLogger(DeferredRenderer.class);
    private static final float GROUND_PLANE_HEIGHT_DISPARITY = -0.7f;
    private RenderingModuleRegistry renderingModuleRegistry;

    private final RenderQueuesHelper renderQueues;
    private final Context context;
    private final BackdropProvider backdropProvider;
    private final WorldProvider worldProvider;
    private final RenderableWorld renderableWorld;
    private final ShaderManager shaderManager;
    private final EngineKernel kernel;
    private final Camera playerCamera;

    private float timeSmoothedMainLightIntensity;

    private float millisecondsSinceRenderingStart;
    private float secondsSinceLastFrame;
    private int statChunkMeshEmpty;
    private int statChunkNotReady;
    private int statRenderedTriangles;

    private final RenderingConfig renderingConfig;
    private final Console console;


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
    public DeferredRenderer(Context context) {
        this.context = context;
        this.worldProvider = context.get(WorldProvider.class);
        this.backdropProvider = context.get(BackdropProvider.class);
        this.renderingConfig = context.get(Config.class).getRendering();
        this.shaderManager = context.get(ShaderManager.class);
        this.kernel = context.get(EngineKernel.class);
        playerCamera = new PerspectiveCamera(renderingConfig, context.get(DisplayDevice.class));
//        currentRenderingStage = RenderingStage.MONO;
        // TODO: won't need localPlayerSystem here once camera is in the ES proper
        LocalPlayerSystem localPlayerSystem = context.get(LocalPlayerSystem.class);
        localPlayerSystem.setPlayerCamera(playerCamera);

        context.put(ChunkTessellator.class, new ChunkTessellator(this.kernel));

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

        initRenderingModules();

        console = context.get(Console.class);
        MethodCommand.registerAvailable(this, console, context);
    }

    private void initRenderingSupport() {
        ScreenGrabber screenGrabber = new ScreenGrabber(context);
        context.put(ScreenGrabber.class, screenGrabber);

//        displayResolutionDependentFbo = new DisplayResolutionDependentFbo(
//                context.get(Config.class).getRendering(), screenGrabber, context.get(DisplayDevice.class));
//        context.put(DisplayResolutionDependentFbo.class, displayResolutionDependentFbo);

        shaderManager.initShaders();

        context.put(WorldRenderer.class, this);
        context.put(RenderQueuesHelper.class, renderQueues);
        context.put(RenderableWorld.class, renderableWorld);
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

        for (ModuleRendering moduleRenderingInstance : renderingModuleRegistry.getOrderedRenderingModules()) {
            if (moduleRenderingInstance.isEnabled()) {
                logger.info(String.format("\nInitialising rendering class %s from %s module.\n",
                        moduleRenderingInstance.getClass().getSimpleName(),
                        moduleRenderingInstance.getProvidingModule()));
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

    @Override
    public void increaseTrianglesCount(int increase) {
        statRenderedTriangles += increase;
    }

    @Override
    public void increaseNotReadyChunkCount(int increase) {
        statChunkNotReady += increase;
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
        statChunkMeshEmpty = 0;
        statChunkNotReady = 0;
        statRenderedTriangles = 0;

        timeSmoothedMainLightIntensity = TeraMath.lerp(timeSmoothedMainLightIntensity,
                getMainLightIntensityAt(playerCamera.getPosition()), secondsSinceLastFrame);

        playerCamera.update(secondsSinceLastFrame);

        renderableWorld.update();
        secondsSinceLastFrame = 0;
        millisecondsSinceRenderingStart += secondsSinceLastFrame * 1000;  // updates the variable animations are

        playerCamera.updateFrustum();

        // this line needs to be here as deep down it relies on the camera's frustrum, updated just above.
        renderableWorld.queueVisibleChunks(true);

        //TODO: implement rendering logic

        playerCamera.updatePrevViewProjectionMatrix();
    }

    @Override
    public void requestTaskListRefresh() {
    }

    @Override
    public boolean isFirstRenderingStageForCurrentFrame() {
        return true;
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
        return RenderingStage.LEFT_EYE;
    }

    @Override
    public RenderGraph getRenderGraph() {
        return null;
    }

}

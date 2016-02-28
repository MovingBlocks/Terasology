/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.Activity;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.RenderHelper;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OculusStereoCamera;
import org.terasology.rendering.cameras.OrthographicCamera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.GraphicState;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.opengl.PostProcessor;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.LightGeometryHelper;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.RenderableChunk;

/**
 */
public final class WorldRendererImpl implements WorldRenderer {

    private static final int SHADOW_FRUSTUM_BOUNDS = 500;

    private final Context context;

    private final BackdropRenderer backdropRenderer;
    private final BackdropProvider backdropProvider;
    private final WorldProvider worldProvider;
    private final RenderableWorld renderableWorld;

    private LocalPlayer player;

    private final Camera playerCamera;
    private final Camera shadowMapCamera = new OrthographicCamera(-SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, -SHADOW_FRUSTUM_BOUNDS);

    // TODO: Review this? (What are we doing with a component not attached to an entity?)
    private LightComponent mainDirectionalLight = new LightComponent();
    private float smoothedPlayerSunlightValue;

    private final RenderQueuesHelper renderQueues;
    private WorldRenderingStage currentRenderingStage;
    private boolean isFirstRenderingStageForCurrentFrame;

    private Material chunkShader;
    private Material lightGeometryShader;
    // private Material simpleShader; // in use by the currently commented out light stencil pass
    private Material shadowMapShader;

    private float tick;
    private float secondsSinceLastFrame;

    private int statChunkMeshEmpty;
    private int statChunkNotReady;
    private int statRenderedTriangles;

    public enum ChunkRenderMode {
        DEFAULT,
        REFLECTION,
        SHADOW_MAP,
        Z_PRE_PASS
    }

    private ComponentSystemManager systemManager;

    private final Config config;
    private final RenderingConfig renderingConfig;
    private final RenderingDebugConfig renderingDebugConfig;

    private FrameBuffersManager buffersManager;
    private GraphicState graphicState;
    private PostProcessor postProcessor;

    public WorldRendererImpl(Context context, GLBufferPool bufferPool) {
        this.context = context;
        this.worldProvider = context.get(WorldProvider.class);
        this.backdropProvider = context.get(BackdropProvider.class);
        this.backdropRenderer = context.get(BackdropRenderer.class);
        this.config = context.get(Config.class);
        this.renderingConfig = config.getRendering();
        this.renderingDebugConfig = renderingConfig.getDebug();
        this.systemManager = context.get(ComponentSystemManager.class);

        // TODO: won't need localPlayerSystem here once camera is in the ES proper
        if (renderingConfig.isOculusVrSupport()) {
            playerCamera = new OculusStereoCamera();
            currentRenderingStage = WorldRenderingStage.LEFT_EYE;

        } else {
            playerCamera = new PerspectiveCamera(renderingConfig.getCameraSettings());
            currentRenderingStage = WorldRenderingStage.MONO;
        }
        LocalPlayerSystem localPlayerSystem = context.get(LocalPlayerSystem.class);
        localPlayerSystem.setPlayerCamera(playerCamera);

        initMainDirectionalLight();

        ChunkProvider chunkProvider = context.get(ChunkProvider.class);

        renderableWorld = new RenderableWorldImpl(worldProvider, chunkProvider, bufferPool, playerCamera, shadowMapCamera);
        renderQueues = renderableWorld.getRenderQueues();

        initRenderingSupport();
    }

    private void initMainDirectionalLight() {
        mainDirectionalLight.lightType = LightComponent.LightType.DIRECTIONAL;
        mainDirectionalLight.lightColorAmbient = new Vector3f(1.0f, 1.0f, 1.0f);
        mainDirectionalLight.lightColorDiffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        mainDirectionalLight.lightAmbientIntensity = 1.0f;
        mainDirectionalLight.lightDiffuseIntensity = 2.0f;
        mainDirectionalLight.lightSpecularIntensity = 0.0f;
    }

    private void initRenderingSupport() {
        buffersManager = new FrameBuffersManager();
        context.put(FrameBuffersManager.class, buffersManager);

        graphicState = new GraphicState(buffersManager);
        postProcessor = new PostProcessor(buffersManager, graphicState);
        context.put(PostProcessor.class, postProcessor);

        buffersManager.setGraphicState(graphicState);
        buffersManager.setPostProcessor(postProcessor);
        buffersManager.initialize();

        context.get(ShaderManager.class).initShaders();
        postProcessor.initializeMaterials();
        initMaterials();
    }

    private void initMaterials() {
        chunkShader = getMaterial("engine:prog.chunk");
        lightGeometryShader = getMaterial("engine:prog.lightGeometryPass");
        //simpleShader = getMaterial("engine:prog.simple");  // in use by the currently commented out light stencil pass
        shadowMapShader = getMaterial("engine:prog.shadowMap");
    }

    private Material getMaterial(String assetId) {
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

    /**
     * @return true if pregeneration is complete
     */
    @Override
    public boolean pregenerateChunks() {
        return renderableWorld.pregenerateChunks();
    }

    @Override
    public void update(float deltaInSeconds) {
        secondsSinceLastFrame += deltaInSeconds;
    }

    public void positionShadowMapCamera() {
        // Shadows are rendered around the player so...
        Vector3f lightPosition = new Vector3f(playerCamera.getPosition().x, 0.0f, playerCamera.getPosition().z);

        // Project the shadowMapCamera position to light space and make sure it is only moved in texel steps (avoids flickering when moving the shadowMapCamera)
        float texelSize = 1.0f / renderingConfig.getShadowMapResolution();
        texelSize *= 2.0f;

        shadowMapCamera.getViewProjectionMatrix().transformPoint(lightPosition);
        lightPosition.set(TeraMath.fastFloor(lightPosition.x / texelSize) * texelSize, 0.0f, TeraMath.fastFloor(lightPosition.z / texelSize) * texelSize);
        shadowMapCamera.getInverseViewProjectionMatrix().transformPoint(lightPosition);

        // ... we position our new shadowMapCamera at the position of the player and move it
        // quite a bit into the direction of the sun (our main light).

        // Make sure the sun does not move too often since it causes massive shadow flickering (from hell to the max)!
        float stepSize = 50f;
        Vector3f sunDirection = backdropProvider.getQuantizedSunDirection(stepSize);

        Vector3f sunPosition = new Vector3f(sunDirection);
        sunPosition.scale(256.0f + 64.0f);
        lightPosition.add(sunPosition);

        shadowMapCamera.getPosition().set(lightPosition);

        // and adjust it to look from the sun direction into the direction of our player
        Vector3f negSunDirection = new Vector3f(sunDirection);
        negSunDirection.scale(-1.0f);

        shadowMapCamera.getViewingDirection().set(negSunDirection);
    }

    private void resetStats() {
        statChunkMeshEmpty = 0;
        statChunkNotReady = 0;
        statRenderedTriangles = 0;
    }

    private void preRenderUpdate(WorldRenderingStage renderingStage) {
        resetStats();

        currentRenderingStage = renderingStage;
        if (currentRenderingStage == WorldRenderingStage.MONO || currentRenderingStage == WorldRenderingStage.LEFT_EYE) {
            isFirstRenderingStageForCurrentFrame = true;
        } else {
            isFirstRenderingStageForCurrentFrame = false;
        }

        // this is done to execute this code block only once per frame
        // instead of once per eye in a stereo setup.
        if (isFirstRenderingStageForCurrentFrame) {
            tick += secondsSinceLastFrame * 1000;  // Updates the tick variable that animation is based on.
            smoothedPlayerSunlightValue = TeraMath.lerp(smoothedPlayerSunlightValue, getSunlightValue(), secondsSinceLastFrame);

            playerCamera.update(secondsSinceLastFrame);
            positionShadowMapCamera();
            shadowMapCamera.update(secondsSinceLastFrame);

            renderableWorld.update();
            renderableWorld.generateVBOs();
            secondsSinceLastFrame = 0;

            buffersManager.preRenderUpdate();
        }

        if (currentRenderingStage != WorldRenderingStage.MONO) {
            playerCamera.updateFrustum();
        }

        // this line needs to be here as deep down it relies on the camera's frustrum, updated just above.
        renderableWorld.queueVisibleChunks(isFirstRenderingStageForCurrentFrame);
    }

    /**
     * Renders the world.
     */
    @Override
    public void render(WorldRenderingStage renderingStage) {
        preRenderUpdate(renderingStage);

        renderShadowMap();          // into shadowMap buffer
        renderWorldReflection();    // into sceneReflect buffer

        graphicState.enableWireframeIf(renderingDebugConfig.isWireframe());
        graphicState.initialClearing();

        graphicState.preRenderSetupSceneOpaque();
        renderBackdrop();   // into sceneOpaque and skyBands[0-1] buffers

        try (Activity ignored = PerformanceMonitor.startActivity("Render World")) {
            renderObjectsOpaque();      //
            renderChunksOpaque();       //
            renderChunksAlphaReject();  //  all into sceneOpaque buffer
            renderOverlays();           //
            renderFirstPersonView();    //

            graphicState.postRenderCleanupSceneOpaque();

            renderLightGeometry();              // into sceneOpaque buffer
            renderChunksRefractiveReflective(); // into sceneReflectiveRefractive buffer
        }

        graphicState.disableWireframeIf(renderingDebugConfig.isWireframe());

        PerformanceMonitor.startActivity("Pre-post composite");

        postProcessor.generateOutline();                      // into outline buffer
        postProcessor.generateAmbientOcclusionPasses();     // into ssao and ssaoBlurred buffers
        postProcessor.generatePrePostComposite();              // into sceneOpaquePingPong, then make it the new sceneOpaque buffer
        PerformanceMonitor.endActivity();

        renderSimpleBlendMaterials();                       // into sceneOpaque buffer

        PerformanceMonitor.startActivity("Post-Processing");
        postProcessor.generateLightShafts();                // into lightShafts buffer

        // Initial Post-Processing: chromatic aberration, light shafts, 1/8th resolution bloom, vignette
        postProcessor.initialPostProcessing();              // into scenePrePost buffer

        // Post-Processing proper: tone mapping, bloom and blur passes // TODO: verify if the order of operations around here is correct
        postProcessor.downsampleSceneAndUpdateExposure();   // downSampledScene buffer used only to update exposure value
        postProcessor.generateToneMappedScene();            // into sceneToneMapped buffer
        postProcessor.generateBloomPasses();                // into sceneHighPass and sceneBloom[0-2]
        postProcessor.generateBlurPasses();                 // into sceneBlur[0-1]

        // Final Post-Processing: depth-of-field blur, motion blur, film grain, grading, OculusVR distortion
        postProcessor.finalPostProcessing(renderingStage);  // to screen normally, to a buffer if a screenshot is being taken
        PerformanceMonitor.endActivity();

        playerCamera.updatePrevViewProjectionMatrix();
    }

    private void renderShadowMap() {
        if (renderingConfig.isDynamicShadows() && isFirstRenderingStageForCurrentFrame) {
            PerformanceMonitor.startActivity("Render World (Shadow Map)");

            graphicState.preRenderSetupSceneShadowMap();
            shadowMapCamera.lookThrough();

            while (renderQueues.chunksOpaqueShadow.size() > 0) {
                renderChunk(renderQueues.chunksOpaqueShadow.poll(), ChunkMesh.RenderPhase.OPAQUE, shadowMapCamera, ChunkRenderMode.SHADOW_MAP);
            }

            for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
                renderer.renderShadows();
            }

            playerCamera.lookThrough(); // not strictly needed: just defensive programming here.
            graphicState.postRenderCleanupSceneShadowMap();

            PerformanceMonitor.endActivity();
        }
    }

    public void renderWorldReflection() {
        PerformanceMonitor.startActivity("Render World (Reflection)");

        graphicState.preRenderSetupReflectedScene();
        playerCamera.setReflected(true);

        playerCamera.lookThroughNormalized(); // we don't want the reflected scene to be bobbing or moving with the player
        backdropRenderer.render(playerCamera);
        playerCamera.lookThrough();

        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

        if (renderingConfig.isReflectiveWater()) {
            while (renderQueues.chunksOpaqueReflection.size() > 0) {
                renderChunk(renderQueues.chunksOpaqueReflection.poll(), ChunkMesh.RenderPhase.OPAQUE, playerCamera, ChunkRenderMode.REFLECTION);
            }
        }

        chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

        playerCamera.setReflected(false);
        graphicState.postRenderCleanupReflectedScene();

        PerformanceMonitor.endActivity();
    }

    private void renderBackdrop() {
        PerformanceMonitor.startActivity("Render Sky");
        playerCamera.lookThroughNormalized();
        graphicState.preRenderSetupBackdrop();

        backdropRenderer.render(playerCamera);
        graphicState.midRenderChangesBackdrop();
        postProcessor.generateSkyBands();

        graphicState.postRenderCleanupBackdrop();

        playerCamera.lookThrough();
        PerformanceMonitor.endActivity();
    }

    private void renderObjectsOpaque() {
        PerformanceMonitor.startActivity("Render Objects (Opaque)");
        for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
            renderer.renderOpaque();
        }
        PerformanceMonitor.endActivity();
    }

    private void renderChunksOpaque() {
        PerformanceMonitor.startActivity("Render Chunks (Opaque)");
        while (renderQueues.chunksOpaque.size() > 0) {
            renderChunk(renderQueues.chunksOpaque.poll(), ChunkMesh.RenderPhase.OPAQUE, playerCamera, ChunkRenderMode.DEFAULT);
        }
        PerformanceMonitor.endActivity();
    }

    private void renderChunksAlphaReject() {
        PerformanceMonitor.startActivity("Render Chunks (Alpha Reject)");
        while (renderQueues.chunksAlphaReject.size() > 0) {
            renderChunk(renderQueues.chunksAlphaReject.poll(), ChunkMesh.RenderPhase.ALPHA_REJECT, playerCamera, ChunkRenderMode.DEFAULT);
        }
        PerformanceMonitor.endActivity();
    }

    private void renderOverlays() {
        PerformanceMonitor.startActivity("Render Overlays");
        for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
            renderer.renderOverlay();
        }
        PerformanceMonitor.endActivity();
    }

    private void renderFirstPersonView() {
        if (!renderingDebugConfig.isFirstPersonElementsHidden()) {
            PerformanceMonitor.startActivity("Render First Person");
            graphicState.preRenderSetupFirstPerson();

            playerCamera.updateMatrices(90f);
            playerCamera.loadProjectionMatrix();

            for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
                renderer.renderFirstPerson();
            }

            playerCamera.updateMatrices();
            playerCamera.loadProjectionMatrix();

            graphicState.postRenderClenaupFirstPerson();
            PerformanceMonitor.endActivity();
        }
    }

    private void renderLightGeometry() {
        PerformanceMonitor.startActivity("Render Light Geometry");
        // DISABLED UNTIL WE CAN FIND WHY IT's BROKEN. SEE ISSUE #1486
        /*
        graphicState.preRenderSetupLightGeometryStencil();

        simple.enable();
        simple.setCamera(playerCamera);
        EntityManager entityManager = context.get(EntityManager.class);
        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, simple, true);
        }

        graphicState.postRenderCleanupLightGeometryStencil();
        */

        graphicState.preRenderSetupLightGeometry();
        EntityManager entityManager = context.get(EntityManager.class);
        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, lightGeometryShader, false);
        }
        graphicState.postRenderCleanupLightGeometry();

        // Sunlight
        graphicState.preRenderSetupDirectionalLights();

        Vector3f sunlightWorldPosition = new Vector3f(backdropProvider.getSunDirection(true));
        sunlightWorldPosition.scale(50000f);
        sunlightWorldPosition.add(playerCamera.getPosition());
        renderLightComponent(mainDirectionalLight, sunlightWorldPosition, lightGeometryShader, false);

        graphicState.postRenderCleanupDirectionalLights();

        postProcessor.applyLightBufferPass();
        PerformanceMonitor.endActivity();
    }

    private boolean renderLightComponent(LightComponent lightComponent, Vector3f lightWorldPosition, Material program, boolean geometryOnly) {
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

            program.setFloat4("lightProperties", lightComponent.lightAmbientIntensity, lightComponent.lightDiffuseIntensity,
                    lightComponent.lightSpecularIntensity, lightComponent.lightSpecularPower, true);
        }

        if (lightComponent.lightType == LightComponent.LightType.POINT) {
            if (!geometryOnly) {
                program.setFloat4("lightExtendedProperties", lightComponent.lightAttenuationRange * 0.975f, lightComponent.lightAttenuationFalloff, 0.0f, 0.0f, true);
            }

            LightGeometryHelper.renderSphereGeometry();
        } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
            // Directional lights cover all pixels on the screen
            postProcessor.renderFullscreenQuad();
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

    private void renderChunksRefractiveReflective() {
        PerformanceMonitor.startActivity("Render Chunks (Refractive/Reflective)");

        boolean isHeadUnderWater = isHeadUnderWater();
        graphicState.preRenderSetupSceneReflectiveRefractive(isHeadUnderWater);

        while (renderQueues.chunksAlphaBlend.size() > 0) {
            renderChunk(renderQueues.chunksAlphaBlend.poll(), ChunkMesh.RenderPhase.REFRACTIVE, playerCamera, ChunkRenderMode.DEFAULT);
        }

        graphicState.postRenderCleanupSceneReflectiveRefractive(isHeadUnderWater);
        PerformanceMonitor.endActivity();
    }

    private void renderSimpleBlendMaterials() {
        PerformanceMonitor.startActivity("Render Objects (Transparent)");
        graphicState.preRenderSetupSimpleBlendMaterials();

        for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
            renderer.renderAlphaBlend();
        }

        graphicState.postRenderCleanupSimpleBlendMaterials();
        PerformanceMonitor.endActivity();
    }

    private void renderChunk(RenderableChunk chunk, ChunkMesh.RenderPhase phase, Camera camera, ChunkRenderMode mode) {
        if (chunk.hasMesh()) {
            final Vector3f cameraPosition = camera.getPosition();
            final Vector3f chunkPosition = chunk.getPosition().toVector3f();
            final Vector3f chunkPositionRelativeToCamera =
                    new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                            chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                            chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);

            if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
                if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                    chunkShader.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                    chunkShader.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
                }

                chunkShader.setFloat3("chunkPositionWorld", chunkPosition.x * ChunkConstants.SIZE_X,
                        chunkPosition.y * ChunkConstants.SIZE_Y,
                        chunkPosition.z * ChunkConstants.SIZE_Z);
                chunkShader.setFloat("animated", chunk.isAnimated() ? 1.0f : 0.0f);

                if (mode == ChunkRenderMode.REFLECTION) {
                    chunkShader.setFloat("clip", camera.getClipHeight());
                } else {
                    chunkShader.setFloat("clip", 0.0f);
                }

                chunkShader.enable();

            } else if (mode == ChunkRenderMode.SHADOW_MAP) {
                shadowMapShader.enable();

            } else if (mode == ChunkRenderMode.Z_PRE_PASS) {
                CoreRegistry.get(ShaderManager.class).disableShader();
            }

            graphicState.preRenderSetupChunk(chunkPositionRelativeToCamera);

            if (chunk.hasMesh()) {
                if (renderingDebugConfig.isRenderChunkBoundingBoxes()) {
                    AABBRenderer aabbRenderer = new AABBRenderer(chunk.getAABB());
                    aabbRenderer.renderLocally(1f);
                    statRenderedTriangles += 12;
                }

                chunk.getMesh().render(phase);
                statRenderedTriangles += chunk.getMesh().triangleCount();
            }

            graphicState.postRenderCleanupChunk();

            // TODO: review - moving the deactivateFeature commands to the analog codeblock above doesn't work. Why?
            if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
                if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                    chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                    chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
                }
            }
        } else {
            statChunkNotReady++;
        }
    }

    /**
     * Disposes this world.
     */
    @Override
    public void dispose() {
        renderableWorld.dispose();
        worldProvider.dispose();
        graphicState.dispose();
        postProcessor.dispose();
    }

    /**
     * Sets a new player and spawns him at the spawning point.
     *
     * @param p The player
     */
    @Override
    public void setPlayer(LocalPlayer p) {
        player = p;
        renderableWorld.updateChunksInProximity(renderingConfig.getViewDistance());
    }

    @Override
    public void changeViewDistance(ViewDistance viewingDistance) {
        renderableWorld.updateChunksInProximity(viewingDistance);
    }

    public boolean isLightVisible(Vector3f positionViewSpace, LightComponent component) {
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
    public float getSmoothedPlayerSunlightValue() {
        return smoothedPlayerSunlightValue;
    }

    @Override
    public float getSunlightValue() {
        return getSunlightValueAt(playerCamera.getPosition());
    }

    @Override
    public float getBlockLightValue() {
        return getBlockLightValueAt(playerCamera.getPosition());
    }

    @Override
    public float getRenderingLightValueAt(Vector3f pos) {
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
    public float getSunlightValueAt(Vector3f position) {
        return backdropProvider.getDaylight() * worldProvider.getSunlight(position) / 15.0f;
    }

    @Override
    public float getBlockLightValueAt(Vector3f position) {
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

    public LocalPlayer getPlayer() {
        return player;
    }

    @Override
    public WorldProvider getWorldProvider() {
        return worldProvider;
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return renderableWorld.getChunkProvider();
    }

    @Override
    public float getTick() {
        return tick;
    }

    @Override
    public Camera getActiveCamera() {
        return playerCamera;
    }

    @Override
    public Camera getLightCamera() {
        return shadowMapCamera;
    }

    @Override
    public WorldRenderingStage getCurrentRenderStage() {
        return currentRenderingStage;
    }

    @Override
    public Vector3f getTint() {
        return worldProvider.getBlock(playerCamera.getPosition()).getTint();
    }
}

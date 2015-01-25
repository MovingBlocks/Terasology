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

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;
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
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.LightGeometryHelper;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.RenderableChunk;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class WorldRendererLwjgl implements WorldRenderer {

    private static final int SHADOW_FRUSTUM_BOUNDS = 500;

    private final int verticalMeshSegments = CoreRegistry.get(Config.class).getSystem().getVerticalChunkMeshSegments();

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

    private final Time time = CoreRegistry.get(Time.class);
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

    private ComponentSystemManager systemManager = CoreRegistry.get(ComponentSystemManager.class);

    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();
    private RenderingDebugConfig renderingDebugConfig = renderingConfig.getDebug();

    // TODO: rendering process as constructor input and setRenderingProcess method
    // TODO: examine the potential to avoid allocation of variables such as Materials

    public WorldRendererLwjgl(BackdropProvider backdropProvider, BackdropRenderer backdropRenderer,
                              WorldProvider worldProvider, ChunkProvider chunkProvider, LocalPlayerSystem localPlayerSystem, GLBufferPool bufferPool) {
        this.worldProvider = worldProvider;
        this.backdropProvider = backdropProvider;
        this.backdropRenderer = backdropRenderer;

        // TODO: won't need localPlayerSystem here once camera is in the ES proper
        if (renderingConfig.isOculusVrSupport()) {
            playerCamera = new OculusStereoCamera();
            currentRenderingStage = WorldRenderingStage.OCULUS_LEFT_EYE;

        } else {
            playerCamera = new PerspectiveCamera(renderingConfig.getCameraSettings());
            currentRenderingStage = WorldRenderingStage.MONO;
        }
        localPlayerSystem.setPlayerCamera(playerCamera);

        initMainDirectionalLight();

        renderableWorld = new RenderableWorldImpl(worldProvider, chunkProvider, bufferPool, playerCamera, shadowMapCamera);
        renderQueues = renderableWorld.getRenderQueues();
    }

    private void initMainDirectionalLight() {
        mainDirectionalLight.lightType = LightComponent.LightType.DIRECTIONAL;
        mainDirectionalLight.lightColorAmbient = new Vector3f(1.0f, 1.0f, 1.0f);
        mainDirectionalLight.lightColorDiffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        mainDirectionalLight.lightAmbientIntensity = 1.0f;
        mainDirectionalLight.lightDiffuseIntensity = 2.0f;
        mainDirectionalLight.lightSpecularIntensity = 0.0f;
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

        currentRenderingStage = renderingStage;
        if (currentRenderingStage == WorldRenderingStage.MONO || currentRenderingStage == WorldRenderingStage.OCULUS_LEFT_EYE) {
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
        resetStats();
        preRenderUpdate(renderingStage);

        renderShadowMap();
        renderWorldReflection();

        preRenderSetup();

        DefaultRenderingProcess.getInstance().beginRenderSceneOpaque();
        renderSky();

        try (Activity ignored = PerformanceMonitor.startActivity("Render World")) {
            renderObjectsOpaque();
            renderChunksOpaque();
            renderChunksAlphaReject();
            renderOverlays();
            renderFirstPersonView();

            DefaultRenderingProcess.getInstance().endRenderSceneOpaque();

            renderLightGeometry();
            renderChunksRefractiveReflective();
        }

        postRenderCleanup();

        combineRefractiveReflectiveAndOpaquePasses();
        renderSimpleBlendMaterialsIntoCombinedPass();

        renderFinalPostProcessedScene();

        playerCamera.updatePrevViewProjectionMatrix();
    }

    private void renderShadowMap() {
        if (renderingConfig.isDynamicShadows() && isFirstRenderingStageForCurrentFrame) {
            PerformanceMonitor.startActivity("Render World (Shadow Map)");

            DefaultRenderingProcess.getInstance().beginRenderSceneShadowMap();
            GL11.glDisable(GL_CULL_FACE);

            shadowMapCamera.lookThrough();

            while (renderQueues.chunksOpaqueShadow.size() > 0) {
                renderChunk(renderQueues.chunksOpaqueShadow.poll(), ChunkMesh.RenderPhase.OPAQUE, shadowMapCamera, ChunkRenderMode.SHADOW_MAP);
            }

            for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
                renderer.renderShadows();
            }

            playerCamera.lookThrough(); // not strictly needed: just defensive programming here.

            GL11.glEnable(GL_CULL_FACE);
            DefaultRenderingProcess.getInstance().endRenderSceneShadowMap();

            PerformanceMonitor.endActivity();
        }
    }

    public void renderWorldReflection() {
        PerformanceMonitor.startActivity("Render World (Reflection)");

        DefaultRenderingProcess.getInstance().beginRenderReflectedScene();
        GL11.glCullFace(GL11.GL_FRONT);
        playerCamera.setReflected(true);

        playerCamera.lookThroughNormalized();
        backdropRenderer.render(playerCamera);
        playerCamera.lookThrough();

        Material chunkShader = Assets.getMaterial("engine:prog.chunk");
        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

        if (renderingConfig.isReflectiveWater()) {
            GL11.glEnable(GL_LIGHT0);

            while (renderQueues.chunksOpaqueReflection.size() > 0) {
                renderChunk(renderQueues.chunksOpaqueReflection.poll(), ChunkMesh.RenderPhase.OPAQUE, playerCamera, ChunkRenderMode.REFLECTION);
            }
        }

        chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

        playerCamera.setReflected(false);
        GL11.glCullFace(GL11.GL_BACK);
        DefaultRenderingProcess.getInstance().endRenderReflectedScene();

        PerformanceMonitor.endActivity();
    }

    private void renderSky() {
        PerformanceMonitor.startActivity("Render Sky");
        playerCamera.lookThroughNormalized();
        DefaultRenderingProcess.getInstance().beginRenderSceneSky();
        backdropRenderer.render(playerCamera);
        DefaultRenderingProcess.getInstance().endRenderSceneSky();
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

            GL11.glPushMatrix();
            GL11.glLoadIdentity();

            playerCamera.updateMatrices(90f);
            playerCamera.loadProjectionMatrix();

            GL11.glDepthFunc(GL11.GL_ALWAYS);

            for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
                renderer.renderFirstPerson();
            }

            GL11.glDepthFunc(GL_LEQUAL);

            playerCamera.updateMatrices();
            playerCamera.loadProjectionMatrix();

            GL11.glPopMatrix();

            PerformanceMonitor.endActivity();
        }
    }

    private void renderLightGeometry() {
        PerformanceMonitor.startActivity("Render Light Geometry");
        // DISABLED UNTIL WE CAN FIND WHY IT's BROKEN. SEE ISSUE #1486
        /*
        DefaultRenderingProcess.getInstance().beginRenderLightGeometryStencilPass();

        Material program = Assets.getMaterial("engine:prog.simple");
        program.enable();
        program.setCamera(playerCamera);
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, program, true);
        }

        DefaultRenderingProcess.getInstance().endRenderLightGeometryStencilPass();
        */

        DefaultRenderingProcess.getInstance().beginRenderLightGeometry();
        Material program = Assets.getMaterial("engine:prog.lightGeometryPass");
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, program, false);
        }
        DefaultRenderingProcess.getInstance().endRenderLightGeometry();

        // Sunlight
        DefaultRenderingProcess.getInstance().beginRenderDirectionalLights();

        Vector3f sunlightWorldPosition = new Vector3f(backdropProvider.getSunDirection(true));
        sunlightWorldPosition.scale(50000f);
        sunlightWorldPosition.add(playerCamera.getPosition());
        renderLightComponent(mainDirectionalLight, sunlightWorldPosition, program, false);

        DefaultRenderingProcess.getInstance().endRenderDirectionalLights();
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
            DefaultRenderingProcess.getInstance().renderFullscreenQuad();
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
        DefaultRenderingProcess.getInstance().beginRenderSceneReflectiveRefractive();
        // Make sure the water surface is rendered if the player is swimming
        boolean isHeadUnderWater = isHeadUnderWater();
        if (isHeadUnderWater) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
        while (renderQueues.chunksAlphaBlend.size() > 0) {
            renderChunk(renderQueues.chunksAlphaBlend.poll(), ChunkMesh.RenderPhase.REFRACTIVE, playerCamera, ChunkRenderMode.DEFAULT);
        }
        if (isHeadUnderWater) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        DefaultRenderingProcess.getInstance().endRenderSceneReflectiveRefractive();
        PerformanceMonitor.endActivity();
    }

    private void combineRefractiveReflectiveAndOpaquePasses() {
        PerformanceMonitor.startActivity("Render Combined Scene");
        DefaultRenderingProcess.getInstance().renderPreCombinedScene();
        PerformanceMonitor.endActivity();
    }

    private void renderSimpleBlendMaterialsIntoCombinedPass() {
        PerformanceMonitor.startActivity("Render Objects (Transparent)");
        DefaultRenderingProcess.getInstance().beginRenderSceneOpaque();

        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        for (RenderSystem renderer : systemManager.iterateRenderSubscribers()) {
            renderer.renderAlphaBlend();
        }

        GL11.glDisable(GL_BLEND);
        GL11.glDepthMask(true);

        DefaultRenderingProcess.getInstance().endRenderSceneOpaque();
        PerformanceMonitor.endActivity();
    }

    private void renderFinalPostProcessedScene() {
        PerformanceMonitor.startActivity("Render Post-Processing");
        DefaultRenderingProcess.getInstance().renderPost(currentRenderingStage);
        PerformanceMonitor.endActivity();
    }

    private void preRenderSetup() {
        if (renderingDebugConfig.isWireframe()) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        DefaultRenderingProcess.getInstance().clear();
    }

    private void postRenderCleanup() {
        if (renderingDebugConfig.isWireframe()) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    private void renderChunk(RenderableChunk chunk, ChunkMesh.RenderPhase phase, Camera camera, ChunkRenderMode mode) {
        if (chunk.hasMesh()) {
            Material shader = null;

            final Vector3f cameraPosition = camera.getPosition();
            final Vector3f chunkPositionRelToCamera =
                    new Vector3f(chunk.getPosition().x * ChunkConstants.SIZE_X - cameraPosition.x,
                            chunk.getPosition().y * ChunkConstants.SIZE_Y - cameraPosition.y,
                            chunk.getPosition().z * ChunkConstants.SIZE_Z - cameraPosition.z);

            if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
                shader = Assets.getMaterial("engine:prog.chunk");
                shader.enable();

                if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                    shader.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                    shader.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
                }

                shader.setFloat3("chunkPositionWorld", chunk.getPosition().x * ChunkConstants.SIZE_X,
                        chunk.getPosition().y * ChunkConstants.SIZE_Y, chunk.getPosition().z * ChunkConstants.SIZE_Z);
                shader.setFloat("animated", chunk.isAnimated() ? 1.0f : 0.0f);

                if (mode == ChunkRenderMode.REFLECTION) {
                    shader.setFloat("clip", camera.getClipHeight());
                } else {
                    shader.setFloat("clip", 0.0f);
                }

            } else if (mode == ChunkRenderMode.SHADOW_MAP) {
                shader = Assets.getMaterial("engine:prog.shadowMap");
                shader.enable();
            } else if (mode == ChunkRenderMode.Z_PRE_PASS) {
                CoreRegistry.get(ShaderManager.class).disableShader();
            }

            GL11.glPushMatrix();

            GL11.glTranslatef(chunkPositionRelToCamera.x, chunkPositionRelToCamera.y, chunkPositionRelToCamera.z);

            for (int i = 0; i < verticalMeshSegments; i++) {
                if (!chunk.getMesh()[i].isEmpty()) {
                    if (renderingDebugConfig.isRenderChunkBoundingBoxes()) {
                        AABBRenderer aabbRenderer = new AABBRenderer(chunk.getSubMeshAABB(i));
                        aabbRenderer.renderLocally(1f);
                        statRenderedTriangles += 12;
                    }

                    if (shader != null) {
                        shader.enable();
                    }

                    chunk.getMesh()[i].render(phase);
                    statRenderedTriangles += chunk.getMesh()[i].triangleCount();
                }
            }

            if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
                // eclipse is paranoid about this - it thinks that shader could be null here
                if (shader != null) {
                    if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                        shader.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
                    } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                        shader.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
                    }
                }
            }

            GL11.glPopMatrix();
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
        CoreRegistry.get(AudioManager.class).stopAllSounds();
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

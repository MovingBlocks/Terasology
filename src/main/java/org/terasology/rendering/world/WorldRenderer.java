/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.controllers.LocalPlayerSystem;
import org.terasology.components.LightComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.config.Config;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.primitives.LightGeometryHelper;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.game.paths.PathManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.WorldTimeEventManager;
import org.terasology.math.*;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.physics.BulletPhysics;
import org.terasology.rendering.renderer.AABBRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OculusStereoCamera;
import org.terasology.rendering.cameras.OrthographicCamera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.logic.MeshRenderer;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.EntityAwareWorldProvider;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldProvider;
import org.terasology.world.WorldProviderCoreImpl;
import org.terasology.world.WorldProviderWrapper;
import org.terasology.world.WorldTimeEvent;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkState;
import org.terasology.world.chunks.provider.ChunkProvider;
import org.terasology.world.chunks.provider.LocalChunkProvider;
import org.terasology.world.chunks.store.ChunkStore;
import org.terasology.world.chunks.store.ChunkStoreProtobuf;
import org.terasology.world.generator.MapGenerator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * The World Renderer class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class WorldRenderer {
    public static final int MAX_ANIMATED_CHUNKS = 64;
    public static final int MAX_BILLBOARD_CHUNKS = 64;
    public static final int VERTICAL_SEGMENTS = CoreRegistry.get(Config.class).getSystem().getVerticalChunkMeshSegments();

    public static final float BLOCK_LIGHT_POW = 0.96f;
    public static final float BLOCK_LIGHT_SUN_POW = 0.8f;
    public static final float BLOCK_INTENSITY_FACTOR = 1.25f;

    private static final Logger logger = LoggerFactory.getLogger(WorldRenderer.class);

    private Config config = CoreRegistry.get(Config.class);

    /* WORLD PROVIDER */
    private final WorldProvider worldProvider;
    private ChunkProvider chunkProvider;
    private ChunkStore chunkStore;

    /* PLAYER */
    private LocalPlayer player;

    /* CAMERAS */
    private Camera localPlayerCamera = null;
    private Camera activeCamera = null;

    /* SHADOW MAPPING */
    private static final int SHADOW_FRUSTUM_BOUNDS = 500;
    private Camera lightCamera = new OrthographicCamera(-SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, -SHADOW_FRUSTUM_BOUNDS);

    /* LIGHTING */
    LightComponent mainDirectionalLight = new LightComponent();

    /* CHUNKS */
    private ChunkTessellator chunkTessellator;
    private boolean pendingChunks = false;
    private final ArrayList<Chunk> chunksInProximity = new ArrayList<Chunk>(64*64);
    private int chunkPosX, chunkPosZ;

    /* RENDERING */
    private final PriorityQueue<Chunk> renderQueueChunksOpaque = new PriorityQueue<Chunk>(64*64, new ChunkFrontToBackComparator());
    private final PriorityQueue<Chunk> renderQueueChunksOpaqueShadow = new PriorityQueue<Chunk>(64*64, new ChunkFrontToBackComparator());
    private final PriorityQueue<Chunk> renderQueueChunksOpaqueReflection = new PriorityQueue<Chunk>(64*64, new ChunkFrontToBackComparator());
    private final PriorityQueue<Chunk> renderQueueChunksAlphaReject = new PriorityQueue<Chunk>(64*64, new ChunkFrontToBackComparator());
    private final PriorityQueue<Chunk> renderQueueChunksAlphaBlend = new PriorityQueue<Chunk>(64*64, new ChunkFrontToBackComparator());

    private WorldRenderingStage currentRenderStage = WorldRenderingStage.DEFAULT;

    /* HORIZON */
    private final Skysphere skysphere;

    /* TICKING */
    private float tick = 0;

    /* LIGHTING */
    private float smoothedPlayerSunlightValue;

    /* UPDATING */
    private final ChunkUpdateManager chunkUpdateManager;

    /* EVENTS */
    private final WorldTimeEventManager worldTimeEventManager;

    /* PHYSICS */
    private final BulletPhysics bulletPhysics;

    /* BLOCK GRID */
    private final BlockGrid blockGrid;

    /* STATISTICS */
    private int statDirtyChunks, statVisibleChunks, statIgnoredPhases;
    private int statChunkMeshEmpty, statChunkNotReady, statRenderedTriangles;

    /* ENUMS */
    public enum ChunkRenderMode {
        DEFAULT,
        REFLECTION,
        SHADOW_MAP,
        Z_PRE_PASS
    }

    public enum WorldRenderingStage {
        DEFAULT,
        OCULUS_LEFT_EYE,
        OCULUS_RIGHT_EYE
    }

    private ComponentSystemManager _systemManager;

    private ChunkStore loadChunkStore(File file) throws IOException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = new FileInputStream(file);
            in = new ObjectInputStream(fileIn);

            ChunkStore cache = (ChunkStore) in.readObject();
            if (cache instanceof ChunkStoreProtobuf)
                ((ChunkStoreProtobuf) cache).setup();
            else
                logger.warn("Chunk store might not have been initialized: {}", cache.getClass().getName());

            return cache;

        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to load chunk cache", e);
        } finally {
            // JAVA7 : cleanup
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Failed to close input stream", e);
                }
            }
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException e) {
                    logger.error("Failed to close input stream", e);
                }
            }
        }
    }

    /**
     * Initializes a new (local) world for the single player mode.
     *
     * @param worldInfo Information describing the world
     */
    public WorldRenderer(WorldInfo worldInfo, MapGenerator mapGenerator, EntityManager manager, LocalPlayerSystem localPlayerSystem) {
        // TODO: Cleaner method for this? Should not be using the world title
        try {
            final long time = System.currentTimeMillis();
            boolean loaded = false;
            File f = new File(PathManager.getInstance().getWorldSavePath(worldInfo.getTitle()), worldInfo.getTitle() + ".chunks");
            if (f.exists()) {
                final ChunkStoreProtobuf store = new ChunkStoreProtobuf(false);
                store.loadFromFile(f);
                store.setup();
                chunkStore = store;
                loaded = true;
            } else {
                f = new File(PathManager.getInstance().getWorldSavePath(worldInfo.getTitle()), worldInfo.getTitle() + ".dat");
                if (f.exists()) {
                    chunkStore = loadChunkStore(f);
                    logger.info("Loaded chunks in old java object serialization format");
                    loaded = true;
                }
            }
            if (loaded)
                logger.info("It took {} ms to load chunks from file {}", (System.currentTimeMillis() - time), f);
        } catch (Exception e) {
            /* TODO: We really should expose this error via UI so player knows that there is an issue with their world
               (don't have the game continue or we risk overwriting their game)
             */
            logger.error("Error loading chunks", e);
        }

        if (chunkStore == null)
            chunkStore = new ChunkStoreProtobuf();
        
        chunkProvider = new LocalChunkProvider(chunkStore, mapGenerator);
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(new WorldProviderCoreImpl(worldInfo, chunkProvider));
        CoreRegistry.put(BlockEntityRegistry.class, entityWorldProvider);
        CoreRegistry.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");
        worldProvider = new WorldProviderWrapper(entityWorldProvider);
        bulletPhysics = new BulletPhysics(worldProvider);
        chunkTessellator = new ChunkTessellator(worldProvider.getBiomeProvider());
        skysphere = new Skysphere(this);
        chunkUpdateManager = new ChunkUpdateManager(chunkTessellator, worldProvider);
        worldTimeEventManager = new WorldTimeEventManager(worldProvider);
        blockGrid = new BlockGrid();

        if (CoreRegistry.get(Config.class).getRendering().isOculusVrSupport()) {
            localPlayerCamera = new OculusStereoCamera();
        } else {
            localPlayerCamera = new PerspectiveCamera();
        }

        activeCamera = localPlayerCamera;

        // Setup the main directional light (the sunlight)
        mainDirectionalLight.lightType = LightComponent.LightType.DIRECTIONAL;
        // TODO: Those values HAVE to match the hardcoded value for the forward transparent pass for chunks
        mainDirectionalLight.lightColorAmbient = new Vector3f(1.0f, 1.0f, 1.0f);
        mainDirectionalLight.lightColorDiffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        mainDirectionalLight.lightAmbientIntensity = 2.0f;
        mainDirectionalLight.lightDiffuseIntensity = 1.0f;
        mainDirectionalLight.lightSpecularIntensity = 0.0f;

        // TODO: won't need localPlayerSystem here once camera is in the ES proper
        localPlayerSystem.setPlayerCamera(localPlayerCamera);
        _systemManager = CoreRegistry.get(ComponentSystemManager.class);

        initTimeEvents();
    }

    /**
     * Updates the list of chunks around the player.
     *
     * @param force Forces the update
     * @return True if the list was changed
     */
    public boolean updateChunksInProximity(boolean force) {
        int newChunkPosX = calcCamChunkOffsetX();
        int newChunkPosZ = calcCamChunkOffsetZ();

        // TODO: This should actually be done based on events from the ChunkProvider on new chunk availability/old chunk removal
        int viewingDistance = config.getRendering().getActiveViewingDistance();

        boolean chunksCurrentlyPending = false;

        if (chunkPosX != newChunkPosX || chunkPosZ != newChunkPosZ || force || pendingChunks) {
            // just add all visible chunks
            if (chunksInProximity.size() == 0 || force || pendingChunks) {
                chunksInProximity.clear();
                for (int x = -(viewingDistance / 2); x < viewingDistance / 2; x++) {
                    for (int z = -(viewingDistance / 2); z < viewingDistance / 2; z++) {
                        Chunk c = chunkProvider.getChunk(newChunkPosX + x, 0, newChunkPosZ + z);
                        if (c != null && c.getChunkState() == ChunkState.COMPLETE && worldProvider.getLocalView(c.getPos()) != null) {
                            chunksInProximity.add(c);
                        } else {
                            chunksCurrentlyPending = true;
                        }
                    }
                }
            }
            // adjust proximity chunk list
            else {
                int vd2 = viewingDistance / 2;

                Rect2i oldView = new Rect2i(chunkPosX - vd2, chunkPosZ - vd2, viewingDistance, viewingDistance);
                Rect2i newView = new Rect2i(newChunkPosX - vd2, newChunkPosZ - vd2, viewingDistance, viewingDistance);

                // remove
                List<Rect2i> removeRects = Rect2i.subtractEqualsSized(oldView, newView);
                for (Rect2i r : removeRects) {
                    for (int x = r.minX(); x < r.maxX(); ++x) {
                        for (int y = r.minY(); y < r.maxY(); ++y) {
                            Chunk c = chunkProvider.getChunk(x, 0, y);
                            chunksInProximity.remove(c);

                            ChunkMesh[] mesh = c.getMesh();
                            if (mesh != null) {
                                // Only keep chunks around the player in the video memory - recreate the VBOs for cached chunks
                                for (ChunkMesh m : mesh) {
                                    m.dispose();
                                }
                                c.setMesh(null);
                            }
                        }
                    }
                }

                // add
                List<Rect2i> addRects = Rect2i.subtractEqualsSized(newView, oldView);
                for (Rect2i r : addRects) {
                    for (int x = r.minX(); x < r.maxX(); ++x) {
                        for (int y = r.minY(); y < r.maxY(); ++y) {
                            Chunk c = chunkProvider.getChunk(x, 0, y);
                            if (c != null && c.getChunkState() == ChunkState.COMPLETE && worldProvider.getLocalView(c.getPos()) != null) {
                                chunksInProximity.add(c);
                            } else {
                                chunksCurrentlyPending = true;
                            }
                        }
                    }
                }
            }

            chunkPosX = newChunkPosX;
            chunkPosZ = newChunkPosZ;
            pendingChunks = chunksCurrentlyPending;

            Collections.sort(chunksInProximity, new ChunkFrontToBackComparator());

            return true;
        }

        return false;
    }

    private static class ChunkFrontToBackComparator implements Comparator<Chunk> {
        @Override
        public int compare(Chunk o1, Chunk o2) {
            double distance = distanceToCamera(o1);
            double distance2 = distanceToCamera(o2);

            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            if (distance == distance2)
                return 0;

            return distance2 > distance ? -1 : 1;
        }
    }

    private static class ChunkBackToFrontComparator implements Comparator<Chunk> {
        @Override
        public int compare(Chunk o1, Chunk o2) {
            double distance = distanceToCamera(o1);
            double distance2 = distanceToCamera(o2);

            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }

            if (distance == distance2)
                return 0;

            return distance2 > distance ? 1 : -1;
        }
    }

    static public float distanceToCamera(Chunk chunk) {
        Vector3f result = new Vector3f((chunk.getPos().x + 0.5f) * Chunk.SIZE_X, 0, (chunk.getPos().z + 0.5f) * Chunk.SIZE_Z);

        Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        result.x -= cameraPos.x;
        result.z -= cameraPos.z;

        return result.length();
    }

    private static class ChunkAlphaBlendComparator implements Comparator<Chunk> {

        @Override
        public int compare(Chunk o1, Chunk o2) {
            double distance = distanceToCamera(o1);
            double distance2 = distanceToCamera(o2);

            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }

            if (distance == distance2)
                return 0;

            return distance2 > distance ? 1 : -1;
        }

        private float distanceToCamera(Chunk chunk) {
            Vector3f result = new Vector3f((chunk.getPos().x + 0.5f) * Chunk.SIZE_X, 0, (chunk.getPos().z + 0.5f) * Chunk.SIZE_Z);

            Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            result.x -= cameraPos.x;
            result.z -= cameraPos.z;

            return result.length();
        }
    }

    private Vector3f getPlayerPosition() {
        if (player != null) {
            return player.getPosition();
        }
        return new Vector3f();
    }

    /**
     * Creates the world time events to play the game's soundtrack at specific times.
     */
    public void initTimeEvents() {

        final AudioManager audioManager = CoreRegistry.get(AudioManager.class);

        // SUNRISE
        worldTimeEventManager.addWorldTimeEvent(new WorldTimeEvent(0.1, true) {
            @Override
            public void run() {
                if (getPlayerPosition().y < 50) {
                    audioManager.playMusic(Assets.getMusic("engine:SpacialWinds"));
                } else if (getPlayerPosition().y > 175) {
                    audioManager.playMusic(Assets.getMusic("engine:Heaven"));
                } else {
                    audioManager.playMusic(Assets.getMusic("engine:Sunrise"));
                }
            }
        });

        // AFTERNOON
        worldTimeEventManager.addWorldTimeEvent(new WorldTimeEvent(0.25, true) {
            @Override
            public void run() {
                if (getPlayerPosition().y < 50) {
                    audioManager.playMusic(Assets.getMusic("engine:DwarfForge"));
                } else if (getPlayerPosition().y > 175) {
                    audioManager.playMusic(Assets.getMusic("engine:SpaceExplorers"));
                } else {
                    audioManager.playMusic(Assets.getMusic("engine:Afternoon"));
                }
            }
        });

        // SUNSET
        worldTimeEventManager.addWorldTimeEvent(new WorldTimeEvent(0.4, true) {
            @Override
            public void run() {
                if (getPlayerPosition().y < 50) {
                    audioManager.playMusic(Assets.getMusic("engine:OrcFortress"));
                } else if (getPlayerPosition().y > 175) {
                    audioManager.playMusic(Assets.getMusic("engine:PeacefulWorld"));
                } else {
                    audioManager.playMusic(Assets.getMusic("engine:Sunset"));
                }
            }
        });

        // NIGHT
        worldTimeEventManager.addWorldTimeEvent(new WorldTimeEvent(0.6, true) {
            @Override
            public void run() {
                if (getPlayerPosition().y < 50) {
                    audioManager.playMusic(Assets.getMusic("engine:CreepyCaves"));
                } else if (getPlayerPosition().y > 175) {
                    audioManager.playMusic(Assets.getMusic("engine:ShootingStars"));
                } else {
                    audioManager.playMusic(Assets.getMusic("engine:Dimlight"));
                }
            }
        });

        // NIGHT
        worldTimeEventManager.addWorldTimeEvent(new WorldTimeEvent(0.75, true) {
            @Override
            public void run() {
                if (getPlayerPosition().y < 50) {
                    audioManager.playMusic(Assets.getMusic("engine:CreepyCaves"));
                } else if (getPlayerPosition().y > 175) {
                    audioManager.playMusic(Assets.getMusic("engine:NightTheme"));
                } else {
                    audioManager.playMusic(Assets.getMusic("engine:OtherSide"));
                }
            }
        });

        // BEFORE SUNRISE
        worldTimeEventManager.addWorldTimeEvent(new WorldTimeEvent(0.9, true) {
            @Override
            public void run() {
                if (getPlayerPosition().y < 50) {
                    audioManager.playMusic(Assets.getMusic("engine:CreepyCaves"));
                } else if (getPlayerPosition().y > 175) {
                    audioManager.playMusic(Assets.getMusic("engine:Heroes"));
                } else {
                    audioManager.playMusic(Assets.getMusic("engine:Resurface"));
                }
            }
        });
    }

    public void updateAndQueueVisibleChunks() {
        updateAndQueueVisibleChunks(true, true);
    }

    /**
     * Updates the currently visible chunks (in sight of the player).
     */
    public int updateAndQueueVisibleChunks(boolean fillShadowRenderQueue, boolean processChunkUpdates) {
        statDirtyChunks = 0;
        statVisibleChunks = 0;
        statIgnoredPhases = 0;

        int processedChunks = 0;
        for (int i = 0; i < chunksInProximity.size(); i++) {
            Chunk c = chunksInProximity.get(i);
            ChunkMesh[] mesh = c.getMesh();

            if (i < TeraMath.clamp(config.getRendering().getMaxChunksUsedForShadowMapping(), 64, 1024)
                    && config.getRendering().isDynamicShadows() && fillShadowRenderQueue) {
                if (isChunkVisibleLight(c) && isChunkValidForRender(c)) {
                    if (triangleCount(mesh, ChunkMesh.RENDER_PHASE.OPAQUE) > 0)
                        renderQueueChunksOpaqueShadow.add(c);
                    else
                        statIgnoredPhases++;
                }
            }

            if (isChunkValidForRender(c)) {
                if (isChunkVisible(c)) {
                    if (triangleCount(mesh, ChunkMesh.RENDER_PHASE.OPAQUE) > 0)
                        renderQueueChunksOpaque.add(c);
                    else
                        statIgnoredPhases++;

                    if (triangleCount(mesh, ChunkMesh.RENDER_PHASE.REFRACTIVE) > 0)
                        renderQueueChunksAlphaBlend.add(c);
                    else
                        statIgnoredPhases++;

                    if (triangleCount(mesh, ChunkMesh.RENDER_PHASE.ALPHA_REJECT) > 0 && i < MAX_BILLBOARD_CHUNKS)
                        renderQueueChunksAlphaReject.add(c);
                    else
                        statIgnoredPhases++;

                    statVisibleChunks++;

                    if (statVisibleChunks < MAX_ANIMATED_CHUNKS)
                        c.setAnimated(true);
                    else
                        c.setAnimated(false);
                }

                if (isChunkVisibleReflection(c)) {
                    renderQueueChunksOpaqueReflection.add(c);
                }

                // Process all chunks in the area, not only the visible ones
                if (processChunkUpdates && processChunkUpdate(c)) {
                    processedChunks++;
                }
            }
        }

        return processedChunks;
    }

    private boolean processChunkUpdate(Chunk c) {
        if (c.getPendingMesh() != null) {
            for (int j = 0; j < c.getPendingMesh().length; j++) {
                c.getPendingMesh()[j].generateVBOs();
            }
            if (c.getMesh() != null) {
                for (int j = 0; j < c.getMesh().length; j++) {
                    c.getMesh()[j].dispose();
                }
            }
            c.setMesh(c.getPendingMesh());
            c.setPendingMesh(null);
        }

        if ((c.isDirty() || c.getMesh() == null) && isChunkValidForRender(c)) {
            statDirtyChunks++;
            chunkUpdateManager.queueChunkUpdate(c, ChunkUpdateManager.UpdateType.DEFAULT);

            return true;
        }

        return false;
    }

    private int triangleCount(ChunkMesh[] mesh, ChunkMesh.RENDER_PHASE type) {
        int count = 0;

        if (mesh != null) {
            for (ChunkMesh aMesh : mesh) {
                count += aMesh.triangleCount(type);
            }
        }

        return count;
    }

    private void resetStats() {
        statChunkMeshEmpty = 0;
        statChunkNotReady = 0;
        statRenderedTriangles = 0;
    }

    /**
     * Renders the world.
     */
    public void render(DefaultRenderingProcess.StereoRenderState stereoRenderState) {

        switch (stereoRenderState) {
            case MONO:
                currentRenderStage = WorldRenderingStage.DEFAULT;
                break;
            case OCULUS_LEFT_EYE:
                currentRenderStage = WorldRenderingStage.OCULUS_LEFT_EYE;
                // Make sure the frustum is up-to-date for each eye
                activeCamera.updateFrustum();
                break;
            case OCULUS_RIGHT_EYE:
                currentRenderStage = WorldRenderingStage.OCULUS_RIGHT_EYE;
                // Make sure the frustum is up-to-date for each eye
                activeCamera.updateFrustum();
                break;
        }

        resetStats();

        if (stereoRenderState == DefaultRenderingProcess.StereoRenderState.MONO
                || stereoRenderState == DefaultRenderingProcess.StereoRenderState.OCULUS_LEFT_EYE) {
            updateAndQueueVisibleChunks();
        } else {
            // Don't cause havoc in the second pass for the second eye
            updateAndQueueVisibleChunks(false, false);
        }

        if (config.getRendering().isDynamicShadows()
                // Only render the shadow map once
                && (stereoRenderState == DefaultRenderingProcess.StereoRenderState.MONO || stereoRenderState == DefaultRenderingProcess.StereoRenderState.OCULUS_LEFT_EYE)) {
            DefaultRenderingProcess.getInstance().beginRenderSceneShadowMap();
            //glCullFace(GL11.GL_FRONT);
            renderShadowMap(lightCamera);
            //glCullFace(GL11.GL_BACK);
            DefaultRenderingProcess.getInstance().endRenderSceneShadowMap();
        }

        DefaultRenderingProcess.getInstance().beginRenderReflectedScene();
        glCullFace(GL11.GL_FRONT);
        getActiveCamera().setReflected(true);
        renderWorldReflection(activeCamera);
        getActiveCamera().setReflected(false);
        glCullFace(GL11.GL_BACK);
        DefaultRenderingProcess.getInstance().endRenderReflectedScene();

        renderWorld(getActiveCamera());

        /* RENDER THE FINAL POST-PROCESSED SCENE */
        PerformanceMonitor.startActivity("Render Post-Processing");
        DefaultRenderingProcess.getInstance().renderScene(stereoRenderState);
        PerformanceMonitor.endActivity();

        if (activeCamera != null) {
            activeCamera.updatePrevViewProjectionMatrix();
        }
    }

    private void renderWorld(Camera camera) {
        if (config.getSystem().isDebugRenderWireframe())
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        DefaultRenderingProcess.getInstance().clear();
        DefaultRenderingProcess.getInstance().beginRenderSceneOpaque();

        /*
         * SKYSPHERE
         */
        camera.lookThroughNormalized();

        PerformanceMonitor.startActivity("Render Sky");
        DefaultRenderingProcess.getInstance().beginRenderSceneSky();
        skysphere.render(camera);
        DefaultRenderingProcess.getInstance().endRenderSceneSky();
        PerformanceMonitor.endActivity();

        camera.lookThrough();

        /* WORLD RENDERING */
        PerformanceMonitor.startActivity("Render World");

        PerformanceMonitor.startActivity("Render Objects (Opaque)");

        for (RenderSystem renderer : _systemManager.iterateRenderSubscribers()) {
            renderer.renderOpaque();
        }

        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Render Chunks (Opaque)");

        /*
         * FIRST CHUNK PASS: OPAQUE
         */
        while (renderQueueChunksOpaque.size() > 0) {
            renderChunk(renderQueueChunksOpaque.poll(), ChunkMesh.RENDER_PHASE.OPAQUE, camera, ChunkRenderMode.DEFAULT);
        }

        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Render Chunks (Billboards)");

        /*
         * SECOND CHUNK PASS: ALPHA REJECT
         */
        while (renderQueueChunksAlphaReject.size() > 0) {
            renderChunk(renderQueueChunksAlphaReject.poll(), ChunkMesh.RENDER_PHASE.ALPHA_REJECT, camera, ChunkRenderMode.DEFAULT);
        }

        PerformanceMonitor.endActivity();

        /*
         * FIRST PERSON VIEW
         */
        if (activeCamera != null && !config.getSystem().isDebugFirstPersonElementsHidden()) {
            PerformanceMonitor.startActivity("Render First Person");

            glPushMatrix();
            glLoadIdentity();

            activeCamera.updateMatrices(90f);
            activeCamera.loadProjectionMatrix();

            glDepthFunc(GL_ALWAYS);

            for (RenderSystem renderer : _systemManager.iterateRenderSubscribers()) {
                renderer.renderFirstPerson();
            }

            glDepthFunc(GL_LEQUAL);

            activeCamera.updateMatrices();
            activeCamera.loadProjectionMatrix();

            glPopMatrix();

            PerformanceMonitor.endActivity();
        }

        /*
         * OVERLAYS
         */
        PerformanceMonitor.startActivity("Render Overlays");

        for (RenderSystem renderer : _systemManager.iterateRenderSubscribers()) {
            renderer.renderOverlay();
        }

        PerformanceMonitor.endActivity();

        DefaultRenderingProcess.getInstance().endRenderSceneOpaque();

        PerformanceMonitor.startActivity("Render Light Geometry");

        /*
         * LIGHT GEOMETRY (STENCIL) PASS
         */
        DefaultRenderingProcess.getInstance().beginRenderLightGeometryStencilPass();

        GLSLShaderProgramInstance program = ShaderManager.getInstance().getShaderProgramInstance("simple");
        program.enable();
        program.setCamera(camera);

        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.iteratorEntities(LocationComponent.class, LightComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, program, camera, true);
        }

        DefaultRenderingProcess.getInstance().endRenderLightGeometryStencilPass();

        /*
         * LIGHT GEOMETRY PASS
         */
        DefaultRenderingProcess.getInstance().beginRenderLightGeometry();

        program = ShaderManager.getInstance().getShaderProgramInstance("lightGeometryPass");

        for (EntityRef entity : entityManager.iteratorEntities(LocationComponent.class, LightComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, program, camera, false);
        }

        DefaultRenderingProcess.getInstance().endRenderLightGeometry();
        DefaultRenderingProcess.getInstance().beginRenderDirectionalLights();

        // Sunlight
        Vector3f sunlightWorldPosition = new Vector3f(skysphere.getSunDirection(true));
        renderLightComponent(mainDirectionalLight, sunlightWorldPosition, program, camera, false);

        DefaultRenderingProcess.getInstance().endRenderDirectionalLights();

        PerformanceMonitor.endActivity();

        DefaultRenderingProcess.getInstance().beginRenderSceneTransparent();

        PerformanceMonitor.startActivity("Render Chunks (Alpha blend)");

        // Make sure the water surface is rendered if the player is swimming
        boolean headUnderWater = isUnderWater();
        if (headUnderWater) {
            glDisable(GL11.GL_CULL_FACE);
        }

        /*
         * THIRD CHUNK PASS: REFRACTIVE CHUNKS
         */
        while (renderQueueChunksAlphaBlend.size() > 0) {
            renderChunk(renderQueueChunksAlphaBlend.poll(), ChunkMesh.RENDER_PHASE.REFRACTIVE, camera, ChunkRenderMode.DEFAULT);
        }

        if (headUnderWater) {
            glEnable(GL11.GL_CULL_FACE);
        }

        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Render Objects (Transparent)");

        /*
         * ALPHA BLEND
         */
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        for (RenderSystem renderer : _systemManager.iterateRenderSubscribers()) {
            renderer.renderAlphaBlend();
        }

        glDisable(GL_BLEND);
        glDepthMask(true);

        PerformanceMonitor.endActivity();

        DefaultRenderingProcess.getInstance().endRenderSceneTransparent();

        if (config.getSystem().isDebugRenderWireframe()) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    private boolean renderLightComponent(LightComponent lightComponent, Vector3f lightWorldPosition, GLSLShaderProgramInstance program, Camera camera, boolean geometryOnly) {
        Vector3f positionViewSpace = new Vector3f();
        positionViewSpace.sub(lightWorldPosition, activeCamera.getPosition());

        boolean doRenderLight = lightComponent.lightType == LightComponent.LightType.DIRECTIONAL
                || lightComponent.lightRenderingDistance == 0.0f
                || positionViewSpace.lengthSquared() < (lightComponent.lightRenderingDistance * lightComponent.lightRenderingDistance);

        doRenderLight &= isLightVisible(positionViewSpace, lightComponent);

        if (!doRenderLight) {
            return false;
        }

        if (!geometryOnly) {
            if (lightComponent.lightType == LightComponent.LightType.POINT ) {
                program.addFeatureIfAvailable(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_LIGHT_POINT);
            } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
                program.addFeatureIfAvailable(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_LIGHT_DIRECTIONAL);
            }
        }
        program.enable();
        program.setCamera(camera);

        Vector3f worldPosition = new Vector3f();
        worldPosition.sub(lightWorldPosition, activeCamera.getPosition());

        Vector3f lightViewPosition = new Vector3f();
        camera.getViewMatrix().transform(worldPosition, lightViewPosition);

        program.setFloat3("lightViewPos", lightViewPosition.x, lightViewPosition.y, lightViewPosition.z);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.setIdentity();

        modelMatrix.setTranslation(worldPosition);
        modelMatrix.setScale(lightComponent.lightAttenuationRange);
        program.setMatrix4("modelMatrix", modelMatrix);

        if (!geometryOnly) {
            program.setFloat3("lightColorDiffuse", lightComponent.lightColorDiffuse.x, lightComponent.lightColorDiffuse.y, lightComponent.lightColorDiffuse.z);
            program.setFloat3("lightColorAmbient", lightComponent.lightColorAmbient.x, lightComponent.lightColorAmbient.y, lightComponent.lightColorAmbient.z);

            program.setFloat4("lightProperties", lightComponent.lightAmbientIntensity, lightComponent.lightDiffuseIntensity,
                    lightComponent.lightSpecularIntensity, lightComponent.lightSpecularPower);
        }

        if (lightComponent.lightType == LightComponent.LightType.POINT) {
            if (!geometryOnly) {
                program.setFloat4("lightExtendedProperties", lightComponent.lightAttenuationRange * 0.975f, lightComponent.lightAttenuationFalloff, 0.0f, 0.0f);
            }

            LightGeometryHelper.renderSphereGeometry();
        } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
            // Directional lights cover all pixels on the screen
            DefaultRenderingProcess.getInstance().renderFullscreenQuad();
        }

        if (!geometryOnly) {
            if (lightComponent.lightType == LightComponent.LightType.POINT ) {
                program.removeFeature(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_LIGHT_POINT);
            } else if (lightComponent.lightType == LightComponent.LightType.DIRECTIONAL) {
                program.removeFeature(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_LIGHT_DIRECTIONAL);
            }
        }

        return true;
    }

    private void renderWorldReflection(Camera camera) {
        PerformanceMonitor.startActivity("Render World (Reflection)");
        camera.lookThroughNormalized();
        skysphere.render(camera);

        GLSLShaderProgramInstance chunkShader = ShaderManager.getInstance().getShaderProgramInstance("chunk");
        chunkShader.addFeatureIfAvailable(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_USE_FORWARD_LIGHTING);

        if (config.getRendering().isReflectiveWater()) {
            camera.lookThrough();

            while (renderQueueChunksOpaqueReflection.size() > 0)
                renderChunk(renderQueueChunksOpaqueReflection.poll(), ChunkMesh.RENDER_PHASE.OPAQUE, camera, ChunkRenderMode.REFLECTION);
        }

        chunkShader.removeFeature(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_USE_FORWARD_LIGHTING);

        PerformanceMonitor.endActivity();
    }

    private void renderShadowMap(Camera camera) {
        PerformanceMonitor.startActivity("Render World (Shadow Map)");

        camera.lookThrough();

        while (renderQueueChunksOpaqueShadow.size() > 0)
            renderChunk(renderQueueChunksOpaqueShadow.poll(), ChunkMesh.RENDER_PHASE.OPAQUE, camera, ChunkRenderMode.SHADOW_MAP);

        for (RenderSystem renderer : _systemManager.iterateRenderSubscribers()) {
            renderer.renderShadows();
        }

        PerformanceMonitor.endActivity();
    }

    private void renderChunk(Chunk chunk, ChunkMesh.RENDER_PHASE phase, Camera camera, ChunkRenderMode mode) {

        if (chunk.getChunkState() == ChunkState.COMPLETE && chunk.getMesh() != null) {

            GLSLShaderProgramInstance shader = null;

            final Vector3f cameraPosition = camera.getPosition();
            final Vector3d chunkPositionRelToCamera =
                    new Vector3d(chunk.getPos().x * Chunk.SIZE_X - cameraPosition.x,
                            chunk.getPos().y * Chunk.SIZE_Y - cameraPosition.y,
                            chunk.getPos().z * Chunk.SIZE_Z - cameraPosition.z);

            if (mode == ChunkRenderMode.DEFAULT || mode == ChunkRenderMode.REFLECTION) {
                shader = ShaderManager.getInstance().getShaderProgramInstance("chunk");
                shader.enable();

                if (phase == ChunkMesh.RENDER_PHASE.REFRACTIVE) {
                    shader.addFeatureIfAvailable(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RENDER_PHASE.ALPHA_REJECT) {
                    shader.addFeatureIfAvailable(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_ALPHA_REJECT);
                }

                shader.setFloat3("chunkPositionWorld", (float) (chunk.getPos().x * Chunk.SIZE_X), (float) (chunk.getPos().y * Chunk.SIZE_Y), (float) (chunk.getPos().z * Chunk.SIZE_Z));
                shader.setFloat("animated", chunk.getAnimated() ? 1.0f : 0.0f);

                if (mode == ChunkRenderMode.REFLECTION) {
                    shader.setFloat("clip", camera.getClipHeight());
                } else {
                    shader.setFloat("clip", 0.0f);
                }

            } else if (mode == ChunkRenderMode.SHADOW_MAP) {
                shader = ShaderManager.getInstance().getShaderProgramInstance("shadowMap");
                shader.enable();
            } else if (mode == ChunkRenderMode.Z_PRE_PASS) {
                ShaderManager.getInstance().disableShader();
            }

            GL11.glPushMatrix();

            GL11.glTranslated(chunkPositionRelToCamera.x, chunkPositionRelToCamera.y, chunkPositionRelToCamera.z);

            for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
                if (!chunk.getMesh()[i].isEmpty()) {
                    if (config.getSystem().isDebugRenderChunkBoundingBoxes()) {
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
                if (phase == ChunkMesh.RENDER_PHASE.REFRACTIVE) {
                    shader.removeFeature(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RENDER_PHASE.ALPHA_REJECT) {
                    shader.removeFeature(GLSLShaderProgramInstance.ShaderProgramFeatures.FEATURE_ALPHA_REJECT);
                }
            }

            GL11.glPopMatrix();
        } else {
            statChunkNotReady++;
        }
    }

    public float getSmoothedPlayerSunlightValue() {
        return smoothedPlayerSunlightValue;
    }

    public float getSunlightValue() {
        return getSunlightValueAt(new Vector3f(getActiveCamera().getPosition()));
    }

    public float getRenderingLightValue() {
        return getRenderingLightValueAt(new Vector3f(getActiveCamera().getPosition()));
    }

    public float getRenderingLightValueAt(Vector3f pos) {
        float rawLightValueSun = worldProvider.getSunlight(pos) / 15.0f;
        float rawLightValueBlock = worldProvider.getLight(pos) / 15.0f;

        float lightValueSun = (float) Math.pow(BLOCK_LIGHT_SUN_POW, (1.0f - rawLightValueSun) * 16.0f) * rawLightValueSun;
        lightValueSun *= getDaylight();
        // TODO: Hardcoded factor and value to compensate for daylight tint and night brightness
        lightValueSun *= 0.9f;
        lightValueSun += 0.05f;

        float lightValueBlock = (float) Math.pow(BLOCK_LIGHT_POW, (1.0f - rawLightValueBlock) * 16.0f) * rawLightValueBlock * BLOCK_INTENSITY_FACTOR;

        return Math.max(lightValueBlock, lightValueSun);
    }

    public float getSunlightValueAt(Vector3f pos) {
        float rawLightValueSun = worldProvider.getSunlight(pos) / 15.0f;

        float lightValueSun = (float) Math.pow(BLOCK_LIGHT_SUN_POW, (1.0f - rawLightValueSun) * 16.0f) * rawLightValueSun;
        lightValueSun *= getDaylight();
        // TODO: Hardcoded factor and value to compensate for daylight tint and night brightness
        lightValueSun *= 0.9f;
        lightValueSun += 0.05f;

        return lightValueSun;
    }

    public void update(float delta) {
        PerformanceMonitor.startActivity("Update Tick");
        updateTick(delta);
        PerformanceMonitor.endActivity();

        // Free unused space
        PerformanceMonitor.startActivity("Update Chunk Cache");
        chunkProvider.update();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Update Close Chunks");
        updateChunksInProximity(false);
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Skysphere");
        skysphere.update(delta);
        PerformanceMonitor.endActivity();

        if (activeCamera != null) {
            activeCamera.update(delta);
        }

        if (lightCamera != null) {
            positionLightCamera();
            lightCamera.update(delta);
        }

        // And finally fire any active events
        PerformanceMonitor.startActivity("Fire Events");
        worldTimeEventManager.fireWorldTimeEvents();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Physics Renderer");
        bulletPhysics.update(delta);
        PerformanceMonitor.endActivity();

        // Continuously updated rendering light value at the players position that fades smoothly from cell to cell
        smoothedPlayerSunlightValue = TeraMath.lerpf(smoothedPlayerSunlightValue, getSunlightValue(), delta);
    }

    public void positionLightCamera() {
        // Shadows are rendered around the player so...
        Vector3f lightPosition = new Vector3f(activeCamera.getPosition().x, 0.0f, activeCamera.getPosition().z);

        // Project the camera position to light space and make sure it is only moved in texel steps (avoids flickering when moving the camera)
        float texelSize = 1.0f / config.getRendering().getShadowMapResolution();
        texelSize *= 2.0f;

        lightCamera.getViewProjectionMatrix().transform(lightPosition);
        lightPosition.set(TeraMath.fastFloor(lightPosition.x / texelSize) * texelSize, 0.0f, TeraMath.fastFloor(lightPosition.z / texelSize) * texelSize);
        lightCamera.getInverseViewProjectionMatrix().transform(lightPosition);

        // ... we position our new camera at the position of the player and move it
        // quite a bit into the direction of the sun (our main light).

        // Make sure the sun does not move too often since it causes massive shadow flickering (from hell to the max)!
        float stepSize = 50f;
        Vector3f sunDirection = skysphere.getQuantizedSunDirection(stepSize);

        Vector3f sunPosition = new Vector3f(sunDirection);
        sunPosition.scale(256.0f + 64.0f);
        lightPosition.add(sunPosition);

        lightCamera.getPosition().set(lightPosition);

        // and adjust it to look from the sun direction into the direction of our player
        Vector3f negSunDirection = new Vector3f(sunDirection);
        negSunDirection.scale(-1.0f);

        lightCamera.getViewingDirection().set(negSunDirection);
    }

    public boolean isUnderWater() {
        Vector3f cameraPos = new Vector3f(CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition());

        // Compensate for waves
        if (config.getRendering().isAnimateWater()) {
            cameraPos.y += 0.5f;
        }

        Block block = CoreRegistry.get(WorldProvider.class).getBlock(new Vector3f(cameraPos));
        return block.isLiquid();
    }

    public Vector3f getTint() {
        Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        Block block = CoreRegistry.get(WorldProvider.class).getBlock(new Vector3f(cameraPos));
        return block.getTint();
    }

    /**
     * Updates the tick variable that animation is based on
     */
    private void updateTick(float delta) {
        tick += delta * 1000;
    }

    /**
     * Returns the maximum height at a given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The maximum height
     */
    public final int maxHeightAt(int x, int z) {
        for (int y = Chunk.SIZE_Y - 1; y >= 0; y--) {
            if (worldProvider.getBlock(x, y, z).getId() != 0x0)
                return y;
        }

        return 0;
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset on the x-axis
     */
    private int calcCamChunkOffsetX() {
        return (int) (getActiveCamera().getPosition().x / Chunk.SIZE_X);
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset on the z-axis
     */
    private int calcCamChunkOffsetZ() {
        return (int) (getActiveCamera().getPosition().z / Chunk.SIZE_Z);
    }

    /**
     * Sets a new player and spawns him at the spawning point.
     *
     * @param p The player
     */
    public void setPlayer(LocalPlayer p) {
        player = p;
        chunkProvider.addRegionEntity(p.getEntity(), config.getRendering().getActiveViewingDistance());
        updateChunksInProximity(true);
    }

    public void changeViewDistance(int viewingDistance) {
        logger.debug("New Viewing Distance: {}", viewingDistance);
        if (player != null) {
            chunkProvider.addRegionEntity(player.getEntity(), viewingDistance);
        }
        updateChunksInProximity(true);
    }

    public ChunkProvider getChunkProvider() {
        return chunkProvider;
    }

    /**
     * Disposes this world.
     */
    public void dispose() {
        worldProvider.dispose();
        WorldInfo worldInfo = worldProvider.getWorldInfo();
        try {
            WorldInfo.save(new File(PathManager.getInstance().getWorldSavePath(worldInfo.getTitle()), WorldInfo.DEFAULT_FILE_NAME), worldInfo);
        } catch (IOException e) {
            logger.error("Failed to save world manifest", e);
        }

        CoreRegistry.get(AudioManager.class).stopAllSounds();

        chunkStore.dispose();

        File chunkFile = new File(PathManager.getInstance().getWorldSavePath(worldProvider.getTitle()), worldProvider.getTitle() + ".chunks");
        final long time = System.currentTimeMillis();
        chunkStore.saveToFile(chunkFile);
        logger.info("It took {} ms to save chunks to file {}", (System.currentTimeMillis() - time), chunkFile);
    }

    /**
     * @return true if pregeneration is complete
     */
    public boolean pregenerateChunks() {
        boolean complete = true;
        int newChunkPosX = calcCamChunkOffsetX();
        int newChunkPosZ = calcCamChunkOffsetZ();
        int viewingDistance = config.getRendering().getActiveViewingDistance();

        chunkProvider.update();
        for (Vector3i pos : Region3i.createFromCenterExtents(new Vector3i(newChunkPosX, 0, newChunkPosZ), new Vector3i(viewingDistance / 2, 0, viewingDistance / 2))) {
            Chunk chunk = chunkProvider.getChunk(pos);
            if (chunk == null || chunk.getChunkState() != ChunkState.COMPLETE) {
                complete = false;
            } else if (chunk.isDirty()) {
                WorldView view = worldProvider.getLocalView(chunk.getPos());
                if (view == null) {
                    continue;
                }
                chunk.setDirty(false);

                ChunkMesh[] newMeshes = new ChunkMesh[VERTICAL_SEGMENTS];
                for (int seg = 0; seg < VERTICAL_SEGMENTS; seg++) {
                    newMeshes[seg] = chunkTessellator.generateMesh(view, chunk.getPos(), Chunk.SIZE_Y / VERTICAL_SEGMENTS, seg * (Chunk.SIZE_Y / VERTICAL_SEGMENTS));
                }

                chunk.setPendingMesh(newMeshes);

                if (chunk.getPendingMesh() != null) {

                    for (int j = 0; j < chunk.getPendingMesh().length; j++) {
                        chunk.getPendingMesh()[j].generateVBOs();
                    }
                    if (chunk.getMesh() != null) {
                        for (int j = 0; j < chunk.getMesh().length; j++) {
                            chunk.getMesh()[j].dispose();
                        }
                    }
                    chunk.setMesh(chunk.getPendingMesh());
                    chunk.setPendingMesh(null);
                }
                return false;
            }
        }
        return complete;
    }

    @Override
    public String toString() {
        float renderedTriangles = 0.0f;
        String renderedTrianglesUnit = "";

        if (statRenderedTriangles > 1000000.0f) {
            renderedTriangles = statRenderedTriangles / 1000000.0f;
            renderedTrianglesUnit = "mil";
        } else if (statRenderedTriangles > 1000.0f) {
            renderedTriangles = statRenderedTriangles / 1000.0f;
            renderedTrianglesUnit = "k";
        }

        return String.format("world (db: %d, b: %s, t: %.1f, exposure: %.1f"
                +" cache: %.1fMb, dirty: %d, ign: %d, vis: %d, tri: %.1f%s, empty: %d, !rdy: %d, seed: \"%s\", title: \"%s\")",

                ((MeshRenderer) CoreRegistry.get(ComponentSystemManager.class).get("engine:MeshRenderer")).lastRendered,
                getPlayerBiome(), worldProvider.getTimeInDays(),
                DefaultRenderingProcess.getInstance().getExposure(),
                chunkProvider.size(),
                statDirtyChunks,
                statIgnoredPhases,
                statVisibleChunks,
                renderedTriangles,
                renderedTrianglesUnit,
                statChunkMeshEmpty,
                statChunkNotReady,
                worldProvider.getSeed(),
                worldProvider.getTitle());
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public boolean isAABBVisible(Camera cam, AABB aabb) {
        return cam.getViewFrustum().intersects(aabb);
    }

    public boolean isAABBVisibleLight(AABB aabb) {
        return isAABBVisible(lightCamera, aabb);
    }

    public boolean isAABBVisible(AABB aabb) {
        return isAABBVisible(activeCamera, aabb);
    }

    public boolean isChunkValidForRender(Chunk c) {
        return worldProvider.getLocalView(c.getPos()) != null;
    }

    public boolean isChunkVisible(Camera cam, Chunk c) {
        return cam.getViewFrustum().intersects(c.getAABB());
    }

    public boolean isChunkVisibleReflection(Chunk c) {
        return activeCamera.getViewFrustumReflected().intersects(c.getAABB());
    }

    public boolean isChunkVisibleLight(Chunk c) {
        return isChunkVisible(lightCamera, c);
    }

    public boolean isChunkVisible(Chunk c) {
        return isChunkVisible(activeCamera, c);
    }

    public boolean isLightVisible(Vector3f positionViewSpace, LightComponent component) {
        if (component.lightType == LightComponent.LightType.DIRECTIONAL) {
            return true;
        }

        return activeCamera.getViewFrustum().intersects(positionViewSpace, component.lightAttenuationRange);
    }

    public double getDaylight() {
        return skysphere.getDaylight();
    }

    public WorldBiomeProvider.Biome getPlayerBiome() {
        Vector3f pos = getPlayerPosition();
        return worldProvider.getBiomeProvider().getBiomeAt(pos.x, pos.z);
    }

    public WorldProvider getWorldProvider() {
        return worldProvider;
    }

    public BlockGrid getBlockGrid() {
        return blockGrid;
    }

    public Skysphere getSkysphere() {
        return skysphere;
    }

    public double getTick() {
        return tick;
    }

    public List<Chunk> getChunksInProximity() {
        return chunksInProximity;
    }

    public BulletPhysics getBulletRenderer() {
        return bulletPhysics;
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }

    public Camera getLightCamera() {
        return lightCamera;
    }

    public ChunkTessellator getChunkTesselator() {
        return chunkTessellator;
    }

    public WorldRenderingStage getCurrentRenderStage() {
        return currentRenderStage;
    }
}

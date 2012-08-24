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
package org.terasology.game.modes;

import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.controllers.LocalPlayerSystem;
import org.terasology.componentSystem.controllers.MenuControlSystem;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.entityFactory.PlayerFactory;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.persistence.EntityPersisterHelper;
import org.terasology.entitySystem.persistence.EntityPersisterHelperImpl;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.events.RespawnEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.BulletPhysics;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.gui.windows.UIScreenLoading;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldBiomeProviderImpl;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.core.ChunkGeneratorManager;
import org.terasology.world.generator.core.ChunkGeneratorManagerImpl;

import javax.vecmath.Vector3f;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

/**
 * Play mode.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public class StateSinglePlayer implements GameState {

    public static final String ENTITY_DATA_FILE = "entity.dat";
    private Logger logger = Logger.getLogger(getClass().getName());

    private WorldInfo worldInfo;

    private PersistableEntityManager entityManager;
    private EventSystem eventSystem;

    /* RENDERING */
    private WorldRenderer worldRenderer;

    private ComponentSystemManager componentSystemManager;
    private LocalPlayerSystem localPlayerSys;
    private CameraTargetSystem cameraTargetSystem;
    private InputSystem inputSystem;

    /* GAME LOOP */
    private boolean pauseGame = false;

    public StateSinglePlayer(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    public void init(GameEngine engine) {
        // TODO: Change to better mod support, should be enabled via config
        ModManager modManager = new ModManager();
        for (Mod mod : modManager.getMods()) {
            mod.setEnabled(true);
        }
        modManager.saveModSelectionToConfig();
        AssetManager.getInstance().clear();
        BlockManager.getInstance().load(worldInfo.getBlockIdMap());
        cacheTextures();
        cacheBlockMesh();

        entityManager = new EntitySystemBuilder().build();
        eventSystem = CoreRegistry.get(EventSystem.class);

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        localPlayerSys = new LocalPlayerSystem();
        componentSystemManager.register(localPlayerSys, "engine:LocalPlayerSystem");
        cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");
        inputSystem = new InputSystem();
        CoreRegistry.put(InputSystem.class, inputSystem);
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        componentSystemManager.loadEngineSystems();
        componentSystemManager.loadSystems("miniions", "org.terasology.mods.miniions");

        CoreRegistry.put(WorldPersister.class, new WorldPersister(entityManager));

        loadPrefabs();
    }

    private void loadPrefabs() {
        EntityPersisterHelper persisterHelper = new EntityPersisterHelperImpl(entityManager);
        for (AssetUri prefabURI : AssetManager.list(AssetType.PREFAB)) {
            logger.info("Loading prefab " + prefabURI);
            try {
                InputStream stream = AssetManager.assetStream(prefabURI);
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
                    stream.close();
                    if (prefabData != null) {
                        persisterHelper.deserializePrefab(prefabData, prefabURI.getPackage());
                    }
                } else {
                    logger.severe("Failed to load prefab '" + prefabURI + "'");
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to load prefab '" + prefabURI + "'", e);
            }
        }
    }

    private void cacheTextures() {
        for (AssetUri textureURI : AssetManager.list(AssetType.TEXTURE)) {
            AssetManager.load(textureURI);
        }
    }

    private void cacheBlockMesh() {
        for (BlockFamily family : BlockManager.getInstance().listBlockFamilies()) {
            if (!family.getArchetypeBlock().isInvisible()) {
                family.getArchetypeBlock().getMesh();
            }
        }
    }

    @Override
    public void activate() {
        initWorld();
    }

    @Override
    public void deactivate() {
        // TODO: Shutdown background threads
        eventSystem.process();
        for (ComponentSystem system : componentSystemManager.iterateAll()) {
            system.shutdown();
        }
        GUIManager.getInstance().removeAllWindows();
        try {
            CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getWorldSavePath(CoreRegistry.get(WorldProvider.class).getTitle()), ENTITY_DATA_FILE), WorldPersister.SaveFormat.Binary);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save entities", e);
        }
        dispose();
        entityManager.clear();
    }

    @Override
    public void dispose() {
        if (worldRenderer != null) {
            worldRenderer.dispose();
            worldRenderer = null;
        }
    }

    @Override
    public void update(float delta) {
        /* GUI */
        updateUserInterface();

        eventSystem.process();

        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            PerformanceMonitor.startActivity(updater.getClass().getSimpleName());
            updater.update(delta);
        }

        if (worldRenderer != null && shouldUpdateWorld()) {
            worldRenderer.update(delta);
        }
    }

    @Override
    public void handleInput(float delta) {
        cameraTargetSystem.update();
        inputSystem.update(delta);
    }

    /**
     * Init. a new random world.
     */
    public void initWorld() {
        final FastRandom random = new FastRandom();

        // Get rid of the old world
        if (worldRenderer != null) {
            worldRenderer.dispose();
            worldRenderer = null;
        }

        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            worldInfo.setSeed(random.randomCharacterString(16));
        }

        logger.log(Level.INFO, "World seed: \"{0}\"", worldInfo.getSeed());

        // Init ChunkGeneratorManager
        ChunkGeneratorManager chunkGeneratorManager = ChunkGeneratorManagerImpl.buildChunkGenerator(Arrays.asList(worldInfo.getChunkGenerators()));
        chunkGeneratorManager.setWorldSeed(worldInfo.getSeed());
        chunkGeneratorManager.setWorldBiomeProvider(new WorldBiomeProviderImpl(worldInfo.getSeed()));

        // Init. a new world
        worldRenderer = new WorldRenderer(worldInfo, chunkGeneratorManager, entityManager, localPlayerSys);
        CoreRegistry.put(WorldRenderer.class, worldRenderer);

        // Create the world entity
        Iterator<EntityRef> worldEntityIterator = entityManager.iteratorEntities(WorldComponent.class).iterator();
        if (worldEntityIterator.hasNext()) {
            worldRenderer.getChunkProvider().setWorldEntity(worldEntityIterator.next());
        } else {
            EntityRef worldEntity = entityManager.create();
            worldEntity.addComponent(new WorldComponent());
            worldRenderer.getChunkProvider().setWorldEntity(worldEntity);
        }

        CoreRegistry.put(WorldRenderer.class, worldRenderer);
        CoreRegistry.put(WorldProvider.class, worldRenderer.getWorldProvider());
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer(EntityRef.NULL));
        CoreRegistry.put(Camera.class, worldRenderer.getActiveCamera());
        CoreRegistry.put(BulletPhysics.class, worldRenderer.getBulletRenderer());

        for (ComponentSystem system : componentSystemManager.iterateAll()) {
            system.initialise();
        }

        // TODO: Should probably not use the world title as a path?
        File entityDataFile = new File(PathManager.getInstance().getWorldSavePath(worldInfo.getTitle()), ENTITY_DATA_FILE);
        entityManager.clear();
        if (entityDataFile.exists()) {
            try {
                CoreRegistry.get(WorldPersister.class).load(entityDataFile, WorldPersister.SaveFormat.Binary);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load entity data", e);
            }
        }

        prepareWorld();
    }

    private boolean screenHasFocus() {
        return GUIManager.getInstance().getFocusedWindow() != null && GUIManager.getInstance().getFocusedWindow().isModal() && GUIManager.getInstance().getFocusedWindow().isVisible();
    }

    private boolean shouldUpdateWorld() {
        return !pauseGame;
    }

    // TODO: Should have its own state
    private void prepareWorld() {
        UIScreenLoading loadingScreen = GUIManager.getInstance().addWindow(new UIScreenLoading(), "engine:loadingScreen");
        GUIManager.getInstance().setFocusedWindow(loadingScreen);
        Display.update();

        Timer timer = CoreRegistry.get(Timer.class);
        long startTime = timer.getTimeInMs();

        Iterator<EntityRef> iterator = entityManager.iteratorEntities(LocalPlayerComponent.class).iterator();
        if (iterator.hasNext()) {
            CoreRegistry.get(LocalPlayer.class).setEntity(iterator.next());
            worldRenderer.setPlayer(CoreRegistry.get(LocalPlayer.class));
        } else {
            // Load spawn zone so player spawn location can be determined
            EntityRef spawnZoneEntity = entityManager.create();
            spawnZoneEntity.addComponent(new LocationComponent(new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2)));
            worldRenderer.getChunkProvider().addRegionEntity(spawnZoneEntity, 1);

            while (!worldRenderer.getWorldProvider().isBlockActive(new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2))) {
                loadingScreen.updateStatus(String.format("Loading spawn area... %.2f%%! :-)", (timer.getTimeInMs() - startTime) / 50.0f), (timer.getTimeInMs() - startTime) / 50.0f);

                renderUserInterface();
                updateUserInterface();
                Display.update();
            }

            Vector3i spawnPoint = new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2);
            while (worldRenderer.getWorldProvider().getBlock(spawnPoint) == BlockManager.getInstance().getAir() && spawnPoint.y > 0) {
                spawnPoint.y--;
            }

            PlayerFactory playerFactory = new PlayerFactory(entityManager);
            CoreRegistry.get(LocalPlayer.class).setEntity(playerFactory.newInstance(new Vector3f(spawnPoint.x, spawnPoint.y + 1.5f, spawnPoint.z)));
            worldRenderer.setPlayer(CoreRegistry.get(LocalPlayer.class));
            worldRenderer.getChunkProvider().removeRegionEntity(spawnZoneEntity);
            spawnZoneEntity.destroy();
        }

        while (!getWorldRenderer().pregenerateChunks() && timer.getTimeInMs() - startTime < 5000) {
            loadingScreen.updateStatus(String.format("Fast forwarding world... %.2f%%! :-)", (timer.getTimeInMs() - startTime) / 50.0f), (timer.getTimeInMs() - startTime) / 50.0f);

            renderUserInterface();
            updateUserInterface();
            Display.update();
        }


        //respawn the player if he was dead and left the game (without respawning)
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        LocalPlayerComponent localPlayerComponent = playerEntity.getComponent(LocalPlayerComponent.class);
        if (localPlayerComponent.isDead) {
            playerEntity.send(new RespawnEvent());
        }

        GUIManager.getInstance().removeWindow(loadingScreen);
        GUIManager.getInstance().setFocusedWindow(MenuControlSystem.HUD);

        // Create the first Portal if it doesn't exist yet
        worldRenderer.initPortal();
        worldRenderer.getWorldProvider().setTime(worldInfo.getTime());
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (worldRenderer != null) {
            worldRenderer.render();
        }

        /* UI */
        PerformanceMonitor.startActivity("Render and Update UI");
        renderUserInterface();
        PerformanceMonitor.endActivity();
    }

    public void renderUserInterface() {
        GUIManager.getInstance().render();
    }

    private void updateUserInterface() {
        GUIManager.getInstance().update();
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    public void pause() {
        pauseGame = true;
    }

    public void unpause() {
        pauseGame = false;
    }

    public void togglePauseGame() {
        if (pauseGame) {
            unpause();
        } else {
            pause();
        }
    }

    public boolean isGamePaused() {
        return pauseGame;
    }

}

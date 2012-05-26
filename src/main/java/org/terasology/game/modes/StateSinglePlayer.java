/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.componentSystem.controllers.LocalPlayerSystem;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entityFactory.PlayerFactory;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.persistence.EntityPersisterHelper;
import org.terasology.entitySystem.persistence.EntityPersisterHelperImpl;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.logic.newWorld.WorldProvider;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.*;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

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

    private String currentWorldName;
    private String currentWorldSeed;

    private PersistableEntityManager entityManager;

    /* GUI */
    private ArrayList<UIDisplayElement> _guiScreens = new ArrayList<UIDisplayElement>();
    private UIHeadsUpDisplay _hud;
    private UIMetrics _metrics;
    private UIPauseMenu _pauseMenu;
    private UILoadingScreen _loadingScreen;
    private UIStatusScreen _statusScreen;
    private UIInventoryScreen _inventoryScreen;

    /* RENDERING */
    private WorldRenderer _worldRenderer;

    private ComponentSystemManager componentSystemManager;
    private LocalPlayerSystem localPlayerSys;

    /* GAME LOOP */
    private boolean _pauseGame = false;

    public StateSinglePlayer(String worldName) {
        this(worldName, null);
    }

    public StateSinglePlayer(String worldName, String seed) {
        this.currentWorldName = worldName;
        this.currentWorldSeed = seed;
    }

    public void init(GameEngine engine) {
        // TODO: Change to better mod support, should be enabled via config
        ModManager modManager = new ModManager();
        for (Mod mod : modManager.getMods()) {
             mod.setEnabled(true);
        }
        modManager.saveModSelectionToConfig();
        cacheTextures();

        entityManager = new EntitySystemBuilder().build();

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        BlockEntityRegistry blockEntityRegistry = new BlockEntityRegistry();
        CoreRegistry.put(BlockEntityRegistry.class, blockEntityRegistry);
        localPlayerSys = new LocalPlayerSystem();
        componentSystemManager.register(localPlayerSys, "engine:LocalPlayerSystem");
        componentSystemManager.register(blockEntityRegistry, "engine:BlockEntityRegistry");

        componentSystemManager.loadEngineSystems();

        CoreRegistry.put(WorldPersister.class, new WorldPersister(entityManager));
        loadPrefabs();

        _hud = new UIHeadsUpDisplay();
        _hud.setVisible(true);

        _pauseMenu = new UIPauseMenu();
        _loadingScreen = new UILoadingScreen();
        _statusScreen = new UIStatusScreen();
        _inventoryScreen = new UIInventoryScreen();
        _metrics = new UIMetrics();

        _metrics.setVisible(true);

        _guiScreens.add(_metrics);
        _guiScreens.add(_hud);
        _guiScreens.add(_pauseMenu);
        _guiScreens.add(_loadingScreen);
        _guiScreens.add(_inventoryScreen);
        _guiScreens.add(_statusScreen);
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

    @Override
    public void activate() {
        initWorld(currentWorldName, currentWorldSeed);
    }

    @Override
    public void deactivate() {
        try {
            CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getWorldSavePath(CoreRegistry.get(WorldProvider.class).getTitle()), ENTITY_DATA_FILE), WorldPersister.SaveFormat.Binary);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save entities", e);
        }
        dispose();
        entityManager.clear();
    }

    @Override
    public void handleInput(float delta) {
        processKeyboardInput();
        processMouseInput();
    }

    @Override
    public void dispose() {
        if (_worldRenderer != null) {
            _worldRenderer.dispose();
            _worldRenderer = null;
        }
    }

    @Override
    public void update(float delta) {
        /* GUI */
        updateUserInterface();
        
        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            PerformanceMonitor.startActivity(updater.getClass().getSimpleName());
            updater.update(delta);
        }

        if (_worldRenderer != null && shouldUpdateWorld())
            _worldRenderer.update(delta);

        if (!screenHasFocus())
            localPlayerSys.updateInput();

            if (screenHasFocus() || !shouldUpdateWorld()) {
                if (Mouse.isGrabbed()) {
                    Mouse.setGrabbed(false);
                    Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
                }
            } else {
                if (!Mouse.isGrabbed())
                    Mouse.setGrabbed(true);
            }

        // TODO: This seems a little off - plus is more of a UI than single player game state concern. Move somewhere
        // more appropriate?
        boolean dead = true;
        for (EntityRef entity : entityManager.iteratorEntities(LocalPlayerComponent.class))
        {
            dead = entity.getComponent(LocalPlayerComponent.class).isDead;
        }
        if (dead) {
            _statusScreen.setVisible(true);
            _statusScreen.updateStatus("Sorry! Seems like you have died. :-(");
        } else {
            _statusScreen.setVisible(false);
        }
    }

    public void initWorld(String title) {
        initWorld(title, null);
    }

    /**
     * Init. a new random world.
     */
    public void initWorld(String title, String seed) {
        final FastRandom random = new FastRandom();

        // Get rid of the old world
        if (_worldRenderer != null) {
            _worldRenderer.dispose();
            _worldRenderer = null;
        }

        if (seed == null) {
            seed = random.randomCharacterString(16);
        } else if (seed.isEmpty()) {
            seed = random.randomCharacterString(16);
        }

        logger.log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Init. a new world
        _worldRenderer = new WorldRenderer(title, seed, entityManager, localPlayerSys);
        CoreRegistry.put(WorldRenderer.class, _worldRenderer);

        File entityDataFile = new File(PathManager.getInstance().getWorldSavePath(title), ENTITY_DATA_FILE);
        entityManager.clear();
        if (entityDataFile.exists()) {
            try {
                CoreRegistry.get(WorldPersister.class).load(entityDataFile, WorldPersister.SaveFormat.Binary);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load entity data", e);
            }
        }

        LocalPlayer localPlayer = null;
        Iterator<EntityRef> iterator = entityManager.iteratorEntities(LocalPlayerComponent.class).iterator();
        if (iterator.hasNext()) {
            localPlayer = new LocalPlayer(iterator.next());
        } else {
            PlayerFactory playerFactory = new PlayerFactory(entityManager);
            localPlayer = new LocalPlayer(playerFactory.newInstance(nextSpawningPoint()));
        }
        _worldRenderer.setPlayer(localPlayer);

        // Create the first Portal if it doesn't exist yet
        _worldRenderer.initPortal();

        fastForwardWorld();
        CoreRegistry.put(WorldRenderer.class, _worldRenderer);
        CoreRegistry.put(WorldProvider.class, _worldRenderer.getWorldProvider());
        CoreRegistry.put(LocalPlayer.class, _worldRenderer.getPlayer());
        CoreRegistry.put(Camera.class, _worldRenderer.getActiveCamera());
        CoreRegistry.put(BulletPhysicsRenderer.class, _worldRenderer.getBulletRenderer());

        for (ComponentSystem system : componentSystemManager.iterateAll()) {
            system.initialise();
        }


    }

    private Vector3f nextSpawningPoint() {
        return new Vector3f(0,5,0);
        // TODO: Need to generate an X/Z coord, force a chunk relevent and calculate Y
        /*
        ChunkGeneratorTerrain tGen = ((ChunkGeneratorTerrain) getGeneratorManager().getChunkGenerators().get(0));

        FastRandom nRandom = new FastRandom(CoreRegistry.get(Timer.class).getTimeInMs());

        for (; ; ) {
            int randX = (int) (nRandom.randomDouble() * 128f);
            int randZ = (int) (nRandom.randomDouble() * 128f);

            for (int y = Chunk.SIZE_Y - 1; y >= 32; y--) {

                double dens = tGen.calcDensity(randX + (int) SPAWN_ORIGIN.x, y, randZ + (int) SPAWN_ORIGIN.y);

                if (dens >= 0 && y < 64)
                    return new Vector3d(randX + SPAWN_ORIGIN.x, y, randZ + SPAWN_ORIGIN.y);
                else if (dens >= 0 && y >= 64)
                    break;
            }
        } */
    }


    private boolean screenHasFocus() {
        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay()) {
                return true;
            }
        }
        return GUIManager.getInstance().getFocusedWindow() != null;
    }

    private boolean shouldUpdateWorld() {
        return !_pauseGame && !_pauseMenu.isVisible();
    }

    // TODO: This should be its own state, really
    private void fastForwardWorld() {
        _loadingScreen.setVisible(true);
        _hud.setVisible(false);
        _metrics.setVisible(false);
        Display.update();

        int chunksGenerated = 0;

        Timer timer = CoreRegistry.get(Timer.class);
        long startTime = timer.getTimeInMs();

        while (!getWorldRenderer().pregenerateChunks() && timer.getTimeInMs() - startTime < 5000) {
            chunksGenerated++;

            _loadingScreen.updateStatus(String.format("Fast forwarding world... %.2f%%! :-)", (timer.getTimeInMs() - startTime) / 50.0f));

            renderUserInterface();
            updateUserInterface();
            Display.update();
        }

        _loadingScreen.setVisible(false);
        _hud.setVisible(true);
        _metrics.setVisible(true);
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (_worldRenderer != null) {
            _worldRenderer.render();
        }

        /* UI */
        PerformanceMonitor.startActivity("Render and Update UI");
        renderUserInterface();
        PerformanceMonitor.endActivity();
    }

    public void renderUserInterface() {
        for (UIDisplayElement screen : _guiScreens) {
            screen.render();
        }
        GUIManager.getInstance().render();
    }

    private void updateUserInterface() {
        for (UIDisplayElement screen : _guiScreens) {
            screen.update();
        }
        GUIManager.getInstance().update();
    }

    public WorldRenderer getWorldRenderer() {
        return _worldRenderer;
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    private void processKeyboardInput() {
        boolean debugEnabled = Config.getInstance().isDebug();

        boolean screenHasFocus = screenHasFocus();

        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (Keyboard.getEventKeyState()) {
                if (!Keyboard.isRepeatEvent()) {
                    if (key == Keyboard.KEY_ESCAPE) {
                        if (GUIManager.getInstance().getFocusedWindow() != null) {
                            GUIManager.getInstance().removeWindow(GUIManager.getInstance().getFocusedWindow());
                        } else {
                            togglePauseMenu();
                        }
                    }

                    //Should this be here?
                    if (key == Keyboard.KEY_I) {
                        toggleInventory();
                    }

                    if (key == Keyboard.KEY_F3) {
                        Config.getInstance().setDebug(!Config.getInstance().isDebug());
                    }

                    if (key == Keyboard.KEY_F && !screenHasFocus) {
                        toggleViewingDistance();
                    }

                    if (key == Keyboard.KEY_F12) {
                        _worldRenderer.printScreen();
                    }
                }

                // Pass input to focused GUI element
                if (GUIManager.getInstance().getFocusedWindow() != null) {
                    GUIManager.getInstance().processKeyboardInput(key);
                } else {
                    for (UIDisplayElement screen : _guiScreens) {
                        if (screenCanFocus(screen)) {
                            screen.processKeyboardInput(key);
                        }
                    }
                }

            }

            // Features for debug mode only
            if (debugEnabled && !screenHasFocus && Keyboard.getEventKeyState()) {
                WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
                if (key == Keyboard.KEY_UP) {
                    worldProvider.setTimeInDays(worldProvider.getTimeInDays() + 0.005f);
                }

                if (key == Keyboard.KEY_DOWN) {
                    worldProvider.setTimeInDays(worldProvider.getTimeInDays() - 0.005f);
                }

                if (key == Keyboard.KEY_RIGHT) {
                    worldProvider.setTimeInDays(worldProvider.getTimeInDays() + 0.02f);
                }

                if (key == Keyboard.KEY_LEFT) {
                    worldProvider.setTimeInDays(worldProvider.getTimeInDays() - 0.02f);
                }

                if (key == Keyboard.KEY_R && !Keyboard.isRepeatEvent()) {
                    getWorldRenderer().setWireframe(!getWorldRenderer().isWireframe());
                }

                if (key == Keyboard.KEY_P && !Keyboard.isRepeatEvent()) {
                    getWorldRenderer().setCameraMode(WorldRenderer.CAMERA_MODE.PLAYER);
            }

                if (key == Keyboard.KEY_O && !Keyboard.isRepeatEvent()) {
                    getWorldRenderer().setCameraMode(WorldRenderer.CAMERA_MODE.SPAWN);
                }
            }

            // Pass input to the current player
            if (!screenHasFocus)
                localPlayerSys.processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
        }
    }


    /*
    * Process mouse input - nothing system-y, so just passing it to the Player class
    */
    private void processMouseInput() {
        boolean screenHasFocus = screenHasFocus();
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();

            if (GUIManager.getInstance().getFocusedWindow() != null) {
                GUIManager.getInstance().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
            } else {
                for (UIDisplayElement screen : _guiScreens) {
                    if (screenCanFocus(screen)) {
                        screen.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
                    }
                }
            }

            if (!screenHasFocus)
                localPlayerSys.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
        }
    }


    private boolean screenCanFocus(UIDisplayElement s) {
        boolean result = true;

        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay() && screen != s)
                result = false;
        }

        return result;
    }

    public void pause() {
        _pauseGame = true;
    }

    public void unpause() {
        _pauseGame = false;
    }

    public void togglePauseGame() {
        if (_pauseGame) {
            unpause();
        } else {
            pause();
        }
    }

    private void toggleInventory() {
        if (screenCanFocus(_inventoryScreen))
            _inventoryScreen.setVisible(!_inventoryScreen.isVisible());
    }

    public void togglePauseMenu() {
        if (screenCanFocus(_pauseMenu)) {
            _pauseMenu.setVisible(!_pauseMenu.isVisible());
        }
    }

    public void toggleViewingDistance() {
        Config.getInstance().setViewingDistanceById((Config.getInstance().getActiveViewingDistanceId() + 1) % 4);
    }

    public boolean isGamePaused() {
        return _pauseGame;
    }

    public UIHeadsUpDisplay getHud()
    {
        return _hud;
    }

}

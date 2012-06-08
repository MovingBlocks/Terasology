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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.componentSystem.BlockParticleEmitterSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.action.*;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.componentSystem.block.BlockEntitySystem;
import org.terasology.componentSystem.characters.CharacterMovementSystem;
import org.terasology.componentSystem.characters.CharacterSoundSystem;
import org.terasology.componentSystem.common.HealthSystem;
import org.terasology.componentSystem.common.StatusAffectorSystem;
import org.terasology.componentSystem.controllers.*;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.componentSystem.items.ItemSystem;
import org.terasology.componentSystem.rendering.BlockDamageRenderer;
import org.terasology.componentSystem.rendering.FirstPersonRenderer;
import org.terasology.componentSystem.rendering.MeshRenderer;
import org.terasology.components.*;
import org.terasology.components.actions.*;
import org.terasology.entityFactory.PlayerFactory;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.metadata.extension.*;
import org.terasology.entitySystem.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.persistence.EntityPersisterHelper;
import org.terasology.entitySystem.persistence.EntityPersisterHelperImpl;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoEventSystem;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.events.input.*;
import org.terasology.events.input.binds.InventoryButton;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.shapes.BlockShapeManager;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;
import org.terasology.mods.miniions.components.SimpleMinionAIComponent;
import org.terasology.mods.miniions.componentsystem.controllers.MinionSystem;
import org.terasology.mods.miniions.componentsystem.controllers.SimpleMinionAISystem;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.gui.menus.*;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.*;
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
    private Logger _logger = Logger.getLogger(getClass().getName());

    private String currentWorldName;
    private String currentWorldSeed;

    /* RENDERING */
    private WorldRenderer _worldRenderer;

    private ComponentLibrary componentLibrary;
    private EntityManager _entityManager;
    private ComponentSystemManager _componentSystemManager;
    private LocalPlayerSystem _localPlayerSys;
    private CameraTargetSystem _cameraTargetSystem;
    private InputSystem _inputSystem;

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
        ModManager modManager = new ModManager();
        for (Mod mod : modManager.getMods()) {
             mod.setEnabled(true);
        }
        modManager.saveModSelectionToConfig();
        cacheTextures();
        BlockShapeManager.getInstance().reload();

        componentLibrary = new ComponentLibraryImpl();
        CoreRegistry.put(ComponentLibrary.class, componentLibrary);

        componentLibrary.registerTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler());
        componentLibrary.registerTypeHandler(Color4f.class, new Color4fTypeHandler());
        componentLibrary.registerTypeHandler(Quat4f.class, new Quat4fTypeHandler());
        componentLibrary.registerTypeHandler(Mesh.class, new AssetTypeHandler(AssetType.MESH, Mesh.class));
        componentLibrary.registerTypeHandler(Sound.class, new AssetTypeHandler(AssetType.SOUND, Sound.class));
        componentLibrary.registerTypeHandler(Material.class, new AssetTypeHandler(AssetType.MATERIAL, Material.class));
        componentLibrary.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        componentLibrary.registerTypeHandler(Vector2f.class, new Vector2fTypeHandler());
        componentLibrary.registerTypeHandler(Vector3i.class, new Vector3iTypeHandler());

        PrefabManager prefabManager = new PojoPrefabManager(componentLibrary);
        CoreRegistry.put(PrefabManager.class, prefabManager);

        _entityManager = new PojoEntityManager(componentLibrary, prefabManager);
        EventSystem eventSystem = new PojoEventSystem(_entityManager);
        _entityManager.setEventSystem(eventSystem);
        CoreRegistry.put(EntityManager.class, _entityManager);
        CoreRegistry.put(EventSystem.class, eventSystem);
        _componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, _componentSystemManager);

        CoreRegistry.put(WorldPersister.class, new WorldPersister(componentLibrary,_entityManager));


        // TODO: Use reflection pending mod support
        eventSystem.registerEvent("engine:inputEvent", InputEvent.class);
        eventSystem.registerEvent("engine:keyDownEvent", KeyDownEvent.class);
        eventSystem.registerEvent("engine:keyEvent", KeyEvent.class);
        eventSystem.registerEvent("engine:keyUpEvent", KeyUpEvent.class);
        eventSystem.registerEvent("engine:keyRepeatEvent", KeyRepeatEvent.class);
        eventSystem.registerEvent("engine:leftMouseDownButtonEvent", LeftMouseDownButtonEvent.class);
        eventSystem.registerEvent("engine:leftMouseUpButtonEvent", LeftMouseUpButtonEvent.class);
        eventSystem.registerEvent("engine:mouseDownButtonEvent", MouseDownButtonEvent.class);
        eventSystem.registerEvent("engine:mouseUpButtonEvent", MouseUpButtonEvent.class);
        eventSystem.registerEvent("engine:mouseButtonEvent", MouseButtonEvent.class);
        eventSystem.registerEvent("engine:mouseWheelEvent", MouseWheelEvent.class);
        eventSystem.registerEvent("engine:rightMouseDownButtonEvent", RightMouseDownButtonEvent.class);
        eventSystem.registerEvent("engine:rightMouseUpButtonEvent", RightMouseUpButtonEvent.class);
        eventSystem.registerEvent("engine:bindButtonEvent", BindButtonEvent.class);
        eventSystem.registerEvent("engine:inventoryButtonEvent", InventoryButton.class);

        // TODO: Use reflection pending mod support
        componentLibrary.registerComponentClass(ExplosionActionComponent.class);
        componentLibrary.registerComponentClass(PlaySoundActionComponent.class);
        componentLibrary.registerComponentClass(TunnelActionComponent.class);
        componentLibrary.registerComponentClass(AABBCollisionComponent.class);
        componentLibrary.registerComponentClass(BlockComponent.class);
        componentLibrary.registerComponentClass(BlockItemComponent.class);
        componentLibrary.registerComponentClass(BlockParticleEffectComponent.class);
        componentLibrary.registerComponentClass(CameraComponent.class);
        componentLibrary.registerComponentClass(CharacterMovementComponent.class);
        componentLibrary.registerComponentClass(CharacterSoundComponent.class);
        componentLibrary.registerComponentClass(HealthComponent.class);
        componentLibrary.registerComponentClass(InventoryComponent.class);
        componentLibrary.registerComponentClass(ItemComponent.class);
        componentLibrary.registerComponentClass(LightComponent.class);
        componentLibrary.registerComponentClass(LocalPlayerComponent.class);
        componentLibrary.registerComponentClass(LocationComponent.class);
        componentLibrary.registerComponentClass(MeshComponent.class);
        componentLibrary.registerComponentClass(PlayerComponent.class);
        componentLibrary.registerComponentClass(SimpleAIComponent.class);
        componentLibrary.registerComponentClass(SimpleMinionAIComponent.class);
        componentLibrary.registerComponentClass(MinionBarComponent.class);
        componentLibrary.registerComponentClass(MinionComponent.class);
        componentLibrary.registerComponentClass(AccessInventoryActionComponent.class);
        componentLibrary.registerComponentClass(SpawnPrefabActionComponent.class);
        componentLibrary.registerComponentClass(BookComponent.class);
        componentLibrary.registerComponentClass(BookshelfComponent.class);
        componentLibrary.registerComponentClass(PotionComponent.class);
        componentLibrary.registerComponentClass(SpeedBoostComponent.class);
        componentLibrary.registerComponentClass(PoisonedComponent.class);
        componentLibrary.registerComponentClass(MinionControllerComponent.class);
        loadPrefabs();

        BlockEntityRegistry blockEntityRegistry = new BlockEntityRegistry();

        _cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class,_cameraTargetSystem);
        _componentSystemManager.register(_cameraTargetSystem, "engine:CameraTargetSystem");
        _inputSystem = new InputSystem();
        CoreRegistry.put(InputSystem.class, _inputSystem);
        _componentSystemManager.register(_inputSystem, "engine:InputSystem");
        _componentSystemManager.register(blockEntityRegistry, "engine:BlockEntityRegistry");
        CoreRegistry.put(BlockEntityRegistry.class, blockEntityRegistry);
        _componentSystemManager.register(new CharacterMovementSystem(), "engine:CharacterMovementSystem");
        _componentSystemManager.register(new SimpleAISystem(), "engine:SimpleAISystem");
        _componentSystemManager.register(new SimpleMinionAISystem(), "engine:SimpleMinionAISystem");
        _componentSystemManager.register(new ItemSystem(), "engine:ItemSystem");
        _componentSystemManager.register(new CharacterSoundSystem(), "engine:CharacterSoundSystem");
        _localPlayerSys = new LocalPlayerSystem();
        _componentSystemManager.register(_localPlayerSys, "engine:LocalPlayerSystem");
        _componentSystemManager.register(new FirstPersonRenderer(), "engine:FirstPersonRenderer");
        _componentSystemManager.register(new HealthSystem(), "engine:HealthSystem");
        _componentSystemManager.register(new BlockEntitySystem(), "engine:BlockEntitySystem");
        _componentSystemManager.register(new BlockParticleEmitterSystem(), "engine:BlockParticleSystem");
        _componentSystemManager.register(new BlockDamageRenderer(), "engine:BlockDamageRenderer");
        _componentSystemManager.register(new InventorySystem(), "engine:InventorySystem");
        _componentSystemManager.register(new MeshRenderer(), "engine:MeshRenderer");
        _componentSystemManager.register(new ExplosionAction(), "engine:ExplosionAction");
        _componentSystemManager.register(new PlaySoundAction(), "engine:PlaySoundAction");
        _componentSystemManager.register(new TunnelAction(), "engine:TunnelAction");
        _componentSystemManager.register(new AccessInventoryAction(), "engine:AccessInventoryAction");
        _componentSystemManager.register(new SpawnPrefabAction(), "engine:SpawnPrefabAction");
        _componentSystemManager.register(new ReadBookAction(), "engine:ReadBookAction");
        _componentSystemManager.register(new BookshelfHandler(), "engine:BookshelfHandler");
        _componentSystemManager.register(new DrinkPotionAction(), "engine:DrinkPotionAction");
        _componentSystemManager.register(new StatusAffectorSystem(), "engine:StatusAffectorSystem");
        _componentSystemManager.register(new MenuControlSystem(), "engine:MenuControlSystem");
        _componentSystemManager.register(new DebugControlSystem(), "engine:DebugControlSystem");
        _componentSystemManager.register(new MinionSystem(), "miniion:MinionSystem");
    }

    private void loadPrefabs() {
        EntityPersisterHelper persisterHelper = new EntityPersisterHelperImpl(componentLibrary, (PersistableEntityManager)_entityManager);
        for (AssetUri prefabURI : AssetManager.list(AssetType.PREFAB)) {
            _logger.info("Loading prefab " + prefabURI);
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
                    _logger.severe("Failed to load prefab '" + prefabURI + "'");
                }
            } catch (IOException e) {
                _logger.log(Level.WARNING, "Failed to load prefab '" + prefabURI + "'", e);
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
        GUIManager.getInstance().closeWindows();
        try {
            CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getWorldSavePath(getActiveWorldProvider().getTitle()), ENTITY_DATA_FILE), WorldPersister.SaveFormat.Binary);
        } catch (IOException e) {
            _logger.log(Level.SEVERE, "Failed to save entities", e);
        }
        dispose();
        _entityManager.clear();
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
        
        for (UpdateSubscriberSystem updater : _componentSystemManager.iterateUpdateSubscribers()) {
            PerformanceMonitor.startActivity(updater.getClass().getSimpleName());
            updater.update(delta);
        }

        if (_worldRenderer != null && shouldUpdateWorld()) {
            _worldRenderer.update(delta);
        }

        /* TODO: This seems a little off - plus is more of a UI than single player game state concern. Move somewhere
           more appropriate? Possibly HUD? */
        boolean dead = true;
        for (EntityRef entity : _entityManager.iteratorEntities(LocalPlayerComponent.class))
        {
            dead = entity.getComponent(LocalPlayerComponent.class).isDead;
        }
        if (dead) {
            if (GUIManager.getInstance().getWindowById("engine:statusScreen") == null) {
                UIStatusScreen statusScreen = GUIManager.getInstance().addWindow(new UIStatusScreen(), "engine:statusScreen");
                statusScreen.updateStatus("Sorry! Seems like you have died :-(");
                statusScreen.setVisible(true);
            }
        } else {
            GUIManager.getInstance().removeWindow("engine:statusScreen");
        }
    }

    @Override
    public void handleInput(float delta) {
        _cameraTargetSystem.update();
        _inputSystem.update(delta);

        // TODO: This should be handled outside of the state, need to fix the screens handling
        if (screenHasFocus() || !shouldUpdateWorld()) {
            if (Mouse.isGrabbed()) {
                Mouse.setGrabbed(false);
                Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            }
        } else {
            if (!Mouse.isGrabbed()) {
                Mouse.setGrabbed(true);
            }
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

        _logger.log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Init. a new world
        _worldRenderer = new WorldRenderer(title, seed, _entityManager, _localPlayerSys);
        CoreRegistry.put(WorldRenderer.class, _worldRenderer);

        File entityDataFile = new File(PathManager.getInstance().getWorldSavePath(title), ENTITY_DATA_FILE);
        _entityManager.clear();
        if (entityDataFile.exists()) {
            try {
                CoreRegistry.get(WorldPersister.class).load(entityDataFile, WorldPersister.SaveFormat.Binary);
            } catch (IOException e) {
                _logger.log(Level.SEVERE, "Failed to load entity data", e);
            }
        }

        LocalPlayer localPlayer = null;
        Iterator<EntityRef> iterator = _entityManager.iteratorEntities(LocalPlayerComponent.class).iterator();
        if (iterator.hasNext()) {
            localPlayer = new LocalPlayer(iterator.next());
        } else {
            PlayerFactory playerFactory = new PlayerFactory(_entityManager);
            localPlayer = new LocalPlayer(playerFactory.newInstance(new Vector3f(_worldRenderer.getWorldProvider().nextSpawningPoint())));
        }
        _worldRenderer.setPlayer(localPlayer);

        // Create the first Portal if it doesn't exist yet
        _worldRenderer.initPortal();

        fastForwardWorld();
        CoreRegistry.put(WorldRenderer.class, _worldRenderer);
        CoreRegistry.put(IWorldProvider.class, _worldRenderer.getWorldProvider());
        CoreRegistry.put(LocalPlayer.class, _worldRenderer.getPlayer());
        CoreRegistry.put(Camera.class, _worldRenderer.getActiveCamera());
        CoreRegistry.put(BulletPhysicsRenderer.class, _worldRenderer.getBulletRenderer());

        for (ComponentSystem system : _componentSystemManager.iterateAll()) {
            system.initialise();
        }


    }

    private boolean screenHasFocus() {
        return GUIManager.getInstance().getFocusedWindow() != null && GUIManager.getInstance().getFocusedWindow().isModal() && GUIManager.getInstance().getFocusedWindow().isVisible();
    }

    private boolean shouldUpdateWorld() {
        return !_pauseGame;
    }

    private void fastForwardWorld() {
        UILoadingScreen loadingScreen = GUIManager.getInstance().addWindow(new UILoadingScreen(), "engine:loadingScreen");
        Display.update();

        int chunksGenerated = 0;

        while (chunksGenerated < 64) {
            getWorldRenderer().generateChunk();
            chunksGenerated++;

            loadingScreen.updateStatus(String.format("Fast forwarding world... %.2f%%! :-)", (chunksGenerated / 64f) * 100f));

            renderUserInterface();
            updateUserInterface();
            Display.update();
        }

        GUIManager.getInstance().removeWindow(loadingScreen);

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
        GUIManager.getInstance().render();
    }

    private void updateUserInterface() {
        GUIManager.getInstance().update();
    }

    public WorldRenderer getWorldRenderer() {
        return _worldRenderer;
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

    public boolean isGamePaused() {
        return _pauseGame;
    }

    public IWorldProvider getActiveWorldProvider() {
        return _worldRenderer.getWorldProvider();
    }
}

/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.engine;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.subsystem.common.CommandSubsystem;
import org.terasology.engine.subsystem.common.ConfigurationSubsystem;
import org.terasology.engine.subsystem.common.GameSubsystem;
import org.terasology.engine.subsystem.common.MonitoringSubsystem;
import org.terasology.engine.subsystem.common.NetworkSubsystem;
import org.terasology.engine.subsystem.common.PhysicsSubsystem;
import org.terasology.engine.subsystem.common.ThreadManagerSubsystem;
import org.terasology.engine.subsystem.common.TimeSubsystem;
import org.terasology.engine.subsystem.common.WorldGenerationSubsystem;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.i18n.I18nSubsystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.monitoring.Activity;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinData;
import org.terasology.version.TerasologyVersion;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeData;
import org.terasology.world.block.shapes.BlockShapeImpl;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.block.sounds.BlockSoundsData;
import org.terasology.world.block.tiles.BlockTile;
import org.terasology.world.block.tiles.TileData;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * This GameEngine implementation is the heart of Terasology.
 * </p>
 * <p>
 * It first takes care of making a number of application-wide initializations (see init()
 * method). It then provides a main game loop (see run() method) characterized by a number
 * of mutually exclusive {@link GameState}s. The current GameState is updated each
 * frame, and a change of state (see changeState() method) can be requested at any time - the
 * switch will occur cleanly between frames. Interested parties can be notified of GameState
 * changes by using the subscribeToStateChange() method.
 * </p>
 * <p>
 * At this stage the engine also provides a number of utility methods (see submitTask() and
 * hasMouseFocus() to name a few) but they might be moved elsewhere.
 * </p>
 * <p>
 * Special mention must be made in regard to EngineSubsystems. An {@link EngineSubsystem}
 * is a pluggable low-level component of the engine, that is processed every frame - like
 * rendering or audio. A list of EngineSubsystems is provided in input to the engine's
 * constructor. Different sets of Subsystems can significantly change the behaviour of
 * the engine, i.e. providing a "no-frills" server in one case or a full-graphics client
 * in another.
 * </p>
 */
public class TerasologyEngine implements GameEngine {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyEngine.class);

    private static final int ONE_MEBIBYTE = 1024 * 1024;

    private GameState currentState;
    private GameState pendingState;
    private Set<StateChangeSubscriber> stateChangeSubscribers = Sets.newLinkedHashSet();

    private EngineStatus status = StandardGameStatus.UNSTARTED;
    private final List<EngineStatusSubscriber> statusSubscriberList = new CopyOnWriteArrayList<>();

    private volatile boolean shutdownRequested;
    private volatile boolean running;

    private TimeSubsystem timeSubsystem;
    private Deque<EngineSubsystem> allSubsystems;
    private ModuleAwareAssetTypeManager assetTypeManager;

    /**
     * Contains objects that life for the duration of this engine.
     */
    private Context rootContext;

    /**
     * This constructor initializes the engine by initializing its systems,
     * subsystems and managers. It also verifies that some required systems
     * are up and running after they have been initialized.
     *
     * @param subsystems Typical subsystems lists contain graphics, timer,
     *                   audio and input subsystems.
     */
    public TerasologyEngine(TimeSubsystem timeSubsystem, Collection<EngineSubsystem> subsystems) {

        this.rootContext = new ContextImpl();
        this.timeSubsystem = timeSubsystem;
        /*
         * We can't load the engine without core registry yet.
         * e.g. the statically created MaterialLoader needs the CoreRegistry to get the AssetManager.
         * And the engine loads assets while it gets created.
         */
        // TODO: Remove
        CoreRegistry.setContext(rootContext);

        this.allSubsystems = Queues.newArrayDeque();
        this.allSubsystems.add(new ConfigurationSubsystem());
        this.allSubsystems.add(timeSubsystem);
        this.allSubsystems.addAll(subsystems);
        this.allSubsystems.add(new ThreadManagerSubsystem());
        this.allSubsystems.add(new MonitoringSubsystem());
        this.allSubsystems.add(new PhysicsSubsystem());
        this.allSubsystems.add(new CommandSubsystem());
        this.allSubsystems.add(new NetworkSubsystem());
        this.allSubsystems.add(new WorldGenerationSubsystem());
        this.allSubsystems.add(new GameSubsystem());
        this.allSubsystems.add(new I18nSubsystem());
    }

    private void initialize() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Stopwatch totalInitTime = Stopwatch.createStarted();
        try {
            logger.info("Initializing Terasology...");
            logEnvironmentInfo();

            // TODO: Need to get everything thread safe and get rid of the concept of "GameThread" as much as possible.
            GameThread.setToCurrentThread();

            preInitSubsystems();

            initManagers();

            initSubsystems();

            changeStatus(TerasologyEngineStatus.INITIALIZING_ASSET_MANAGEMENT);
            initAssets();

            EnvironmentSwitchHandler environmentSwitcher = new EnvironmentSwitchHandler();
            rootContext.put(EnvironmentSwitchHandler.class, environmentSwitcher);

            environmentSwitcher.handleSwitchToGameEnvironment(rootContext);

            postInitSubsystems();

            verifyInitialisation();

            /**
             * Prevent objects being put in engine context after init phase. Engine states should use/create a
             * child context.
             */
            CoreRegistry.setContext(null);
        } catch (RuntimeException e) {
            logger.error("Failed to initialise Terasology", e);
            cleanup();
            throw e;
        }


        double seconds = 0.001 * totalInitTime.elapsed(TimeUnit.MILLISECONDS);
        logger.info("Initialization completed in {}sec.", String.format("%.2f", seconds));
    }

    private void verifyInitialisation() {
        verifyRequiredSystemIsRegistered(Time.class);
        verifyRequiredSystemIsRegistered(DisplayDevice.class);
        verifyRequiredSystemIsRegistered(RenderingSubsystemFactory.class);
        verifyRequiredSystemIsRegistered(InputSystem.class);
    }

    /**
     * Logs software, environment and hardware information.
     */
    private void logEnvironmentInfo() {
        logger.info(TerasologyVersion.getInstance().toString());
        logger.info("Home path: {}", PathManager.getInstance().getHomePath());
        logger.info("Install path: {}", PathManager.getInstance().getInstallPath());
        logger.info("Java: {} in {}", System.getProperty("java.version"), System.getProperty("java.home"));
        logger.info("Java VM: {}, version: {}", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"));
        logger.info("OS: {}, arch: {}, version: {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        logger.info("Max. Memory: {} MiB", Runtime.getRuntime().maxMemory() / ONE_MEBIBYTE);
        logger.info("Processors: {}", Runtime.getRuntime().availableProcessors());
    }

    /**
     * Gives a chance to subsystems to do something BEFORE managers and Time are initialized.
     */
    private void preInitSubsystems() {
        changeStatus(TerasologyEngineStatus.PREPARING_SUBSYSTEMS);
        for (EngineSubsystem subsystem : getSubsystems()) {
            changeStatus(() -> "Pre-initialising " + subsystem.getName() + " subsystem");
            subsystem.preInitialise(rootContext);
        }
    }

    private void initSubsystems() {
        changeStatus(TerasologyEngineStatus.INITIALIZING_SUBSYSTEMS);
        for (EngineSubsystem subsystem : getSubsystems()) {
            changeStatus(() -> "Initialising " + subsystem.getName() + " subsystem");
            subsystem.initialise(this, rootContext);
        }
    }

    /**
     * Gives a chance to subsystems to do something AFTER managers and Time are initialized.
     */
    private void postInitSubsystems() {
        for (EngineSubsystem subsystem : getSubsystems()) {
            subsystem.postInitialise(rootContext);
        }
    }

    /**
     * Verifies that a required class is available through the core registry.
     *
     * @param clazz The required type, i.e. Time.class
     * @throws IllegalStateException Details the required system that has not been registered.
     */
    private void verifyRequiredSystemIsRegistered(Class<?> clazz) {
        if (rootContext.get(clazz) == null) {
            throw new IllegalStateException(clazz.getSimpleName() + " not registered as a core system.");
        }
    }

    private void initManagers() {

        changeStatus(TerasologyEngineStatus.INITIALIZING_MODULE_MANAGER);
        ModuleManager moduleManager = new ModuleManagerImpl();
        rootContext.put(ModuleManager.class, moduleManager);

        changeStatus(TerasologyEngineStatus.INITIALIZING_LOWLEVEL_OBJECT_MANIPULATION);
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        rootContext.put(ReflectFactory.class, reflectFactory);

        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        rootContext.put(CopyStrategyLibrary.class, copyStrategyLibrary);
        rootContext.put(TypeSerializationLibrary.class, new TypeSerializationLibrary(reflectFactory,
                copyStrategyLibrary));

        changeStatus(TerasologyEngineStatus.INITIALIZING_ASSET_TYPES);
        assetTypeManager = new ModuleAwareAssetTypeManager();
        rootContext.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        rootContext.put(AssetManager.class, assetTypeManager.getAssetManager());
    }

    private void initAssets() {
        DefaultBlockFamilyFactoryRegistry familyFactoryRegistry = new DefaultBlockFamilyFactoryRegistry();
        rootContext.put(BlockFamilyFactoryRegistry.class, familyFactoryRegistry);

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.registerCoreAssetType(Prefab.class,
                (AssetFactory<Prefab, PrefabData>) PojoPrefab::new, false, "prefabs");
        assetTypeManager.registerCoreAssetType(BlockShape.class,
                (AssetFactory<BlockShape, BlockShapeData>) BlockShapeImpl::new, "shapes");
        assetTypeManager.registerCoreAssetType(BlockSounds.class,
                (AssetFactory<BlockSounds, BlockSoundsData>) BlockSounds::new, "blockSounds");
        assetTypeManager.registerCoreAssetType(BlockTile.class,
                (AssetFactory<BlockTile, TileData>) BlockTile::new, "blockTiles");
        assetTypeManager.registerCoreAssetType(BlockFamilyDefinition.class,
                (AssetFactory<BlockFamilyDefinition, BlockFamilyDefinitionData>) BlockFamilyDefinition::new, "blocks");
        assetTypeManager.registerCoreFormat(BlockFamilyDefinition.class,
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager(), familyFactoryRegistry));
        assetTypeManager.registerCoreAssetType(UISkin.class,
                (AssetFactory<UISkin, UISkinData>) UISkin::new, "skins");
        assetTypeManager.registerCoreAssetType(BehaviorTree.class,
                (AssetFactory<BehaviorTree, BehaviorTreeData>) BehaviorTree::new, false, "behaviors");
        assetTypeManager.registerCoreAssetType(UIElement.class,
                (AssetFactory<UIElement, UIData>) UIElement::new, "ui");

        for (EngineSubsystem subsystem : allSubsystems) {
            subsystem.registerCoreAssetTypes(assetTypeManager);
        }
    }

    @Override
    public EngineStatus getStatus() {
        return status;
    }

    @Override
    public void subscribe(EngineStatusSubscriber subscriber) {
        statusSubscriberList.add(subscriber);
    }

    @Override
    public void unsubscribe(EngineStatusSubscriber subscriber) {
        statusSubscriberList.remove(subscriber);
    }

    private void changeStatus(EngineStatus newStatus) {
        status = newStatus;
        for (EngineStatusSubscriber subscriber : statusSubscriberList) {
            subscriber.onEngineStatusChanged(newStatus);
        }
    }

    /**
     * Runs the engine, including its main loop. This method is called only once per
     * application startup, which is the reason the GameState provided is the -initial-
     * state rather than a generic game state.
     *
     * @param initialState In at least one context (the PC facade) the GameState
     *                     implementation provided as input may vary, depending if
     *                     the application has or hasn't been started headless.
     */
    @Override
    public synchronized void run(GameState initialState) {
        Preconditions.checkState(!running);
        running = true;
        initialize();
        changeStatus(StandardGameStatus.RUNNING);

        try {
            rootContext.put(GameEngine.class, this);
            changeState(initialState);

            mainLoop(); // -THE- MAIN LOOP. Most of the application time and resources are spent here.
        } catch (Throwable e) {
            logger.error("Uncaught exception, attempting clean game shutdown", e);
            throw e;
        } finally {
            try {
                cleanup();
            } catch (RuntimeException t) {
                logger.error("Clean game shutdown after an uncaught exception failed", t);
            }
            running = false;
            shutdownRequested = false;
            changeStatus(StandardGameStatus.UNSTARTED);
        }
    }

    /**
     * The main loop runs until the EngineState is set back to INITIALIZED by shutdown()
     * or until the OS requests the application's window to be closed. Engine cleanup
     * and disposal occur afterwards.
     */
    private void mainLoop() {
        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        while (!shutdownRequested) {
            assetTypeManager.reloadChangedOnDisk();

            processPendingState();

            if (currentState == null) {
                shutdown();
                break;
            }

            Iterator<Float> updateCycles = timeSubsystem.getEngineTime().tick();

            for (EngineSubsystem subsystem : allSubsystems) {
                try (Activity ignored = PerformanceMonitor.startActivity(subsystem.getName() + " PreUpdate")) {
                    subsystem.preUpdate(currentState, timeSubsystem.getEngineTime().getRealDelta());
                }
            }

            while (updateCycles.hasNext()) {
                float updateDelta = updateCycles.next(); // gameTime gets updated here!
                try (Activity ignored = PerformanceMonitor.startActivity("Main Update")) {
                    currentState.update(updateDelta);
                }
            }

            // Waiting processes are set by modules via GameThread.a/synch() methods.
            GameThread.processWaitingProcesses();

            for (EngineSubsystem subsystem : getSubsystems()) {
                try (Activity ignored = PerformanceMonitor.startActivity(subsystem.getName() + " Subsystem postUpdate")) {
                    subsystem.postUpdate(currentState, timeSubsystem.getEngineTime().getRealDelta());
                }
            }
            assetTypeManager.disposedUnusedAssets();

            PerformanceMonitor.rollCycle();
            PerformanceMonitor.startActivity("Other");
        }
        PerformanceMonitor.endActivity();
    }

    private void cleanup() {
        logger.info("Shutting down Terasology...");
        changeStatus(StandardGameStatus.SHUTTING_DOWN);

        if (currentState != null) {
            currentState.dispose();
            currentState = null;
        }

        Iterator<EngineSubsystem> preshutdownIter = allSubsystems.descendingIterator();
        while (preshutdownIter.hasNext()) {
            EngineSubsystem subsystem = preshutdownIter.next();
            try {
                subsystem.preShutdown();
            } catch (RuntimeException e) {
                logger.error("Error preparing to shutdown {} subsystem", subsystem.getName(), e);
            }
        }

        Iterator<EngineSubsystem> shutdownIter = allSubsystems.descendingIterator();
        while (shutdownIter.hasNext()) {
            EngineSubsystem subsystem = shutdownIter.next();
            try {
                subsystem.shutdown();
            } catch (RuntimeException e) {
                logger.error("Error shutting down {} subsystem", subsystem.getName(), e);
            }
        }
    }

    /**
     * Causes the main loop to stop at the end of the current frame, cleanly ending
     * the current GameState, all running task threads and disposing subsystems.
     */
    @Override
    public void shutdown() {
        shutdownRequested = true;
    }

    /**
     * Changes the game state, i.e. to switch from the MainMenu to Ingame via Loading screen
     * (each is a GameState). The change can be immediate, if there is no current game
     * state set, or scheduled, when a current state exists and the new state is stored as
     * pending. That been said, scheduled changes occurs in the main loop through the call
     * processStateChanges(). As such, from a user perspective in normal circumstances,
     * scheduled changes are likely to be perceived as immediate.
     */
    @Override
    public void changeState(GameState newState) {
        if (currentState != null) {
            pendingState = newState;    // scheduled change
        } else {
            switchState(newState);      // immediate change
        }
    }

    private void processPendingState() {
        if (pendingState != null) {
            switchState(pendingState);
            pendingState = null;
        }
    }

    private void switchState(GameState newState) {
        if (currentState != null) {
            currentState.dispose();
        }
        currentState = newState;
        LoggingContext.setGameState(newState);
        newState.init(this);
        stateChangeSubscribers.forEach(StateChangeSubscriber::onStateChange);
        InputSystem inputSystem = rootContext.get(InputSystem.class);
        inputSystem.drainQueues();
    }

    @Override
    public boolean hasPendingState() {
        return pendingState != null;
    }

    @Override
    public GameState getState() {
        return currentState;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public Iterable<EngineSubsystem> getSubsystems() {
        return allSubsystems;
    }

    @Override
    public void subscribeToStateChange(StateChangeSubscriber subscriber) {
        stateChangeSubscribers.add(subscriber);
    }

    @Override
    public void unsubscribeToStateChange(StateChangeSubscriber subscriber) {
        stateChangeSubscribers.remove(subscriber);
    }

    @Override
    public Context createChildContext() {
        return new ContextImpl(rootContext);
    }

    /**
     * Allows it to obtain objects directly from the context of the game engine. It exists only for situations in
     * which no child context exists yet. If there is a child context then it automatically contains the objects of
     * the engine context. Thus normal code should just work with the (child) context that is available to it
     * instead of using this method.
     *
     * @return a object directly from the context of the game engine
     */
    public <T> T getFromEngineContext(Class<? extends T> type) {
        return rootContext.get(type);
    }
}

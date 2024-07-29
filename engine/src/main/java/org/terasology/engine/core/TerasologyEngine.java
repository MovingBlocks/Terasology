// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.core.subsystem.common.CommandSubsystem;
import org.terasology.engine.core.subsystem.common.ConfigurationSubsystem;
import org.terasology.engine.core.subsystem.common.GameSubsystem;
import org.terasology.engine.core.subsystem.common.MonitoringSubsystem;
import org.terasology.engine.core.subsystem.common.NetworkSubsystem;
import org.terasology.engine.core.subsystem.common.PhysicsSubsystem;
import org.terasology.engine.core.subsystem.common.TelemetrySubSystem;
import org.terasology.engine.core.subsystem.common.TimeSubsystem;
import org.terasology.engine.core.subsystem.common.WorldGenerationSubsystem;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.i18n.I18nSubsystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.monitoring.Activity;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.recording.CharacterStateEventPositionMap;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.gltf.ByteBufferAsset;
import org.terasology.engine.version.TerasologyVersion;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.shapes.BlockShapeImpl;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.engine.world.block.tiles.BlockTile;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.autoreload.AutoReloadAssetTypeManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * This GameEngine implementation is the heart of Terasology.
 * </p>
 * <p>
 * It first takes care of making a number of application-wide initializations (see init() method). It then provides a
 * main game loop (see run() method) characterized by a number of mutually exclusive {@link GameState}s. The current
 * GameState is updated each frame, and a change of state (see changeState() method) can be requested at any time - the
 * switch will occur cleanly between frames. Interested parties can be notified of GameState changes by using the
 * subscribeToStateChange() method.
 * </p>
 * <p>
 * At this stage the engine also provides a number of utility methods (see submitTask() and hasMouseFocus() to name a
 * few) but they might be moved elsewhere.
 * </p>
 * <p>
 * Special mention must be made in regard to EngineSubsystems. An {@link EngineSubsystem} is a pluggable low-level
 * component of the engine, that is processed every frame - like rendering or audio. A list of EngineSubsystems is
 * provided in input to the engine's constructor. Different sets of Subsystems can significantly change the behaviour of
 * the engine, i.e. providing a "no-frills" server in one case or a full-graphics client in another.
 * </p>
 */
public class TerasologyEngine implements GameEngine {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyEngine.class);

    private static final int ONE_MEBIBYTE = 1024 * 1024;

    /**
     * Subsystem classes that automatically make their classpath part of the engine module.
     * <p>
     * You don't want to add to this! If you need a module, make a module!
     */
    private static final Set<String> LEGACY_ENGINE_MODULE_POLLUTERS = Set.of(
            "org.terasology.subsystem.discordrpc.DiscordRPCSubSystem"
    );

    private final List<Class<?>> classesOnClasspathsToAddToEngine = new ArrayList<>();

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
    private boolean initialisedAlready;

    /**
     * Contains objects that live for the duration of this engine.
     */
    private Context rootContext;

    /**
     * This constructor initializes the engine by initializing its systems, subsystems and managers. It also verifies
     * that some required systems are up and running after they have been initialized.
     *
     * @param timeSubsystem the timer subsystem
     * @param subsystems other typical subsystems, e.g., graphics, audio and input subsystems.
     */
    public TerasologyEngine(TimeSubsystem timeSubsystem, Collection<EngineSubsystem> subsystems) {
        // configure native paths
        PathManager.getInstance();
        Bullet.init(true, false);

        this.rootContext = new ContextImpl();
        rootContext.put(GameEngine.class, this);
        this.timeSubsystem = timeSubsystem;

        //Record and Replay classes
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        rootContext.put(RecordAndReplayCurrentStatus.class, recordAndReplayCurrentStatus);
        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        rootContext.put(RecordAndReplayUtils.class, recordAndReplayUtils);
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        rootContext.put(CharacterStateEventPositionMap.class, characterStateEventPositionMap);
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        rootContext.put(DirectionAndOriginPosRecorderList.class, directionAndOriginPosRecorderList);
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
        this.allSubsystems.add(new MonitoringSubsystem());
        this.allSubsystems.add(new PhysicsSubsystem());
        this.allSubsystems.add(new CommandSubsystem());
        this.allSubsystems.add(new NetworkSubsystem());
        this.allSubsystems.add(new WorldGenerationSubsystem());
        this.allSubsystems.add(new GameSubsystem());
        this.allSubsystems.add(new I18nSubsystem());
        this.allSubsystems.add(new TelemetrySubSystem());

        for (EngineSubsystem subsystem : allSubsystems) {
            if (LEGACY_ENGINE_MODULE_POLLUTERS.contains(subsystem.getClass().getName())) {
                // add subsystem as engine module part. (needed for ECS classes loaded from external subsystems)
                addToClassesOnClasspathsToAddToEngine(subsystem.getClass());
            }
        }

        // the TypeHandlerLibrary is technically not a subsystem (although it lives in the subsystem space)
        // therefore, we have to manually register the type handler classes with the engine module
        addToClassesOnClasspathsToAddToEngine(TypeHandler.class);

        // register NUI classes with engine module
        addToClassesOnClasspathsToAddToEngine(UIWidget.class);
        // register gestalt asset classes with engine module
        addToClassesOnClasspathsToAddToEngine(ResourceUrn.class);
    }

    /**
     * Provide ability to set additional engine classpath locations.   This must be called before initialize() or
     * run().
     *
     * @param clazz any class that appears in the resource location to treat as an engine classpath.
     */
    protected void addToClassesOnClasspathsToAddToEngine(Class<?> clazz) {
        classesOnClasspathsToAddToEngine.add(clazz);
    }

    public void initialize() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Stopwatch totalInitTime = Stopwatch.createStarted();
        if (!initialisedAlready) {
            try {
                logger.info("Initializing Terasology...");
                logEnvironmentInfo();

                // TODO: Need to get everything thread safe and get rid of the concept of "GameThread" as much as
                //  possible.
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

                /*
                 * Prevent objects being put in engine context after init phase. Engine states should use/create a
                 * child context.
                 */
                CoreRegistry.setContext(null);
                initialisedAlready = true;
            } catch (RuntimeException e) {
                logger.error("Failed to initialise Terasology", e);
                cleanup();
                throw e;
            }
        }

        double seconds = 0.001 * totalInitTime.elapsed(TimeUnit.MILLISECONDS);
        logger.info("Initialization completed in {}sec.", String.format("%.2f", seconds)); //NOPMD
    }

    private void verifyInitialisation() {
        verifyRequiredSystemIsRegistered(Time.class);
        verifyRequiredSystemIsRegistered(DisplayDevice.class);
        verifyRequiredSystemIsRegistered(RenderingSubsystemFactory.class);
    }

    /**
     * Logs software, environment and hardware information.
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    private void logEnvironmentInfo() {
        logger.info("{}", TerasologyVersion.getInstance());
        logger.info("Home path: {}", PathManager.getInstance().getHomePath());
        logger.info("Install path: {}", PathManager.getInstance().getInstallPath());
        logger.info("Java: {} in {}", System.getProperty("java.version"), System.getProperty("java.home"));
        logger.info("Java VM: {}, version: {}", System.getProperty("java.vm.name"), System.getProperty("java.vm" +
                ".version"));
        logger.info("OS: {}, arch: {}, version: {}", System.getProperty("os.name"), System.getProperty("os.arch"),
                System.getProperty("os.version"));
        logger.info("Max. Memory: {} MiB", Runtime.getRuntime().maxMemory() / ONE_MEBIBYTE);
        logger.info("Processors: {}", Runtime.getRuntime().availableProcessors());
        if (NonNativeJVMDetector.JVM_ARCH_IS_NONNATIVE) {
            logger.warn("Running on a 32-bit JVM on a 64-bit system. This may limit performance.");
        }
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
            changeStatus(() -> "Post-Initialising " + subsystem.getName() + " subsystem");
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
        TypeRegistry.WHITELISTED_CLASSES =
                ExternalApiWhitelist.CLASSES.stream().map(Class::getName).collect(Collectors.toSet());
        TypeRegistry.WHITELISTED_PACKAGES = ExternalApiWhitelist.PACKAGES;

        ModuleManager moduleManager = new ModuleManager(rootContext.get(Config.class),
                classesOnClasspathsToAddToEngine);
        ModuleTypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());

        rootContext.put(ModuleTypeRegistry.class, typeRegistry);
        rootContext.put(TypeRegistry.class, typeRegistry);
        rootContext.put(ModuleManager.class, moduleManager);

        changeStatus(TerasologyEngineStatus.INITIALIZING_LOWLEVEL_OBJECT_MANIPULATION);
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        rootContext.put(ReflectFactory.class, reflectFactory);

        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        rootContext.put(CopyStrategyLibrary.class, copyStrategyLibrary);

        rootContext.put(TypeHandlerLibrary.class, TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager,
                typeRegistry));

        changeStatus(TerasologyEngineStatus.INITIALIZING_ASSET_TYPES);
        assetTypeManager = new AutoReloadAssetTypeManager();
        rootContext.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        rootContext.put(AssetManager.class, assetTypeManager.getAssetManager());
    }

    private void initAssets() {


        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");

        assetTypeManager.createAssetType(BlockShape.class, BlockShapeImpl::new, "shapes");
        assetTypeManager.createAssetType(BlockSounds.class, BlockSounds::new, "blockSounds");
        assetTypeManager.createAssetType(BlockTile.class, BlockTile::new, "blockTiles");

        AssetType<BlockFamilyDefinition, BlockFamilyDefinitionData> blockFamilyDefinitionAssetType =
                assetTypeManager.createAssetType(BlockFamilyDefinition.class, BlockFamilyDefinition::new, "blocks");

        assetTypeManager.getAssetFileDataProducer(blockFamilyDefinitionAssetType).addAssetFormat(
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager()));
        assetTypeManager.createAssetType(UISkinAsset.class, UISkinAsset::new, "skins");
        assetTypeManager.createAssetType(BehaviorTree.class, BehaviorTree::new, "behaviors");
        assetTypeManager.createAssetType(UIElement.class, UIElement::new, "ui");

        assetTypeManager.createAssetType(ByteBufferAsset.class, ByteBufferAsset::new, "mesh");

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
     * Runs the engine, including its main loop. This method is called only once per application startup, which is the
     * reason the GameState provided is the -initial- state rather than a generic game state.
     *
     * @param initialState In at least one context (the PC facade) the GameState implementation provided as
     *         input may vary, depending if the application has or hasn't been started headless.
     */
    @Override
    public synchronized void run(GameState initialState) {
        initializeRun(initialState);
        runMain();
    }

    @Override
    public synchronized void initializeRun(GameState initialState) {
        Preconditions.checkState(!running);
        running = true;
        changeStatus(StandardGameStatus.INITIALIZING);
        initialize();

        try {
            rootContext.put(GameEngine.class, this);
            changeState(initialState);
        } catch (Throwable e) {
            logger.error("Uncaught exception, attempting clean game shutdown", e);

            try {
                cleanup();
            } catch (RuntimeException t) {
                logger.error("Clean game shutdown after an uncaught exception failed", t);
            }
            running = false;
            shutdownRequested = false;
            changeStatus(StandardGameStatus.UNSTARTED);

            throw e;
        }
    }

    @Override
    public synchronized void runMain() {
        Preconditions.checkState(running);
        changeStatus(StandardGameStatus.RUNNING);
        try {
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
     * The main loop runs until the EngineState is set back to INITIALIZED by shutdown() or until the OS requests the
     * application's window to be closed. Engine cleanup and disposal occur afterwards.
     */
    @SuppressWarnings("checkstyle:EmptyBlock")
    private void mainLoop() {
        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        while (tick()) {
            /* do nothing */
        }
        PerformanceMonitor.endActivity();
    }

    /**
     * Runs a single "tick" of the engine
     *
     * @return true if the loop requesting a tick should continue running
     */
    public boolean tick() {
        if (shutdownRequested) {
            return false;
        }

        if (assetTypeManager instanceof AutoReloadAssetTypeManager) {
            try {
                ((AutoReloadAssetTypeManager) assetTypeManager).reloadChangedAssets();
            } catch (IllegalStateException ignore) {
                // ignore: This can happen if a module environment switch is happening in a different thread.
                return true;
            }
        }

        processPendingState();

        if (currentState == null) {
            shutdown();
            return false;
        }

        Iterator<Float> updateCycles = timeSubsystem.getEngineTime().tick();
        CoreRegistry.setContext(currentState.getContext());
        rootContext.get(NetworkSystem.class).setContext(currentState.getContext());

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
        return true;
    }

    public void cleanup() {
        logger.info("Shutting down Terasology...");
        changeStatus(StandardGameStatus.SHUTTING_DOWN);

        if (currentState != null) {
            currentState.dispose(true);
            currentState = null;
        }

        Iterator<EngineSubsystem> preshutdownIter = allSubsystems.descendingIterator();
        while (preshutdownIter.hasNext()) {
            EngineSubsystem subsystem = preshutdownIter.next();
            try {
                subsystem.preShutdown();
            } catch (RuntimeException e) {
                logger.error("Error preparing to shutdown {} subsystem", subsystem.getName(), e); //NOPMD
            }
        }

        Iterator<EngineSubsystem> shutdownIter = allSubsystems.descendingIterator();
        while (shutdownIter.hasNext()) {
            EngineSubsystem subsystem = shutdownIter.next();
            try {
                subsystem.shutdown();
            } catch (RuntimeException e) {
                logger.error("Error shutting down {} subsystem", subsystem.getName(), e); //NOPMD
            }
        }
    }

    /**
     * Causes the main loop to stop at the end of the current frame, cleanly ending the current GameState, all running
     * task threads and disposing subsystems.
     */
    @Override
    public void shutdown() {
        shutdownRequested = true;
    }

    /**
     * Changes the game state, i.e. to switch from the MainMenu to Ingame via Loading screen (each is a GameState). The
     * change can be immediate, if there is no current game state set, or scheduled, when a current state exists and the
     * new state is stored as pending. That been said, scheduled changes occurs in the main loop through the call
     * processStateChanges(). As such, from a user perspective in normal circumstances, scheduled changes are likely to
     * be perceived as immediate.
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
        CoreRegistry.setContext(newState.getContext());
        currentState = newState;
        LoggingContext.setGameState(newState);
        newState.init(this);
        stateChangeSubscribers.forEach(StateChangeSubscriber::onStateChange);
        InputSystem inputSystem = rootContext.get(InputSystem.class);
        if (inputSystem != null) {
            inputSystem.drainQueues();
        }
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
     * Allows it to obtain objects directly from the context of the game engine. It exists only for situations in which
     * no child context exists yet. If there is a child context then it automatically contains the objects of the engine
     * context. Thus normal code should just work with the (child) context that is available to it instead of using this
     * method.
     *
     * @return a object directly from the context of the game engine
     */
    public <T> T getFromEngineContext(Class<? extends T> type) {
        return rootContext.get(type);
    }
}

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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.bootstrap.ApplyModulesUtil;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.module.EngineModulePolicy;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.engine.module.ModuleSecurityManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.game.Game;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.input.InputSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.gui.AdvancedMonitor;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinData;
import org.terasology.utilities.concurrency.ShutdownTask;
import org.terasology.utilities.concurrency.Task;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.version.TerasologyVersion;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeData;
import org.terasology.world.block.shapes.BlockShapeImpl;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ReflectPermission;
import java.nio.file.Files;
import java.security.Policy;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Immortius
 *
 * This GameEngine implementation is the kernel of Terasology.
 *
 * It is normally started by a facade which provides it with a runtime context and a
 * launch profile (i.e. headless vs with-graphics). It first takes care of making
 * a number of application-wide initializations (see init() method). It then provides
 * a main game loop (see mainLoop() method) characterized by a number of mutually
 * exclusive GameStates.
 *
 * Each GameState encapsulates a different set of systems and managers being initialized
 * on state change and updated every iteration of the main loop. Existing GameState
 * implementation can be found in engine/modes and do not necessarily represent a state
 * of play. I.e. interacting with the Main Menu is handled through a GameState.
 *
 * The engine also provides methods to deal with its shutdown (see cleanup() and
 * dispose()) alongside some utility methods to submit background tasks and to subscribe
 * to GameState changes.
 *
 * Special mention must be made in regard to EngineSubsystems. They are not individually
 * specified here in the engine and are provided instead by the facade via the constructor.
 * They take care of things such as Input and Rendering and are not to be confused with
 * the systems and managers initialized by the engine or in the GameStates. They can be
 * found in engine/subsystem.
 *
 * As an overview of this class, and to jump to subsections, here is an index:
 *
 * Declarations - introducing all class-level variables
 * Constructors - only a simple one at this stage and might stay that way.
 * Initialization - the init() method on its own, given its importance
 * Initialization Support Methods - methods used by init()
 * LifeCycle and GameState methods - i.e. run(), shutdown() and changeState()
 * -THE- MAIN LOOP - Where most of the application's time and resources are spent
 * Miscellaneous Methods - i.e. submitTask(), subscribeToStateChange()
 * Getter/Setter/Checker Methods - for the variables encountered in Declarations
 *
 */
public class TerasologyEngine implements GameEngine {

    // ----------------------------------------------------------------------------------
    // DECLARATIONS
    // ----------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(TerasologyEngine.class);

    private Config config;
    private EngineTime time;

    private GameState currentState;
    private GameState pendingState;
    private Set<StateChangeSubscriber> stateChangeSubscribers = Sets.newLinkedHashSet();

    // TODO: Convert to an EngineState enum? Initialized, Running, Paused (= initialized, not running), Disposed
    private boolean initialised;
    private boolean running;
    private boolean disposed;

    // TODO: Convert magic number 16 to a readable constant - why 16?
    private final TaskMaster<Task> commonThreadPool = TaskMaster.createFIFOTaskMaster("common", 16);

    private boolean hibernationAllowed;
    private boolean gameFocused = true;

    private Deque<EngineSubsystem> subsystems;

    // ----------------------------------------------------------------------------------
    // CONSTRUCTORS - even though at this stage there's only one and might stay that way.
    // ----------------------------------------------------------------------------------
    /**
     -THE- Engine constructor. Terasology is started by a set of
     platform-specific scripts/executables called "the facade".
     For example, on PCs the file Terasology.java contains the
     main() method that constructs, initializes, runs and eventually
     dispose of this engine.

     @param subsystems  Typical subsystems lists contain graphics, timer,
                        audio and input subsystems. On PC they are currently
                        available in two flavours depending if the application
                        is run with or without graphics (aka headless, i.e.
                        to run a server).
     */
    public TerasologyEngine(Collection<EngineSubsystem> subsystems) {
        this.subsystems = Queues.newArrayDeque(subsystems);
    }

    // ----------------------------------------------------------------------------------
    // INITIALIZATION
    // ----------------------------------------------------------------------------------

    /**
     * Initializes the engine by initializing its systems, subsystems and managers.
     * Also verifies that some required systems are up and running.
     */
    @Override
    public void init() {

        if (initialised) {
            return;
        }

        Stopwatch totalInitTime = Stopwatch.createStarted();

        try {

            // logging info such as platform, version, paths, java version...
            logGenericInfo();

            // load configuration from file or create a configuration from scratch.
            initConfig();

            // subsystems have a chance to do something BEFORE managers and Time are initialized.
            subsystemsPreInitialization();

            // time must be set here as it is needed by some of the managers.
            verifyRequiredSystemIsRegistered(Time.class);
            time = (EngineTime) CoreRegistry.get(Time.class);

            // the thread currently running is set as -THE- game thread.
            // no other thread should be confused with it.
            GameThread.setGameThread();

            // initializes a number of managers and systems
            initManagers();

            // subsystems have a chance to do something AFTER managers and Time are initialized
            subsystemsPostInitialization();

            // some systems absolutely need to be up and running by now
            verifyRequiredSystemIsRegistered(DisplayDevice.class);
            verifyRequiredSystemIsRegistered(RenderingSubsystemFactory.class);
            verifyRequiredSystemIsRegistered(InputSystem.class);

            // Instantiates a number of asset factories, each for a specific asset type.
            initAssets();

            // Do we want to monitor Threads, Chunks and Performance?
            initAdvancedMonitorIfEnabled();

            initialised = true;

        } catch (RuntimeException e) {
            logger.error("Failed to initialise Terasology", e);
            throw e;
        }

        double seconds = 0.001 * totalInitTime.elapsed(TimeUnit.MILLISECONDS);
        logger.info("Initialization completed in {}sec.", String.format("%.2f", seconds));
    }

    // ----------------------------------------------------------------------------------
    // Initialization Support Methods, i.e. initConfig(), initManagers(), initAssets()...
    // ----------------------------------------------------------------------------------

    /**
     * Logs software, environment and hardware information.
     */
    private void logGenericInfo() {
        logger.info("Initializing Terasology...");
        logger.info(TerasologyVersion.getInstance().toString());
        logger.info("Home path: {}", PathManager.getInstance().getHomePath());
        logger.info("Install path: {}", PathManager.getInstance().getInstallPath());
        logger.info("Java: {} in {}", System.getProperty("java.version"), System.getProperty("java.home"));
        logger.info("Java VM: {}, version: {}", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"));
        logger.info("OS: {}, arch: {}, version: {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        logger.info("Max. Memory: {} MB", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        logger.info("Processors: {}", Runtime.getRuntime().availableProcessors());
    }

    /**
     * Gives a chance to subsystems to do something BEFORE managers and Time are initialized.
     */
    private void subsystemsPreInitialization() {
        for (EngineSubsystem subsystem : getSubsystems()) {
            subsystem.preInitialise();
        }
    }

    /**
     * Gives a chance to subsystems to do something AFTER managers and Time are initialized.
     */
    private void subsystemsPostInitialization() {
        for (EngineSubsystem subsystem : getSubsystems()) {
            subsystem.postInitialise(config);
        }
    }

    /**
     * Verifies that a required class is available through the core registry.
     *
     * @param clazz The required type, i.e. Time.class
     * @throws IllegalStateException Details the required system that has not been registered.
     */
    private void verifyRequiredSystemIsRegistered(Class clazz) {
        if (CoreRegistry.get(clazz) == null) {
            throw new IllegalStateException(clazz.getSimpleName() + " not registered as a core system.");
        }
    }

    /**
     * The Advanced Monitor is a display allowing monitoring of Threads, Chunks and Performance.
     */
    private void initAdvancedMonitorIfEnabled() {
        if (config.getSystem().isMonitoringEnabled()) {
            new AdvancedMonitor().setVisible(true);
        }
    }

    /**
     * Used by the init() method, this method loads config data from file
     * when available or creates a new config object from scratch.
     * Also registers the core module as part of the default module selection
     * and generates some security-related data.
     */
    private void initConfig() {

        if (Files.isRegularFile(Config.getConfigFile())) {
            try {
                config = Config.load(Config.getConfigFile());
            } catch (IOException e) {
                logger.error("Failed to load config", e);
                config = new Config();
            }
        } else {
            config = new Config();
        }

        if (!config.getDefaultModSelection().hasModule("core")) {
            config.getDefaultModSelection().addModule("core");
        }

        if (config.getSecurity().getServerPrivateCertificate() == null) {
            CertificateGenerator generator = new CertificateGenerator();
            CertificatePair serverIdentity = generator.generateSelfSigned();
            config.getSecurity().setServerCredentials(serverIdentity.getPublicCert(), serverIdentity.getPrivateCert());
            config.save();
        }
        logger.info("Video Settings: " + config.getRendering().toString());
        CoreRegistry.putPermanently(Config.class, config);
    }

    /**
     * Used by the init() method, this method generally creates instances
     * for a number of managers and systems and registers them with the
     * CoreRegistry.
     */
    private void initManagers() {

        // TODO: Q: Of all the managers and systems mentioned here, the next three lines are the most obscure.
        // TODO:    The words used are plain english and some terms are known in a generic java
        // TODO:    context, i.e. reflection. But how are these objects used in Terasology? What do they allow?
        // TODO:    Just a few introductory words about them would do a lot of good!
        ReflectFactory reflectFactory = CoreRegistry.putPermanently(ReflectFactory.class, new ReflectionReflectFactory());
        CopyStrategyLibrary copyStrategyLibrary = CoreRegistry.putPermanently(CopyStrategyLibrary.class, new CopyStrategyLibrary(reflectFactory));
        CoreRegistry.putPermanently(TypeSerializationLibrary.class, new TypeSerializationLibrary(reflectFactory, copyStrategyLibrary));

        // WARNING: the next line was at the beginning of the method and might be needed there!
        ModuleManager moduleManager = initModuleManager();

        CoreRegistry.putPermanently(CollisionGroupManager.class, new CollisionGroupManager());
        CoreRegistry.putPermanently(WorldGeneratorManager.class, new WorldGeneratorManager());
        CoreRegistry.putPermanently(ComponentSystemManager.class, new ComponentSystemManager());
        CoreRegistry.putPermanently(NetworkSystem.class, new NetworkSystemImpl(time));
        CoreRegistry.putPermanently(Game.class, new Game(this, time));

        // WARNING the next line was at the beginning of the try block in run()
        CoreRegistry.putPermanently(GameEngine.class, this);

        // WARNING: the next line was before the CoreRegistry statements and might be needed there!
        AssetManager assetManager = CoreRegistry.putPermanently(AssetManager.class, new AssetManager(moduleManager));
        AssetType.registerAssetTypes(assetManager);
        assetManager.addAssetSource(moduleManager.getActiveModule(TerasologyConstants.ENGINE_MODULE).getModuleSource());

        // TODO: Q: ApplyModulesUtil has only one significant method, applyModules().
        // TODO:    Why put it in a separate class instead of keeping it here?
        // TODO:    Furthermore, what does it mean to "apply" a module?
        ApplyModulesUtil.applyModules();
    }

    /**
     TODO: broad outline of what this method does?
     */
    private ModuleManager initModuleManager() {
        ModuleSecurityManager moduleSecurityManager = new ModuleSecurityManager();
        ModuleManager moduleManager = CoreRegistry.putPermanently(ModuleManager.class,
                new ModuleManagerImpl(moduleSecurityManager, config.getSystem().isReflectionsCacheEnabled()));

        // WARNING: the next two lines were at the end of the method, just before the return line.
        Policy.setPolicy(new EngineModulePolicy());
        System.setSecurityManager(moduleSecurityManager);

        // TODO: Q: It'd be interesting to know here what all these class/package additions do.
        // TODO:    Are they related to what these packages are allowed to do or to what can use these packages?
        // TODO:    I guess it isn't worth it to make a loop iterating over an array of string? Faster build times perhaps?
        moduleSecurityManager.addAPIPackage("java.lang");
        moduleSecurityManager.addAPIPackage("java.lang.ref");
        moduleSecurityManager.addAPIPackage("java.math");
        moduleSecurityManager.addAPIPackage("java.util");
        moduleSecurityManager.addAPIPackage("java.util.concurrent");
        moduleSecurityManager.addAPIPackage("java.util.concurrent.atomic");
        moduleSecurityManager.addAPIPackage("java.util.concurrent.locks");
        moduleSecurityManager.addAPIPackage("java.util.regex");
        moduleSecurityManager.addAPIPackage("java.awt");
        moduleSecurityManager.addAPIPackage("java.awt.geom");
        moduleSecurityManager.addAPIPackage("java.awt.image");
        moduleSecurityManager.addAPIPackage("com.google.common.annotations");
        moduleSecurityManager.addAPIPackage("com.google.common.cache");
        moduleSecurityManager.addAPIPackage("com.google.common.collect");
        moduleSecurityManager.addAPIPackage("com.google.common.base");
        moduleSecurityManager.addAPIPackage("com.google.common.math");
        moduleSecurityManager.addAPIPackage("com.google.common.primitives");
        moduleSecurityManager.addAPIPackage("com.google.common.util.concurrent");
        moduleSecurityManager.addAPIPackage("gnu.trove");
        moduleSecurityManager.addAPIPackage("gnu.trove.decorator");
        moduleSecurityManager.addAPIPackage("gnu.trove.function");
        moduleSecurityManager.addAPIPackage("gnu.trove.iterator");
        moduleSecurityManager.addAPIPackage("gnu.trove.iterator.hash");
        moduleSecurityManager.addAPIPackage("gnu.trove.list");
        moduleSecurityManager.addAPIPackage("gnu.trove.list.array");
        moduleSecurityManager.addAPIPackage("gnu.trove.list.linked");
        moduleSecurityManager.addAPIPackage("gnu.trove.map");
        moduleSecurityManager.addAPIPackage("gnu.trove.map.hash");
        moduleSecurityManager.addAPIPackage("gnu.trove.map.custom_hash");
        moduleSecurityManager.addAPIPackage("gnu.trove.procedure");
        moduleSecurityManager.addAPIPackage("gnu.trove.procedure.array");
        moduleSecurityManager.addAPIPackage("gnu.trove.queue");
        moduleSecurityManager.addAPIPackage("gnu.trove.set");
        moduleSecurityManager.addAPIPackage("gnu.trove.set.hash");
        moduleSecurityManager.addAPIPackage("gnu.trove.stack");
        moduleSecurityManager.addAPIPackage("gnu.trove.stack.array");
        moduleSecurityManager.addAPIPackage("gnu.trove.strategy");
        moduleSecurityManager.addAPIPackage("javax.vecmath");
        moduleSecurityManager.addAPIPackage("com.yourkit.runtime");
        moduleSecurityManager.addAPIPackage("com.bulletphysics.linearmath");

        moduleSecurityManager.addAPIClass(com.esotericsoftware.reflectasm.MethodAccess.class);
        moduleSecurityManager.addAPIClass(IOException.class);
        moduleSecurityManager.addAPIClass(InvocationTargetException.class);
        moduleSecurityManager.addAPIClass(LoggerFactory.class);
        moduleSecurityManager.addAPIClass(Logger.class);
        // WARNING: the next two lines were under the addAllowedPermission statements below!
        moduleSecurityManager.addAPIClass(java.nio.ByteBuffer.class);
        moduleSecurityManager.addAPIClass(java.nio.IntBuffer.class);

        for (Class<?> apiClass : moduleManager.getActiveModuleReflections().getTypesAnnotatedWith(API.class)) {
            if (apiClass.isSynthetic()) {
                // This is a package-info
                moduleSecurityManager.addAPIPackage(apiClass.getPackage().getName());
            } else {
                moduleSecurityManager.addAPIClass(apiClass);
            }
        }

        moduleSecurityManager.addFullPrivilegePackage("ch.qos.logback.classic");
        moduleSecurityManager.addAllowedPermission("com.google.gson", ReflectPermission.class);
        moduleSecurityManager.addAllowedPermission("com.google.gson.internal", ReflectPermission.class);

        return moduleManager;
    }

    /**
     * Creates a number of asset factories, each for a specific asset type.
     */
    private void initAssets() {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);

        // TODO: 41 similar blocks elsewhere in the project could take advantage
        // TODO: of an AssetFactoryBuilder<PrefabData, Prefab, PrafabImpl>().
        assetManager.setAssetFactory(AssetType.PREFAB, new AssetFactory<PrefabData, Prefab>() {

            @Override
            public Prefab buildAsset(AssetUri uri, PrefabData data) {
                return new PojoPrefab(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.SHAPE, new AssetFactory<BlockShapeData, BlockShape>() {

            @Override
            public BlockShape buildAsset(AssetUri uri, BlockShapeData data) {
                return new BlockShapeImpl(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.UI_SKIN, new AssetFactory<UISkinData, UISkin>() {
            @Override
            public UISkin buildAsset(AssetUri uri, UISkinData data) {
                return new UISkin(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.BEHAVIOR, new AssetFactory<BehaviorTreeData, BehaviorTree>() {
            @Override
            public BehaviorTree buildAsset(AssetUri uri, BehaviorTreeData data) {
                return new BehaviorTree(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.UI_ELEMENT, new AssetFactory<UIData, UIElement>() {
            @Override
            public UIElement buildAsset(AssetUri uri, UIData data) {
                return new UIElement(uri, data);
            }
        });

    }

    // ----------------------------------------------------------------------------------
    // Life-Cycle and GameState methods
    // ----------------------------------------------------------------------------------
    /**
     * Runs the engine, including its main loop. Initializes the engine if necessary,
     * i.e. if the facade didn't do it. This method is called only once per application
     * startup, which is the reason the GameState provided is the -initial- state
     * rather than a generic game state.
     *
     * @param initialState In at least one context (the PC facade) the GameState
     *                     implementation provided as input may vary, depending if
     *                     the application has been started with or without graphics
     *                     (headless).
     */
    // TODO: Q: perhaps this could be called startup() rather than run, to better
    // TODO:    match it to shutdown(). On the other hand, shutdown() seems quite
    // TODO:    final, when cleanup() and dispose() will in fact follow. Perhaps
    // TODO:    then it is shutdown() that could be renamed to stop() or halt().
    @Override
    public void run(GameState initialState) {
        try {
            if (!initialised) {
                init();
            }

            changeState(initialState);
            running = true;
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            mainLoop(); // -THE- MAIN LOOP. Most of the application time and resources are spent here.
            cleanup();

        } catch (RuntimeException e) {
            logger.error("Uncaught exception", e);
            throw e;
        }
    }

    /**
     * Causes the main loop to be interrupted. Notice that despite the "finality"
     * of the method name, cleanup() and dispose() will follow, in this order,
     * only once the main loop has been stopped. This occurs shortly, but not
     * immediately after this method is called. See mainLoop() method for details.
     */
    @Override
    public void shutdown() {
        running = false;
    }


    /**
     * After the main loop has been interrupted, shuts down subsystems,
     * dispose of the current game state (if any) and stops all threads.
     */
    private void cleanup() {
        logger.info("Shutting down Terasology...");

        Iterator<EngineSubsystem> iter = subsystems.descendingIterator();
        while (iter.hasNext()) {
            EngineSubsystem subsystem = iter.next();
            subsystem.shutdown(config);
        }

        config.save();

        if (currentState != null) {
            currentState.dispose();
            currentState = null;
        }

        stopThreads();
    }

    /**
     * If the engine is not running, disposes of its subsystems.
     */
    @Override
    public void dispose() {
        try {
            if (!running) {
                disposed = true;
                initialised = false;
                Iterator<EngineSubsystem> iter = subsystems.descendingIterator();
                while (iter.hasNext()) {
                    EngineSubsystem subsystem = iter.next();
                    subsystem.dispose();
                }
            }
        } catch (RuntimeException e) {
            logger.error("Uncaught exception", e);
            throw e;
        }
    }

    /**
     Changes the game state, i.e. to switch from the MainMenu to Ingame via Loading screen
     (each is a GameState). The change can be immediate, if there is no current game
     state set, or scheduled, when a current state exists and the new state is stored as
     pending. That been said, scheduled changes occurs in the main loop through the call
     processStateChanges(). As such, from a user perspective in normal circumstances,
     scheduled changes are likely to be perceived as immediate.
     */
    @Override
    public void changeState(GameState newState) {
        if (currentState != null) {
            pendingState = newState; // scheduled change
        } else {
            switchState(newState); // immediate change
        }
    }

    /**
     Called in the main loop, this method triggers a GameState change
     if one has been scheduled by setting a pending state in changeState().
     */
    private void processPendingStateIfNecessary() {
        if (pendingState != null) {
            switchState(pendingState);
            pendingState = null;
        }
    }

    /**
     This method is where the actual GameState change occurs, by disposing of the
     current state (if any) and then initializing the new state. Interested parties
     are notified of the change and any unused input from mouse/keyboard is cleared.
     The new state is then updated every frame by the main loop.
     */
    private void switchState(GameState newState) {
        if (currentState != null) {
            currentState.dispose();
        }
        currentState = newState;
        newState.init(this);
        for (StateChangeSubscriber subscriber : stateChangeSubscribers) {
            subscriber.onStateChange();
        }
        // drain input queues
        InputSystem inputSystem = CoreRegistry.get(InputSystem.class);
        inputSystem.getMouseDevice().getInputQueue();
        inputSystem.getKeyboard().getInputQueue();
    }

    // ------------------------------------------------------------------------------------
    // -THE- MAIN LOOP. This is where most of the application time and resources are spent.
    // ------------------------------------------------------------------------------------

    /**
     * The main loop runs until the running flag is set to false by the shutdown() method
     * or until the OS requests the application's window to be closed. Engine cleanup
     * and disposal occur afterwards.
     */
    private void mainLoop() {

        long totalDelta;
        float updateDelta;
        float subsystemsDelta;

        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        DisplayDevice display = CoreRegistry.get(DisplayDevice.class);

        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        while (running && !display.isCloseRequested()) {

            // Only process rendering and updating once a second
            // TODO: Q: why process rendering and updating at all
            // TODO:    while the display is not active? To make
            // TODO:    sure it doesn't "jump" when it is reactivated?
            // TODO:    I'm asking so that I can then explain it here...
            if (!display.isActive() && isHibernationAllowed()) {
                time.setPaused(true);
                Iterator<Float> updateCycles = time.tick();
                while (updateCycles.hasNext()) {
                    updateCycles.next();
                }
                try {
                    // TODO: Q: isn't this 1/10th of a second? Above it says "once a second"?
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.warn("Display inactivity sleep interrupted", e);
                }

                display.processMessages();
                time.setPaused(false);
                continue;
            }

            processPendingStateIfNecessary();

            // TODO: Q: what sets currentState to null? cleanup() does but is called after the
            // TODO:    main loop is interrupted. switchState() cannot, as calling init() on
            // TODO:    null object would trigger a NullPointerException?
            if (currentState == null) {
                shutdown();
                break;
            }

            PerformanceMonitor.startActivity("Network Update");
            networkSystem.update();
            PerformanceMonitor.endActivity();

            // WARNING: the following line was before the networkSystem.update() block.
            Iterator<Float> updateCycles = time.tick();

            // TODO: review this whole following section to make sure everything is necessary and in the right order
            totalDelta = 0;
            while (updateCycles.hasNext()) {
                updateDelta = updateCycles.next();  // gameTime gets updated here!
                totalDelta += time.getDeltaInMs();
                PerformanceMonitor.startActivity("Main Update");
                currentState.update(updateDelta);
                PerformanceMonitor.endActivity();
            }

            subsystemsDelta = totalDelta / 1000f;

            for (EngineSubsystem subsystem : getSubsystems()) {
                PerformanceMonitor.startActivity(subsystem.getClass().getSimpleName());
                subsystem.preUpdate(currentState, subsystemsDelta);
                PerformanceMonitor.endActivity();
            }

            // Waiting processes are set by modules via GameThread.a/synch() methods.
            GameThread.processWaitingProcesses();

            for (EngineSubsystem subsystem : getSubsystems()) {
                PerformanceMonitor.startActivity(subsystem.getClass().getSimpleName());
                subsystem.postUpdate(currentState, subsystemsDelta);
                PerformanceMonitor.endActivity();
            }

            PerformanceMonitor.rollCycle();
            PerformanceMonitor.startActivity("Other");
        }
        PerformanceMonitor.endActivity();

        // This becomes important only if display.isCloseRequested() is true.
        // In all other circumstances "running" is already false by the time
        // the flow gets here.
        running = false;
    }

    // ------------------------------------------------------------------------------------
    // Misc. Methods: i.e. submitTask(), thread-related, state-change subscriptions...
    // ------------------------------------------------------------------------------------

    @Override
    public void submitTask(final String name, final Runnable task) {
        try {
            commonThreadPool.put(new Task() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.currentThread().setName("Engine-Task-Pool");
                    try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getClass().getSimpleName())) {
                        task.run();
                    } catch (RejectedExecutionException e) {
                        ThreadMonitor.addError(e);
                        logger.error("Thread submitted after shutdown requested: {}", name);
                    } catch (Throwable e) {
                        ThreadMonitor.addError(e);
                    }
                }

                @Override
                public boolean isTerminateSignal() {
                    return false;
                }
            });
        } catch (InterruptedException e) {
            logger.error("Failed to submit task {}, running on main thread", name, e);
            task.run();
        }
    }

    public void stopThreads() {
        commonThreadPool.shutdown(new ShutdownTask(), false);
    }

    public void restartThreads() {
        commonThreadPool.restart();
    }

    @Override
    public void subscribeToStateChange(StateChangeSubscriber subscriber) {
        stateChangeSubscribers.add(subscriber);
    }

    @Override
    public void unsubscribeToStateChange(StateChangeSubscriber subscriber) {
        stateChangeSubscribers.remove(subscriber);
    }

    // ------------------------------------------------------------------------------------
    // Getter/Setter/Checker Methods
    // ------------------------------------------------------------------------------------
    public Iterable<EngineSubsystem> getSubsystems() {
        return subsystems;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public GameState getState() {
        return currentState;
    }

    public boolean isFullscreen() {
        return config.getRendering().isFullscreen();
    }

    public void setFullscreen(boolean state) {
        RenderingConfig renderingConfig = config.getRendering();
        if (renderingConfig.isFullscreen() != state) {
            renderingConfig.setFullscreen(state);
            DisplayDevice display = CoreRegistry.get(DisplayDevice.class);
            display.setFullscreen(state);
        }
    }

    public boolean isHibernationAllowed() {
        return hibernationAllowed && currentState.isHibernationAllowed();
    }

    public void setHibernationAllowed(boolean allowed) {
        this.hibernationAllowed = allowed;
    }

    public boolean hasFocus() {
        DisplayDevice display = CoreRegistry.get(DisplayDevice.class);
        return gameFocused && display.isActive();
    }

    @Override
    public boolean hasMouseFocus() {
        return gameFocused;
    }

    public void setFocus(boolean focused) {
        gameFocused = focused;
    }
}

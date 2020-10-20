// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.headless.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.WorldGenerationConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.game.GameManifest;
import org.terasology.input.InputSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.logic.console.ConsoleSystem;
import org.terasology.logic.console.commands.CoreCommands;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

import java.util.List;

/**
 * The class is game selection menu replacement for the headless server.
 */
public class StateHeadlessSetup implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateHeadlessSetup.class);
    @In
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;
    @In
    private ContextAwareClassFactory classFactory;
    @In
    private InputSystem inputSystem;
    @In
    private Config config;
    @In
    private ModuleManager moduleManager;

    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;
    private Context context;

    @Override
    public void init(GameEngine gameEngine) {
        context = gameEngine.createChildContext();
        updateContext(context);

        // let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);
        eventSystem = context.get(EventSystem.class);

        classFactory.createToContext(ConsoleImpl.class, Console.class);

        classFactory.createToContext(NUIManagerInternal.class);

        componentSystemManager = classFactory.createToContext(ComponentSystemManager.class);
        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = classFactory.createToContext(LocalPlayer.class);
        localPlayer.setRecordAndReplayClasses(directionAndOriginPosRecorderList, recordAndReplayCurrentStatus);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        GameManifest gameManifest;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        if (!savedGames.isEmpty()) {
            gameManifest = savedGames.get(0).getManifest();
        } else {
            gameManifest = createGameManifest();
        }

        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        config.getUniverseConfig().addWorldManager(worldInfo);
        config.getUniverseConfig().setSpawnWorldTitle(worldInfo.getTitle());
        config.getUniverseConfig().setUniverseSeed(gameManifest.getSeed());

        gameEngine.changeState(new StateLoading(gameManifest, NetworkMode.LISTEN_SERVER));
    }

    public GameManifest createGameManifest() {
        GameManifest gameManifest = new GameManifest();
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
            if (module != null) {
                gameManifest.addModule(module.getId(), module.getVersion());
            } else {
                logger.warn("ModuleRegistry has no latest version for module {}", moduleName);
            }
        }

        WorldGenerationConfig worldGenConfig = config.getWorldGeneration();

        // If no valid default world generator set then try to find one - no option to pick one manually in headless
        if (!worldGenConfig.getDefaultGenerator().isValid()) {

            // find the first gameplay module that is available, it should have a preferred world gen
            for (Name moduleName : config.getDefaultModSelection().listModules()) {
                Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
                if (StandardModuleExtension.isGameplayModule(module)) {
                    SimpleUri defaultWorldGenerator = StandardModuleExtension.getDefaultWorldGenerator(module);
                    worldGenConfig.setDefaultGenerator(defaultWorldGenerator);
                    break;
                }
            }
        }
        SimpleUri worldGeneratorUri = worldGenConfig.getDefaultGenerator();

        gameManifest.setTitle(worldGenConfig.getWorldTitle());
        gameManifest.setSeed(worldGenConfig.getDefaultSeed());
        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, worldGenConfig.getWorldTitle(),
                gameManifest.getSeed(),
                (long) (WorldTime.DAY_LENGTH * WorldTime.NOON_OFFSET), worldGeneratorUri);
        gameManifest.addWorld(worldInfo);
        return gameManifest;
    }

    @Override
    public void dispose(boolean shuttingDown) {
        eventSystem.process();

        componentSystemManager.shutdown();

        entityManager.clear();
    }

    @Override
    public void handleInput(float delta) {
        // headless haven't input
    }

    @Override
    public void update(float delta) {
        eventSystem.process();
    }

    @Override
    public void render() {
        // headless haven't output
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

    @Override
    public String getLoggingPhase() {
        return LoggingContext.INIT_PHASE;
    }

    @Override
    public Context getContext() {
        return context;
    }

    private void updateContext(Context context) {
        /*
         * We can't load the engine without core registry yet.
         * e.g. the statically created MaterialLoader needs the CoreRegistry to get the AssetManager.
         * And the engine loads assets while it gets created.
         */
        // TODO: Remove
        CoreRegistry.setContext(context);
        classFactory.setCurrentContext(context);
    }
}

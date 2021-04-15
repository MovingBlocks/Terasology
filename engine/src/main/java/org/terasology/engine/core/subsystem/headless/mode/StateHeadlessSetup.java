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
package org.terasology.engine.core.subsystem.headless.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.WorldGenerationConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.StandardModuleExtension;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.engine.logic.console.ConsoleSystem;
import org.terasology.engine.logic.console.commands.CoreCommands;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.time.WorldTime;

import java.util.List;

/**
 * The class is game selection menu replacement for the headless server.
 *
 */
public class StateHeadlessSetup implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateHeadlessSetup.class);

    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;
    private Context context;

    public StateHeadlessSetup() {
    }

    @Override
    public void init(GameEngine gameEngine) {
        context = gameEngine.createChildContext();
        CoreRegistry.setContext(context);

        // let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);

        eventSystem = context.get(EventSystem.class);
        context.put(Console.class, new ConsoleImpl(context));

        NUIManager nuiManager = new NUIManagerInternal((TerasologyCanvasRenderer) context.get(CanvasRenderer.class), context);
        context.put(NUIManager.class, nuiManager);

        componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);

        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");
        componentSystemManager.register(context.get(InputSystem.class), "engine:InputSystem");

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class), context.get(RecordAndReplayCurrentStatus.class));
        context.put(LocalPlayer.class, localPlayer);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        GameManifest gameManifest;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        if (savedGames.size() > 0) {
            gameManifest = savedGames.get(0).getManifest();
        } else {
            gameManifest = createGameManifest();
        }

        Config config = context.get(Config.class);
        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        config.getUniverseConfig().addWorldManager(worldInfo);
        config.getUniverseConfig().setSpawnWorldTitle(worldInfo.getTitle());
        config.getUniverseConfig().setUniverseSeed(gameManifest.getSeed());

        gameEngine.changeState(new StateLoading(gameManifest, NetworkMode.LISTEN_SERVER));
    }

    public GameManifest createGameManifest() {
        GameManifest gameManifest = new GameManifest();

        Config config = context.get(Config.class);
        ModuleManager moduleManager = context.get(ModuleManager.class);
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
        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, worldGenConfig.getWorldTitle(), gameManifest.getSeed(),
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
    }

    @Override
    public void update(float delta) {
        eventSystem.process();
    }

    @Override
    public void render() {
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
}

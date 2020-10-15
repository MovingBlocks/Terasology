// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.modes.VariableStepLoadProcess;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.NameVersion;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkSystem;
import org.terasology.network.Server;
import org.terasology.network.ServerInfoMessage;
import org.terasology.registry.In;
import org.terasology.world.internal.WorldInfo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//TODO document this!
@ExpectedCost(10)
public class JoinServer extends VariableStepLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(JoinServer.class);

    @In
    private ModuleManager moduleManager;
    @In
    private NetworkSystem networkSystem;
    @In
    private GameManifest gameManifest;
    @In
    private JoinStatus joinStatus;
    @In
    private GameEngine gameEngine;
    @In
    private Game game;
    @In
    private EnvironmentSwitchHandler environmentSwitchHandler;
    @In
    private Context context;

    private Thread applyModuleThread;
    private ModuleEnvironment oldEnvironment;

    @Override
    public String getMessage() {
        if (applyModuleThread != null) {
            return "${engine:menu#scanning-for-assets}";
        } else {
            return joinStatus.getCurrentActivity();
        }
    }

    @Override
    public boolean step() {
        if (applyModuleThread != null) {
            if (!applyModuleThread.isAlive()) {
                if (oldEnvironment != null) {
                    oldEnvironment.close();
                }
                return true;
            }
            return false;
        } else if (joinStatus.getStatus() == JoinStatus.Status.COMPLETE) {
            Server server = networkSystem.getServer();
            ServerInfoMessage serverInfo = networkSystem.getServer().getInfo();

            // If no GameName, use Server IP Address
            if (serverInfo.getGameName().length() > 0) {
                gameManifest.setTitle(serverInfo.getGameName());
            } else {
                gameManifest.setTitle(server.getRemoteAddress());
            }

            for (WorldInfo worldInfo : serverInfo.getWorldInfoList()) {
                gameManifest.addWorld(worldInfo);
            }

            Map<String, Short> blockMap = Maps.newHashMap();
            for (Entry<Integer, String> entry : serverInfo.getBlockIds().entrySet()) {
                String name = entry.getValue();
                short id = entry.getKey().shortValue();
                Short oldId = blockMap.put(name, id);
                if (oldId != null && oldId != id) {
                    logger.warn("Overwriting Id {} for {} with Id {}", oldId, name, id);
                }
            }
            gameManifest.setRegisteredBlockFamilies(serverInfo.getRegisterBlockFamilyList());
            gameManifest.setBlockIdMap(blockMap);
            gameManifest.setTime(networkSystem.getServer().getInfo().getTime());

            Set<Module> moduleSet = Sets.newLinkedHashSet();
            for (NameVersion moduleInfo : networkSystem.getServer().getInfo().getModuleList()) {
                Module module = moduleManager.getRegistry().getModule(moduleInfo.getName(), moduleInfo.getVersion());
                if (module == null) {
                    StateMainMenu mainMenu = new StateMainMenu("Missing required module: " + moduleInfo);
                    gameEngine.changeState(mainMenu);
                    return false;
                } else {
                    logger.info("Activating module: {}:{}", moduleInfo.getName(), moduleInfo.getVersion());
                    gameManifest.addModule(module.getId(), module.getVersion());
                    moduleSet.add(module);
                }
            }

            oldEnvironment = moduleManager.getEnvironment();
            moduleManager.loadEnvironment(moduleSet, true);

            game.load(gameManifest);

            applyModuleThread = new Thread(() -> environmentSwitchHandler.handleSwitchToGameEnvironment(context));
            applyModuleThread.start();

            return false;
        } else if (joinStatus.getStatus() == JoinStatus.Status.FAILED) {
            StateMainMenu mainMenu = new StateMainMenu("Failed to connect to server: " + joinStatus.getErrorMessage());
            gameEngine.changeState(mainMenu);
            networkSystem.shutdown();
        }
        return false;
    }

    @Override
    public float getProgress() {
        return joinStatus.getCurrentActivityProgress();
    }
}

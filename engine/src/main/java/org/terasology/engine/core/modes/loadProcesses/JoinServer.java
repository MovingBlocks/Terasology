// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.modes.VariableStepLoadProcess;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.game.Game;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.JoinStatus;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.Server;
import org.terasology.engine.network.ServerInfoMessage;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.NameVersion;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//TODO document this!
public class JoinServer extends VariableStepLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(JoinServer.class);

    private Context context;
    private NetworkSystem networkSystem;
    private GameManifest gameManifest;
    private JoinStatus joinStatus;

    private Thread applyModuleThread;
    private ModuleEnvironment oldEnvironment;

    public JoinServer(Context context, GameManifest gameManifest, JoinStatus joinStatus) {
        this.context = context;
        this.networkSystem = context.get(NetworkSystem.class);
        this.gameManifest = gameManifest;
        this.joinStatus = joinStatus;
    }

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

            ModuleManager moduleManager = context.get(ModuleManager.class);

            Set<Module> moduleSet = Sets.newLinkedHashSet();
            for (NameVersion moduleInfo : networkSystem.getServer().getInfo().getModuleList()) {
                Module module = moduleManager.getRegistry().getModule(moduleInfo.getName(), moduleInfo.getVersion());
                if (module == null) {
                    StateMainMenu mainMenu = new StateMainMenu("Missing required module: " + moduleInfo);
                    context.get(GameEngine.class).changeState(mainMenu);
                    return false;
                } else {
                    logger.atInfo().log("Activating module: {}:{}", moduleInfo.getName(), moduleInfo.getVersion());
                    gameManifest.addModule(module.getId(), module.getVersion());
                    moduleSet.add(module);
                }
            }

            oldEnvironment = moduleManager.getEnvironment();
            moduleManager.loadEnvironment(moduleSet, true);

            context.get(Game.class).load(gameManifest);

            EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
            applyModuleThread = new Thread(() -> environmentSwitchHandler.handleSwitchToGameEnvironment(context));
            applyModuleThread.start();

            return false;
        } else if (joinStatus.getStatus() == JoinStatus.Status.FAILED) {
            StateMainMenu mainMenu = new StateMainMenu("Failed to connect to server: " + joinStatus.getErrorMessage());
            context.get(GameEngine.class).changeState(mainMenu);
            networkSystem.shutdown();
        }
        return false;
    }

    @Override
    public void begin() {
    }

    @Override
    public float getProgress() {
        return joinStatus.getCurrentActivityProgress();
    }

    @Override
    public int getExpectedCost() {
        return 10;
    }
}

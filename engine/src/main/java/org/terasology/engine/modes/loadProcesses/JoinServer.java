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

package org.terasology.engine.modes.loadProcesses;

import com.google.common.collect.Maps;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;
import org.terasology.naming.NameVersion;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.ApplyModulesUtil;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkSystem;
import org.terasology.network.ServerInfoMessage;
import org.terasology.world.internal.WorldInfo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Immortius
 */
public class JoinServer implements LoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(JoinServer.class);

    private NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
    private GameManifest gameManifest;
    private JoinStatus joinStatus;

    public JoinServer(GameManifest gameManifest, JoinStatus joinStatus) {
        this.gameManifest = gameManifest;
        this.joinStatus = joinStatus;
    }

    @Override
    public String getMessage() {
        return joinStatus.getCurrentActivity();
    }

    @Override
    public boolean step() {
        if (joinStatus.getStatus() == JoinStatus.Status.COMPLETE) {
            ServerInfoMessage serverInfo = networkSystem.getServer().getInfo();
            gameManifest.setTitle(serverInfo.getGameName());
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

            ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

            Set<Module> moduleSet = Sets.newLinkedHashSet();
            for (NameVersion moduleInfo : networkSystem.getServer().getInfo().getModuleList()) {
                Module module = moduleManager.getRegistry().getModule(moduleInfo.getName(), moduleInfo.getVersion());
                if (module == null) {
                    StateMainMenu mainMenu = new StateMainMenu("Missing required module: " + moduleInfo);
                    CoreRegistry.get(GameEngine.class).changeState(mainMenu);
                    return false;
                } else {

                    logger.debug("Activating module: {}:{}", moduleInfo.getName(), moduleInfo.getVersion());
                    gameManifest.addModule(module.getId(), module.getVersion());
                    moduleSet.add(module);
                }
            }

            CoreRegistry.get(Game.class).load(gameManifest);
            ApplyModulesUtil.applyModules();

            return true;
        } else if (joinStatus.getStatus() == JoinStatus.Status.FAILED) {
            StateMainMenu mainMenu = new StateMainMenu("Failed to connect to server: " + joinStatus.getErrorMessage());
            CoreRegistry.get(GameEngine.class).changeState(mainMenu);
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

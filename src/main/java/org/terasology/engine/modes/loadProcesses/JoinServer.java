/*
 * Copyright 2013 Moving Blocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.ModConfig;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldInfo;

import java.util.Map;

/**
 * @author Immortius
 */
public class JoinServer implements LoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(JoinServer.class);

    private NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
    private GameManifest gameManifest;

    public JoinServer(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Connecting to server";
    }

    @Override
    public boolean step() {
        if (networkSystem.getServer() != null && networkSystem.getServer().getInfo() != null) {
            NetData.ServerInfoMessage serverInfo = networkSystem.getServer().getInfo();
            gameManifest.setTitle(serverInfo.getGameName());
            for (NetData.WorldInfo worldInfo : serverInfo.getWorldInfoList()) {
                WorldInfo world = new WorldInfo();
                world.setTime(worldInfo.getTime());
                world.setTitle(worldInfo.getTitle());
                gameManifest.addWorldInfo(world);
            }

            Map<String, Byte> blockMap = Maps.newHashMap();
            for (int i = 0; i < serverInfo.getBlockIdCount(); ++i) {
                blockMap.put(serverInfo.getBlockName(i), (byte) serverInfo.getBlockId(i));
            }
            gameManifest.setRegisteredBlockFamilies(serverInfo.getRegisterBlockFamilyList());
            gameManifest.setBlockIdMap(blockMap);
            gameManifest.setTime(networkSystem.getServer().getInfo().getTime());

            ModConfig modConfig = gameManifest.getModConfiguration();
            ModManager modManager = CoreRegistry.get(ModManager.class);
            for (NetData.ModuleInfo moduleInfo : networkSystem.getServer().getInfo().getModuleList()) {
                Mod mod = modManager.getMod(moduleInfo.getModuleId());
                if (mod == null) {
                    // TODO: Missing module, fail and disconnect
                } else {
                    logger.debug("Activating module: {}", moduleInfo.getModuleId());
                    modConfig.addMod(moduleInfo.getModuleId());
                }
            }

            CoreRegistry.get(Game.class).load(gameManifest);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int begin() {
        return 1;
    }
}

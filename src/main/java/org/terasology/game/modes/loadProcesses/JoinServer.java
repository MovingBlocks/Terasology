package org.terasology.game.modes.loadProcesses;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.ModConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
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
    private WorldInfo worldInfo;
    private String address;
    private int port;

    public JoinServer(String address, int port, WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
        this.address = address;
        this.port = port;
    }

    @Override
    public String getMessage() {
        return "Connecting to server";
    }

    @Override
    public boolean step() {
        if (networkSystem.getServer().getInfo() != null) {
            worldInfo.setTitle(networkSystem.getServer().getInfo().getWorldName());
            Map<String, Byte> blockMap = Maps.newHashMap();
            for (NetData.BlockMapping mapping : networkSystem.getServer().getInfo().getBlockMappingList()) {
                blockMap.put(mapping.getBlockName(), (byte)mapping.getBlockId());
            }
            worldInfo.setBlockIdMap(blockMap);
            worldInfo.setTime(networkSystem.getServer().getInfo().getTime());

            ModConfig modConfig = worldInfo.getModConfiguration();
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

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int begin() {
        if (!networkSystem.join(address, port)) {
            // TODO: Deal with failure to connect
        }
        return 1;
    }
}

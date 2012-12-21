package org.terasology.game.modes.loadProcesses;

import com.google.common.collect.Maps;
import org.terasology.game.CoreRegistry;
import org.terasology.game.TerasologyConstants;
import org.terasology.game.modes.LoadProcess;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldInfo;

import java.util.Map;

/**
 * @author Immortius
 */
public class JoinServer implements LoadProcess {

    private NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
    private WorldInfo worldInfo;

    public JoinServer(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public String getMessage() {
        return "Connecting to server";
    }

    @Override
    public boolean step() {
        if (networkSystem.getServerInfo() != null) {
            worldInfo.setTitle(networkSystem.getServerInfo().getWorldName());
            Map<String, Byte> blockMap = Maps.newHashMap();
            for (NetData.BlockMapping mapping : networkSystem.getServerInfo().getBlockMappingList()) {
                blockMap.put(mapping.getBlockName(), (byte)mapping.getBlockId());
            }
            worldInfo.setBlockIdMap(blockMap);
            worldInfo.setTime(networkSystem.getServerInfo().getTime());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int begin() {
        if (!networkSystem.join("127.0.0.1", TerasologyConstants.DEFAULT_PORT)) {
            // TODO: Deal with failure to connect
        }
        return 1;
    }
}

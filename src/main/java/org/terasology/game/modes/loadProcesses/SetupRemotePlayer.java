package org.terasology.game.modes.loadProcesses;

import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.LocalPlayer;
import org.terasology.network.NetEntityRef;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
public class SetupRemotePlayer implements LoadProcess {
    @Override
    public String getMessage() {
        return "Awaiting player data";
    }

    @Override
    public boolean step() {
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        EntityRef client = new NetEntityRef(networkSystem.getServer().getInfo().getClientId(), networkSystem);
        if (client.exists()) {
            CoreRegistry.get(LocalPlayer.class).setClientEntity(client);
            return true;
        }
        return false;
    }

    @Override
    public int begin() {
        return 1;
    }
}

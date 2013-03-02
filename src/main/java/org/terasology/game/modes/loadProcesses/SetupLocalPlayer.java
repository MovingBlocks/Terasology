package org.terasology.game.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
public class SetupLocalPlayer implements LoadProcess {
    @Override
    public String getMessage() {
        return "Setting up local player";
    }

    @Override
    public boolean step() {
        Client localClient = CoreRegistry.get(NetworkSystem.class).joinLocal(CoreRegistry.get(Config.class).getPlayer().getName());
        CoreRegistry.get(LocalPlayer.class).setClientEntity(localClient.getEntity());
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}

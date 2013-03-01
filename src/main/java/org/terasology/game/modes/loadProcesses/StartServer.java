package org.terasology.game.modes.loadProcesses;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.TerasologyConstants;
import org.terasology.game.modes.LoadProcess;
import org.terasology.network.NetworkEntitySystem;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
public class StartServer implements LoadProcess {
    @Override
    public String getMessage() {
        return "Starting Server";
    }

    @Override
    public boolean step() {
        CoreRegistry.get(NetworkSystem.class).host(TerasologyConstants.DEFAULT_PORT);
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}

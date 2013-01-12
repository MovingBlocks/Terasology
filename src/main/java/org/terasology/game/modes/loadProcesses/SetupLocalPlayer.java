package org.terasology.game.modes.loadProcesses;

import org.terasology.components.DisplayInformationComponent;
import org.terasology.config.Config;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.events.ConnectedEvent;

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
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        EntityRef client = entityManager.create("engine:client");
        CoreRegistry.get(LocalPlayer.class).setClientEntity(client);
        EntityRef clientInfo = entityManager.create("engine:clientInfo");
        DisplayInformationComponent displayInfo = clientInfo.getComponent(DisplayInformationComponent.class);
        if (displayInfo != null) {
            displayInfo.name = CoreRegistry.get(Config.class).getPlayerConfig().getName();
            clientInfo.saveComponent(displayInfo);
        }
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        if (clientComp != null) {
            clientComp.clientInfo = clientInfo;
            client.saveComponent(clientComp);
        }
        client.send(new ConnectedEvent());
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}

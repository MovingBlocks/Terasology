package org.terasology.MultiSystems.Systems;

import org.terasology.logic.manager.MessageManager;

/**
 * @author Julien "NowNewStart" Gelmar <master@nownewstart.net>
 */
public class KickSystem {
    public void kick(String Player)
    {
    //TODO: Add Kick Possibility
        MessageManager.getInstance().addMessage(Player + " was kicked from the server.", MessageManager.EMessageScope.PUBLIC);
    }
}

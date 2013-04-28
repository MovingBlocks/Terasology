package org.terasology.MultiSystems.Systems;

import org.terasology.logic.manager.MessageManager;

/**
 * @author Julien "NowNewStart" Gelmar <master@nownewstart.net>
 */
public class MotdSystem {
    public String motd = "Welcome to the Server! Press /help to see a list of avaiable commands";

    public void setmotd(String newmotd)
    {
    String motd = newmotd;
    MessageManager.getInstance().addMessage("The MOTD was successfully changed.", MessageManager.EMessageScope.PRIVATE);
    }
    public void motd()
    {
        MessageManager.getInstance().addMessage(motd, MessageManager.EMessageScope.PRIVATE);
    }
}

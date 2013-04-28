package org.terasology.MultiSystems.Systems;

import org.terasology.logic.commands.Command;
import org.terasology.logic.manager.MessageManager;

/**
 * @author Julien "NowNewStart" Gelmar <master@nownewstart.net>
 */
public class BanSystem {
    //TODO: Add Possibilty to ban Members
    //TODO: Add Ban Message for banned Players?
    @Command(shortDescription = "Ban Player")
    public void ban(String player)
    {
        MessageManager.getInstance().addMessage(player + " was banned from the Server.", MessageManager.EMessageScope.PUBLIC);
    }
    @Command(shortDescription = "Ban Player with Duration")
    public void ban(String player, int duration)
    {
      MessageManager.getInstance().addMessage(player + " was banned for " + duration + " minutes.", MessageManager.EMessageScope.PUBLIC);
    }
}

package org.terasology.MultiSystems.Systems;

import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.MessageManager;

/**
 * @author Julien "NowNewStart" Gelmar <master@nownewstart.net>
 */
public class PermissionSystem {
    public int exploded;
    public String power = "Player,100";
    public String player = LocalPlayer.class.getName();
    public void PSystem() {
        //TODO: Possibility to expand Power function with additional power states
       exploded = Integer.parseInt(String.valueOf(exploded));
        switch(exploded)
        {
            case 0:
                MessageManager.getInstance().addMessage("Welcome " + player + " , please use /register to register yourself to this server.", MessageManager.EMessageScope.PRIVATE);
            case 10:
                MessageManager.getInstance().addMessage("Welcome " + player + " , please use /login to log into this server.", MessageManager.EMessageScope.PRIVATE);
            case 25:
                MessageManager.getInstance().addMessage("Welcome " + player, MessageManager.EMessageScope.PUBLIC);
            case 50:
                MessageManager.getInstance().addMessage("Welcome Mod " + player, MessageManager.EMessageScope.PUBLIC);
            case 80:
                MessageManager.getInstance().addMessage("Welcome Admin " + player, MessageManager.EMessageScope.PUBLIC);
            case 100:
                MessageManager.getInstance().addMessage("Welcome Server Admin " + player, MessageManager.EMessageScope.PUBLIC);
            default:
                MessageManager.getInstance().addMessage("Welcome " + player, MessageManager.EMessageScope.PUBLIC);
        }

    }
    public void getPower(String player, String p_power)
    {
       //TODO: Think about Possibility to split the String "power" 2 times
    }
    public void getCpower()
    {
        //TODO: Add Function to deny/allow commands for special user powers.
    }

}

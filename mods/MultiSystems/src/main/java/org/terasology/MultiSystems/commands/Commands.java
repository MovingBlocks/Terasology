package org.terasology.MultiSystems.commands;

import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandParam;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.manager.MessageManager;

/**
 * @author Julien "NowNewStart" Gelmar <master@nownewstart.net>
 *     TODO: Add more Commands
 *     TODO: Add Possibility to show a message for the kicked/banned Member
 */
public class Commands implements CommandProvider {
    public String motd = "Welcome to the Server!";
    public String Player = "Player";
    @Command(shortDescription="Change MOTD")
    public void setmotd(@CommandParam(name="MOTD") String newmotd)
    {
        String motd = newmotd;
        MessageManager.getInstance().addMessage("The MOTD was successfully changed.", MessageManager.EMessageScope.PRIVATE);
    }
    @Command(shortDescription="Display MOTD")
    public void motd()
    {
        MessageManager.getInstance().addMessage(motd, MessageManager.EMessageScope.PRIVATE);
    }
    @Command(shortDescription="Kick Player")
    public void kick(@CommandParam(name="Player") String Player)
    {
        MessageManager.getInstance().addMessage(Player + " was kicked from the Server.", MessageManager.EMessageScope.PUBLIC);
    }
    @Command(shortDescription="Ban Player")
    public void ban(@CommandParam(name="Player") String Player)
    {
        MessageManager.getInstance().addMessage(Player + " was banned.", MessageManager.EMessageScope.PUBLIC);
    }
    @Command(shortDescription = "Ban Player with Duration")
    public void ban(@CommandParam(name="Player") String Player, @CommandParam(name="Duration") int duration)
    {
        MessageManager.getInstance().addMessage(Player + " was banned for " + duration + " Minutes.", MessageManager.EMessageScope.PUBLIC);
    }
    @Command(shortDescription = "Register Nickname")
    public void nick(@CommandParam(name="Name") String Name, @CommandParam(name="Password") String Password)
    {
        // TODO: Add File with Nickname List
        MessageManager.getInstance().addMessage("You registered the Nickname " + Name + " with the Password '" + Password + "'.", MessageManager.EMessageScope.PRIVATE);
    }
    @Command(shortDescription = "Login with registered Nickname")
    public void login(@CommandParam(name="Name") String Name, @CommandParam(name="Password") String Password)
    {
        // TODO: Add Function to split and load Nickname File
        MessageManager.getInstance().addMessage("You are now logged in.", MessageManager.EMessageScope.PRIVATE);
        String power = Name + "," + Password + ",25";
       //TODO: Add Function to save into Nickname File
    }
    @Command(shortDescription = "Server Message")
    public void shout(@CommandParam(name="Message") String Message)
    {
        MessageManager.getInstance().addMessage("SERVER: " + Message, MessageManager.EMessageScope.PUBLIC);
    }
    @Command(shortDescription = "Say into Chat")
    public void say(@CommandParam(name="Message") String Message)
    {
     MessageManager.getInstance().addMessage(Player + ": " + Message, MessageManager.EMessageScope.PUBLIC);
    }
    @Command(shortDescription = "Teleport to Player")
    public void tpto(@CommandParam(name="Player") String Player)
    {

    }
    @Command(shortDescription = "Teleport Player to you")
    public void tphere(@CommandParam(name="Player") String Player)
    {

    }
}

package org.terasology.logic.manager;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.terasology.game.Terasology;
import org.terasology.game.modes.StateSinglePlayer;
import org.terasology.rendering.gui.menus.UIDebugConsole;

import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 29/04/12
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class GroovyHelpManager {

    private GroovyHelp groovyhelp = new GroovyHelp();

    public GroovyHelpManager()
    {}

    public ArrayList<String> getCommandList() throws IOException
    {
        ArrayList<String> commandlist = new ArrayList<String>();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(".\\data\\help\\commands\\commands.json"));
        reader.beginArray();
        while (reader.hasNext()) {
            groovyhelp = gson.fromJson(reader,GroovyHelp.class);
            commandlist.add(groovyhelp.getCommandName());
        }
        reader.endArray();
        reader.close();
        return commandlist;
    }

    public GroovyHelp readCommandHelp(String commandname)
    {
        try
        {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(".\\data\\help\\commands\\commands.json"));
            reader.beginArray();
            while (reader.hasNext()) {
                groovyhelp = gson.fromJson(reader,GroovyHelp.class);
                if(groovyhelp.getCommandName().equals(commandname))
                { return groovyhelp; }
            }
            reader.endArray();
            reader.close();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void writeHelp(StateSinglePlayer singlePlayer)
    {
        try
        {
            groovyhelp.setCommandName("giveBlock");
            String[] strings = {"blockNbr","blockName","quantity"};
            groovyhelp.setParameters(strings);
            groovyhelp.setCommandDesc("Adds blocks of the given blockNbr or blockName to your inventory");
            groovyhelp.setCommandHelp("test");
            String[] strings2 = {"'giveBlock Water' Gives 16 water blocks","'giveBlock IronPyrites, 42' Gives 42 Iron Pyrite (Fool's Gold) blocks", "'giveBlock Chest' Gives you a Chest block you can place, activate ('E'), put stuff in, destroy, pick up, place elsewhere, find same stuff in it!"};
            groovyhelp.setExamples(strings2);
            Gson gson = new Gson();
            String test = gson.toJson(groovyhelp, groovyhelp.getClass());
            UIDebugConsole _console = singlePlayer.getHud().getDebugConsole();
            _console.setHelpText(groovyhelp);
            JsonWriter writer = new JsonWriter(new FileWriter(".\\data\\help\\commands\\commands.json"));
            writer.beginArray();
            gson.toJson(groovyhelp,groovyhelp.getClass(),writer);
            writer.endArray();
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //return true;
    }
}

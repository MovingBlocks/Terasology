package org.terasology.logic.manager;

import java.util.*;
import java.lang.reflect.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.ClasspathResourceLoader;

import java.io.FileReader;
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

    public HashMap<String,String> getHelpCommands() throws IOException
    {
        HashMap<String,String> commandlist = new HashMap<String, String>();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(".\\data\\help\\commands\\commands.json"));
        reader.beginArray();
        while (reader.hasNext()) {
            groovyhelp = gson.fromJson(reader,GroovyHelp.class);
            commandlist.put(groovyhelp.getCommandName(), groovyhelp.getCommandDesc());
        }
        reader.endArray();
        reader.close();
        return commandlist;
    }

    public String[] getGroovyCommands(){
        Method[] methods = GroovyManager.CommandHelper.class.getDeclaredMethods();
        String[] tempval = new String[methods.length];
        for (int i=0;i<methods.length;i++)
        {
            tempval[i] = methods[i].getName();
        }
        Set<String> set = new HashSet<String>(Arrays.asList(tempval));
        String[] retval = new String[set.size()];
        set.toArray(retval);
        return retval;
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

    public HashMap<Byte,String> getGroovyBlocks()
    {
        HashMap<Byte,String> retval = new HashMap<Byte, String>();
        for(byte i = -127;i<128;i++){
            Block b = BlockManager.getInstance().getBlock(i);
            if(b.getId() != 0){
                retval.put(b.getId(),b.getTitle());
            }
            if(i == 117){break;}
        }

        return retval;
    }
}

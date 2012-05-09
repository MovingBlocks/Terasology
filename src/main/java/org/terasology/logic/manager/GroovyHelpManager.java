package org.terasology.logic.manager;

import java.util.*;
import java.lang.reflect.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.game.CoreRegistry;
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
        String[] endfilter = {"FRONT","BACK","TOP","BOTTOM","LEFT","RIGHT"};
        String fampref = "org.terasology.model.blocks.";
        String tempval = "";
        boolean nodup = true;
        for(byte i = -127;i<127;i++){
            Block b = BlockManager.getInstance().getBlock(i);
            if(b.getId() != 0){
                if(tempval.length() > 0)
                {
                    if(b.getTitle().startsWith(tempval)){
                        nodup = false;
                    }
                    else{
                        nodup = true;
                        tempval = "";
                    }
                }
                else{
                    for(int j = 0;j<endfilter.length;j++){
                        if(b.getTitle().endsWith(endfilter[j])){
                            tempval = b.getTitle().substring(0,b.getTitle().length() - endfilter[j].length());
                        }
                    }
                }
                if(nodup){
                    String tempfam = b.getBlockFamily().toString().split("@")[0];
                    if(tempfam.startsWith(fampref)){
                        tempfam = tempfam.substring(fampref.length(), tempfam.length());
                    }
                    if(tempval.length() < 1){
                        retval.put(b.getId(),b.getTitle() + " and belongs to " + tempfam);
                    }
                    else{
                        retval.put(b.getId(),tempval + " and belongs to " + tempfam);
                    }
                }
            }
            if(i == 127){break;}
        }

        return retval;
    }

    public  ArrayList<Prefab> getItems(){
        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        ArrayList<Prefab> prefabs = new ArrayList<Prefab>();
        Iterator<Prefab> it =  prefMan.listPrefabs().iterator();
        while(it.hasNext()){
            Prefab prefab = it.next();
            //grabb all
            prefabs.add(prefab);
        }
        return prefabs;
    }
}

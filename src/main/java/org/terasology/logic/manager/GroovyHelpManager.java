package org.terasology.logic.manager;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;
import org.terasology.mods.miniions.componentsystem.entityfactory.MiniionFactory;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 29/04/12
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class GroovyHelpManager {

    private GroovyHelp groovyhelp = new GroovyHelp();

    public GroovyHelpManager() {
    }

    public HashMap<String, String> getHelpCommands() throws IOException {
        HashMap<String, String> commandlist = new HashMap<String, String>();
        Gson gson = new Gson();
        String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "help" + File.separator + "commands" + File.separator + "commands.json";
        JsonReader reader = new JsonReader(new FileReader(helpFile));
        reader.beginArray();
        while (reader.hasNext()) {
            groovyhelp = gson.fromJson(reader, GroovyHelp.class);
            commandlist.put(groovyhelp.getCommandName(), groovyhelp.getCommandDesc());
        }
        reader.endArray();
        reader.close();
        return commandlist;
    }

    public String[] getGroovyCommands() {
        Method[] methods = GroovyManager.CommandHelper.class.getDeclaredMethods();
        String[] tempval = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            tempval[i] = methods[i].getName();
        }
        Set<String> set = new HashSet<String>(Arrays.asList(tempval));
        String[] retval = new String[set.size()];
        set.toArray(retval);
        return retval;
    }

    public GroovyHelp readCommandHelp(String commandname) {
        try {
            Gson gson = new Gson();
            String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "help" + File.separator + "commands" + File.separator + "commands.json";
            JsonReader reader = new JsonReader(new FileReader(helpFile));
            reader.beginArray();
            while (reader.hasNext()) {
                groovyhelp = gson.fromJson(reader, GroovyHelp.class);
                if (groovyhelp.getCommandName().equals(commandname)) {
                    return groovyhelp;
                }
            }
            reader.endArray();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<Byte, String> getGroovyBlocks() {
        HashMap<Byte, String> retval = new HashMap<Byte, String>();
        String[] endfilter = {"FRONT", "BACK", "TOP", "BOTTOM", "LEFT", "RIGHT"};
        String fampref = "org.terasology.model.blocks.";
        String tempval = "";
        boolean nodup = true;
        for (byte i = -127; i < 127; i++) {
            Block b = BlockManager.getInstance().getBlock(i);
            if (b.getId() != 0) {
                if (tempval.length() > 0) {
                    if (b.getTitle().startsWith(tempval)) {
                        nodup = false;
                    } else {
                        nodup = true;
                        tempval = "";
                    }
                } else {
                    for (String element : endfilter) {
                        if (b.getTitle().endsWith(element)) {
                            tempval = b.getTitle().substring(0, b.getTitle().length() - element.length());
                        }
                    }
                }
                if (nodup) {
                    String tempfam = b.getBlockFamily().toString().split("@")[0];
                    if (tempfam.startsWith(fampref)) {
                        tempfam = tempfam.substring(fampref.length(), tempfam.length());
                    }
                    if (tempval.length() < 1) {
                        retval.put(b.getId(), b.getTitle() + " and belongs to " + tempfam);
                    } else {
                        retval.put(b.getId(), tempval + " and belongs to " + tempfam);
                    }
                }
            }
            if (i == 127) {
                break;
            }
        }

        return retval;
    }

    public ArrayList<Prefab> getItems() {
        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        ArrayList<Prefab> prefabs = new ArrayList<Prefab>();
        Iterator<Prefab> it = prefMan.listPrefabs().iterator();
        while (it.hasNext()) {
            Prefab prefab = it.next();
            //grabb all
            prefabs.add(prefab);
        }
        return prefabs;
    }
}

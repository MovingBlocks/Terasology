/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.console;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.game.CoreRegistry;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.terasology.logic.manager.PathManager;

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

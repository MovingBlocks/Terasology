/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

package org.terasology.logic.manager;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.inventory.Item;
import org.terasology.model.inventory.ItemBlock;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages everything related to using Groovy from within Java.
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class GroovyManager {
    /**
     * The Binding allows us to keep variable references around where Groovy can play with them
     */
    private final Binding _bind;

    /**
     * Directory where we keep "plugin" files (Groovy scripts we'll run - prolly move this setting elsewhere sometime)
     */
    private static final String PLUGINS_PATH = "groovy/plugins";

    /**
     * Initialize the GroovyManager and "share" the given World variable via the Binding
     */
    public GroovyManager() {
        _bind = new Binding();
        //loadAllPlugins();
    }

    /**
     * Method to initialize a plugin - a.k.a. execute a Groovy script in the plugin dir
     *
     * @param pluginName Name of a particular plugin file to execute
     */
    public void initializePlugin(String pluginName) {
        GroovyScriptEngine gse = null;
        try {
            // Create an engine tied to the dir we keep plugins in
            gse = new GroovyScriptEngine(PLUGINS_PATH);
        } catch (IOException ioe) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize plugin (IOException): " + pluginName + ", reason: " + ioe.toString(), ioe);
            ioe.printStackTrace();
        }

        if (gse != null) {
            try {
                updateBinding();
                // Run the specified plugin
                gse.run(pluginName, _bind);
            } catch (ResourceException re) {
                Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ResourceException): " + pluginName + ", reason: " + re.toString(), re);
                re.printStackTrace();
            } catch (ScriptException se) {
                Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ScriptException): " + pluginName + ", reason: " + se.toString(), se);
                se.printStackTrace();
            }
        }
    }

    private void updateBinding() {
        _bind.setVariable("tera", Terasology.getInstance());
        _bind.setVariable("configuration", Config.getInstance());
        _bind.setVariable("cmd", new CommandHelper());
    }

    /**
     * Executes the given command with Groovy.
     *
     * @param consoleString Contains what the user entered into the console
     * @return boolean indicating command success or not
     */
    public boolean runGroovyShell(String consoleString) {
        Terasology.getInstance().getLogger().log(Level.INFO, "Groovy console about to execute command: " + consoleString);
        updateBinding();
        GroovyShell shell = new GroovyShell(_bind);
        try {
            shell.evaluate(consoleString);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class CommandHelper {
        public void giveBlock(int blockId) {
            giveBlock(blockId, 16);
        }

        public void giveBlock(int blockId, int quantity) {
            Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            Item block = new ItemBlock(BlockManager.getInstance().getBlock((byte) blockId).getBlockGroup());
            player.getInventory().addItem(block, quantity);
        }

        public void giveBlock(String title) {
            giveBlock(title, 16);
        }

        public void giveBlock(String title, int quantity) {
            Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            BlockGroup group = BlockManager.getInstance().getBlockGroup(title);
            if (group == null) {
                Block block = BlockManager.getInstance().getBlock(title);
                if (block != null) {
                    group = block.getBlockGroup();
                }
            }
            if (group != null) {
                player.getInventory().addItem(new ItemBlock(group), quantity);
            }
        }

        public void fullHealth() {
            Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            player.heal(player.getMaxHealthPoints() - player.getHealthPoints());
        }

        public void teleport(double x, double y, double z) {
            Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            player.setPosition(x, y, z);
        }

    }
}

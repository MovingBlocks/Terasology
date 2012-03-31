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
import org.terasology.components.*;
import org.terasology.entityFactory.GelatinousCubeFactory;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.game.modes.IGameState;
import org.terasology.game.modes.StateSinglePlayer;
import org.terasology.logic.LocalPlayer;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.Helper;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    private Logger logger = Logger.getLogger(getClass().getName());

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
        }

        if (gse != null) {
            try {
                updateBinding();
                // Run the specified plugin
                gse.run(pluginName, _bind);
            } catch (ResourceException re) {
                Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ResourceException): " + pluginName + ", reason: " + re.toString(), re);
            } catch (ScriptException se) {
                Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ScriptException): " + pluginName + ", reason: " + se.toString(), se);
            }
        }
    }

    private void updateBinding() {
        _bind.setVariable("cfg", Config.getInstance());
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
        // Lets mess with the consoleString!
        consoleString = consoleString.trim();
        if (!(consoleString.startsWith("cmd.") || consoleString.startsWith("cfg."))) {
            consoleString = "cmd." + consoleString;
        }
        if (!consoleString.endsWith(")") && !consoleString.contains(" ") && !consoleString.contains(",")) {
            consoleString += "()";
        }
        updateBinding();
        GroovyShell shell = new GroovyShell(_bind);
        try {
            Object result = shell.evaluate(consoleString);
            if (result != null) {
                logger.log(Level.INFO, "Result [" + result + "] from '" + consoleString + "'");
            }
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
            BlockGroup blockGroup = BlockManager.getInstance().getBlock((byte) blockId).getBlockGroup();
            giveBlock(blockGroup, quantity);
        }

        public void giveBlock(String title) {
            giveBlock(title, 16);
        }

        public void giveBlock(String title, int quantity) {
            BlockGroup blockGroup = BlockManager.getInstance().getBlockGroup(title);
            giveBlock(blockGroup, quantity);
        }

        private void giveBlock(BlockGroup blockGroup, int quantity) {
            if (quantity < 1) return;

            BlockItemFactory factory = new BlockItemFactory(Terasology.getInstance().getCurrentGameState().getEntityManager());
            EntityRef item = factory.newInstance(blockGroup);
            ItemComponent itemComp = item.getComponent(ItemComponent.class);
            itemComp.stackCount = (byte)quantity;

            InventorySystem inventorySystem = CoreRegistry.get(ComponentSystemManager.class).get(InventorySystem.class);
            if (!inventorySystem.addItem(Terasology.getInstance().getActivePlayer().getEntity(), item)) {
                item.destroy();
            }
        }

        public void fullHealth() {
            /*Player player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            player.heal(player.getMaxHealthPoints() - player.getHealthPoints());*/
        }

        public void teleport(float x, float y, float z) {
            LocalPlayer player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            if (player != null) {
                LocationComponent location = player.getEntity().getComponent(LocationComponent.class);
                if (location != null) {
                    location.setWorldPosition(new Vector3f(x, y, z));
                }
            }
        }

        public void spawnTest() {
            LocalPlayer player = Terasology.getInstance().getActiveWorldRenderer().getPlayer();
            GelatinousCubeFactory factory = new GelatinousCubeFactory();
            factory.setEntityManager(Terasology.getInstance().getCurrentGameState().getEntityManager());
            factory.setRandom(Terasology.getInstance().getActiveWorldRenderer().getWorldProvider().getRandom());
            factory.generateGelatinousCube(new Vector3f((float)player.getPosition().x, (float)player.getPosition().y, (float)player.getPosition().z));
        }
        public void gotoWorld(String title) {
            IGameState state = Terasology.getInstance().getCurrentGameState();

            if (state instanceof StateSinglePlayer) {
                StateSinglePlayer spState = (StateSinglePlayer) state;
                spState.initWorld(title);
            }
        }

        public void gotoWorld(String title, String seed) {
            IGameState state = Terasology.getInstance().getCurrentGameState();

            if (state instanceof StateSinglePlayer) {
                StateSinglePlayer spState = (StateSinglePlayer) state;
                spState.initWorld(title, seed);
            }
        }

        public void dumpEntities() throws IOException {
            CoreRegistry.get(EntityManager.class).save(Helper.fixSavePath(new File("entityDump.txt")), EntityManager.SaveFormat.JSON);
        }
        
        public void debugCollision() {
            Config.getInstance().setDebugCollision(!Config.getInstance().isDebugCollision());
        }
        
        public void exit() {
            Terasology.getInstance().exit();
        }
    }
}

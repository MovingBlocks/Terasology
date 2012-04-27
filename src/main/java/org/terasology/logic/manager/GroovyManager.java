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
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Terasology;
import org.terasology.game.modes.GameState;
import org.terasology.game.modes.StateSinglePlayer;
import org.terasology.logic.LocalPlayer;
import org.terasology.model.blocks.BlockFamily;
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
            logger.log(Level.SEVERE, "Failed to initialize plugin (IOException): " + pluginName + ", reason: " + ioe.toString(), ioe);
        }

        if (gse != null) {
            try {
                updateBinding();
                // Run the specified plugin
                gse.run(pluginName, _bind);
            } catch (ResourceException re) {
                logger.log(Level.SEVERE, "Failed to execute plugin (ResourceException): " + pluginName + ", reason: " + re.toString(), re);
            } catch (ScriptException se) {
                logger.log(Level.SEVERE, "Failed to execute plugin (ScriptException): " + pluginName + ", reason: " + se.toString(), se);
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
        logger.log(Level.INFO, "Groovy console about to execute command: " + consoleString);
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
            BlockFamily blockFamily = BlockManager.getInstance().getBlock((byte) blockId).getBlockFamily();
            giveBlock(blockFamily, quantity);
        }

        public void giveBlock(String title) {
            giveBlock(title, 16);
        }

        public void giveBlock(String title, int quantity) {
            BlockFamily blockFamily = BlockManager.getInstance().getBlockFamily(title);
            giveBlock(blockFamily, quantity);
        }

        private void giveBlock(BlockFamily blockFamily, int quantity) {
            if (quantity < 1) return;

            BlockItemFactory factory = new BlockItemFactory(CoreRegistry.get(EntityManager.class), CoreRegistry.get(PrefabManager.class));
            EntityRef item = factory.newInstance(blockFamily, quantity);

            EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
            playerEntity.send(new ReceiveItemEvent(item));
            ItemComponent itemComp = item.getComponent(ItemComponent.class);
            if (itemComp != null && !itemComp.container.exists()) {
                item.destroy();
            }
        }

        private void giveItem(String itemPrefabName) {
            Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(itemPrefabName);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef item = CoreRegistry.get(EntityManager.class).create(prefab);
                EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
                playerEntity.send(new ReceiveItemEvent(item));
                ItemComponent itemComp = item.getComponent(ItemComponent.class);
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
            } else {
                giveBlock(itemPrefabName);
            }
        }

        public void fullHealth() {
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
            health.currentHealth = health.maxHealth;
            localPlayer.getEntity().saveComponent(health);
        }

        public void teleport(float x, float y, float z) {
            LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
            if (player != null) {
                LocationComponent location = player.getEntity().getComponent(LocationComponent.class);
                if (location != null) {
                    location.setWorldPosition(new Vector3f(x, y, z));
                }
            }
        }

        public void gotoWorld(String title) {
            CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer(title));
        }

        public void gotoWorld(String title, String seed) {
            CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer(title, seed));
        }

        public void dumpEntities() throws IOException {
            CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getDataPath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
        }
        
        public void debugCollision() {
            Config.getInstance().setDebugCollision(!Config.getInstance().isDebugCollision());
        }

        public void exit() {
            CoreRegistry.get(GameEngine.class).shutdown();
        }
    }
}

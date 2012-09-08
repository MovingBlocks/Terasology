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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.lwjgl.input.Keyboard;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.SimpleAIComponent;
import org.terasology.components.rendering.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.input.InputSystem;
import org.terasology.input.binds.BackwardsButton;
import org.terasology.input.binds.ForwardsButton;
import org.terasology.input.binds.LeftStrafeButton;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PathManager;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.StringConstants;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPickupComponent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
     * @return a ConsoleResult indicating command success and any display string
     */
    public ConsoleResult runGroovyShell(String consoleString) {
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
            if (result instanceof ConsoleResult) {
                return (ConsoleResult) result;
            }
            if (result != null) {
                logger.log(Level.INFO, "Result [" + result + "] from '" + consoleString + "'");
                return new ConsoleResult(true, result.toString());
            }
            return new ConsoleResult(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ConsoleResult(false);
        }
    }

    // TODO: Better implementation for commands
    public static class CommandHelper {

        public String listBlocks() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Used Blocks");
            stringBuilder.append(StringConstants.NEW_LINE);
            stringBuilder.append("-----------");
            stringBuilder.append(StringConstants.NEW_LINE);
            List<BlockUri> registeredBlocks = sortItems(BlockManager.getInstance().listRegisteredBlockUris());
            for (BlockUri blockUri : registeredBlocks) {
                stringBuilder.append(blockUri.toString());
                stringBuilder.append(StringConstants.NEW_LINE);
            }
            stringBuilder.append(StringConstants.NEW_LINE);

            stringBuilder.append("Available Blocks");
            stringBuilder.append(StringConstants.NEW_LINE);
            stringBuilder.append("----------------");
            stringBuilder.append(StringConstants.NEW_LINE);
            List<BlockUri> availableBlocks = sortItems(BlockManager.getInstance().listAvailableBlockUris());
            for (BlockUri blockUri : availableBlocks) {
                stringBuilder.append(blockUri.toString());
                stringBuilder.append(StringConstants.NEW_LINE);
            }
            return stringBuilder.toString();
        }

        public String listShapes() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Shapes");
            stringBuilder.append(StringConstants.NEW_LINE);
            stringBuilder.append("-----------");
            stringBuilder.append(StringConstants.NEW_LINE);
            List<AssetUri> sortedUris = sortItems(AssetManager.list(AssetType.SHAPE));
            for (AssetUri uri : sortedUris) {
                stringBuilder.append(uri.getSimpleString());
                stringBuilder.append(StringConstants.NEW_LINE);
            }
            return stringBuilder.toString();
        }

        public String listFreeShapeBlocks() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Free Shape Blocks");
            stringBuilder.append(StringConstants.NEW_LINE);
            stringBuilder.append("-----------------");
            stringBuilder.append(StringConstants.NEW_LINE);
            List<BlockUri> sortedUris = sortItems(BlockManager.getInstance().listShapelessBlockUris());
            for (BlockUri uri : sortedUris) {
                stringBuilder.append(uri.toString());
                stringBuilder.append(StringConstants.NEW_LINE);
            }
            return stringBuilder.toString();
        }

        private <T extends Comparable<T>> List<T> sortItems(Iterable<T> items) {
            List<T> result = Lists.newArrayList();
            for (T item : items) {
                result.add(item);
            }
            Collections.sort(result);
            return result;
        }

        public String giveBlock(String uri) {
            return giveBlock(uri, 16);
        }

        public String giveBlock(String uri, String shapeUri) {
            return giveBlock(uri, shapeUri, 16);
        }

        public String giveBlock(String uri, int quantity) {
            List<BlockUri> matchingUris = resolveBlockUri(uri);
            if (matchingUris.size() == 1) {
                BlockFamily blockFamily = BlockManager.getInstance().getBlockFamily(matchingUris.get(0));
                return giveBlock(blockFamily, quantity);
            } else if (matchingUris.isEmpty()) {
                return "No block found for '" + uri + "'";
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Non-unique block name, possible matches: ");
                Joiner.on(", ").appendTo(builder, matchingUris);
                return builder.toString();
            }
        }

        private List<BlockUri> resolveBlockUri(String uri) {
            List<BlockUri> matches = Lists.newArrayList();
            BlockUri straightUri = new BlockUri(uri);
            if (straightUri.isValid()) {
                if (BlockManager.getInstance().hasBlockFamily(straightUri)) {
                    matches.add(straightUri);
                }
            } else {
                for (String packageName : AssetManager.listPackages()) {
                    BlockUri modUri = new BlockUri(packageName, uri);
                    if (BlockManager.getInstance().hasBlockFamily(modUri)) {
                        matches.add(modUri);
                    }
                }
            }
            return matches;
        }

        private List<AssetUri> resolveShapeUri(String uri) {
            List<AssetUri> matches = Lists.newArrayList();
            AssetUri straightUri = new AssetUri(AssetType.SHAPE, uri);
            if (straightUri.isValid()) {
                Asset asset = AssetManager.load(straightUri);
                if (asset != null) {
                    matches.add(straightUri);
                }
            } else {
                for (String packageName : AssetManager.listPackages()) {
                    AssetUri modUri = new AssetUri(AssetType.SHAPE, packageName, uri);
                    Asset asset = AssetManager.tryLoad(modUri);
                    if (asset != null) {
                        matches.add(modUri);
                    }
                }
            }
            return matches;
        }

        public String giveBlock(String uri, String shapeUri, int quantity) {
            List<BlockUri> resolvedBlockUris = resolveBlockUri(uri);
            if (resolvedBlockUris.isEmpty()) {
                return "No block found for '" + uri + "'";
            } else if (resolvedBlockUris.size() > 1) {
                StringBuilder builder = new StringBuilder();
                builder.append("Non-unique block name, possible matches: ");
                Joiner.on(", ").appendTo(builder, resolvedBlockUris);
                return builder.toString();
            }
            List<AssetUri> resolvedShapeUris = resolveShapeUri(shapeUri);
            if (resolvedShapeUris.isEmpty()) {
                return "No shape found for '" + shapeUri + "'";
            } else if (resolvedShapeUris.size() > 1) {
                StringBuilder builder = new StringBuilder();
                builder.append("Non-unique shape name, possible matches: ");
                Iterator<AssetUri> shapeUris = resolvedShapeUris.iterator();
                while (shapeUris.hasNext()) {
                    builder.append(shapeUris.next().getSimpleString());
                    if (shapeUris.hasNext()) {
                        builder.append(", ");
                    }
                }
                return builder.toString();
            }

            BlockUri blockUri = new BlockUri(resolvedBlockUris.get(0).toString() + BlockUri.PACKAGE_SEPARATOR + resolvedShapeUris.get(0).getSimpleString());
            if (blockUri.isValid()) {
                return giveBlock(BlockManager.getInstance().getBlockFamily(blockUri), quantity);
            }
            return "Invalid block or shape";
        }

        private String giveBlock(BlockFamily blockFamily, int quantity) {
            if (quantity < 1) return "Here, have these zero (0) items just like you wanted.";

            BlockItemFactory factory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
            EntityRef item = factory.newInstance(blockFamily, quantity);
            if (!item.exists()) {
                return "Unknown block or item.";
            }
            EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
            playerEntity.send(new ReceiveItemEvent(item));
            ItemComponent itemComp = item.getComponent(ItemComponent.class);
            if (itemComp != null && !itemComp.container.exists()) {
                item.destroy();
            }
            return "Success";
        }

        private void setStepHeight(float amount) {
            EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
            CharacterMovementComponent comp = playerEntity.getComponent(CharacterMovementComponent.class);
            comp.stepHeight = amount;
        }

        private void spawnPrefab(String prefabName) {
            Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            Vector3f spawnPos = camera.getPosition();
            Vector3f offset = camera.getViewingDirection();
            offset.scale(3);
            spawnPos.add(offset);

            Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(prefabName);
            if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
                CoreRegistry.get(EntityManager.class).create(prefab, spawnPos);
            }
        }

        private void spawnDroppedBlock(String blockName) {
            Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            Vector3f spawnPos = camera.getPosition();
            Vector3f offset = camera.getViewingDirection();
            offset.scale(3);
            spawnPos.add(offset);

            Block block = BlockManager.getInstance().getBlock(blockName);
            if (block == null) return;

            Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab("core:droppedBlock");
            if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
                EntityRef blockEntity = CoreRegistry.get(EntityManager.class).create(prefab, spawnPos);
                MeshComponent blockMesh = blockEntity.getComponent(MeshComponent.class);
                BlockPickupComponent blockPickup = blockEntity.getComponent(BlockPickupComponent.class);
                blockPickup.blockFamily = block.getBlockFamily();
                blockMesh.mesh = block.getMesh();
                blockEntity.saveComponent(blockMesh);
                blockEntity.saveComponent(blockPickup);
            }
        }

        private String giveItem(String itemPrefabName) {
            Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(itemPrefabName);
            System.out.println("Found prefab: " + prefab);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef item = CoreRegistry.get(EntityManager.class).create(prefab);
                EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
                playerEntity.send(new ReceiveItemEvent(item));
                ItemComponent itemComp = item.getComponent(ItemComponent.class);
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                return "Success";
            } else {
                return giveBlock(itemPrefabName);
            }
        }

        public void setupForAZERTY() {
            InputSystem input = CoreRegistry.get(InputSystem.class);
            input.linkBindButtonToKey(Keyboard.KEY_Z, ForwardsButton.ID);
            input.linkBindButtonToKey(Keyboard.KEY_S, BackwardsButton.ID);
            input.linkBindButtonToKey(Keyboard.KEY_Q, LeftStrafeButton.ID);

        }

        public void bindKey(String key, String bind) {
            InputSystem input = CoreRegistry.get(InputSystem.class);
            input.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
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

        public void deslime() {
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            for (EntityRef ref : entityManager.iteratorEntities(SimpleAIComponent.class)) {
                ref.destroy();
                ;
            }
        }

        public void sleigh() {
            LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
            if (player != null) {
                CharacterMovementComponent moveComp = player.getEntity().getComponent(CharacterMovementComponent.class);
                if (moveComp.slopeFactor > 0.7f) {
                    moveComp.slopeFactor = 0.6f;
                } else {
                    moveComp.slopeFactor = 0.9f;
                }
            }
        }

        public void dumpEntities() throws IOException {
            CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getDataPath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
        }

        public void debugCollision() {
            Config.getInstance().setDebugCollision(!Config.getInstance().isDebugCollision());
        }

        public void setSpawn() {
            EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
            PlayerComponent spawn = playerEntity.getComponent(PlayerComponent.class);
            spawn.spawnPosition = playerEntity.getComponent(LocationComponent.class).getWorldPosition();
            playerEntity.saveComponent(spawn);
        }

        public void exit() {
            CoreRegistry.get(GameEngine.class).shutdown();
        }
    }
}

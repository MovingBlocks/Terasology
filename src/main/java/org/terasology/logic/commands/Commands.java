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

package org.terasology.logic.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3f;

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
import org.terasology.events.FullHealthEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.input.InputSystem;
import org.terasology.input.binds.BackwardsButton;
import org.terasology.input.binds.ForwardsButton;
import org.terasology.input.binds.LeftStrafeButton;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.manager.MessageManager.EMessageScope;
import org.terasology.logic.manager.CommandManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.manager.CommandManager.Command;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.StringConstants;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPickupComponent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * The controller class for all commands which can be executed through the in-game chat.
 * To add a command there needs to be an entry in the JSON file under "/data/console/commands.json" with a corresponding public method in this class.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class Commands implements CommandController {

    //==============================
    //        Helper Methods
    //==============================
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
    
    private <T extends Comparable<T>> List<T> sortItems(Iterable<T> items) {
        List<T> result = Lists.newArrayList();
        for (T item : items) {
            result.add(item);
        }
        Collections.sort(result);
        return result;
    }
    
    //==============================
    //          Commands
    //==============================
    public void listBlocks() {
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
        
        MessageManager.getInstance().addMessage(stringBuilder.toString(), EMessageScope.PRIVATE);
    }
    
    public void listItems() {
        StringBuilder items = new StringBuilder();
        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        Iterator<Prefab> it = prefMan.listPrefabs().iterator();
        while (it.hasNext()) {
            Prefab prefab = it.next();
            if (!items.toString().isEmpty()) {
                items.append("\n");
            }
            items.append(prefab.getName());
        }
        
        MessageManager.getInstance().addMessage(items.toString(), EMessageScope.PRIVATE);
    }

    public void listShapes() {
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

        MessageManager.getInstance().addMessage(stringBuilder.toString(), EMessageScope.PRIVATE);
    }

    public void listFreeShapeBlocks() {
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

        MessageManager.getInstance().addMessage(stringBuilder.toString(), EMessageScope.PRIVATE);
    }
    
    public void giveBlock(String uri) {
        giveBlock(uri, 16);
    }

    public void giveBlock(String uri, String shapeUri) {
        giveBlock(uri, shapeUri, 16);
    }

    public void giveBlock(String uri, int quantity) {
        List<BlockUri> matchingUris = resolveBlockUri(uri);
        if (matchingUris.size() == 1) {
            BlockFamily blockFamily = BlockManager.getInstance().getBlockFamily(matchingUris.get(0));
            
            giveBlock(blockFamily, quantity);
            
            return;
        } else if (matchingUris.isEmpty()) {
            MessageManager.getInstance().addMessage("No block found for '" + uri + "'", EMessageScope.PRIVATE);
            
            return;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            MessageManager.getInstance().addMessage(builder.toString(), EMessageScope.PRIVATE);
            
            return;
        }
    }

    public void giveBlock(String uri, String shapeUri, int quantity) {
        List<BlockUri> resolvedBlockUris = resolveBlockUri(uri);
        if (resolvedBlockUris.isEmpty()) {
            MessageManager.getInstance().addMessage("No block found for '" + uri + "'", EMessageScope.PRIVATE);
            
            return;
        } else if (resolvedBlockUris.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, resolvedBlockUris);
            MessageManager.getInstance().addMessage(builder.toString(), EMessageScope.PRIVATE);
            
            return;
        }
        List<AssetUri> resolvedShapeUris = resolveShapeUri(shapeUri);
        if (resolvedShapeUris.isEmpty()) {
            MessageManager.getInstance().addMessage("No shape found for '" + shapeUri + "'", EMessageScope.PRIVATE);
            
            return;
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
            
            return;
        }

        BlockUri blockUri = new BlockUri(resolvedBlockUris.get(0).toString() + BlockUri.PACKAGE_SEPARATOR + resolvedShapeUris.get(0).getSimpleString());
        if (blockUri.isValid()) {
            giveBlock(BlockManager.getInstance().getBlockFamily(blockUri), quantity);
            
            return;
        }
        
        MessageManager.getInstance().addMessage("Invalid block or shape", EMessageScope.PRIVATE);
    }

    private void giveBlock(BlockFamily blockFamily, int quantity) {
        if (quantity < 1) {
            MessageManager.getInstance().addMessage("Here, have these zero (0) items just like you wanted", EMessageScope.PRIVATE);
            
            return;
        }

        BlockItemFactory factory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
        EntityRef item = factory.newInstance(blockFamily, quantity);
        if (!item.exists()) {
            MessageManager.getInstance().addMessage("Unknown block or item", EMessageScope.PRIVATE);
            
            return;
        }
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        playerEntity.send(new ReceiveItemEvent(item));
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp != null && !itemComp.container.exists()) {
            item.destroy();
        }
        
        MessageManager.getInstance().addMessage("You received " + quantity +" blocks of " + blockFamily.getDisplayName(), EMessageScope.PRIVATE);
    }

    public void giveItem(String itemPrefabName) {
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
            MessageManager.getInstance().addMessage("You received an item of " + prefab.getName(), EMessageScope.PRIVATE);
        } else {
            giveBlock(itemPrefabName);
        }
    }
    
    public void health() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
        health.currentHealth = health.maxHealth;
        localPlayer.getEntity().send(new HealthChangedEvent(localPlayer.getEntity(), health.currentHealth, health.maxHealth));
        localPlayer.getEntity().saveComponent(health);
    }
    
    public void health(int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
        health.currentHealth = amount;
        if (health.currentHealth >= health.maxHealth) {
            health.currentHealth = health.maxHealth;
            localPlayer.getEntity().send(new FullHealthEvent(localPlayer.getEntity(), health.maxHealth));
        } else if (health.currentHealth <= 0) {
            health.currentHealth = 0;
            localPlayer.getEntity().send(new NoHealthEvent(localPlayer.getEntity(), health.maxHealth));
        } else {
            localPlayer.getEntity().send(new HealthChangedEvent(localPlayer.getEntity(), health.currentHealth, health.maxHealth));
        }
        
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
    
    public void loadWorld(String worldName, String seed) {
        
    }

    public void dumpEntities() throws IOException {
        CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getDataPath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
    }

    public void collision() {
        Config.getInstance().setDebugCollision(!Config.getInstance().isDebugCollision());
    }
    
    public void spawnLoot() {
        
    }

    public void bindKey(String key, String bind) {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
    }
    
    public void AZERTY() {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.KEY_Z, ForwardsButton.ID);
        input.linkBindButtonToKey(Keyboard.KEY_S, BackwardsButton.ID);
        input.linkBindButtonToKey(Keyboard.KEY_Q, LeftStrafeButton.ID);

    }
    
    public void spawnPrefab(String prefabName) {
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
    
    public void destroyAI() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef ref : entityManager.iteratorEntities(SimpleAIComponent.class)) {
            ref.destroy();
        }
    }
    
    public void stepHeight(float amount) {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        CharacterMovementComponent comp = playerEntity.getComponent(CharacterMovementComponent.class);
        comp.stepHeight = amount;
    }
    
    public void spawnBlock(String blockName) {
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
    
    public void setSpawn() {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        PlayerComponent spawn = playerEntity.getComponent(PlayerComponent.class);
        spawn.spawnPosition = playerEntity.getComponent(LocationComponent.class).getWorldPosition();
        playerEntity.saveComponent(spawn);
    }
    
    public void help() {
        StringBuilder msg = new StringBuilder();
        List<Command> commands = CommandManager.getInstance().getCommandList();
        for (Command cmd : commands) {
            if (!msg.toString().isEmpty()) {
                msg.append("\n");
            }
            msg.append(cmd.getName() + " - " + cmd.getShortDescription());
        }
        MessageManager.getInstance().addMessage(msg.toString(), EMessageScope.PRIVATE);
    }
    
    public void help(String command) {
        Command cmd = CommandManager.getInstance().getCommand(command);
        if (cmd == null) {
            MessageManager.getInstance().addMessage("No help available for command '" + command + "'. Unknown command.", EMessageScope.PRIVATE);
        } else {
            StringBuilder msg = new StringBuilder();
            
            msg.append("=====================================================================================================================");
            msg.append("\n" + cmd.getName());
            for (String param : cmd.getParameter()) {
                msg.append(" <" + param + ">");
            }
            if (!cmd.getShortDescription().isEmpty()) {
                msg.append(" - " + cmd.getShortDescription());
            }
            
            if (!cmd.getLongDescription().isEmpty()) {
                msg.append("\n\n" + cmd.getLongDescription());
            }
            
            if (cmd.getExamples().length > 0) {
                msg.append("\n\nExamples:");
                for (String example : cmd.getExamples()) {
                    msg.append("\n" + example);
                }
            }
            
            msg.append("\n=====================================================================================================================");
            
            MessageManager.getInstance().addMessage(msg.toString(), EMessageScope.PRIVATE);
        }
    }
    
    public void exit() {
        CoreRegistry.get(GameEngine.class).shutdown();
    }
}

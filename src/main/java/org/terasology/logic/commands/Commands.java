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

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.lwjgl.input.Keyboard;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.components.HealthComponent;
import org.terasology.components.HierarcialAIComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.SimpleAIComponent;
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
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.CommandManager;
import org.terasology.logic.manager.CommandManager.CommandInfo;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.manager.MessageManager.EMessageScope;
import org.terasology.logic.manager.PathManager;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.StringConstants;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPickupComponent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The controller class for all commands which can be executed through the in-game chat.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class Commands implements CommandProvider {
	
	
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

    //TODO  Add multiplayer commands, when ready for that
    //==============================
    //          Commands
    //==============================
    @Command(shortDescription = "List all available blocks")
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

    @Command(shortDescription = "Lists all blocks by category")
    public void listBlocksByCategory() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String category : BlockManager.getInstance().getBlockCategories()) {
            stringBuilder.append(category);
            stringBuilder.append(StringConstants.NEW_LINE);
            stringBuilder.append("-----------");
            stringBuilder.append(StringConstants.NEW_LINE);
            List<BlockUri> categoryBlocks = sortItems(BlockManager.getInstance().getBlockFamiliesWithCategory(category));
            for (BlockUri uri : categoryBlocks) {
                stringBuilder.append(uri.toString());
                stringBuilder.append(StringConstants.NEW_LINE);
            }
            stringBuilder.append(StringConstants.NEW_LINE);
        }
        MessageManager.getInstance().addMessage(stringBuilder.toString(), EMessageScope.PRIVATE);
    }

    @Command(shortDescription = "Lists all available items")
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

    @Command(shortDescription = "Lists all available shapes")
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

    @Command(shortDescription = "Lists available free shape blocks", helpText = "Lists all the available free shape blocks. These blocks can be created with any shape.")
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

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts 16 of the given block into your inventory")
    public void giveBlock(@CommandParam(name = "blockName") String uri) {
        giveBlock(uri, 16);
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts 16 blocks of the given block, with the given shape, into your inventory")
    public void giveBlock(@CommandParam(name = "blockName") String uri, @CommandParam(name = "shapeName") String shapeUri) {
        giveBlock(uri, shapeUri, 16);
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts a desired number of the given block into your inventory")
    public void giveBlock(@CommandParam(name = "blockName") String uri, @CommandParam(name = "quantity") int quantity) {
        List<BlockUri> matchingUris = resolveBlockUri(uri);
        if (matchingUris.size() == 1) {
            BlockFamily blockFamily = BlockManager.getInstance().getBlockFamily(matchingUris.get(0));

            giveBlock(blockFamily, quantity);
        } else if (matchingUris.isEmpty()) {
            MessageManager.getInstance().addMessage("No block found for '" + uri + "'", EMessageScope.PRIVATE);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            MessageManager.getInstance().addMessage(builder.toString(), EMessageScope.PRIVATE);
        }
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts a desired number of the given block with the give shape into your inventory")
    public void giveBlock(@CommandParam(name = "blockName") String uri, @CommandParam(name = "shapeName") String shapeUri, @CommandParam(name = "quantity") int quantity) {
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

        MessageManager.getInstance().addMessage("You received " + quantity + " blocks of " + blockFamily.getDisplayName(), EMessageScope.PRIVATE);
    }

    @Command(shortDescription = "Adds an item to your inventory")
    public void giveItem(@CommandParam(name = "prefabId or blockName") String itemPrefabName) {
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

    @Command(shortDescription = "Restores your health to max")
    public void health() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
        health.currentHealth = health.maxHealth;
        localPlayer.getEntity().send(new FullHealthEvent(localPlayer.getEntity(), health.maxHealth));
        localPlayer.getEntity().saveComponent(health);
    }

    @Command(shortDescription = "Restores your health by an amount")
    public void health(@CommandParam(name = "amount") int amount) {
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

    @Command(shortDescription = "Kill Yourself")
    public void kill() {
    	LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
    	localPlayer.getEntity().send(new NoHealthEvent(localPlayer.getEntity(), health.maxHealth));
    }
    
    @Command(shortDescription = "Damage you by an amount")
    public void damage(@CommandParam(name="amount") int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
        health.currentHealth -= amount;
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
    
    @Command(shortDescription = "Teleports you to a location")
    public void teleport(@CommandParam(name = "x") float x, @CommandParam(name = "y") float y, @CommandParam(name = "z") float z) {
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
        if (player != null) {
            LocationComponent location = player.getEntity().getComponent(LocationComponent.class);
            if (location != null) {
                location.setWorldPosition(new Vector3f(x, y, z));
            }
        }
    }

    @Command(shortDescription = "Writes out information on all entities to a text file for debugging")
    public void dumpEntities() throws IOException {
        CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getDataPath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
    }

    @Command(shortDescription = "Maps a key to a function")
    public void bindKey(@CommandParam(name = "key") String key, @CommandParam(name = "function") String bind) {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
    }

    @Command(shortDescription = "Switches to typical key binds for AZERTY")
    public void AZERTY() {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.KEY_Z, "engine:forwards");
        input.linkBindButtonToKey(Keyboard.KEY_S, "engine:backwards");
        input.linkBindButtonToKey(Keyboard.KEY_Q, "engine:left");
    }

    @Command(shortDescription = "Switches to typical key binds for NEO 2 keyboard layout")
    public void NEO() {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.KEY_V, "engine:forwards");
        input.linkBindButtonToKey(Keyboard.KEY_I, "engine:backwards");
        input.linkBindButtonToKey(Keyboard.KEY_U, "engine:left");
        input.linkBindButtonToKey(Keyboard.KEY_A, "engine:right");
        input.linkBindButtonToKey(Keyboard.KEY_L, "engine:useItem");
        input.linkBindButtonToKey(Keyboard.KEY_G, "engine:inventory");
    }

    @Command(shortDescription = "Spawns an instance of a prefab in the world")
    public void spawnPrefab(@CommandParam(name = "prefabId") String prefabName) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = new Vector3f(camera.getViewingDirection());
        offset.scale(2);
        spawnPos.add(offset);
        Vector3f dir = new Vector3f(camera.getViewingDirection());
        dir.y = 0;
        if (dir.lengthSquared() > 0.001f) {
            dir.normalize();
        } else {
            dir.set(0, 0, 1);
        }
        Quat4f rotation = QuaternionUtil.shortestArcQuat(new Vector3f(0, 0, 1), dir, new Quat4f());

        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(prefabName);
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            CoreRegistry.get(EntityManager.class).create(prefab, spawnPos, rotation);
        }
    }

    @Command(shortDescription = "Destroys all AI in the world")
    public void destroyAI() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef ref : entityManager.iteratorEntities(SimpleAIComponent.class)) {
            ref.destroy();
        }
        for (EntityRef ref : entityManager.iteratorEntities(HierarcialAIComponent.class)) {
            ref.destroy();
        }
    }

    @Command(shortDescription = "Sets the height the player can step up")
    public void stepHeight(@CommandParam(name = "height") float amount) {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        CharacterMovementComponent comp = playerEntity.getComponent(CharacterMovementComponent.class);
        comp.stepHeight = amount;
    }

    @Command(shortDescription = "Spawns a block in front of the player")
    public void spawnBlock(@CommandParam(name = "blockName") String blockName) {
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

    @Command(shortDescription = "Places a block in front of the player")
    public void placeBlock(@CommandParam(name = "blockName") String blockName) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        Block block = BlockManager.getInstance().getBlock(blockName);
        if (block == null) return;

        WorldProvider provider = CoreRegistry.get(WorldProvider.class);
        if (provider != null) {
            Block oldBlock = provider.getBlock(spawnPos);
            provider.setBlock((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z, block, oldBlock);
        }
    }

    @Command(shortDescription = "Toggles the maximum slope the player can walk up")
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

    @Command(shortDescription = "Sets the respawn position of the player")
    public void setSpawn() {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        PlayerComponent spawn = playerEntity.getComponent(PlayerComponent.class);
        spawn.spawnPosition = playerEntity.getComponent(LocationComponent.class).getWorldPosition();
        playerEntity.saveComponent(spawn);
    }

    @Command(shortDescription = "General help")
    public void help() {
        StringBuilder msg = new StringBuilder();
        List<CommandInfo> commands = CoreRegistry.get(CommandManager.class).getCommandList();
        for (CommandInfo cmd : commands) {
            if (!msg.toString().isEmpty()) {
                msg.append("\n");
            }
            msg.append(cmd.getUsageMessage() + " - " + cmd.getShortDescription());
        }
        MessageManager.getInstance().addMessage(msg.toString(), EMessageScope.PRIVATE);
    }

    @Command(shortDescription = "Detailed help on a command")
    public void help(@CommandParam(name = "command") String command) {
        Collection<CommandInfo> cmdCollection = CoreRegistry.get(CommandManager.class).getCommand(command);
        if (cmdCollection.isEmpty()) {
            MessageManager.getInstance().addMessage("No help available for command '" + command + "'. Unknown command.", EMessageScope.PRIVATE);
        } else {
            StringBuilder msg = new StringBuilder();

            for (CommandInfo cmd : cmdCollection) {
                msg.append("=====================================================================================================================");
                msg.append(StringConstants.NEW_LINE);
                msg.append(cmd.getUsageMessage());
                msg.append(StringConstants.NEW_LINE);
                msg.append("=====================================================================================================================");
                msg.append(StringConstants.NEW_LINE);
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(StringConstants.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(StringConstants.NEW_LINE);
                } else if (!cmd.getShortDescription().isEmpty()) {
                    msg.append(cmd.getShortDescription());
                    msg.append(StringConstants.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(StringConstants.NEW_LINE);
                }
                msg.append(StringConstants.NEW_LINE);
            }
            MessageManager.getInstance().addMessage(msg.toString(), EMessageScope.PRIVATE);
        }
    }

    @Command(shortDescription = "Exits the game")
    public void exit() {
        CoreRegistry.get(GameEngine.class).shutdown();
    }
}

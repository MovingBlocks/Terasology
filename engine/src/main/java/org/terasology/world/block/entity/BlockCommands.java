/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world.block.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.console.Message;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import javax.vecmath.Vector3f;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem
@Share(BlockCommands.class)
public class BlockCommands extends BaseComponentSystem {

    // TODO: Remove once camera is handled better
    @In
    private WorldRenderer renderer;

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider world;

    @In
    private PrefabManager prefabManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    private BlockItemFactory blockItemFactory;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
    }

    // TODO: Fix this up for multiplayer (cannot at the moment due to the use of camera)
    @Command(shortDescription = "Places a block in front of the player", helpText = "Places the specified block in front of the player. " +
            "The block is set directly into the world and might override existing blocks. After placement the block can be destroyed like any regular placed block.")
    public String placeBlock(@CommandParam("blockName") String blockName) {
        Camera camera = renderer.getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily blockFamily;

        List<BlockUri> matchingUris = blockManager.resolveAllBlockFamilyUri(blockName);
        if (matchingUris.size() == 1) {
            blockFamily = blockManager.getBlockFamily(matchingUris.get(0));

        } else if (matchingUris.isEmpty()) {
            throw new IllegalArgumentException("No block found for '" + blockName + "'");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            return builder.toString();
        }

        if (world != null) {
            world.setBlock(new Vector3i((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z), blockFamily.getArchetypeBlock());

            StringBuilder builder = new StringBuilder();
            builder.append(blockFamily.getArchetypeBlock());
            builder.append(" block placed at position (");
            builder.append((int) spawnPos.x).append((int) spawnPos.y).append((int) spawnPos.z).append(")");
            return builder.toString();
        }
        throw new IllegalArgumentException("Sorry, something went wrong!");
    }

    @Command(shortDescription = "Lists all available items (prefabs)")
    public String listItems() {

        List<String> stringItems = Lists.newArrayList();

        for (Prefab prefab : prefabManager.listPrefabs()) {
            stringItems.add(prefab.getName());
        }

        Collections.sort(stringItems);

        StringBuilder items = new StringBuilder();
        for (String item : stringItems) {
            if (!items.toString().isEmpty()) {
                items.append(Message.NEW_LINE);
            }
            items.append(item);
        }

        return items.toString();
    }

    @Command(shortDescription = "List all available blocks")
    public String listBlocks() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Used Blocks");
        stringBuilder.append(Message.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(Message.NEW_LINE);
        List<BlockUri> registeredBlocks = sortItems(blockManager.listRegisteredBlockUris());
        for (BlockUri blockUri : registeredBlocks) {
            stringBuilder.append(blockUri.toString());
            stringBuilder.append(Message.NEW_LINE);
        }
        stringBuilder.append(Message.NEW_LINE);

        stringBuilder.append("Available Blocks");
        stringBuilder.append(Message.NEW_LINE);
        stringBuilder.append("----------------");
        stringBuilder.append(Message.NEW_LINE);
        List<BlockUri> availableBlocks = sortItems(blockManager.listAvailableBlockUris());
        for (BlockUri blockUri : availableBlocks) {
            stringBuilder.append(blockUri.toString());
            stringBuilder.append(Message.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists all blocks by category")
    public String listBlocksByCategory() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String category : blockManager.getBlockCategories()) {
            stringBuilder.append(category);
            stringBuilder.append(Message.NEW_LINE);
            stringBuilder.append("-----------");
            stringBuilder.append(Message.NEW_LINE);
            List<BlockUri> categoryBlocks = sortItems(blockManager.getBlockFamiliesWithCategory(category));
            for (BlockUri uri : categoryBlocks) {
                stringBuilder.append(uri.toString());
                stringBuilder.append(Message.NEW_LINE);
            }
            stringBuilder.append(Message.NEW_LINE);
        }
        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists all available shapes")
    public String listShapes() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Shapes");
        stringBuilder.append(Message.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(Message.NEW_LINE);
        List<AssetUri> sortedUris = sortItems(Assets.list(AssetType.SHAPE));
        for (AssetUri uri : sortedUris) {
            stringBuilder.append(uri.toSimpleString());
            stringBuilder.append(Message.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists available free shape blocks", helpText = "Lists all the available free shape blocks. These blocks can be created with any shape.")
    public String listFreeShapeBlocks() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Free Shape Blocks");
        stringBuilder.append(Message.NEW_LINE);
        stringBuilder.append("-----------------");
        stringBuilder.append(Message.NEW_LINE);
        List<BlockUri> sortedUris = sortItems(blockManager.listFreeformBlockUris());
        for (BlockUri uri : sortedUris) {
            stringBuilder.append(uri.toString());
            stringBuilder.append(Message.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts 16 of the given block into your inventory", runOnServer = true)
    public String giveBlock(@CommandParam("blockName") String uri, EntityRef client) {
        return giveBlock(uri, 16, client);
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts a desired number of the given block into your inventory", runOnServer = true)
    public String giveBlock(@CommandParam("blockName") String uri, @CommandParam("quantity") int quantity, EntityRef client) {
        List<BlockUri> matchingUris = blockManager.resolveAllBlockFamilyUri(uri);
        if (matchingUris.size() == 1) {
            BlockFamily blockFamily = blockManager.getBlockFamily(matchingUris.get(0));
            return giveBlock(blockFamily, quantity, client);
        } else if (matchingUris.isEmpty()) {
            throw new IllegalArgumentException("No block found for '" + uri + "'");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            return builder.toString();
        }
    }

    @Command(shortDescription = "Adds a block to your inventory",
            helpText = "Puts a desired number of the given block with the give shape into your inventory",
            runOnServer = true)
    public String giveBlock(@CommandParam("blockName") String uri, @CommandParam("shapeName") String shapeUri, @CommandParam("quantity") int quantity, EntityRef client) {
        List<BlockUri> resolvedBlockUris = blockManager.resolveAllBlockFamilyUri(uri);
        if (resolvedBlockUris.isEmpty()) {
            throw new IllegalArgumentException("No block found for '" + uri + "'");
        } else if (resolvedBlockUris.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, resolvedBlockUris);
            return builder.toString();
        }
        List<AssetUri> resolvedShapeUris = Assets.resolveAllUri(AssetType.SHAPE, shapeUri);
        if (resolvedShapeUris.isEmpty()) {
            throw new IllegalArgumentException("No shape found for '" + shapeUri + "'");
        } else if (resolvedShapeUris.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique shape name, possible matches: ");
            Iterator<AssetUri> shapeUris = resolvedShapeUris.iterator();
            while (shapeUris.hasNext()) {
                builder.append(shapeUris.next().toSimpleString());
                if (shapeUris.hasNext()) {
                    builder.append(", ");
                }
            }

            return builder.toString();
        }

        BlockUri blockUri = new BlockUri(resolvedBlockUris.get(0).toString() + BlockUri.MODULE_SEPARATOR + resolvedShapeUris.get(0).toSimpleString());
        if (blockUri.isValid()) {
            return giveBlock(blockManager.getBlockFamily(blockUri), quantity, client);
        }

        throw new IllegalArgumentException("Invalid block or shape");
    }

    /**
     * Actual implementation of the giveBlock command.
     *
     * @param blockFamily the block family of the queried block
     * @param quantity    the number of blocks that are queried
     */
    private String giveBlock(BlockFamily blockFamily, int quantity, EntityRef client) {
        if (quantity < 1) {
            return "Here, have these zero (0) items just like you wanted";
        }

        EntityRef item = blockItemFactory.newInstance(blockFamily, quantity);
        if (!item.exists()) {
            throw new IllegalArgumentException("Unknown block or item");
        }
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;

        if (!inventoryManager.giveItem(playerEntity, playerEntity, item)) {
            item.destroy();
        }

        return "You received " + quantity + " blocks of " + blockFamily.getDisplayName();
    }

    private <T extends Comparable<? super T>> List<T> sortItems(Iterable<T> items) {
        List<T> result = Lists.newArrayList();
        for (T item : items) {
            result.add(item);
        }
        Collections.sort(result);
        return result;
    }

}

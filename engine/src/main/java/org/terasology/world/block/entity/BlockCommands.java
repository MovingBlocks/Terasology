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
import org.terasology.logic.console.Message;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

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

    @Command(shortDescription = "Lists all available items (prefabs)",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
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

    @Command(shortDescription = "List all available blocks", requiredPermission = PermissionManager.CHEAT_PERMISSION)
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

    @Command(shortDescription = "Lists all blocks by category", requiredPermission = PermissionManager.CHEAT_PERMISSION)
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

    @Command(shortDescription = "Lists all available shapes",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
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

    @Command(shortDescription = "Lists available free shape blocks",
            helpText = "Lists all the available free shape blocks. These blocks can be created with any shape.",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
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

    @Command(shortDescription = "Adds a block to your inventory",
            helpText = "Puts a desired number of the given block with the give shape into your inventory",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String giveBlock(
            @Sender EntityRef sender,
            @CommandParam("blockName") String uri,
            @CommandParam(value = "quantity", required = false) Integer quantityParam,
            @CommandParam(value = "shapeName", required = false) String shapeUriParam) {
        int quantity = quantityParam != null ? quantityParam : 16;
        if (shapeUriParam == null) {
            List<BlockUri> matchingUris = blockManager.resolveAllBlockFamilyUri(uri);
            if (matchingUris.size() == 1) {
                BlockFamily blockFamily = blockManager.getBlockFamily(matchingUris.get(0));
                return giveBlock(blockFamily, quantity, sender);
            } else if (matchingUris.isEmpty()) {
                throw new IllegalArgumentException("No block found for '" + uri + "'");
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Non-unique block name, possible matches: ");
                Joiner.on(", ").appendTo(builder, matchingUris);
                return builder.toString();
            }
        } else {
            List<BlockUri> resolvedBlockUris = blockManager.resolveAllBlockFamilyUri(uri);
            if (resolvedBlockUris.isEmpty()) {
                throw new IllegalArgumentException("No block found for '" + uri + "'");
            } else if (resolvedBlockUris.size() > 1) {
                StringBuilder builder = new StringBuilder();
                builder.append("Non-unique block name, possible matches: ");
                Joiner.on(", ").appendTo(builder, resolvedBlockUris);
                return builder.toString();
            }
            List<AssetUri> resolvedShapeUris = Assets.resolveAllUri(AssetType.SHAPE, shapeUriParam);
            if (resolvedShapeUris.isEmpty()) {
                throw new IllegalArgumentException("No shape found for '" + shapeUriParam + "'");
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
                return giveBlock(blockManager.getBlockFamily(blockUri), quantity, sender);
            }

            throw new IllegalArgumentException("Invalid block or shape");
        }
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

/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.asset.Asset;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.Share;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.StringConstants;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem()
@Share(BlockCommands.class)
public class BlockCommands implements ComponentSystem {

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

    private BlockItemFactory blockItemFactory;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Places a block in front of the player", helpText = "Places the specified block in " +
            "front of the player. The block is set directly into the world and might override existing blocks. After " +
            "placement the block can be destroyed like any regular placed block.")
    public String placeBlock(@CommandParam(name = "blockName") String blockName) {
        Camera camera = renderer.getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily blockFamily;

        List<BlockUri> matchingUris = blockManager.resolveBlockUri(blockName);
        if (matchingUris.size() == 1) {
            blockFamily = blockManager.getBlockFamily(matchingUris.get(0));

        } else if (matchingUris.isEmpty()) {
            return "No block found for '" + blockName + "'";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            return builder.toString();
        }

        if (world != null) {
            Block oldBlock = world.getBlock((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z);
            world.setBlock((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z, blockFamily.getArchetypeBlock(), oldBlock);

            StringBuilder builder = new StringBuilder();
            builder.append(blockFamily.getArchetypeBlock());
            builder.append(" block placed at position (");
            builder.append((int) spawnPos.x).append((int) spawnPos.y).append((int) spawnPos.z).append(")");
            return builder.toString();
        }
        return "Sorry, something went wrong!";
    }

    @Command(shortDescription = "Lists all available items")
    public String listItems() {
        StringBuilder items = new StringBuilder();
        for (Prefab prefab : prefabManager.listPrefabs()) {
            if (!items.toString().isEmpty()) {
                items.append("\n");
            }
            items.append(prefab.getName());
        }

        return items.toString();
    }

    @Command(shortDescription = "List all available blocks")
    public String listBlocks() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Used Blocks");
        stringBuilder.append(StringConstants.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(StringConstants.NEW_LINE);
        List<BlockUri> registeredBlocks = sortItems(blockManager.listRegisteredBlockUris());
        for (BlockUri blockUri : registeredBlocks) {
            stringBuilder.append(blockUri.toString());
            stringBuilder.append(StringConstants.NEW_LINE);
        }
        stringBuilder.append(StringConstants.NEW_LINE);

        stringBuilder.append("Available Blocks");
        stringBuilder.append(StringConstants.NEW_LINE);
        stringBuilder.append("----------------");
        stringBuilder.append(StringConstants.NEW_LINE);
        List<BlockUri> availableBlocks = sortItems(blockManager.listAvailableBlockUris());
        for (BlockUri blockUri : availableBlocks) {
            stringBuilder.append(blockUri.toString());
            stringBuilder.append(StringConstants.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists all blocks by category")
    public String listBlocksByCategory() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String category : blockManager.getBlockCategories()) {
            stringBuilder.append(category);
            stringBuilder.append(StringConstants.NEW_LINE);
            stringBuilder.append("-----------");
            stringBuilder.append(StringConstants.NEW_LINE);
            List<BlockUri> categoryBlocks = sortItems(blockManager.getBlockFamiliesWithCategory(category));
            for (BlockUri uri : categoryBlocks) {
                stringBuilder.append(uri.toString());
                stringBuilder.append(StringConstants.NEW_LINE);
            }
            stringBuilder.append(StringConstants.NEW_LINE);
        }
        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists all available shapes")
    public String listShapes() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Shapes");
        stringBuilder.append(StringConstants.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(StringConstants.NEW_LINE);
        List<AssetUri> sortedUris = sortItems(Assets.list(AssetType.SHAPE));
        for (AssetUri uri : sortedUris) {
            stringBuilder.append(uri.getSimpleString());
            stringBuilder.append(StringConstants.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists available free shape blocks", helpText = "Lists all the available free shape blocks. These blocks can be created with any shape.")
    public String listFreeShapeBlocks() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Free Shape Blocks");
        stringBuilder.append(StringConstants.NEW_LINE);
        stringBuilder.append("-----------------");
        stringBuilder.append(StringConstants.NEW_LINE);
        List<BlockUri> sortedUris = sortItems(blockManager.listFreeformBlockUris());
        for (BlockUri uri : sortedUris) {
            stringBuilder.append(uri.toString());
            stringBuilder.append(StringConstants.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts 16 of the given block into your inventory")
    public String giveBlock(@CommandParam(name = "blockName") String uri) {
        return giveBlock(uri, 16);
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts 16 blocks of the given block, with the given shape, into your inventory")
    public String giveBlock(@CommandParam(name = "blockName") String uri, @CommandParam(name = "shapeName") String shapeUri) {
        return giveBlock(uri, shapeUri, 16);
    }

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts a desired number of the given block into your inventory")
    public String giveBlock(@CommandParam(name = "blockName") String uri, @CommandParam(name = "quantity") int quantity) {
        List<BlockUri> matchingUris = blockManager.resolveBlockUri(uri);
        if (matchingUris.size() == 1) {
            BlockFamily blockFamily = blockManager.getBlockFamily(matchingUris.get(0));
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

    @Command(shortDescription = "Adds a block to your inventory", helpText = "Puts a desired number of the given block with the give shape into your inventory")
    public String giveBlock(@CommandParam(name = "blockName") String uri, @CommandParam(name = "shapeName") String shapeUri, @CommandParam(name = "quantity") int quantity) {
        List<BlockUri> resolvedBlockUris = blockManager.resolveBlockUri(uri);
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
            return giveBlock(blockManager.getBlockFamily(blockUri), quantity);
        }

        return "Invalid block or shape";
    }

    /**
     * Actual implementation of the giveBlock command.
     *
     * @param blockFamily the block family of the queried block
     * @param quantity    the number of blocks that are queried
     */
    private String giveBlock(BlockFamily blockFamily, int quantity) {
        if (quantity < 1) {
            return "Here, have these zero (0) items just like you wanted";
        }

        EntityRef item = blockItemFactory.newInstance(blockFamily, quantity);
        if (!item.exists()) {
            return "Unknown block or item";
        }
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        if (!inventoryManager.giveItem(playerEntity, item)) {
            item.destroy();
        }

        return "You received " + quantity + " blocks of " + blockFamily.getDisplayName();
    }


    /**
     * Retrieve all {@code AssetUri}s that match the given string pattern.
     * <p/>
     * In order to find all fitting shapes, all asset packages are searched and a list of matching asset uris is returned.
     *
     * @param uri the uri pattern to match
     * @return a list of matching asset uris
     */
    private List<AssetUri> resolveShapeUri(String uri) {
        List<AssetUri> matches = Lists.newArrayList();
        AssetUri straightUri = new AssetUri(AssetType.SHAPE, uri);
        if (straightUri.isValid()) {
            Asset asset = Assets.get(straightUri);
            if (asset != null) {
                matches.add(straightUri);
            }
        } else {
            for (String packageName : Assets.listModules()) {
                AssetUri modUri = new AssetUri(AssetType.SHAPE, packageName, uri);
                Asset asset = Assets.get(modUri);
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

}

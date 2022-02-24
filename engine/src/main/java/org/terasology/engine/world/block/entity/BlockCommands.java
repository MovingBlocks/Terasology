// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.cameraTarget.TargetSystem;
import org.terasology.engine.logic.characters.GazeAuthoritySystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.internal.WorldProviderCoreImpl;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockExplorer;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.items.BlockItemFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains a series of handy game console commands associated with blocks.
 */
@RegisterSystem
@Share(BlockCommands.class)
public class BlockCommands extends BaseComponentSystem {
    private TargetSystem targetSystem;
    // TODO: Remove once camera is handled better
    @In
    private WorldRenderer renderer;

    @In
    private AssetManager assetManager;

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider world;

    @In
    private PrefabManager prefabManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer player;

    @In
    private Physics physics;

    @In
    private BlockEntityRegistry blockRegistry;

    @In
    private WorldProviderCoreImpl worldImpl;

    private BlockItemFactory blockItemFactory;
    private BlockExplorer blockExplorer;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
        blockExplorer = new BlockExplorer(assetManager);
        targetSystem = new TargetSystem(blockRegistry, physics);
    }

    @Command(shortDescription = "List all available blocks\nYou can filter by adding the beginning of words after the" +
            " commands, e.g.: \"listBlocks engine: core\" will list all blocks from the engine and modules starting with 'core'",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String listBlocks(@CommandParam(value = "startsWith", required = false) String[] startsWith) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Used Blocks");
        stringBuilder.append(Console.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(Console.NEW_LINE);
        List<BlockUri> registeredBlocks = sortItems(blockManager.listRegisteredBlockUris());
        for (BlockUri blockUri : registeredBlocks) {
            if (!uriStartsWithAnyString(blockUri.toString(), startsWith)) {
                continue;
            }
            stringBuilder.append(blockUri.toString());
            stringBuilder.append(Console.NEW_LINE);
        }
        stringBuilder.append(Console.NEW_LINE);

        stringBuilder.append("Available Blocks");
        stringBuilder.append(Console.NEW_LINE);
        stringBuilder.append("----------------");
        stringBuilder.append(Console.NEW_LINE);
        List<BlockUri> availableBlocks = sortItems(blockExplorer.getAvailableBlockFamilies());
        for (BlockUri blockUri : availableBlocks) {
            if (!uriStartsWithAnyString(blockUri.toString(), startsWith)) {
                continue;
            }
            stringBuilder.append(blockUri.toString());
            stringBuilder.append(Console.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists all available shapes\nYou can filter by adding the beginning of words after the" +
            " commands, e.g.: \"listShapes engine: core\" will list all shapes from the engine and modules starting with 'core'",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String listShapes(@CommandParam(value = "startsWith", required = false) String[] startsWith) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Shapes");
        stringBuilder.append(Console.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(Console.NEW_LINE);
        List<ResourceUrn> sortedUris = sortItems(Assets.list(BlockShape.class));
        for (ResourceUrn uri : sortedUris) {
            if (!uriStartsWithAnyString(uri.toString(), startsWith)) {
                continue;
            }
            stringBuilder.append(uri.toString());
            stringBuilder.append(Console.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Lists available free shape blocks",
            helpText = "Lists all the available free shape blocks. These blocks can be created with any shape.\n" +
                    "You can filter by adding the beginning of words after the commands, e.g.: \"listFreeShapeBlocks" +
                    "engine: core\" will list all free shape blocks from the engine and modules starting with 'core'",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String listFreeShapeBlocks(@CommandParam(value = "startsWith", required = false) String[] startsWith) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Free Shape Blocks");
        stringBuilder.append(Console.NEW_LINE);
        stringBuilder.append("-----------------");
        stringBuilder.append(Console.NEW_LINE);
        List<BlockUri> sortedUris = sortItems(blockExplorer.getFreeformBlockFamilies());
        for (BlockUri uri : sortedUris) {
            if (!uriStartsWithAnyString(uri.toString(), startsWith)) {
                continue;
            }
            stringBuilder.append(uri.toString());
            stringBuilder.append(Console.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    @Command(shortDescription = "Replaces a block in front of user",
            helpText = "Replaces a block in front of the user at the specified max distance",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public void replaceBlock(
            @Sender EntityRef sender,
            @CommandParam("blockName") String uri,
            @CommandParam(value = "maxDistance", required = false) Integer maxDistanceParam) {
        int maxDistance = maxDistanceParam != null ? maxDistanceParam : 12;
        EntityRef playerEntity = sender.getComponent(ClientComponent.class).character;
        EntityRef gazeEntity = GazeAuthoritySystem.getGazeEntityForCharacter(playerEntity);
        LocationComponent gazeLocation = gazeEntity.getComponent(LocationComponent.class);
        Set<ResourceUrn> matchingUris = Assets.resolveAssetUri(uri, BlockFamilyDefinition.class);
        targetSystem.updateTarget(gazeLocation.getWorldPosition(new Vector3f()), gazeLocation.getWorldDirection(new Vector3f()),
                maxDistance);
        EntityRef target = targetSystem.getTarget();
        BlockComponent targetLocation = target.getComponent(BlockComponent.class);
        if (matchingUris.size() == 1) {
            Optional<BlockFamilyDefinition> def = Assets.get(matchingUris.iterator().next(), BlockFamilyDefinition.class);
            if (def.isPresent()) {
                BlockFamily blockFamily = blockManager.getBlockFamily(uri);
                Block block = blockManager.getBlock(blockFamily.getURI());
                world.setBlock(targetLocation.getPosition(), block);
            } else if (matchingUris.size() > 1) {
                StringBuilder builder = new StringBuilder();
                builder.append("Non-unique shape name, possible matches: ");
                Iterator<ResourceUrn> shapeUris = sortItems(matchingUris).iterator();
                while (shapeUris.hasNext()) {
                    builder.append(shapeUris.next().toString());
                    if (shapeUris.hasNext()) {
                        builder.append(", ");
                    }
                }
            }
        }
    }

    @Command(shortDescription = "Gives multiple stacks of blocks matching a search",
            helpText = "Adds all blocks that match the search parameter into your inventory",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String bulkGiveBlock(
            @Sender EntityRef sender,
            @CommandParam("searched") String searched,
            @CommandParam(value = "quantity", required = false) Integer quantityParam,
            @CommandParam(value = "shapeName", required = false) String shapeUriParam) {

        if (quantityParam != null && quantityParam < 1) {
            return "Here, have these zero (0) blocks just like you wanted";
        }

        String searchLowercase = searched.toLowerCase();
        List<String> blocks = findBlockMatches(searchLowercase);
        String result = "Found " + blocks.size() + " block matches when searching for '" + searched + "'.";
        if (blocks.size() > 0) {
            result += "\nBlocks:";
            for (String block : blocks) {
                result += "\n" + block + "\n";
                result += giveBlock(sender, block, quantityParam, shapeUriParam);
            }
        }
        return result;
    }

    private List<String> findBlockMatches(String searchLowercase) {
        return assetManager.getAvailableAssets(BlockFamilyDefinition.class)
                .stream().<Optional<BlockFamilyDefinition>>map(urn -> assetManager.getAsset(urn, BlockFamilyDefinition.class))
                .filter(def -> def.isPresent() && def.get().isLoadable() && matchesSearch(searchLowercase, def.get()))
                .map(r -> new BlockUri(r.get().getUrn()).toString()).collect(Collectors.toList());
    }

    private static boolean matchesSearch(String searchLowercase, BlockFamilyDefinition def) {
        return def.getUrn().toString().toLowerCase().contains(searchLowercase);
    }

    /**
     * Called by 'give' command in ItemCommands.java to attempt to put a block in the player's inventory when no item is found.
     * Called by 'giveBulkBlock' command in BlockCommands.java to put a block in the player's inventory.
     *
     * @return Null if not found, otherwise success or warning message
     */
    public String giveBlock(
            @Sender EntityRef sender,
            @CommandParam("blockName") String uri,
            @CommandParam(value = "quantity", required = false) Integer quantityParam,
            @CommandParam(value = "shapeName", required = false) String shapeUriParam) {
        Set<ResourceUrn> matchingUris = Assets.resolveAssetUri(uri, BlockFamilyDefinition.class);

        BlockFamily blockFamily = null;

        if (matchingUris.size() == 1) {
            Optional<BlockFamilyDefinition> def = Assets.get(matchingUris.iterator().next(), BlockFamilyDefinition.class);
            if (def.isPresent()) {
                if (def.get().isFreeform()) {
                    if (shapeUriParam == null) {
                        blockFamily = blockManager.getBlockFamily(new BlockUri(def.get().getUrn(), new ResourceUrn("engine:cube")));
                    } else {
                        Set<ResourceUrn> resolvedShapeUris = Assets.resolveAssetUri(shapeUriParam, BlockShape.class);
                        if (resolvedShapeUris.isEmpty()) {
                            return "Found block. No shape found for '" + shapeUriParam + "'";
                        } else if (resolvedShapeUris.size() > 1) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("Found block. Non-unique shape name, possible matches: ");
                            Iterator<ResourceUrn> shapeUris = sortItems(resolvedShapeUris).iterator();
                            while (shapeUris.hasNext()) {
                                builder.append(shapeUris.next().toString());
                                if (shapeUris.hasNext()) {
                                    builder.append(", ");
                                }
                            }

                            return builder.toString();
                        }
                        blockFamily = blockManager.getBlockFamily(new BlockUri(def.get().getUrn(), resolvedShapeUris.iterator().next()));
                    }
                } else {
                    blockFamily = blockManager.getBlockFamily(new BlockUri(def.get().getUrn()));
                }
            }

            if (blockFamily == null) {
                //Should never be reached
                return "Block not found";
            }

            int defaultQuantity = blockFamily.getArchetypeBlock().isStackable() ? 16 : 1;
            int quantity = quantityParam != null ? quantityParam : defaultQuantity;

            return giveBlock(blockFamily, quantity, sender);

        } else if (matchingUris.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            return builder.toString();
        }

        return null;
    }

    /**
     * Actual implementation of the giveBlock command.
     *
     * @param blockFamily the block family of the queried block
     * @param quantity the number of blocks that are queried
     */
    private String giveBlock(BlockFamily blockFamily, int quantity, EntityRef client) {
        if (quantity < 1) {
            return "Here, have these zero (0) blocks just like you wanted";
        }

        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        int stackLimit = blockFamily.getArchetypeBlock().isStackable() ? 99 : 1;
        int quantityLeft;
        for (quantityLeft = quantity; quantityLeft > 0; quantityLeft = quantityLeft - stackLimit) {
            EntityRef item = blockItemFactory.newInstance(blockFamily, Math.min(quantityLeft, stackLimit));
            if (!item.exists()) {
                throw new IllegalArgumentException("Unknown block or item");
            }

            GiveItemEvent giveItemEvent = new GiveItemEvent(playerEntity);
            item.send(giveItemEvent);

            if (!giveItemEvent.isHandled()) {
                item.destroy();
                break;
            }
        }
        //to prevent the wrong display of blocks added to inventory
        if (quantityLeft < 0) {
            quantityLeft = 0;
        }
        return "You received " + (quantity - quantityLeft) + " blocks of " + blockFamily.getDisplayName();
    }

    private <T extends Comparable<T>> List<T> sortItems(Iterable<T> items) {
        List<T> result = Lists.newArrayList();
        for (T item : items) {
            result.add(item);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Used to check if an item/prefab/etc name starts with a string that is in {@code uri}
     *
     * @param uri the name to be checked
     * @param startsWithArray array of possible word to match at the beginning of {@code uri}
     * @return true if {@code startsWithArray} is null, empty or {@code uri} starts with one of the elements in it
     */
    public static boolean uriStartsWithAnyString(String uri, String[] startsWithArray) {
        if (startsWithArray == null || startsWithArray.length == 0) {
            return true;
        }
        for (String startsWith : startsWithArray) {
            if (uri.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }
}

/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.entity.commands;

import com.google.common.base.Joiner;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class GiveBlockCommand extends Command {
    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    private BlockItemFactory blockItemFactory;

    public GiveBlockCommand() {
        super("giveBlock", true, "Adds a block to your inventory",
                "Puts a desired number (or 16) of the given block with the give shape into your inventory");
    }

    @Override
    public void initialiseMore() {
        blockItemFactory = new BlockItemFactory(entityManager);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("blockName", String.class, true),
                CommandParameter.single("quantity", Integer.class, false),
                CommandParameter.single("shapeName", String.class, false),
        };
    }

    public String execute(EntityRef sender, String uri, Integer nullableQuantity, String shapeUri) {
        int quantity = nullableQuantity != null ? nullableQuantity : 16;

        if (shapeUri == null) {
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

    //TODO Implement the suggest method
}

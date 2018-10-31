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

package org.terasology.logic.inventory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.entity.BlockCommands;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RegisterSystem
public class ItemCommands extends BaseComponentSystem {

    @In
    private BlockCommands blockCommands;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;


    @Command(shortDescription = "Adds an item or block to your inventory",
            helpText = "Puts the desired number of the given item or block with the given shape into your inventory",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String give(
            @Sender EntityRef client,
            @CommandParam("prefabId or blockName") String itemPrefabName,
            @CommandParam(value = "amount", required = false) Integer amount,
            @CommandParam(value = "blockShapeName", required = false) String shapeUriParam) {

        int itemAmount = amount != null ? amount : 1;
        if (itemAmount < 1) {
            return "Requested zero (0) items / blocks!";
        }

        Set<ResourceUrn> matches = assetManager.resolve(itemPrefabName, Prefab.class);

        if (matches.size() == 1) {
            Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef playerEntity = client.getComponent(ClientComponent.class).character;

                for (int quantityLeft = itemAmount; quantityLeft > 0; quantityLeft--) {
                    EntityRef item = entityManager.create(prefab);
                    if (!inventoryManager.giveItem(playerEntity, playerEntity, item)) {
                        item.destroy();
                        itemAmount -= quantityLeft;
                        break;
                    }
                }

                return "You received "
                        + (itemAmount > 1 ? itemAmount + " items of " : "an item of ")
                        + prefab.getName() //TODO Use item display name
                        + (shapeUriParam != null ? " (Item can not have a shape)" : "");
            }

        } else if (matches.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Requested item \"");
            builder.append(itemPrefabName);
            builder.append("\": matches ");
            Joiner.on(" and ").appendTo(builder, matches);
            builder.append(". Please fully specify one.");
            return builder.toString();
        }

        // If no no matches are found for items, try blocks
        String message = blockCommands.giveBlock(client, itemPrefabName, amount, shapeUriParam);
        if (message != null) {
            return message;
        }

        return "Could not find an item or block matching \"" + itemPrefabName + "\"";
    }

    @Command(shortDescription = "Lists all available items (prefabs)\nYou can filter by adding the beginning of words " +
            "after the commands, e.g.: \"listItems engine: core:\" will list all items from the engine and core module",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String listItems(@CommandParam(value = "startsWith", required = false) String[] startsWith) {

        List<String> stringItems = Lists.newArrayList();

        for (Prefab prefab : prefabManager.listPrefabs()) {
            if (!BlockCommands.uriStartsWithAnyString(prefab.getName(), startsWith)) {
                continue;
            }
            stringItems.add(prefab.getName());
        }

        Collections.sort(stringItems);

        StringBuilder items = new StringBuilder();
        for (String item : stringItems) {
            if (!items.toString().isEmpty()) {
                items.append(Console.NEW_LINE);
            }
            items.append(item);
        }

        return items.toString();
    }

    @Command(shortDescription = "Gives multiple stacks of items matching a search",
            helpText = "Adds all items that match the search parameter into your inventory",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String bulkGiveItem(
            @Sender EntityRef sender,
            @CommandParam("searched") String searched,
            @CommandParam(value = "quantity", required = false) Integer quantityParam) {

        if (quantityParam != null && quantityParam < 1) {
            return "Here, have these zero (0) items just like you wanted";
        }

        List<String> items = Lists.newArrayList();
        for (String item : listItems(null).split("\n")) {
            if (item.contains(searched.toLowerCase())) {
                items.add(item);
            }
        }

        String result = "Found " + items.size() + " item matches when searching for '" + searched + "'.";
        if (items.size() > 0) {
            result += "\nItems:";
            for (String item : items) {
                result += "\n" + item + "\n";
                give(sender, item, quantityParam, null);
            }
        }
        return result;
    }
}

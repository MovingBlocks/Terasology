/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.items;

import com.google.common.base.Joiner;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.items.components.ItemComponent;
import org.terasology.logic.items.events.ItemGiveEvent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.entity.BlockCommands;

import java.util.Set;

@RegisterSystem
public class ItemCommands extends BaseComponentSystem {

    @In
    AssetManager assetManager;
    @In
    EntityManager entityManager;
    @In
    BlockCommands blockCommands;

    @Command(shortDescription = "Adds an item or block to your inventory",
            helpText = "Puts the desired number of the given item or block with the given shape into your inventory",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String give(
            @Sender EntityRef client,
            @CommandParam("prefabId or blockName") String name,
            @CommandParam(value = "amount", required = false) Integer itemAmount,
            @CommandParam(value = "blockShapeName", required = false) String shape) {
        int amount = itemAmount == null ? 1 : itemAmount;

        Set<ResourceUrn> results = assetManager.resolve(name, Prefab.class);

        if (results.size() == 1) {
            Prefab prefab = assetManager.getAsset(results.iterator().next(), Prefab.class).orElse(null);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef item = entityManager.create(prefab);
                EntityRef playerEntity = client.getComponent(ClientComponent.class).character;

                ItemGiveEvent giveEvent = new ItemGiveEvent(playerEntity, amount);
                item.send(giveEvent);
                if (!giveEvent.wasSuccessful()) {
                    item.destroy();
                }

                return "You received "
                        + (amount > 1 ? amount + " items of " : "an item of ")
                        + prefab.getName()
                        + (shape != null ? " (Item can not have a shape)" : "");
            }
        } else if (results.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Requested item \"");
            builder.append(name);
            builder.append("\": matches ");
            Joiner.on(" and ").appendTo(builder, results);
            builder.append(". Please fully specify one.");
            return builder.toString();
        }
        // If no no matches are found for items, try blocks
        /*String message = blockCommands.giveBlock(client, name, amount, shape);
        if (message != null) {
            return message;
        }*/

        return "Could not find an item or block matching \"" + name + "\"";

    }
}

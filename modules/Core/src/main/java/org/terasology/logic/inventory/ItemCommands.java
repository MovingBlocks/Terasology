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
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.entity.BlockCommands;

import java.util.Set;

/**
 */
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

    @Command(shortDescription = "Adds an item to your inventory", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String giveItem(
            @Sender EntityRef client,
            @CommandParam("prefabId or blockName") String itemPrefabName,
            @CommandParam(value = "amount", required = false) Integer amount) {
        Set<ResourceUrn> matches = assetManager.resolve(itemPrefabName, Prefab.class);
        switch(matches.size()) {
            case 0:
                return "Could not find any item matching \"" + itemPrefabName + "\"";
            case 1:
                Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
                if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                    EntityRef item = entityManager.create(prefab);
                    EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
                    if (!inventoryManager.giveItem(playerEntity, playerEntity, item)) {
                        item.destroy();
                    }
                    return "You received an item of " + prefab.getName();
                } else {
                    return blockCommands.giveBlock(client, itemPrefabName, amount, null);
                }
            default:
                StringBuilder builder = new StringBuilder();
                builder.append("Requested item \"");
                builder.append(itemPrefabName);
                builder.append("\": matches ");
                Joiner.on(" and ").appendTo(builder, matches);
                builder.append(". Please fully specify one.");
                return builder.toString();
        }
    }

}

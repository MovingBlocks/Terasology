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

package org.terasology.logic.commands;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.players.LocalPlayer;

/**
 * @author Immortius
 */
public class ItemCommands implements CommandProvider {

    private BlockCommands blockCommands = new BlockCommands();

    @Command(shortDescription = "Adds an item to your inventory")
    public void giveItem(@CommandParam(name = "prefabId or blockName") String itemPrefabName) {
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(itemPrefabName);
        System.out.println("Found prefab: " + prefab);
        if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
            EntityRef item = CoreRegistry.get(EntityManager.class).create(prefab);
            EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
            if (!inventoryManager.giveItem(playerEntity, item)) {
                item.destroy();
            }
            MessageManager.getInstance().addMessage("You received an item of " + prefab.getName());
        } else {
            blockCommands.giveBlock(itemPrefabName);
        }
    }
}

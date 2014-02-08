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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.entity.BlockCommands;

/**
 * @author Immortius
 */
@RegisterSystem
public class ItemCommands implements ComponentSystem {

    @In
    private BlockCommands blockCommands;

    @In
    private InventoryManager inventoryManager;

    @In
    private PrefabManager prefabManager;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Adds an item to your inventory", runOnServer = true)
    public String giveItem(@CommandParam("prefabId or blockName") String itemPrefabName, EntityRef client) {
        Prefab prefab = prefabManager.getPrefab(itemPrefabName);
        if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
            EntityRef item = entityManager.create(prefab);
            EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
            playerEntity.send(new GiveItemAction(item));
            return "You received an item of " + prefab.getName();
        } else {
            return blockCommands.giveBlock(itemPrefabName, client);
        }
    }

}

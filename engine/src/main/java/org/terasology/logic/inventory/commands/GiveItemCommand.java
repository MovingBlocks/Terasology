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
package org.terasology.logic.inventory.commands;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.entity.commands.GiveBlockCommand;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class GiveItemCommand extends Command {
    @In
    private InventoryManager inventoryManager;

    @In
    private PrefabManager prefabManager;

    @In
    private EntityManager entityManager;

    public GiveItemCommand() {
        super("giveItem", true, "Adds an item to your inventory", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("prefabId or blockName", String.class, true)
        };
    }

    public String execute(EntityRef sender, String itemPrefabName) {
        Prefab prefab = prefabManager.getPrefab(itemPrefabName);
        if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
            EntityRef item = entityManager.create(prefab);
            EntityRef playerEntity = sender.getComponent(ClientComponent.class).character;
            if (!inventoryManager.giveItem(playerEntity, playerEntity, item)) {
                item.destroy();
            }
            return "You received an item of " + prefab.getName();
        } else {
            return GiveBlockCommand.execute(sender, itemPrefabName, null, null);
        }
    }

    //TODO Implement the suggest method
}

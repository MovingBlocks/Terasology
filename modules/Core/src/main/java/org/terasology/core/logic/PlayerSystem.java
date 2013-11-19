/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.logic;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

@RegisterSystem
public class PlayerSystem implements ComponentSystem {

    @In
    BlockManager blockManager;
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void onPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        // Goodie chest
        EntityRef chest = blockFactory.newInstance(blockManager.getBlockFamily("core:chest"));
        chest.addComponent(new InventoryComponent(30));

        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:companion"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:brick:core:stair"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:Tnt"), 99));

        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:StoneStair"), 99));

        inventoryManager.giveItem(chest, entityManager.create("core:railgunTool"));

        inventoryManager.giveItem(chest, entityManager.create("core:mrbarsack"));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:Brick"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:Ice"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:Plank"), 99));

        EntityRef doorItem = entityManager.create("core:door");
        ItemComponent doorItemComp = doorItem.getComponent(ItemComponent.class);
        doorItemComp.stackCount = 20;
        doorItem.saveComponent(doorItemComp);
        inventoryManager.giveItem(chest, doorItem);

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(blockManager.getBlockFamily("core:Chest"));
        innerChest.addComponent(new InventoryComponent(30));

        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("core:lava"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("core:water"), 99));

        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("core:Iris"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("core:Dandelion"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("core:Tulip"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("core:YellowFlower"), 99));

        // Place inner chest into outer chest
        inventoryManager.giveItem(chest, innerChest);


        inventoryManager.giveItem(player, entityManager.create("core:pickaxe"));
        inventoryManager.giveItem(player, entityManager.create("core:axe"));
        inventoryManager.giveItem(player, blockFactory.newInstance(blockManager.getBlockFamily("core:Torch"), 99));
        inventoryManager.giveItem(player, entityManager.create("core:explodeTool"));
        inventoryManager.giveItem(player, entityManager.create("core:railgunTool"));
        inventoryManager.giveItem(player, entityManager.create("core:miniaturizer"));
        inventoryManager.giveItem(player, chest);
        inventoryManager.giveItem(player, blockFactory.newInstance(blockManager.getBlockFamily("core:OakSapling"), 10));

    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }
}

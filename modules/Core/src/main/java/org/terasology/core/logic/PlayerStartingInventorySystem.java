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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

@RegisterSystem
public class PlayerStartingInventorySystem implements ComponentSystem {

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

        chest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:companion"), 99)));
        chest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:brick:engine:stair"), 99)));
        chest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Tnt"), 99)));

        chest.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:railgunTool")));

        chest.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:mrbarsack")));
        chest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Brick"), 99)));
        chest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Ice"), 99)));
        chest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Plank"), 99)));

        EntityRef doorItem = entityManager.create("core:door");
        ItemComponent doorItemComp = doorItem.getComponent(ItemComponent.class);
        doorItemComp.stackCount = 20;
        doorItem.saveComponent(doorItemComp);
        chest.send(new GiveItemAction(EntityRef.NULL, doorItem));

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(blockManager.getBlockFamily("core:Chest"));
        innerChest.addComponent(new InventoryComponent(30));

        innerChest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:lava"), 99)));
        innerChest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:water"), 99)));

        innerChest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Iris"), 99)));
        innerChest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Dandelion"), 99)));
        innerChest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Tulip"), 99)));
        innerChest.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:YellowFlower"), 99)));

        // Place inner chest into outer chest
        chest.send(new GiveItemAction(EntityRef.NULL, innerChest));

        player.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:pickaxe")));
        player.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:axe")));
        player.send(new GiveItemAction(EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Torch"), 99)));
        player.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:explodeTool")));
        player.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:railgunTool")));
        player.send(new GiveItemAction(EntityRef.NULL, entityManager.create("core:miniaturizer")));
        player.send(new GiveItemAction(EntityRef.NULL, chest));
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }
}

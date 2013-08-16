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
package org.terasology.logic.players;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.SelectedItemChangedEvent;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private SlotBasedInventoryManager inventoryManager;
    private BlockItemFactory blockFactory;
    private BlockManager blockManager;

    public PlayerFactory(EntityManager entityManager, SlotBasedInventoryManager inventoryManager) {
        this.entityManager = entityManager;
        this.inventoryManager = inventoryManager;
        blockFactory = new BlockItemFactory(entityManager);
        blockManager = CoreRegistry.get(BlockManager.class);
    }

    public EntityRef newInstance(Vector3f spawnPosition, EntityRef controller) {
        EntityBuilder builder = entityManager.newBuilder("engine:player");
        builder.getComponent(LocationComponent.class).setWorldPosition(spawnPosition);
        builder.setOwner(controller);
        EntityRef transferSlot = entityManager.create("engine:transferSlot");

        CharacterComponent playerComponent = builder.getComponent(CharacterComponent.class);
        playerComponent.spawnPosition.set(spawnPosition);
        playerComponent.movingItem = transferSlot;
        playerComponent.controller = controller;

        // Goodie chest
        EntityRef chest = blockFactory.newInstance(blockManager.getBlockFamily("core:chest"));
        chest.addComponent(new InventoryComponent(30));

        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:companion"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("engine:brick:engine:stair"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("core:Tnt"), 99));

        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("engine:StoneStair"), 99));

        inventoryManager.giveItem(chest, entityManager.create("core:railgunTool"));

        inventoryManager.giveItem(chest, entityManager.create("core:mrbarsack"));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Brick"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Ice"), 99));
        inventoryManager.giveItem(chest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Plank"), 99));

        EntityRef doorItem = entityManager.create("core:door");
        ItemComponent doorItemComp = doorItem.getComponent(ItemComponent.class);
        doorItemComp.stackCount = 20;
        doorItem.saveComponent(doorItemComp);
        inventoryManager.giveItem(chest, doorItem);

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(blockManager.getBlockFamily("core:Chest"));
        innerChest.addComponent(new InventoryComponent(30));

        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Lava"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Water"), 99));

        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Iris"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Dandelion"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("engine:Tulip"), 99));
        inventoryManager.giveItem(innerChest, blockFactory.newInstance(blockManager.getBlockFamily("engine:YellowFlower"), 99));

        // Place inner chest into outer chest
        inventoryManager.giveItem(chest, innerChest);

        EntityRef player = builder.build();

        inventoryManager.giveItem(player, entityManager.create("core:pickaxe"));
        inventoryManager.giveItem(player, entityManager.create("core:axe"));
        inventoryManager.giveItem(player, blockFactory.newInstance(blockManager.getBlockFamily("engine:Torch"), 99));
        inventoryManager.giveItem(player, entityManager.create("core:explodeTool"));
        inventoryManager.giveItem(player, entityManager.create("core:railgunTool"));
        inventoryManager.giveItem(player, entityManager.create("core:miniaturizer"));
        inventoryManager.giveItem(player, chest);

        player.send(new SelectedItemChangedEvent(EntityRef.NULL, inventoryManager.getItemInSlot(player, 0)));

        return player;
    }

}

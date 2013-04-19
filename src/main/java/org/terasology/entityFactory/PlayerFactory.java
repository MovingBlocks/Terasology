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
package org.terasology.entityFactory;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.entity.BlockItemComponent;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private InventoryManager inventoryManager;
    private BlockItemFactory blockFactory;
    private BlockManager blockManager;

    public PlayerFactory(EntityManager entityManager, InventoryManager inventoryManager) {
        this.entityManager = entityManager;
        this.inventoryManager = inventoryManager;
        blockFactory = new BlockItemFactory(entityManager);
        blockManager = CoreRegistry.get(BlockManager.class);
    }

    public EntityRef newInstance(Vector3f spawnPosition, EntityRef controller) {
        EntityRef player = entityManager.create("core:player", spawnPosition);
        EntityRef transferSlot = entityManager.create("engine:transferSlot");
        NetworkComponent netComp = transferSlot.getComponent(NetworkComponent.class);
        netComp.owner = player;
        transferSlot.saveComponent(netComp);

        CharacterComponent playerComponent = player.getComponent(CharacterComponent.class);
        playerComponent.spawnPosition.set(spawnPosition);
        playerComponent.movingItem = transferSlot;
        playerComponent.controller = controller;
        player.saveComponent(playerComponent);

        // Goodie chest
        EntityRef chest = blockFactory.newInstance(blockManager.getBlockFamily("core:chest"));
        BlockItemComponent blockItem = chest.getComponent(BlockItemComponent.class);
        EntityRef chestContents = blockItem.placedEntity;

        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("core:companion"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:brick:engine:stair"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("core:Tnt"), 99));

        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:StoneStair"), 99));

        inventoryManager.giveItem(chestContents, entityManager.create("core:railgunTool"));

        inventoryManager.giveItem(chestContents, entityManager.create("core:mrbarsack"));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Brick"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Ice"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Plank"), 99));

        EntityRef doorItem = entityManager.create("core:door");
        ItemComponent doorItemComp = doorItem.getComponent(ItemComponent.class);
        doorItemComp.stackCount = 20;
        doorItem.saveComponent(doorItemComp);
        inventoryManager.giveItem(chestContents, doorItem);

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(blockManager.getBlockFamily("core:Chest"));
        BlockItemComponent innerBlockItem = innerChest.getComponent(BlockItemComponent.class);
        EntityRef innerChestContents = innerBlockItem.placedEntity;

        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Lava"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Water"), 99));

        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Iris"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Dandelion"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:Tulip"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(blockManager.getBlockFamily("engine:YellowFlower"), 99));

        // Place inner chest into outer chest
        inventoryManager.giveItem(chestContents, innerChest);

        inventoryManager.giveItem(player, entityManager.create("core:pickaxe"));
        inventoryManager.giveItem(player, entityManager.create("core:axe"));
        inventoryManager.giveItem(player, blockFactory.newInstance(blockManager.getBlockFamily("engine:Torch"), 99));
        inventoryManager.giveItem(player, entityManager.create("core:explodeTool"));
        inventoryManager.giveItem(player, entityManager.create("core:railgunTool"));
        inventoryManager.giveItem(player, entityManager.create("core:miniaturizer"));
        inventoryManager.giveItem(player, chest);

        return player;
    }

}

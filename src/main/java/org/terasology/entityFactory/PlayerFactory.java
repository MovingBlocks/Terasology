/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import javax.vecmath.Vector3f;

import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.entity.BlockItemComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.world.block.management.BlockManager;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private InventoryManager inventoryManager;
    private BlockItemFactory blockFactory;

    public PlayerFactory(EntityManager entityManager, InventoryManager inventoryManager) {
        this.entityManager = entityManager;
        this.inventoryManager = inventoryManager;
        blockFactory = new BlockItemFactory(entityManager);
    }

    public EntityRef newInstance(Vector3f spawnPosition) {
        EntityRef player = entityManager.create("core:player", spawnPosition);
        EntityRef transferSlot = entityManager.create("engine:transferSlot");
        NetworkComponent netComp = transferSlot.getComponent(NetworkComponent.class);
        netComp.owner = player;
        transferSlot.saveComponent(netComp);

        CharacterComponent playerComponent = player.getComponent(CharacterComponent.class);
        playerComponent.spawnPosition.set(spawnPosition);
        playerComponent.movingItem = transferSlot;
        player.saveComponent(playerComponent);

        // Goodie chest
        EntityRef chest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("core:chest"));
        BlockItemComponent blockItem = chest.getComponent(BlockItemComponent.class);
        EntityRef chestContents = blockItem.placedEntity;

        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("core:companion"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:brick:engine:stair"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("core:Tnt"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("books:Bookcase"), 1));

        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:clay:engine:slope"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:clay:engine:steepslope"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:StoneStair"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:marble:engine:stair"), 99));

        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Marble"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:marble:engine:testsphere"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:marble:engine:slope"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:marble:engine:steepslope"), 99));

        inventoryManager.giveItem(chestContents, entityManager.create("potions:purplepotion"));
        inventoryManager.giveItem(chestContents, entityManager.create("potions:greenpotion"));
        inventoryManager.giveItem(chestContents, entityManager.create("potions:orangepotion"));
        inventoryManager.giveItem(chestContents, entityManager.create("potions:redpotion"));

        inventoryManager.giveItem(chestContents, entityManager.create("books:book"));
        inventoryManager.giveItem(chestContents, entityManager.create("books:bluebook"));
        inventoryManager.giveItem(chestContents, entityManager.create("books:redbook"));
        inventoryManager.giveItem(chestContents, entityManager.create("core:railgunTool"));

        inventoryManager.giveItem(chestContents, entityManager.create("core:mrbarsack"));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Cobaltite"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:NativeGoldOre"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Microcline"), 99));

        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Brick"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Ice"), 99));
        inventoryManager.giveItem(chestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Plank"), 99));

        EntityRef doorItem = entityManager.create("core:door");
        ItemComponent doorItemComp = doorItem.getComponent(ItemComponent.class);
        doorItemComp.stackCount = 20;
        doorItem.saveComponent(doorItemComp);
        inventoryManager.giveItem(chestContents, doorItem);

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("core:Chest"));
        BlockItemComponent innerBlockItem = innerChest.getComponent(BlockItemComponent.class);
        EntityRef innerChestContents = innerBlockItem.placedEntity;


        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Alabaster"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Basalt"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Gabbro"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Hornblende"), 99));

        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:OrangeSandStone"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Phyllite"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Schist"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Cinnabar"), 99));

        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Lava"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Water"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Rutile"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("minerals:Kaolinite"), 99));

        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Iris"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Dandelion"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Tulip"), 99));
        inventoryManager.giveItem(innerChestContents, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:YellowFlower"), 99));

        // Place inner chest into outer chest
        inventoryManager.giveItem(chestContents, innerChest);

        inventoryManager.giveItem(player, entityManager.create("core:pickaxe"));
        inventoryManager.giveItem(player, entityManager.create("core:axe"));
        inventoryManager.giveItem(player, blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("engine:Torch"), 99));
        inventoryManager.giveItem(player, entityManager.create("core:explodeTool"));
        inventoryManager.giveItem(player, entityManager.create("core:railgunTool"));
        inventoryManager.giveItem(player, entityManager.create("core:miniaturizer"));
        inventoryManager.giveItem(player, chest);

        inventoryManager.giveItem(player, entityManager.create("dynamicBlocks:train"));
        inventoryManager.giveItem(player, entityManager.create("dynamicBlocks:boat"));

        return player;
    }

}

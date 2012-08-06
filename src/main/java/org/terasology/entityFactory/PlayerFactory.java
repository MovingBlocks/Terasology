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

import org.terasology.components.*;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private BlockItemFactory blockFactory;

    public PlayerFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
        blockFactory = new BlockItemFactory(entityManager);
    }

    public EntityRef newInstance(Vector3f spawnPosition) {
        EntityRef player = entityManager.create("core:player");
        LocationComponent location = player.getComponent(LocationComponent.class);
        location.setWorldPosition(spawnPosition);
        player.saveComponent(location);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        playerComponent.spawnPosition.set(spawnPosition);
        player.saveComponent(playerComponent);
        player.addComponent(new LocalPlayerComponent());

        // Goodie chest
        EntityRef chest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:chest")));
        BlockItemComponent blockItem = chest.getComponent(BlockItemComponent.class);
        EntityRef chestContents = blockItem.placedEntity;

        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:companion")), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:BrickStair")), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:ClaySlope")), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Tnt")), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Bookcase")), 1)));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:purplepotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:greenpotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:orangepotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:redpotion")));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:book")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:bluebook")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:redbook")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:mrbarsack")));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Cobaltite")), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:NativeGoldOre")), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Microcline")), 99)));

        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Brick")), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Ice")), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Plank")), 99)));

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Chest")));
        BlockItemComponent innerBlockItem = innerChest.getComponent(BlockItemComponent.class);
        EntityRef innerChestContents = innerBlockItem.placedEntity;

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Alabaster")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Basalt")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Gabbro")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Hornblende")), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:OrangeSandStone")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Phyllite")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Schist")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Cinnabar")), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Lava")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Water")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Rutile")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Kaolinite")), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Iris")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Dandelion")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Tulip")), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:YellowFlower")), 42)));

        // Place inner chest into outer chest
        chestContents.send(new ReceiveItemEvent(innerChest));

        player.send(new ReceiveItemEvent(entityManager.create("core:pickaxe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:axe")));
        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(new BlockUri("engine:Torch")), 99)));
        player.send(new ReceiveItemEvent(entityManager.create("core:explodeTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:miniaturizer")));
        player.send(new ReceiveItemEvent(chest));

        return player;
    }

}

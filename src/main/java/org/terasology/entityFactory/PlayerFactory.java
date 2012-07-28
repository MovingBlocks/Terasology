package org.terasology.entityFactory;

import org.terasology.components.*;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.model.blocks.management.BlockManager;

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
        EntityRef chest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Chest"));
        BlockItemComponent blockItem = chest.getComponent(BlockItemComponent.class);
        EntityRef chestContents = blockItem.placedEntity;

        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Companion"), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("BrickStair"), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Tnt"), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Bookcase"), 1)));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:purplepotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:greenpotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:orangepotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:redpotion")));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:book")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:bluebook")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:redbook")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:mrbarsack")));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Cobaltite"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("NativeGoldOre"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Microcline"), 99)));

        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Brick"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Ice"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Plank"), 99)));

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Chest"));
        BlockItemComponent innerBlockItem = innerChest.getComponent(BlockItemComponent.class);
        EntityRef innerChestContents = innerBlockItem.placedEntity;

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Alabaster"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Basalt"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Gabbro"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Hornblende"), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("OrangeSandStone"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Phyllite"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Schist"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Cinnabar"), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Lava"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Water"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Rutile"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Kaolinite"), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Iris"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Dandelion"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Tulip"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("YellowFlower"), 42)));

        // Place inner chest into outer chest
        chestContents.send(new ReceiveItemEvent(innerChest));

        player.send(new ReceiveItemEvent(entityManager.create("core:pickaxe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:axe")));
        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Torch"), 99)));
        player.send(new ReceiveItemEvent(entityManager.create("core:explodeTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:miniaturizer")));
        player.send(new ReceiveItemEvent(chest));

        return player;
    }

}

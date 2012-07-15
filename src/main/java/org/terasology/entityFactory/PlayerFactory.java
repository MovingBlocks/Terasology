package org.terasology.entityFactory;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.components.*;
import org.terasology.components.world.BlockItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private BlockItemFactory blockFactory;

    public PlayerFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
        blockFactory = new BlockItemFactory(entityManager, CoreRegistry.get(PrefabManager.class));
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

        EntityRef chest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Chest"));
        BlockItemComponent blockItem = chest.getComponent(BlockItemComponent.class);
        EntityRef chestContents = blockItem.placedEntity;
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Companion"), 16)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("BrickStair"), 16)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Tnt"), 16)));

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

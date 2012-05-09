package org.terasology.entityFactory;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AudioManager;
import org.terasology.model.blocks.management.BlockManager;

import javax.vecmath.Vector3f;
import java.util.Arrays;

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
        EntityRef player = entityManager.create();

        player.addComponent(new LocationComponent(spawnPosition));
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.spawnPosition.set(spawnPosition);
        player.addComponent(playerComponent);
        HealthComponent healthComponent = new HealthComponent(255, 5, 2f);
        healthComponent.excessSpeedDamageMultiplier = 20f;
        healthComponent.fallingDamageSpeedThreshold = 15f;
        player.addComponent(healthComponent);

        AABBCollisionComponent collision = player.addComponent(new AABBCollisionComponent());
        collision.setExtents(new Vector3f(.3f, 0.8f, .3f));

        CharacterMovementComponent movementComp = player.addComponent(new CharacterMovementComponent());
        movementComp.groundFriction = 16f;
        movementComp.maxGroundSpeed = 3f;
        movementComp.distanceBetweenFootsteps = 1.5f;
        CharacterSoundComponent sounds = player.addComponent(new CharacterSoundComponent());
        sounds.footstepSounds.addAll(Arrays.asList(AudioManager.sounds("FootGrass1", "FootGrass2", "FootGrass3", "FootGrass4", "FootGrass5")));
        player.addComponent(new LocalPlayerComponent());
        player.addComponent(new InventoryComponent(36));
        player.addComponent(new MinionComponent(9));

        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Companion"), 16)));
        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Torch"), 99)));
        player.send(new ReceiveItemEvent(entityManager.create("core:axe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:pickaxe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:explodeTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));

        return player;
    } 

}

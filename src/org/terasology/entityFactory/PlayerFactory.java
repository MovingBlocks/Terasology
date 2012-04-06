package org.terasology.entityFactory;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
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
    private ItemFactory itemFactory;
    
    public PlayerFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
        blockFactory = new BlockItemFactory(entityManager, CoreRegistry.get(PrefabManager.class));
        itemFactory = new ItemFactory(entityManager);
    }
    
    public EntityRef newInstance(Vector3f spawnPosition) {
        EntityRef player = entityManager.create();

        player.addComponent(new LocationComponent(spawnPosition));
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.spawnPosition.set(spawnPosition);
        player.addComponent(playerComponent);
        player.addComponent(new HealthComponent(255, 5, 2f));

        AABBCollisionComponent collision = player.addComponent(new AABBCollisionComponent());
        collision.setExtents(new Vector3f(.3f, 0.8f, .3f));

        CharacterMovementComponent movementComp = player.addComponent(new CharacterMovementComponent());
        movementComp.groundFriction = 16f;
        movementComp.maxGroundSpeed = 3f;
        movementComp.distanceBetweenFootsteps = 1.5f;
        CharacterSoundComponent sounds = player.addComponent(new CharacterSoundComponent());
        sounds.footstepSounds.addAll(Arrays.asList(AudioManager.sounds("FootGrass1", "FootGrass2", "FootGrass3", "FootGrass4", "FootGrass5")));
        player.addComponent(new LocalPlayerComponent());
        
        InventoryComponent inventory = player.addComponent(new InventoryComponent(36));
        inventory.itemSlots.set(0, blockFactory.newInstance(BlockManager.getInstance().getBlockGroup("Companion"), 16));
        inventory.itemSlots.set(1, blockFactory.newInstance(BlockManager.getInstance().getBlockGroup("Torch"), 99));
        inventory.itemSlots.set(2, itemFactory.createAxe());
        inventory.itemSlots.set(3, itemFactory.createPickAxe());
        inventory.itemSlots.set(4, itemFactory.createDynamite());
        inventory.itemSlots.set(5, itemFactory.createRailgun());

        return player;
    } 

}

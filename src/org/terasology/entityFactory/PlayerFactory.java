package org.terasology.entityFactory;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.manager.AudioManager;
import org.terasology.model.blocks.management.BlockManager;

import javax.vecmath.Vector3f;
import java.util.Arrays;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private PlaceableBlockFactory blockFactory;
    
    public PlayerFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
        blockFactory = new PlaceableBlockFactory(entityManager);
    }
    
    public EntityRef newInstance() {
        EntityRef player = entityManager.create();

        player.addComponent(new LocationComponent());
        player.addComponent(new PlayerComponent());

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

        return player;
    } 
    
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

package org.terasology.entityFactory;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    
    public EntityRef newInstance() {
        EntityRef player = entityManager.create();

        player.addComponent(new LocationComponent());
        player.addComponent(new PlayerComponent());

        AABBCollisionComponent collision = player.addComponent(new AABBCollisionComponent());
        collision.extents = new Vector3f(.3f, 0.8f, .3f);

        CharacterMovementComponent movementComp = player.addComponent(new CharacterMovementComponent());
        movementComp.groundFriction = 16f;
        player.addComponent(new CharacterSoundComponent());
        player.addComponent(new LocalPlayerComponent());

        return player;
    } 
    
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

package org.terasology.entityFactory;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.AudioManager;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class GelatinousCubeFactory {

    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private FastRandom random;
    private EntityManager entityManager;
    
    public EntityRef generateGelatinousCube(Vector3f position) {
        // TODO: Replace a lot of this with prefab instantiation
        EntityRef entity = Terasology.getInstance().getCurrentGameState().getEntityManager().create();

        LocationComponent loc = entity.addComponent(new LocationComponent());
        loc.position.set(position);
        loc.scale = (float) (((random.randomDouble() + 1.0) / 2.0) * 0.8 + 0.2);

        MeshComponent mesh = entity.addComponent(new MeshComponent());
        int colorId = Math.abs(random.randomInt()) % COLORS.length;
        mesh.color.set(COLORS[colorId].x, COLORS[colorId].y, COLORS[colorId].z, 1.0f);

        CharacterMovementComponent moveComp = entity.addComponent(new CharacterMovementComponent());
        moveComp.faceMovementDirection = true;

        entity.addComponent(new SimpleAIComponent());
        AABBCollisionComponent comp = entity.addComponent(new AABBCollisionComponent());
        comp.extents.set(0.5f, 0.5f, 0.5f);
        
        CharacterSoundComponent soundComp = entity.addComponent(new CharacterSoundComponent());
        soundComp.footstepSounds = AudioManager.sounds("Slime1", "Slime2", "Slime3", "Slime4", "Slime5");
        soundComp.footstepVolume = 0.7f;

        return entity;
    }

    public FastRandom getRandom() {
        return random;
    }

    public void setRandom(FastRandom random) {
        this.random = random;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}

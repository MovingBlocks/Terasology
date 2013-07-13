package org.terasology.ligthandshadow.componentsystem.controllers;

import com.google.common.collect.Sets;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.ligthandshadow.components.AnimationComponent;
import org.terasology.ligthandshadow.components.SpawnerBlockComponent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.logic.AnimEndEvent;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.Random;
import java.util.Set;

/**
 * Spawns minions at a given frequency. All spawner blocks spawns one minion of its side.
 *
 * @author synopia
 */
@RegisterComponentSystem
public class SpawnerSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    public static final float SPAWN_COOLDOWN = 10f; // in seconds

    private EntityManager entityManager;
    private WorldProvider worldProvider;
    private float cooldown = SPAWN_COOLDOWN;
    private Set<EntityRef> spawnPoints;
    private int count;
    private Random random;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        spawnPoints = Sets.newHashSet();
        random = new Random();
    }

    @Override
    public void update(float delta) {
        if( cooldown<=0 ) {
            cooldown = SPAWN_COOLDOWN;
            spawnAll();
        } else {
            cooldown -= delta;
        }
    }

    private void spawnAll() {
        for (EntityRef block : entityManager.iteratorEntities(SpawnerBlockComponent.class)) {
            SpawnerBlockComponent spawnerBlock = block.getComponent(SpawnerBlockComponent.class);
            LocationComponent location = block.getComponent(LocationComponent.class);
            Vector3f worldPosition = location.getWorldPosition();
            float x = worldPosition.x + random.nextInt(3)-1;
            float z = worldPosition.z + random.nextInt(3)-1;
            Vector3f spawnPosition = new Vector3f(x, worldPosition.y+1, z);
            String minionType;
            switch (count%1) {
                case 0: minionType = "Minion"; break;
                case 1: minionType = "King"; break;
                case 2: minionType = "Queen"; break;
                case 3: minionType = "Elephantrook"; break;
                default: throw new IllegalStateException();
            }
            EntityRef minion = entityManager.create("lightAndShadow:" + spawnerBlock.side + minionType, spawnPosition);
            CharacterMovementComponent move = minion.getComponent(CharacterMovementComponent.class);
            move.height = 0.31f;
            minion.saveComponent(move);
        }
        count++;
    }

    /**
     * destroys a minion at the end of their dying animation this implies that
     * setting their animation to die will destroy them.
     */
    @ReceiveEvent(components = { SkeletalMeshComponent.class, AnimationComponent.class })
    public void onAnimationEnd(AnimEndEvent event, EntityRef entity) {
        AnimationComponent animcomp = entity.getComponent(AnimationComponent.class);
        if (animcomp != null && event.getAnimation().equals(animcomp.dieAnim)) {
            entity.destroy();
        }
    }

    @Override
    public void shutdown() {

    }
}

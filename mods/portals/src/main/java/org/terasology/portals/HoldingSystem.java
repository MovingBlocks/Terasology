package org.terasology.portals;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.logic.MeshComponent;

import javax.vecmath.Color4f;

import java.util.ArrayList;
import java.util.Set;

/**
 * System that handles Holdings! Goal is that a Holding has a set of Spawnables associated with it and thresholds may trigger stuff.
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@RegisterComponentSystem
public class HoldingSystem implements UpdateSubscriberSystem {

    protected EntityManager entityManager;

    private long tick = 0;
    private long classLastTick = 0;

    private static final Logger logger = LoggerFactory.getLogger(HoldingSystem.class);

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    /**
     * Responsible for tick update - see if we should review thresholds (like making a new queen)
     *
     * @param delta time step since last update
     */
    public void update(float delta) {
        // Do a time check to see if we should even bother calculating stuff (really only needed every second or so)
        // Keep a ms counter handy, delta is in seconds
        tick += delta * 1000;
      
        if (tick - classLastTick < 5000) {
            return;
        }
        classLastTick = tick;

        // Prep a list of the holdings we know about
        ArrayList<EntityRef> holdingEntities = new ArrayList<EntityRef>(4);

        // Go fetch all the Holdings.
        // TODO: Do we have a helper method for this? I forgot and it is late :P
        for (EntityRef holdingEntity : entityManager.iteratorEntities(HoldingComponent.class)) {
            holdingEntities.add(holdingEntity);
        }

        // Prep a fancy multi map of the Holdings and which if any Spawnables belong to each
        SetMultimap<EntityRef, EntityRef> spawnableEntitiesByHolding = HashMultimap.create();

        // Loop through all Spawnables and assign them to a Holding if one exists that is also the Spawnables parent (its Spawner)
        for (EntityRef spawnableEntity : entityManager.iteratorEntities(SpawnableComponent.class)) {

            for (EntityRef holdingEntity : holdingEntities) {

                // Check the spawnable's parent (a reference to a Spawner entity) against the holding (which we cheat-know to also be a Spawner)
                if (spawnableEntity.getComponent(SpawnableComponent.class).parent == holdingEntity) {
                    // Map this Spawnable to its Holding
                    spawnableEntitiesByHolding.put(holdingEntity, spawnableEntity);
                    break;
                }
            }
        }

        // For each Holding check if there are enough Spawnables to create a queen
        for (EntityRef holdingEntity : holdingEntities) {
            Set <EntityRef> holdingSpawnables = spawnableEntitiesByHolding.get(holdingEntity);
            int spawnableCount = holdingSpawnables.size();
            HoldingComponent holdComp = holdingEntity.getComponent(HoldingComponent.class);
            logger.info("Processing a holding with a spawnableCount of {} and threshold of {}", spawnableCount, holdComp.queenThreshold);
            if (spawnableCount > holdComp.queenThreshold && holdComp.queenMax > holdComp.queenCurrent ) {

                EntityRef queenInWaiting = null;
                // See if we have a candidate queen to crown
                for (EntityRef queenCandidate : holdingSpawnables) {
                    // Come up with some criteria - for now just pick the first one
                    queenInWaiting = queenCandidate;
                }

                if (queenInWaiting == null) {
                    logger.info("We failed to find a worthy queen :-(");
                    continue;
                }

                // After success make sure to increment the threshold and queen count (queen count never decrements, queens could disappear in the future)
                holdComp.queenThreshold += 10;
                holdComp.queenCurrent++;

                // Then prep the "crown" in the shape of a shiny new SpawnerComponent ;-)
                SpawnerComponent newSpawnerComp = new SpawnerComponent();
                newSpawnerComp.types.add("gelcube");
                newSpawnerComp.maxMobsPerSpawner = 666;
                newSpawnerComp.timeBetweenSpawns = 1000;

                // Make the queen a unique color (in this case, black)
                MeshComponent mesh = queenInWaiting.getComponent(MeshComponent.class);
                if (mesh != null) {
                    mesh.color.set(new Color4f(0, 0, 0, 1));
                }

                queenInWaiting.addComponent(newSpawnerComp);
                logger.info("Queen prepped, id {} and spawnercomp {}", queenInWaiting, queenInWaiting.getComponent(SpawnerComponent.class));

            } else {
                logger.info("Haven't reached the threshold yet or queenMax {} is not greater than queenCurrent {}", holdComp.queenMax, holdComp.queenCurrent);
            }
        }
    }
}

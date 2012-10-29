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
package org.terasology.portals;

import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.SimpleAIComponent;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.utilities.FastRandom;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.Set;

/**
 * System that handles spawning of stuff
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@RegisterComponentSystem
public class SpawnerSystem implements UpdateSubscriberSystem {

    protected EntityManager entityManager;

    private int maxMobsPerSpawner = 16;
    private final FastRandom random = new FastRandom();
    private GelatinousCubeFactory factory;

    private long tick = 0;
    private long lastTick = 0;
    private int timeBetweenSpawns = 5000; // TODO: Refactor later when UpdateSubscriberSystem has a better way to trigger at arbitrary intervals

    private static final Logger logger = LoggerFactory.getLogger(SpawnerSystem.class);

    /** Contains Spawnable prefabs mapped to their spawn type name (not the prefab name!) - each type name may reference multiple prefabs */
    private SetMultimap<String, Prefab> typeLists = HashMultimap.create();

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        factory = new GelatinousCubeFactory();
        factory.setEntityManager(entityManager);
        factory.setRandom(random);

        cacheTypes();
    }

    /**
     * Looks through all loaded prefabs and determines which are spawnable, then stores them in a local SetMultimap
     * This method should be called (or adders/removers?) whenever available spawnable prefabs change, if ever
     */
    public void cacheTypes() {
        Collection<Prefab> spawnablePrefabs = CoreRegistry.get(PrefabManager.class).listPrefabs(SpawnableComponent.class);
        logger.info("Grabbed all Spawnable entities - got: {}", spawnablePrefabs);
        for (Prefab prefab : spawnablePrefabs) {
            logger.info("Prepping a Spawnable prefab: {}", prefab);
            SpawnableComponent spawnableComponent = prefab.getComponent(SpawnableComponent.class);
            typeLists.put(spawnableComponent.type, prefab);
        }

        logger.info("Full typeLists: {}", typeLists);
    }

    @Override
    public void shutdown() {
    }

    /**
     * Responsible for tick update - see if we should attempt to spawn something
     *
     * @param delta time step since last update
     */
    public void update(float delta) {
        // Do a time check to see if we should even bother calculating stuff (really only needed every second or so)
        // Keep a ms counter handy, delta is in seconds
        tick += delta * 1000;
        //logger.info("tick is " + tick + ", lastTick is " + lastTick);
        if (tick - lastTick < timeBetweenSpawns) {
            return;
        }

        logger.info("Going to do stuff");
        lastTick = tick;

        // Make sure we don't spawn too much stuff. Not very robust yet
        int maxMobs = entityManager.getComponentCount(SpawnerComponent.class) * maxMobsPerSpawner + maxMobsPerSpawner;
        int currentMobs = entityManager.getComponentCount(SimpleAIComponent.class);
        logger.info("Mob count: {}/{}", currentMobs, maxMobs);

        // Probably need something better to base this threshold on eventually
        if ( currentMobs >= maxMobs) {
            logger.info("Too many mobs! Returning early");
            return;
        }

        // Go through entities that are spawners. Only accept block-based spawners for now (due to location need)
        logger.info("Count of entities with a SpawnerComponent: {}", entityManager.getComponentCount(SpawnerComponent.class));
        for (EntityRef entity : entityManager.iteratorEntities(SpawnerComponent.class, BlockComponent.class)) {
            logger.info("Found a spawner: " + entity);
            SpawnerComponent spawnComp = entity.getComponent(SpawnerComponent.class);
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);

            if (currentMobs < maxMobs) {
                Vector3f pos = blockComp.getPosition().toVector3f();
                logger.info("Going to spawn something at {}", pos);

                String chosenSpawnerType = spawnComp.types.get(random.randomIntAbs(spawnComp.types.size()));
                Set randomType = typeLists.get(chosenSpawnerType);
                logger.info("Picked random type {} which returned {} prefabs", chosenSpawnerType, randomType.size());
                if (randomType.size() == 0) {
                    logger.warn("That type wasn't found, sad :-( Won't spawn anything this time");
                    return;
                }
                int anotherRandomIndex = random.randomIntAbs(randomType.size());
                Object[] randomPrefabs = randomType.toArray();
                Prefab chosenPrefab = (Prefab) randomPrefabs[anotherRandomIndex];
                logger.info("Picked index {} of types {} which is a {}", anotherRandomIndex, chosenSpawnerType, chosenPrefab);

                // TODO: Currently assuming all Spawnables are GelCubes, who have their own factory. Won't be the case long-term
                factory.generateGelatinousCube(pos, chosenPrefab);
                currentMobs++;
            }

            // TODO: Use some sort of parent/inheritance thing with gelcubes -> specialized gelcubes
            // TODO: Introduce a ranged spawning option for portals (old "wild gelcubes")
            // TODO: Introduce proper probability-based spawning
        }
    }
}



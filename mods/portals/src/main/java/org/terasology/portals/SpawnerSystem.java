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
import org.terasology.components.HierarchicalAIComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.utilities.EntityTools;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * System that handles spawning of stuff
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@RegisterComponentSystem
public class SpawnerSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    protected EntityManager entityManager;

    private final FastRandom random = new FastRandom();
    private DefaultMobFactory factory;

    private long tick = 0;
    private long classLastTick = 0;

    private static final Logger logger = LoggerFactory.getLogger(SpawnerSystem.class);

    /** Contains Spawnable prefabs mapped to their spawn type name (not the prefab name!) - each type name may reference multiple prefabs */
    private SetMultimap<String, Prefab> typeLists = HashMultimap.create();

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        factory = new DefaultMobFactory();
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
      
        if (tick - classLastTick < 1000) {
            return;
        }
        classLastTick = tick;

        PerformanceMonitor.startActivity("Spawn creatures");
        try {


            // Prep a list of the Spawners we know about and a total count for max mobs (which is a hack - needs to be per Spawner)
            // TODO: Do we have a helper method for this? I forgot and it is late :P
            int maxMobs = 0;
            ArrayList<EntityRef> spawnerEntities = new ArrayList<EntityRef>(4);
            for (EntityRef spawnerEntity : entityManager.iteratorEntities(SpawnerComponent.class)) {
                spawnerEntities.add(spawnerEntity);
                maxMobs += spawnerEntity.getComponent(SpawnerComponent.class).maxMobsPerSpawner;
            }

            // Go through entities that are spawners and check to see if something should spawn
            //logger.info("Count of entities with a SpawnerComponent: {}", entityManager.getComponentCount(SpawnerComponent.class));
            for (EntityRef entity : spawnerEntities) {
                //logger.info("Found a spawner: {}", entity);
                SpawnerComponent spawnComp = entity.getComponent(SpawnerComponent.class);

                if(spawnComp.lastTick > tick) {
                    spawnComp.lastTick = tick;
                }

                //logger.info("tick is " + tick + ", lastTick is " + spawnComp.lastTick);
                if (tick - spawnComp.lastTick < spawnComp.timeBetweenSpawns) {
                    return;
                }

                //logger.info("Going to do stuff");
                spawnComp.lastTick = tick;

                if(spawnComp.maxMobsPerSpawner > 0) {
                    // TODO Make sure we don't spawn too much stuff. Not very robust yet and doesn't tie mobs to their spawner of origin right
                    //int maxMobs = entityManager.getComponentCount(SpawnerComponent.class) * spawnComp.maxMobsPerSpawner;
                    int currentMobs = entityManager.getComponentCount(SimpleAIComponent.class) + entityManager.getComponentCount(HierarchicalAIComponent.class);

                    logger.info("Mob count: {}/{}", currentMobs, maxMobs);

                    // TODO Probably need something better to base this threshold on eventually
                    if (currentMobs >= maxMobs) {
                        logger.info("Too many mobs! Returning early");
                        return;
                    }
                }

                int spawnTypes = spawnComp.types.size();
                if (spawnTypes == 0) {
                    logger.warn("Spawner has no types, sad - stopping this loop iteration early :-(");
                    continue;
                }

                // Find spawn origin
                Vector3f originPos;
                if (entity.hasComponent(BlockComponent.class)) {
                    BlockComponent blockComp = entity.getComponent(BlockComponent.class);
                    originPos = blockComp.getPosition().toVector3f();

                } else if (entity.hasComponent(LocationComponent.class)) {
                    // Check if this is a player in which case don't spawn if the player is dead
                    // TODO: Does this really matter?
                    if (entity.hasComponent(LocalPlayerComponent.class)) {
                        LocalPlayerComponent lpc = entity.getComponent(LocalPlayerComponent.class);
                        if (lpc.isDead) {
                            continue;
                        }
                    }

                    LocationComponent lc = entity.getComponent(LocationComponent.class);
                    originPos = lc.getWorldPosition();
                    logger.info("Spawner has a LocationComponent with position: {}, {}, {}", originPos.x, originPos.y, originPos.z);

                } else {
                    logger.warn("Spawning was attempted by a Spawner entity without a BlockComponent or LocationComponent. No can do.");
                    continue;
                }

                // Check for spawning that depends on a player position
                if (spawnComp.needsPlayer) {
                    // TODO: shouldn't use local player, need some way to find nearest player
                    LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
                    if (localPlayer != null) {
                        Vector3f dist = new Vector3f(originPos);
                        dist.sub(localPlayer.getPosition());
                        double distanceToPlayer = dist.lengthSquared();
                        if (distanceToPlayer > spawnComp.playerNeedRange) {
                            logger.info("Spawner {} too far from player {}<{}", entity.getId(), distanceToPlayer, spawnComp.playerNeedRange);
                            continue;
                        }
                    }
                }

                //TODO check for bigger creatures and creatures with special needs like biome

                // In case we're doing ranged spawning we might be changing the exact spot to spawn at (otherwise they're the same)
                Vector3f spawnPos = originPos;
                if (spawnComp.rangedSpawning) {

                    // Add random range on the x and z planes, leave y (height) unchanged for now
                    spawnPos = new Vector3f(originPos.x + random.randomFloat() * spawnComp.range, originPos.y, originPos.z + random.randomFloat() * spawnComp.range);

                    // If a minimum distance is set make sure we're beyond it
                    if (spawnComp.minDistance != 0) {
                        Vector3f dist = new Vector3f(spawnPos);
                        dist.sub(originPos);

                        if (spawnComp.minDistance > dist.lengthSquared()) {
                            return;
                        }
                    }

                    // Look for an open spawn position either above or below the chosen spot.
                    int offset = 1;
                    while (offset < 30) {
                        if (worldProvider.getBlock(new Vector3f(spawnPos.x , spawnPos.y + offset, spawnPos.z)).isPenetrable()
                                && validateSpawnPos(new Vector3f(spawnPos.x , spawnPos.y + offset, spawnPos.z), 1, 1, 1)) {
                            break;
                        } else if (worldProvider.getBlock(new Vector3f(spawnPos.x , spawnPos.y - offset, spawnPos.z)).isPenetrable()
                                && validateSpawnPos(new Vector3f(spawnPos.x , spawnPos.y - offset, spawnPos.z), 1, 1, 1)) {
                            offset *= -1;
                            break;
                        }

                        offset++;
                    }

                    if (offset == 30) {
                        logger.info("Failed to find an open position to spawn at, sad");
                        return;
                    } else {
                        spawnPos = new Vector3f(spawnPos.x , spawnPos.y + offset, spawnPos.z);
                        logger.info("Found a valid spawn position that can fit the Spawnable! {}", spawnPos);
                    }
                }

                String chosenSpawnerType = spawnComp.types.get(random.randomIntAbs(spawnComp.types.size()));
                Set randomType = typeLists.get(chosenSpawnerType);
                logger.info("Picked random type {} which returned {} prefabs", chosenSpawnerType, randomType.size());
                if (randomType.size() == 0) {
                    logger.warn("Type {} wasn't found, sad :-( Won't spawn anything this time", chosenSpawnerType);
                    return;
                }
                int anotherRandomIndex = random.randomIntAbs(randomType.size());
                Object[] randomPrefabs = randomType.toArray();
                Prefab chosenPrefab = (Prefab) randomPrefabs[anotherRandomIndex];
                logger.info("Picked index {} of types {} which is a {}, to spawn at {}", anotherRandomIndex, chosenSpawnerType, chosenPrefab, spawnPos);

                // Finally create the Spawnable. Assign parentage so we can tie Spawnables to their Spawner if needed
                EntityRef newSpawnableRef = factory.generate(spawnPos, chosenPrefab);
                SpawnableComponent newSpawnable = newSpawnableRef.getComponent(SpawnableComponent.class);
                newSpawnable.parent = entity;

                // TODO: Use some sort of parent/inheritance thing with gelcubes -> specialized gelcubes
                // TODO: Introduce proper probability-based spawning

            }

        } finally {
            PerformanceMonitor.endActivity();
        }
    }

    /**
     * Validates a position as open enough to fit a Spawnable (or something else?) of the given dimensions
     *
     * @param pos position to start from
     * @param spawnableHeight height of what we want to fit into the space
     * @param spawnableDepth depth of what we want to fit into the space
     * @param spawnableWidth width of what we want to fit into the space
     * @return true if the spawn position will fit the Spawnable
     */
    private boolean validateSpawnPos(Vector3f pos, int spawnableHeight, int spawnableDepth, int spawnableWidth) {
        // TODO: Fill in with clean code or even switch to a generic utility method.
        // TODO: Could enhance this further with more suitability like ground below, water/non-water, etc. Just pass the whole prefab in
        return true;
    }
    
}

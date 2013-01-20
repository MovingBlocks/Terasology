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
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

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
        
        
        // Go through entities that are spawners. Only accept block-based spawners for now (due to location need)
        //logger.info("Count of entities with a SpawnerComponent: {}", entityManager.getComponentCount(SpawnerComponent.class));
        for (EntityRef entity : entityManager.iteratorEntities(SpawnerComponent.class)) {
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

            if(spawnComp.maxMobsPerSpawner>0){
            	// TODO Make sure we don't spawn too much stuff. Not very robust yet and doesn't tie mobs to their spawner of origin right
            	int maxMobs = entityManager.getComponentCount(SpawnerComponent.class) * spawnComp.maxMobsPerSpawner;
            	int currentMobs = entityManager.getComponentCount(SimpleAIComponent.class) + entityManager.getComponentCount(HierarchicalAIComponent.class);

            	logger.info("Mob count: {}/{}", currentMobs, maxMobs);

            	// TODO Probably need something better to base this threshold on eventually
            	if (currentMobs >= maxMobs ) {
            		logger.info("Too many mobs! Returning early");
            		return;
            	}
            }

            int spawnTypes = spawnComp.types.size();
            if (spawnTypes == 0) {
                logger.warn("Spawner has no types, sad - stopping this loop iteration early :-(");
                continue;
            }
            
            Vector3f spawnPos= new Vector3f();
            Vector3f pos= new Vector3f();
			if (entity.hasComponent(BlockComponent.class)) {
				BlockComponent blockComp = entity.getComponent(BlockComponent.class);
				spawnPos = blockComp.getPosition().toVector3f();
				// find player position
				// TODO: shouldn't use local player, need some way to find nearest player
				if (spawnComp.needsPlayer) {
					LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
					if (localPlayer != null) {
						Vector3f dist = new Vector3f(spawnPos);
						dist.sub(localPlayer.getPosition());
						double distanceToPlayer = dist.lengthSquared();

						if (distanceToPlayer > spawnComp.playerNeedRange) {
							logger.info("Spawner {} too far from player {}<{}",entity.getId(),distanceToPlayer,spawnComp.playerNeedRange);
							continue;
						}
					}
				}
			}else if(entity.hasComponent(LocalPlayerComponent.class)){
				LocalPlayerComponent lpc = entity.getComponent(LocalPlayerComponent.class);
				if(lpc.isDead)
					return;
				LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
				spawnPos =localPlayer.getPosition();
				logger.info("your position"+ spawnPos.x +":"+ spawnPos.y +":"+spawnPos.z);
			}
            
            //TODO chech for biger creatures and creatures whit special needs
            //TODO check biome
            if (spawnComp.rangedSpawning) {
            	int i=0,a=0;
            	do{
                pos = new Vector3f(spawnPos.x + random.randomFloat() * spawnComp.range, spawnPos.y, spawnPos.z + random.randomFloat() * spawnComp.range);
                if(spawnComp.minDistance!=0){
                	Vector3f dist = new Vector3f(pos);
                	dist.sub(spawnPos);
                	if(spawnComp.minDistance>dist.lengthSquared())
                		continue;
                }
                //check that spawned wont drop far
                while(worldProvider.getBlock(new Vector3f(pos.x , pos.y-1, pos.z)).isPenetrable() && i < 30){
                	pos =new Vector3f(pos.x , pos.y-1, pos.z);
                	//logger.info("changing position"+ pos.x +":"+ pos.y +":"+pos.z);
                	i++;
                }
                i=0;
                while(!worldProvider.getBlock(new Vector3f(pos.x , pos.y, pos.z)).isPenetrable() && i < 30){
                	pos =new Vector3f(pos.x , pos.y+1, pos.z);
                	//logger.info("changing position"+ pos.x +":"+ pos.y +":"+pos.z);
                	i++;
                }
                
                a++;
                if(a>10)
                	return;
                //logger.info("trying find spawn point");
            	}while(testEnviriment(pos,1,1,1));
            }else{
            	pos=spawnPos;
            	if(!testEnviriment(pos,1,1,1)){
            		logger.info("cannot local spawn inside terain");
            		return;
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
            logger.info("Picked index {} of types {} which is a {}, to spawn at {}", anotherRandomIndex, chosenSpawnerType, chosenPrefab, pos);

            factory.generate(pos, chosenPrefab);
            
            // TODO: Use some sort of parent/inheritance thing with gelcubes -> specialized gelcubes
            // TODO: Introduce proper probability-based spawning
        }
    }
    
    /**
     * test if surounding blocks are penetrable
     * starts from that level upward
     * @return
     */
    private boolean testEnviriment(Vector3f pos,int height,int depth,int width){
    	boolean pass=true;
    	int h=height/2;
    	int w=width/2;
    	int d=depth;
    	int x=-h,y=-w,z=0;
    	while(x<=h){
    		while(y<=w){
    			while(z<=d){
    			if(!worldProvider.getBlock(new Vector3f(pos.x+x,pos.y+y,pos.z+z)).isPenetrable())
    				pass=false;
    				z++;
    			}
    			y++;
    			z=0;
    		}
    		x++;
    		y=-w;
    	}
    	return pass;
    }
    
}
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
package org.terasology.utilities;

import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;


/**
 * Tools for managing Entities
 * 
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public class EntityTools {

    private WorldProvider       worldProvider;

    private EntityManager       entityManager;

    private static final Logger logger = LoggerFactory.getLogger(EntityTools.class);

    public EntityTools(WorldProvider worldProvider, EntityManager entityManager) {
        this.entityManager = entityManager;// CoreRegistry.get(EntityManager.class);
        this.worldProvider = worldProvider;
    }

    // TODO create cheaper way to do these

    /**
     * return nearest entity
     * Warning: calculation heavy algorithm
     * 
     * @param position
     * @return Nearest entity or NULL if none
     */
    public EntityRef findNearestEntity(Vector3f position) {
        EntityRef nearest = EntityRef.NULL;
        float lengthSquared = Float.MAX_VALUE;
        for (EntityRef entity : entityManager.iteratorEntities(LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            Vector3f dist = new Vector3f(position);
            dist.sub(loc.getWorldPosition());
            if (dist.lengthSquared() < lengthSquared) {
                lengthSquared = dist.lengthSquared();
                nearest = entity;
            }
        }
        return nearest;
    }
       
    /**
     * return nearest entity whit given Component and location component
     * Warning: calculation heavy algorithm
     * 
     * @param position
     * @param componentClass
     * @return Nearest entity or NULL if none
     */
    public EntityRef findNearestEntityWhitComponent(Vector3f position, Class<? extends Component> componentClass) {
        EntityRef nearest = EntityRef.NULL;
        float lengthSquared = Float.MAX_VALUE;
        for (EntityRef entity : entityManager.iteratorEntities(componentClass, LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            Vector3f dist = new Vector3f(position);
            dist.sub(loc.getWorldPosition());
            if (dist.lengthSquared() < lengthSquared) {
                lengthSquared = dist.lengthSquared();
                nearest = entity;
            }
        }
        return nearest;
    }
    
    /**
     * return entities in range
     * Warning: calculation heavy algorithm
     * 
     * @param position
     * @param range
     * @return
     */
    public Map<Float, EntityRef> findEntitysInRange(Vector3f position, float range) {
        Map<Float, EntityRef> tree = new TreeMap<Float, EntityRef>();
        for (EntityRef entity : entityManager.iteratorEntities(LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            Vector3f dist = new Vector3f(position);
            dist.sub(loc.getWorldPosition());
            if (dist.lengthSquared() < range) {
                tree.put(new Float(dist.lengthSquared()), entity);
            }
        }
        return tree;
    }

    /**
     * return entities in range whit given Component and location component
     * Warning: calculation heavy algorithm
     * 
     * @param position
     * @param range
     * @param componentClass
     * @return
     */
    public Map<Float, EntityRef> findEntitysInRangeWhitComponent(Vector3f position, float range, Class<? extends Component> componentClass) {
        Map<Float, EntityRef> tree = new TreeMap<Float, EntityRef>();
        for (EntityRef entity : entityManager.iteratorEntities(componentClass, LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            Vector3f dist = new Vector3f(position);
            dist.sub(loc.getWorldPosition());
            if (dist.lengthSquared() < range) {
                tree.put(new Float(dist.lengthSquared()), entity);
            }
        }
        return tree;
    }

    /**
     * return first entity with given component
     * Warning: calculation heavy algorithm
     * 
     * @param componentClass
     * @return entity whit component
     */
    public EntityRef findFirstEntityWhitComponent(Class<? extends Component> componentClass) {
        for (EntityRef entity : entityManager.iteratorEntities(componentClass, LocationComponent.class)) {
            return entity;
        }
        return EntityRef.NULL;
    }

    /**
     * return first entity in range whit given Component and location component
     * Warning: calculation heavy algorithm
     * 
     * @param position
     * @param range
     * @param componentClass
     * @return
     */
    public EntityRef findFirstEntityWhitComponentInRange(Vector3f position, float range, Class<? extends Component> componentClass) {
        for (EntityRef entity : entityManager.iteratorEntities(componentClass, LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            Vector3f dist = new Vector3f(position);
            dist.sub(loc.getWorldPosition());
            if (dist.lengthSquared() < range) {
                return entity;
            }
        }
        return EntityRef.NULL;
    }
    
    /**
     * check if players in range
     * Warning: calculation heavy algorithm
     * 
     * @param position
     * @param range
     * @return boolean
     */
    public boolean isPlayerInRange(Vector3f position, float range) {
        for (EntityRef entity : entityManager.iteratorEntities(LocationComponent.class)) {
            if ((entity.hasComponent(LocalPlayerComponent.class) || entity.hasComponent(PlayerComponent.class)) && entity.hasComponent(LocationComponent.class)) {

                LocationComponent loc = entity.getComponent(LocationComponent.class);
                Vector3f dist = new Vector3f(position);
                dist.sub(loc.getWorldPosition());
                if (dist.lengthSquared() < range) {
                    return true;
                }
            } else {
                continue;
            }
        }
        return false;
    }

    /**
     * Test if surrounding blocks are penetrable starts given level upward
     * 
     * @param pos
     *            position
     * @param x
     *            dimension of checked area on x
     * @param y
     *            dimension of checked area on y
     * @param z
     *            dimension of checked area on z
     * @return if surrounding is penetrable
     */
    public boolean isSurroundingPenetrable(Vector3f position, int x, int y, int z) {
        if (!testVariablesSurounding(position)) {
            return false;
        }
        int h = x / 2;
        int w = z / 2;
        int d = y;
        int x1 = -h, y1 = -w, z1 = 0;
        while (x1 <= h) {
            while (y1 <= w) {
                while (z1 <= d) {
                    if (!worldProvider.getBlock(new Vector3f(position.x + x1, position.y + y1, position.z + z1)).isPenetrable()) {
                        return false;
                    }
                    z1++;
                }
                y1++;
                z1 = 0;
            }
            x1++;
            y1 = -w;
        }
        return true;
    }
    
    /**
     * Test if surrounding blocks are equal to given block starts given level upward
     * 
     * @param pos
     *            position
     * @param x
     *            dimension of checked area on x
     * @param y
     *            dimension of checked area on y
     * @param z
     *            dimension of checked area on z
     * @param Block
     *            block to be compared to
     * @return if surrounding are given block
     */
    public boolean isSurrounding(Vector3f position, int x, int y, int z, Block block) {
        if (!testVariablesSurounding(position)) {
            return false;
        }
        int h = x / 2;
        int w = z / 2;
        int d = y;
        int x1 = -h, y1 = -w, z1 = 0;
        while (x1 <= h) {
            while (y1 <= w) {
                while (z1 <= d) {
                    if (!worldProvider.getBlock(new Vector3f(position.x + x1, position.y + y1, position.z + z1)).equals(block)) {
                        return false;
                    }
                    z1++;
                }
                y1++;
                z1 = 0;
            }
            x1++;
            y1 = -w;
        }
        return true;
    }
    
    /**
     * Test if surrounding blocks are equal to given block's name starts given level upward
     * 
     * @param pos
     *            position
     * @param x
     *            dimension of checked area on x
     * @param y
     *            dimension of checked area on y
     * @param z
     *            dimension of checked area on z
     * @param String
     *            name of block to be compared to
     * @return if surrounding are given block
     */
    public boolean isSurrounding(Vector3f position, int x, int y, int z, String name) {
        if (!testVariablesSurounding(position)) {
            return false;
        }
        int h = x / 2;
        int w = z / 2;
        int d = y;
        int x1 = -h, y1 = -w, z1 = 0;
        while (x1 <= h) {
            while (y1 <= w) {
                while (z1 <= d) {
                    if (worldProvider.getBlock(new Vector3f(position.x + x1, position.y + y1, position.z + z1)).getDisplayName().compareTo(name) != 0) {
                        return false;
                    }
                    z1++;
                }
                y1++;
                z1 = 0;
            }
            x1++;
            y1 = -w;
        }
        return true;
    }
    
    /**
     * 
     * @param position
     * @param maxChange
     */
    public void liftToPenetrable(Vector3f position, int maxChange) {
        if (!testVariablesLift(position, maxChange)) {
            return;
        }
        int i = 0;
        // check that spawned wont drop far
        while (worldProvider.getBlock(new Vector3f(position.x, position.y - 1, position.z)).isPenetrable() && i < maxChange) {
            position = new Vector3f(position.x, position.y - 1, position.z);
            // logger.info("changing position"+ pos.x +":"+ pos.y +":"+pos.z);
            i++;
        }
        i = 0;
        //check that position isn't in terrain
        while (!worldProvider.getBlock(new Vector3f(position.x, position.y, position.z)).isPenetrable() && i < maxChange) {
            position = new Vector3f(position.x, position.y + 1, position.z);
            // logger.info("changing position"+ pos.x +":"+ pos.y +":"+pos.z);
            i++;
        }
    }
    
    /**
     * 
     * @param position
     * @param maxChange
     */
    public void liftToSurface(Vector3f position, int maxChange) {
        if (!testVariablesLift(position, maxChange)) {
            return;
        }
        int i = 0;
        // check that spawned wont drop far
        while (worldProvider.getBlock(new Vector3f(position.x, position.y - 1, position.z)).getDisplayName().compareTo("Air") == 0 && i < maxChange) {
            position = new Vector3f(position.x, position.y - 1, position.z);
            // logger.info("changing position"+ position.x +":"+ position.y +":"+position.z);
            // logger.info("block"+ worldProvider.getBlock(new Vector3f(position.x , position.y, position.z)).getDisplayName());
            i++;
        }
        i = 0;
        //check that position isn't in terrain
        while (worldProvider.getBlock(new Vector3f(position.x, position.y, position.z)).getDisplayName().compareTo("Air") != 0 && i < maxChange) {
            position = new Vector3f(position.x, position.y + 1, position.z);
            // logger.info("changing position"+ position.x +":"+ position.y +":"+position.z);
            // logger.info("block"+ worldProvider.getBlock(new Vector3f(position.x , position.y, position.z)).getDisplayName());
            i++;
        }
    }
    
    /**
     * 
     * @param position
     * @param maxChange
     * @param name
     */
    public void liftToBlock(Vector3f position, int maxChange, String name) {
        if (!testVariablesLift(position, maxChange)) {
            return;
        }
        int i = 0;
        // check that spawned wont drop far
        while (worldProvider.getBlock(new Vector3f(position.x, position.y - 1, position.z)).getDisplayName().compareTo(name) == 0 && i < maxChange) {
            position = new Vector3f(position.x, position.y - 1, position.z);
            // logger.info("changing position"+ pos.x +":"+ pos.y +":"+pos.z);
            i++;
        }
        i = 0;
      //check that position isn't in terrain
        while (worldProvider.getBlock(new Vector3f(position.x, position.y, position.z)).getDisplayName().compareTo(name) != 0 && i < maxChange) {
            position = new Vector3f(position.x, position.y + 1, position.z);
            // logger.info("changing position"+ pos.x +":"+ pos.y +":"+pos.z);
            i++;
        }
    }
    
    /**
     * Test initializer and stuff for avoid nullPointerExpection
     * @param position
     * @return
     */
    private boolean testVariablesSurounding(Vector3f position) {
        if (position.y < 0) {
            logger.warn("trying to spawn under min y");
            return false;
        }
        if (worldProvider != null) {
            if (!worldProvider.isBlockActive(position)) {
                logger.warn("block not active skipping");
                return false;
            }
        } else {
            logger.error("world provider is null");
            return false;
        }
        return true;
    }

    /**
     * Test initializer and stuff for avoid nullPointerExpection
     * @param position
     * @param maxChange
     * @return
     */
    private boolean testVariablesLift(Vector3f position, int maxChange) {
        if (position.y - maxChange < 0) {
            logger.warn("trying to spawn under min y");
            return false;
        }
        if (worldProvider != null) {
            if (!worldProvider.isBlockActive(position)) {
                logger.warn("block not active skipping");
                return false;
            }
        } else {
            logger.error("world provider is null");
            return false;
        }

        return true;
    }

    /**
     * @return the worldProvider
     */
    public WorldProvider getWorldProvider() {
        return worldProvider;
    }

    /**
     * @param worldProvider
     *            the worldProvider to set
     */
    public void setWorldProvider(WorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @param entityManager
     *            the entityManager to set
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}

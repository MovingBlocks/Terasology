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
package org.terasology.logic.manager;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Vector3f;

import org.terasology.components.SimpleAIComponent;
import org.terasology.entityFactory.GelatinousCubeFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.logic.portals.Portal;
import org.terasology.utilities.FastRandom;

/**
 * Manages Portals - a core game feature anchoring parts of the world, allowing spawning within a certain radius,
 * linking estates, easy travel, community features, etc.
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PortalManager {
    /**
     * Set that contains Portals
     */
    private final HashSet<Portal> _portalStore = new HashSet<Portal>();

    private final int maxMobs = 16;
    private final FastRandom _random = new FastRandom();
    private EntityManager _entityManager;
    private GelatinousCubeFactory factory;
    private Logger logger = Logger.getLogger(getClass().getName());

    public PortalManager(EntityManager entityManager) {
        _entityManager = entityManager;
        factory = new GelatinousCubeFactory();
        factory.setEntityManager(entityManager);
        factory.setRandom(_random);
    }

    /**
     * Acts on a tick to consider spawning a mob within the range of one or more Portals
     */
    public void tickSpawn() {
        // Loop through Portals, calculate probability to spawn something, then maybe spawn something
        for (Portal p : _portalStore) {
            spawnLocal(p);
            spawnWild(p);
        }
    }

    /**
     * Has a chance to spawn something locally to Portal p
     *
     * @param p the portal we're possibly spawning something nearby
     * @return boolean indicating if something spawned
     */
    private boolean spawnLocal(Portal p) {
        if (_entityManager.getComponentCount(SimpleAIComponent.class) >= maxMobs)
            return false;

        // 12.5% chance something will spawn locally to the portal - will get fancier later
        boolean spawn = _random.randomBoolean() && _random.randomBoolean() && _random.randomBoolean();
        if (spawn) {
            Vector3f pos = new Vector3f((float) p.getBlockLocation().x, (float) p.getBlockLocation().y - 1, (float) p.getBlockLocation().z);
            factory.generateGelatinousCube(pos);
            logger.log(Level.INFO, "Spawning local GelatinousCube at " + pos);
        }
        return spawn;
    }

    /**
     * Has a chance to spawn something in the "wild" around Portal p
     *
     * @param p the portal we're possibly spawning something nearby
     * @return boolean indicating if something spawned
     */
    private boolean spawnWild(Portal p) {
        if (_entityManager.getComponentCount(SimpleAIComponent.class) >= maxMobs)
            return false;

        // 25% change something will spawn in the wild around the portal - will get fancier later
        boolean spawn = _random.randomBoolean() && _random.randomBoolean();
        if (spawn) {
            Vector3f pos = new Vector3f(_random.randomFloat(), 0, _random.randomFloat());
            pos.scale(256);
            factory.generateGelatinousCube(pos);
            logger.log(Level.INFO, "Spawning wild GelatinousCube at " + pos);
        }
        return spawn;
    }

    /**
     * A check for whether a new Portal is needed for a new world
     *
     * @return boolean indicating whether or not a portal exists somewhere
     */
    public boolean hasPortal() {

        return _portalStore.size() >= 1;
    }

    public void addPortal(Vector3f loc) {
        _portalStore.add(new Portal(loc));
    }

}

/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.terasology.game.Terasology;
import org.terasology.logic.characters.GelatinousCube;
import org.terasology.logic.portals.Portal;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;
import java.util.HashSet;
import java.util.logging.Level;

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

    private final FastRandom _random = new FastRandom();
    private final WorldRenderer _parent;

    public PortalManager(WorldRenderer parent) {
        _parent = parent;
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
        /*if (_parent.getMobManager().getActiveMobAmount() > 32)
            return false;

        // 12.5% chance something will spawn locally to the portal - will get fancier later
        boolean spawn = _random.randomBoolean() && _random.randomBoolean() && _random.randomBoolean();
        if (spawn) {
            GelatinousCube cubey = new GelatinousCube(_parent);
            cubey.setSpawningPoint(new Vector3d(p.getBlockLocation().x, p.getBlockLocation().y - 1, p.getBlockLocation().z));
            cubey.respawn();
            Terasology.getInstance().getLogger().log(Level.INFO, "Spawning local GelatinousCube at " + cubey.getSpawningPoint());
            _parent.getMobManager().addMob(cubey);
        }
        return spawn;*/
        return false;
    }

    /**
     * Has a chance to spawn something in the "wild" around Portal p
     *
     * @param p the portal we're possibly spawning something nearby
     * @return boolean indicating if something spawned
     */
    private boolean spawnWild(Portal p) {
        /*if (_parent.getMobManager().getActiveMobAmount() > 32)
            return false;

        // 25% change something will spawn in the wild around the portal - will get fancier later
        boolean spawn = _random.randomBoolean() && _random.randomBoolean();
        if (spawn) {
            GelatinousCube cubey = new GelatinousCube(_parent);

            // Spawn some Gel. Cubes in the wilderness!
            Vector3d randomOffset = new Vector3d(_parent.getWorldProvider().getRandom().randomDouble(), 0, _parent.getWorldProvider().getRandom().randomDouble());
            randomOffset.scale(256);

            cubey.setSpawningPoint(new Vector3d(p.getBlockLocation().x + randomOffset.x, p.getBlockLocation().y + 1, p.getBlockLocation().z + randomOffset.z));
            cubey.respawn();
            Terasology.getInstance().getLogger().log(Level.INFO, "Spawning wild GelatinousCube at " + cubey.getSpawningPoint());
            _parent.getMobManager().addMob(cubey);
        }
        return spawn; */
        return false;
    }

    /**
     * A check for whether a new Portal is needed for a new world
     *
     * @return boolean indicating whether or not a portal exists somewhere
     */
    public boolean hasPortal() {

        return _portalStore.size() >= 1;
    }

    public void addPortal(Vector3d loc) {
        _portalStore.add(new Portal(loc));
    }

}

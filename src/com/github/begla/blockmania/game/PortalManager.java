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

package com.github.begla.blockmania.game;

import com.github.begla.blockmania.game.mobs.GelatinousCube;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.main.World;
import javolution.util.FastSet;

import javax.vecmath.Vector3f;
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
    private FastSet<Portal> _portalStore = new FastSet<Portal>();

    private final FastRandom _random = new FastRandom();
    private World _parent;

    public PortalManager(World parent) {
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
        if (_parent.getMobManager().getActiveMobAmount() > 64)
            return false;

        // 25% change something will spawn locally to the portal - will get fancier later
        boolean spawn = _random.randomBoolean() && _random.randomBoolean();
        if (spawn) {
            GelatinousCube s = new GelatinousCube(_parent);
            s.setSpawningPoint(new Vector3f(p.getBlockLocation().x, p.getBlockLocation().y - 1, p.getBlockLocation().z));
            s.respawn();
            Blockmania.getInstance().getLogger().log(Level.INFO, "Spawning local slime at " + s.getSpawningPoint());
            _parent.getMobManager().addMob(s);
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
        if (_parent.getMobManager().getActiveMobAmount() > 128)
            return false;

        // 25% change something will spawn in the wild around the portal - will get fancier later
        boolean spawn = _random.randomBoolean() && _random.randomBoolean();
        if (spawn) {
            GelatinousCube s = new GelatinousCube(_parent);

            // Spawn some Gel. Cubes in the wilderness!
            Vector3f randomOffset = new Vector3f((float) _parent.getWorldProvider().getRandom().randomDouble(), 0 ,(float) _parent.getWorldProvider().getRandom().randomDouble());
            randomOffset.scale(64);

            s.setSpawningPoint(new Vector3f(p.getBlockLocation().x + randomOffset.x, p.getBlockLocation().y + 1, p.getBlockLocation().z + randomOffset.z));
            s.respawn();
            Blockmania.getInstance().getLogger().log(Level.INFO, "Spawning wild slime at " + s.getSpawningPoint());
            _parent.getMobManager().addMob(s);
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

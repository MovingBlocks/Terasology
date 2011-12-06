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

package com.github.begla.blockmania.rendering.manager;

import com.github.begla.blockmania.world.entity.MovableEntity;
import com.github.begla.blockmania.world.main.WorldRenderer;

import java.util.HashSet;

/**
 * MobManager handles non-player entities that do stuff in the world
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class MobManager {

    /**
     * Set that contains mobs
     */
    private HashSet<MovableEntity> _mobStore = new HashSet<MovableEntity>();
    private WorldRenderer _parent;

    /**
     * Default constructor - doesn't do anything yet
     */
    public MobManager(WorldRenderer parent) {
        _parent = parent;
    }

    /**
     * Acts on a tick to consider AI actions that involve multiple mobs at once (individual AI is the mob's responsibility)
     */
    public void tickAI() {
        // Nothing yet - first example might be identifying when 2+ mobs are close enough to encourage them to do something
        // Actions to maybe take: group up (slimes could even merge), start an NPC construction, etc
    }

    /**
     * Loops through all mobs in the Set and calls their update() method
     */
    public void updateAll() {
        for (MovableEntity mob : _mobStore) {
            if (_parent.isInRange(mob.getPosition()))
                mob.update();
        }
    }

    /**
     * Loops through all mobs in the Set and calls their render() method
     */
    public void renderAll() {
        for (MovableEntity mob : _mobStore) {
            if (_parent.isEntityVisible(mob) && _parent.isInRange(mob.getPosition()))
                mob.render();
        }
    }

    /**
     * Adds a mob to the manager
     *
     * @param mob The provided mob
     */
    public void addMob(MovableEntity mob) {
        _mobStore.add(mob);
    }

    /**
     * Returns the amount of spawned mobs.
     *
     * @return The amount of mobs active
     */
    public int getActiveMobAmount() {
        return _mobStore.size();
    }
}

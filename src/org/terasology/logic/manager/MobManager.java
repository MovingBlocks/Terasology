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

import org.terasology.logic.entities.MovableEntity;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.world.WorldRenderer;

import java.util.HashSet;

/**
 * MobManager handles non-player entities that do stuff in the world
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MobManager implements IGameObject {

    /**
     * Set that contains mobs
     */
    private final HashSet<MovableEntity> _mobStore = new HashSet<MovableEntity>();
    private final WorldRenderer _parent;

    /**
     * Default constructor - doesn't do anything yet
     *
     * @param parent The parent world renderer
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

    public void render() {
        for (MovableEntity mob : _mobStore) {
            if (_parent.isEntityVisible(mob) && _parent.isInRange(mob.getPosition()))
                mob.render();
        }
    }

    public void update(double delta) {
        for (MovableEntity mob : _mobStore) {
            if (_parent.isInRange(mob.getPosition()))
                mob.update(delta);
        }
    }
}

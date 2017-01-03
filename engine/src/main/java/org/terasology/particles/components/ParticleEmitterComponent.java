/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.particles.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.particles.events.ParticleSystemUpdateEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a particle emitter. A particle emitter must be added to a ParticleSystemComponent to produce particles.
 */
public class ParticleEmitterComponent implements Component {
    public static final int INFINITE_PARTICLE_SPAWNS = -1;

    /**
     * Reference to the entity this component is attached to
     */
    public EntityRef ownerEntity;

    /**
     * The maximum spawn rate of this emitter
     */
    public float spawnRateMax = 11.0f;

    /**
     * The minimum spawn rate of this emitter
     */
    public float spawnRateMin = 9.0f;

    /**
     * Toggles this particle emitter.
     */
    public boolean enabled = true;

    /**
     * The maximum life time of this emitter, the emitter will auto-remove upon reaching 0 TODO: Implement emitter lifetime
     */
    public float maxLifeTime = Float.POSITIVE_INFINITY;

    /**
     * The maximum amount of particle this emitter can emit before auto-removing, the emitter will auto-remove upon reaching 0 TODO: Implement emitter max spawns
     */
    public int particleSpawnsLeft = INFINITE_PARTICLE_SPAWNS;

    @Owns
    private List<EntityRef> generators = new ArrayList<>();

    /**
     * Notifies ParticleSystemManagerImpl that this system needs an updated ParticleSystemStateData
     */
    private void requestUpdate() {
        if (ownerEntity != null) {
            ownerEntity.send(new ParticleSystemUpdateEvent());
        }
    }

    // GENERATOR LIST ACCESS

    public void addGenerator(final EntityRef generator) {
        generators.add(generator);
        requestUpdate();
    }

    public boolean removeGenerator(final EntityRef generator) {
        boolean removed = generators.remove(generator);
        requestUpdate();
        return removed;
    }

    public EntityRef removeGenerator(final int generatorIndex) {
        EntityRef generator = generators.remove(generatorIndex);
        requestUpdate();
        return generator;
    }

    public EntityRef getGenerator(int index) {
        return generators.get(index);
    }

    public List<EntityRef> getGenerators() {
        return generators;
    }
}

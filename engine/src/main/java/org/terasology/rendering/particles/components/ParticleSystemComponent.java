/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.particles.components;

import org.lwjgl.util.vector.Vector2f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.particles.events.ParticleSystemUpdateEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a particle system. A particle system must have emitters in order to produce particles.
 */
public class ParticleSystemComponent implements Component {

    // Reference to owner entity
    public EntityRef entityRef;

    /** The lifetime of this system */
    public float maxLifeTime = Float.POSITIVE_INFINITY;
    public boolean destroyWhenFinished = true;

    /** The amount of particles in this system's pool. Emitters under this system will share the particles. */
    public int nrOfParticles = 1000;

    @Owns
    private List<EntityRef> emitters = new LinkedList<>();

    @Owns
    private List<EntityRef> affectors = new LinkedList<>();

    /** This system's particle texture */
    public Texture texture = null;
    /** This system's particle texture size, in percents x: [0.0, 1.0], y: [0.0, 1.0] */
    public Vector2f textureSize = new Vector2f(1.0f, 1.0f);


    /**
    * Notify ParticleSystemManager that this system needs an updated ParticleSystemStateData
    * */
    private void requestUpdate () {
        if (entityRef != null) {
            entityRef.send(new ParticleSystemUpdateEvent());
        }
    }

    // EMITTER LIST ACCESS
    public void addEmitter (final EntityRef emitter) {
        emitters.add(emitter);
        requestUpdate();
    }

    public boolean removeEmitter (final EntityRef emitter) {
        boolean removed = emitters.remove(emitter);
        requestUpdate();
        return removed;
    }

    public EntityRef removeEmitter (final int emitterIndex) {
        EntityRef emitter = emitters.remove(emitterIndex);
        requestUpdate();
        return emitter;
    }

    public EntityRef getEmitter (int index) {
        return emitters.get(index);
    }

    public List<EntityRef> getEmitters () {
        return emitters;
    }

    // AFFECTOR LIST ACCESS
    public void addAffector (final EntityRef affector) {
        affectors.add(affector);
        requestUpdate();
    }

    public boolean removeAffector (final EntityRef affector) {
        boolean removed = affectors.remove(affector);
        requestUpdate();
        return removed;
    }

    public EntityRef removeAffector (final int affectorIndex) {
        EntityRef affector = affectors.remove(affectorIndex);
        requestUpdate();
        return affector;
    }

    public EntityRef getAffector (int index) {
        return affectors.get(index);
    }

    public List<EntityRef> getAffectors () {
        return affectors;
    }
}

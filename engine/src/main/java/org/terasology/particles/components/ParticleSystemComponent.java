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

import org.lwjgl.util.vector.Vector2f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.particles.events.ParticleSystemUpdateEvent;
import org.terasology.particles.rendering.ParticleRenderer;
import org.terasology.rendering.assets.texture.Texture;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a particle system. A particle system must have emitters in order to produce particles.
 */
public class ParticleSystemComponent implements Component {
    public static final int INDEFINITE_SYSTEM_LIFETIME = -1;

    /**
     * Reference to the entity this component is attached to
     */
    public EntityRef ownerEntity;

    /**
     * The renderer that should be used to render this system.
     */
    private Class<ParticleRenderer> renderer;

    /**
     * Toggles whether this particle system should auto-add emitters attached to the same entity. (For use in prefabs)
     */
    public boolean isEmitter = true;

    /**
     * The remaining lifetime of this system in seconds.
     */
    public float lifeTime = INDEFINITE_SYSTEM_LIFETIME;

    /**
     * Toggles whether when this system is destroyed only the component should be destroyed or the whole entity.
     */
    public boolean destroyEntityWhenDead;

    /**
     * Destroy this particle system's emitters when it runs out of lifetime TODO: Implement destroyWhenFinished
     */
    public boolean destroyEmittersWhenDead = true;

    /**
     * Destroy this particle system when its last emitter is dead TODO: Implement destroyWhenFinished
     */
    public boolean destroyWhenFinished = true;

    /**
     * The amount of particles in this system's pool. Emitters under this system will share the particles.
     */
    public int particlePoolSize = 500;

    /**
     * Toggles if this system's particles should collide with blocks.
     */
    public boolean particleCollision = true;

    /**
     * Toggles if this particle system should emit new particles.
     */
    public boolean enabled = true;

    @Owns
    private List<EntityRef> emitters = new LinkedList<>();

    @Owns
    private List<EntityRef> affectors = new LinkedList<>();

    /**
     * This system's particle texture
     */
    public Texture texture;

    /**
     * This system's particle texture size, in percents x: [0.0, 1.0], y: [0.0, 1.0]
     */
    public Vector2f textureSize = new Vector2f(1.0f, 1.0f);

    /**
     * This system's particle texture offset, in percents x: [0.0, 1.0], y: [0.0, 1.0]
     */
    public Vector2f textureOffset = new Vector2f(1.0f, 1.0f);


    /**
     * Notifies ParticleSystemManagerImpl that this system needs an updated ParticleSystem
     */
    private void requestUpdate() {
        if (ownerEntity != null) {
            ownerEntity.send(new ParticleSystemUpdateEvent());
        }
    }

    /**
     * Changes the particle system's desired renderer. Will register a new renderer if it does not already exist.
     *
     * @param renderer The renderer to set this system to.
     */
    public void setRenderer(Class<ParticleRenderer> renderer) {
        this.renderer = renderer;
        requestUpdate();
    }

    /**
     * @return The renderer this system should be rendered with.
     */
    public Class<? extends ParticleRenderer> getRenderer() {
        return renderer != null ? renderer : org.terasology.particles.rendering.DisplayListParticleRenderer.class;
    }

    // EMITTER LIST ACCESS

    public void addEmitter(final EntityRef emitter, boolean update) {
        emitters.add(emitter);
        if (update) {
            requestUpdate();
        }
    }

    public void addEmitter(final EntityRef emitter) {
        addEmitter(emitter, true);
    }

    public boolean removeEmitter(final EntityRef emitter) {
        boolean removed = emitters.remove(emitter);
        requestUpdate();
        return removed;
    }

    public EntityRef removeEmitter(final int emitterIndex) {
        EntityRef emitter = emitters.remove(emitterIndex);
        requestUpdate();
        return emitter;
    }

    public EntityRef getEmitter(int index) {
        return emitters.get(index);
    }

    public List<EntityRef> getEmitters() {
        return emitters;
    }

    // AFFECTOR LIST ACCESS

    public void addAffector(final EntityRef affector) {
        affectors.add(affector);
        requestUpdate();
    }

    public boolean removeAffector(final EntityRef affector) {
        boolean removed = affectors.remove(affector);
        requestUpdate();
        return removed;
    }

    public EntityRef removeAffector(final int affectorIndex) {
        EntityRef affector = affectors.remove(affectorIndex);
        requestUpdate();
        return affector;
    }

    public EntityRef getAffector(int index) {
        return affectors.get(index);
    }

    public List<EntityRef> getAffectors() {
        return affectors;
    }
}

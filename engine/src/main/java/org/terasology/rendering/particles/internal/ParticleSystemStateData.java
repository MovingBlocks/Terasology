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
package org.terasology.rendering.particles.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.particles.components.ParticleEmitterComponent;
import org.terasology.rendering.particles.components.ParticleSystemComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Linus on 4-3-2015.
 */
public class ParticleSystemStateData {

    public final EntityRef entityRef;

    public final ParticleEmitterComponent emitterComponent;

    public float nextEmission;

    public final ParticlePool particlePool;
    public int collisionUpdateIteration;

    public List<Component> affectorComponents;

    public ParticleSystemStateData(final EntityRef entityRef, final ParticlePool particlePool) {
        this.entityRef = entityRef;
        this.particlePool = particlePool;
        this.emitterComponent = entityRef.getComponent(ParticleSystemComponent.class).emitter.getComponent(ParticleEmitterComponent.class);
        this.collisionUpdateIteration = 0;

        this.nextEmission = 0;

        this.affectorComponents = new ArrayList<>();
    }
}

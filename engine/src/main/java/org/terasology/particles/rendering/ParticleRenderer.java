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
package org.terasology.particles.rendering;

import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.particles.ParticleSystemManager;
import org.terasology.particles.ParticleSystemStateData;
import org.terasology.registry.In;

import java.util.stream.Stream;

/**
 *
 */
public abstract class ParticleRenderer implements RenderSystem {

    @In
    ParticleSystemManager particleSystemManager;

    public Stream<ParticleSystemStateData> getParticleSystems() {
        return particleSystemManager.getStateDataForRenderer(getClass());
    }
}

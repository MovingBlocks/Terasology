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
package org.terasology.rendering.particles;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.particles.functions.affectors.AffectorFunction;
import org.terasology.rendering.particles.functions.generators.GeneratorFunction;

/**
 * Created by Linus on 10-3-2015.
 */
public interface ParticleManagerInterface {
    public EntityRef createParticleSystem();

    //public EntityRef createEmmiter(EntityRef particleSystem);

    public void registerAffectorFunction(AffectorFunction affectorFunction);

    public void registerGeneratorFunction(GeneratorFunction generatorFunction);

    //public EntityRef createEmmiterParticleSystem(EntityRef particleSystem);
}

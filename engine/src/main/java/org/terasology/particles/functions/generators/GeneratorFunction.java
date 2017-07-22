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
package org.terasology.particles.functions.generators;

import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.particles.ParticleData;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.functions.ParticleSystemFunction;
import org.terasology.utilities.random.Random;

/**
 * A generator function is called on a particle's data when it is created to set its fields.
 */
@API
public abstract class GeneratorFunction<T extends Component> extends ParticleSystemFunction<T> {
    public GeneratorFunction(Class<T> component, ParticleDataMask dataMask, ParticleDataMask... dataMasks) {
        super(component, dataMask, dataMasks);
    }

    public abstract void onEmission(T component, ParticleData particleData, Random random);
}

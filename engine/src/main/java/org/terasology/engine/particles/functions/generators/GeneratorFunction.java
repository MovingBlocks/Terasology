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
package org.terasology.engine.particles.functions.generators;

import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.functions.ParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

/**
 * A generator function is called on a particle's data when it is created to set its fields.
 */
@API
public abstract class GeneratorFunction<T extends Component> extends ParticleSystemFunction<T> {
    public GeneratorFunction(ParticleDataMask dataMask, ParticleDataMask... dataMasks) {
        super(dataMask, dataMasks);
    }

    public abstract void onEmission(T component, ParticleData particleData, Random random);
}

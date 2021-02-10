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
package org.terasology.particles.functions.affectors;

import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.particles.ParticleData;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.functions.ParticleSystemFunction;
import org.terasology.utilities.random.Random;

/**
 * A affector function is called on a particle's data when it is updated to set its fields (Ex. Apply a force to a particle).
 */

@API
public abstract class AffectorFunction<T extends Component> extends ParticleSystemFunction<T> implements Cloneable {
    public AffectorFunction(ParticleDataMask dataMask, ParticleDataMask... dataMasks) {
        super(dataMask, dataMasks);
    }

    public abstract void update(T component, ParticleData particleData, Random random, float delta);

    public void beforeUpdates(T component, Random random, float delta) {
        // does nothing by default
    }

    public void afterUpdates(T component, Random random, float delta) {
        // does nothing by default
    }
}

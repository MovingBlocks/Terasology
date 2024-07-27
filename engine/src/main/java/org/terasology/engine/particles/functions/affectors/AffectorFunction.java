// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.affectors;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.functions.ParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

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

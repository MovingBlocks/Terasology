// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

/**
 * Generator for a particle's velocity.
 * Upon generation sets particle velocity at random between minVelocity and maxVelocity.
 */
@API
public class VelocityRangeGeneratorComponent implements Component<VelocityRangeGeneratorComponent> {
    public Vector3f minVelocity;
    public Vector3f maxVelocity;

    public VelocityRangeGeneratorComponent(final Vector3f minVelocity, final Vector3f maxVelocity) {
        this.minVelocity = new Vector3f(minVelocity);
        this.maxVelocity = new Vector3f(maxVelocity);
    }

    public VelocityRangeGeneratorComponent() {
        this.minVelocity = new Vector3f();
        this.maxVelocity = new Vector3f();
    }

    @Override
    public void copyFrom(VelocityRangeGeneratorComponent other) {
        this.minVelocity = new Vector3f(other.minVelocity);
        this.maxVelocity = new Vector3f(other.maxVelocity);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.affectors;

import org.joml.Vector3f;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;


@API
public class AccelerationAffectorComponent implements Component<AccelerationAffectorComponent> {

    @Replicate
    public Vector3f acceleration;

    public AccelerationAffectorComponent() {
        this.acceleration = new Vector3f();
    }

    public AccelerationAffectorComponent(Vector3f acceleration) {
        this.acceleration = acceleration;
    }

    @Override
    public void copyFrom(AccelerationAffectorComponent other) {
        this.acceleration = new Vector3f(other.acceleration);
    }
}

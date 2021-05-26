// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.affectors;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.network.Replicate;


@API
public class AccelerationAffectorComponent implements Component {

    @Replicate
    public Vector3f acceleration;

    public AccelerationAffectorComponent() {
        this.acceleration = new Vector3f();
    }

    public AccelerationAffectorComponent(Vector3f acceleration) {
        this.acceleration = acceleration;
    }
}

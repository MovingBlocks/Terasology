// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class BlockImpactEvent extends ImpactEvent {
    public BlockImpactEvent(Vector3f impactPoint, Vector3f impactNormal, Vector3f impactSpeed, float travelDistance, EntityRef impactEntity) {
        super(impactPoint, impactNormal, impactSpeed, travelDistance, impactEntity);
    }
}

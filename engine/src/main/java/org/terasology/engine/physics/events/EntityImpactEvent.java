// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;

public class EntityImpactEvent extends ImpactEvent {
    public EntityImpactEvent(Vector3f impactPoint, Vector3f impactNormal, Vector3f impactSpeed, float travelDistance, EntityRef impactEntity) {
        super(impactPoint, impactNormal, impactSpeed, travelDistance, impactEntity);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LocationChangedSystem extends BaseComponentSystem {

    @ReceiveEvent(components = LocationComponent.class)
    public void onItemUpdate(OnChangedComponent event, EntityRef entity) {
        LocationComponent lc = entity.getComponent(LocationComponent.class);
        if (!lc.lastPosition.equals(lc.position) || !lc.lastRotation.equals(lc.rotation)) {
            entity.send(new LocationChangedEvent(lc, lc.lastPosition, lc.lastRotation));
            lc.lastPosition.set(lc.position);
            lc.lastRotation.set(lc.rotation);
        }
    }
}

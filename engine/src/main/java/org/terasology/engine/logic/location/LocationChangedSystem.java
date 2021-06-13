// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;

import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LocationChangedSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private Set<EntityRef> process = Sets.newHashSet();

    @ReceiveEvent(components = LocationComponent.class)
    public void locationChanged(OnAddedComponent event, EntityRef entity, LocationComponent lc) {
        lc.clearDirtyFlag();
    }

    @ReceiveEvent(components = LocationComponent.class)
    public void locationChanged(OnChangedComponent event, EntityRef entity, LocationComponent lc) {
        if (lc.isDirty() && (!lc.position.equals(lc.lastPosition) || !lc.rotation.equals(lc.lastRotation))) {
            process.add(entity);
        } else {
            lc.clearDirtyFlag();
        }
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : process) {
            LocationComponent lc = entity.getComponent(LocationComponent.class);
            if (lc != null) {
                entity.send(new LocationChangedEvent(lc.lastPosition, lc.lastRotation, lc.position, lc.rotation));
                lc.clearDirtyFlag();
            }
        }
        process.clear();
    }
}

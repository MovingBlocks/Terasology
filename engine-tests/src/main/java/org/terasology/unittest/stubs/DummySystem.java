// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@Share(DummySystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class DummySystem extends BaseComponentSystem {
    @ReceiveEvent
    public void onDummyEvent(DummyEvent event, EntityRef entity, DummyComponent component) {
        component.eventReceived = true;
        entity.saveComponent(component);
    }
}

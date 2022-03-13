// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.common.lifespan;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LifespanSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;
    @In
    private Time time;

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        for (EntityRef entity : entityManager.getEntitiesWith(LifespanComponent.class)) {
            LifespanComponent lifespan = entity.getComponent(LifespanComponent.class);
            if (lifespan.deathTime < currentTime) {
                entity.destroy();
            }
        }
    }

    @ReceiveEvent
    public void addedLifeSpanComponent(OnAddedComponent event, EntityRef entityRef, LifespanComponent lifespanComponent) {
        lifespanComponent.deathTime = time.getGameTimeInMs() + (long) (lifespanComponent.lifespan * 1000);
        entityRef.saveComponent(lifespanComponent);
    }
}

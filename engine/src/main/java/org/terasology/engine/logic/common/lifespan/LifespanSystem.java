/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.common.lifespan;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;

/**
 */
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

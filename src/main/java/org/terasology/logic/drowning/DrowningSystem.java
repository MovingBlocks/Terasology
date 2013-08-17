/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.logic.drowning;

import org.terasology.engine.Time;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.events.OnEnterLiquidEvent;
import org.terasology.logic.characters.events.OnLeaveLiquidEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.location.LocationComponent;
import org.terasology.world.BlockEntityRegistry;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DrowningSystem implements UpdateSubscriberSystem {

    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(DrowningComponent.class, DrownsComponent.class, LocationComponent.class)) {
            DrowningComponent drowning = entity.getComponent(DrowningComponent.class);
            if (drowning.nextDrownDamageTime < time.getGameTimeInMs()) {
                DrownsComponent drowns = entity.getComponent(DrownsComponent.class);

                drowning.nextDrownDamageTime = time.getGameTimeInMs() + (long) (drowns.timeBetweenDrownDamage * 1000);
                entity.saveComponent(drowning);

                LocationComponent loc = entity.getComponent(LocationComponent.class);
                EntityRef liquidBlock = blockEntityProvider.getBlockEntityAt(loc.getWorldPosition());
                entity.send(new DoDamageEvent(drowns.drownDamage, EngineDamageTypes.DROWNING.get(), liquidBlock));
            }
        }
    }

    @ReceiveEvent
    public void onEnterLiquid(OnEnterLiquidEvent event, EntityRef entity, DrownsComponent drowns) {
        DrowningComponent drowning = new DrowningComponent();
        drowning.startDrowningTime = time.getGameTimeInMs() + (long) (1000 * drowns.timeBeforeDrownStart);
        drowning.nextDrownDamageTime = drowning.startDrowningTime;
        entity.addComponent(drowning);
    }

    @ReceiveEvent(components = DrowningComponent.class)
    public void onLeaveLiquid(OnLeaveLiquidEvent event, EntityRef entity) {
        entity.removeComponent(DrowningComponent.class);
    }

}

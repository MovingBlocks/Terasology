/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.logic.afk;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AfkAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final String PERIODIC_ID = "AFK_PERIODIC";

    private static final long AFK_PERIOD = 10 * 1000L;

    @In
    private Time time;

    @In
    private DelayManager delayManager;

    @In
    private LocalPlayer localPlayer;

    private boolean firstFrame = true;

    @Override
    public void update(float delta) {
        if (firstFrame) {
            delayManager.addPeriodicAction(localPlayer.getClientEntity(), PERIODIC_ID, 0, AFK_PERIOD);
            firstFrame = false;
        }
    }

    @ReceiveEvent
    public void onAfkRequest(AfkRequest request, EntityRef entity) {
        AfkEvent event = new AfkEvent(request.getInstigator(), request.isAfk());
        entity.send(event);
    }

    @ReceiveEvent
    public void onPeriodicTrigger(PeriodicActionTriggeredEvent triggeredEvent, EntityRef entity) {
        if (triggeredEvent.getActionId().equals(PERIODIC_ID)) {
            AfkDetectEvent event = new AfkDetectEvent();
            entity.send(event);
        }
    }

}

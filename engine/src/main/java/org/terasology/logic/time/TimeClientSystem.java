/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.time;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.event.WorldtimeResetEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;

@RegisterSystem(RegisterMode.CLIENT)
public class TimeClientSystem extends BaseComponentSystem {
    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider world;

    @Override
    public void postBegin() {
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            entity.send(new WorldtimeResetEvent(world.getTime().getDays()));
            return;
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynchTime(TimeResynchEvent event, EntityRef entityRef) {
        time.setGameTimeDilation(event.getGameTimeDilation());
    }
}

/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.time.WorldtimeResyncEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

@RegisterSystem(RegisterMode.CLIENT)
public class WorldtimeResyncSystem extends BaseComponentSystem {

    @In
    private WorldProvider world;

    @ReceiveEvent(components = ClientComponent.class)
    public void resyncWorldTime(WorldtimeResyncEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            world.getTime().setDays(event.days);
        }
    }
}

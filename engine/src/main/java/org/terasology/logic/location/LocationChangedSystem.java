/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.location;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LocationChangedSystem extends BaseComponentSystem {
    @ReceiveEvent(components = {LocationComponent.class})
    public void onItemUpdate(OnChangedComponent event, EntityRef entity)
    {
        LocationComponent lc = entity.getComponent(LocationComponent.class);
        if (!lc.lastPosition.equals(lc.position) || !lc.lastRotation.equals(lc.rotation))
        {
            entity.send(new LocationChangedEvent(lc, lc.lastPosition, lc.lastRotation));
            lc.lastPosition.set(lc.position);
            lc.lastRotation.set(lc.rotation);
        }
    }
}

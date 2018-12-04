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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.util.HashMap;
import java.util.Map;

@Share(AFK.class)
@RegisterSystem(RegisterMode.ALWAYS)
public class AFKSystem extends BaseComponentSystem implements AFK {

    private Map<Long, Boolean> afkMap;

    @Override
    public void initialise() {
        afkMap = new HashMap<>();
    }

    @Override
    public void onCommand() {

    }

    @Override
    public void onEvent(AFKEvent event, EntityRef entity) {

    }

    @Override
    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onRequest(AFKRequest request, EntityRef entity) {

    }

    @Override
    public boolean isAFK(long id) {
        if (afkMap.containsKey(id)) {
            return afkMap.get(id);
        }
        return false;
    }

}

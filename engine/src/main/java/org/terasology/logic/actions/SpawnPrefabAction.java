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

package org.terasology.logic.actions;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnPrefabAction extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = SpawnPrefabActionComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        SpawnPrefabActionComponent spawnInfo = entity.getComponent(SpawnPrefabActionComponent.class);
        if (spawnInfo.prefab != null) {
            Vector3f spawnLoc = new Vector3f();
            switch (spawnInfo.spawnLocationRelativeTo) {
                case Instigator:
                    spawnLoc.set(event.getInstigatorLocation());
                    break;
                case Target:
                    Vector3f pos = event.getTargetLocation();
                    if (pos != null) {
                        spawnLoc.set(pos);
                    }
                    break;
                default:
                    break;
            }

            entityManager.create(spawnInfo.prefab, spawnLoc);
        }
    }
}

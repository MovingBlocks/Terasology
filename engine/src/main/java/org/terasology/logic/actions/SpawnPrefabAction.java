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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnPrefabAction implements ComponentSystem {

    private EntityManager entityManager;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

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
            }

            EntityRef newEntity = entityManager.create(spawnInfo.prefab, spawnLoc);
        }
    }
}

/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.componentSystem.action;

import org.terasology.components.actions.SpawnPrefabActionComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class SpawnPrefabAction implements EventHandlerSystem {

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
            EntityRef newEntity = entityManager.create(spawnInfo.prefab);
            LocationComponent loc = newEntity.getComponent(LocationComponent.class);
            if (loc != null) {
                switch (spawnInfo.spawnLocationRelativeTo) {
                    case Instigator:
                        loc.setWorldPosition(event.getInstigatorLocation());
                        break;
                    case Target:
                        Vector3f pos = event.getTargetLocation();
                        if (pos != null) {
                            loc.setWorldPosition(pos);
                        }
                        break;
                }
                // TODO: Set rotation
                newEntity.saveComponent(loc);
            }
        }
    }
}

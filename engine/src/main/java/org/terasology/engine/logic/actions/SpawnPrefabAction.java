// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.actions;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.registry.In;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnPrefabAction extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    /**
     * @param event contains the details for the active event, used here for spawn location
     * @param entity is entity which will be spawned
     */
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

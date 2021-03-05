// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.autoCreate;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;

import java.util.Set;

/**
 */
@RegisterSystem
public class AutoCreateSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void postBegin() {
        Set<Prefab> existingGlobals = Sets.newHashSet();
        NetworkMode mode = networkSystem.getMode();

        for (EntityRef entity : entityManager.getEntitiesWith(AutoCreateComponent.class)) {
            existingGlobals.add(entity.getParentPrefab());
        }

        for (Prefab prefab : prefabManager.listPrefabs(AutoCreateComponent.class)) {
            AutoCreateComponent comp = prefab.getComponent(AutoCreateComponent.class);
            if (!existingGlobals.contains(prefab) && (comp.createClientSide || mode.isAuthority())) {
                entityManager.create(prefab);
            }
        }
    }
}

/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.autoCreate;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

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

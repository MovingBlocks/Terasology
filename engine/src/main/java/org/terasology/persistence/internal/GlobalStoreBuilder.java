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
package org.terasology.persistence.internal;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
final class GlobalStoreBuilder {

    private final long nextEntityId;
    private final PrefabSerializer prefabSerializer;

    public GlobalStoreBuilder(EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.nextEntityId = entityManager.getNextId();
        this.prefabSerializer = prefabSerializer;
    }
    
    public EntityData.GlobalStore build(EngineEntityManager entityManager, Iterable<EntityRef> entities) {
        EntityData.GlobalStore.Builder store = EntityData.GlobalStore.newBuilder();

        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (ComponentMetadata<?> componentMetadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
            store.addComponentClass(componentMetadata.getUri().toString());
            componentIdTable.put(componentMetadata.getType(), componentIdTable.size());
        }
        prefabSerializer.setComponentIdMapping(componentIdTable);
        /*
         * The prefabs can't be obtained from  entityManager.getPrefabManager().listPrefabs() as that might not
         * be thread save.
         */
        Set<Prefab> prefabsRequiredForEntityStorage = new HashSet<>();
        for (EntityRef entityRef : entityManager.getAllEntities()) {
            Prefab prefab = entityRef.getParentPrefab();
            if (prefab != null) {
                prefabsRequiredForEntityStorage.add(prefab);
            }
        }
        for (Prefab prefab: prefabsRequiredForEntityStorage) {
            store.addPrefab(prefabSerializer.serialize(prefab));
        }

        EntitySerializer entitySerializer = new EntitySerializer(entityManager);
        entitySerializer.setComponentIdMapping(componentIdTable);
        for (EntityRef entity: entities) {
            if (entity.isPersistent()) {
                store.addEntity(entitySerializer.serialize(entity));
            }
        }
        store.setNextEntityId(nextEntityId);
        return store.build();
    }


}

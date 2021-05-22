// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.persistence.serializers.EntitySerializer;
import org.terasology.engine.persistence.serializers.PersistenceComponentSerializeCheck;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
final class GlobalStoreBuilder {

    private final long nextEntityId;
    private final PrefabSerializer prefabSerializer;

    GlobalStoreBuilder(EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.nextEntityId = entityManager.getNextId();
        this.prefabSerializer = prefabSerializer;
    }
    
    public EntityData.GlobalStore build(EngineEntityManager entityManager, Iterable<EntityRef> entities) {
        EntityData.GlobalStore.Builder store = EntityData.GlobalStore.newBuilder();

        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (ComponentMetadata<?> componentMetadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
            store.addComponentClass(componentMetadata.getId().toString());
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
        entitySerializer.setComponentSerializeCheck(new PersistenceComponentSerializeCheck());
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

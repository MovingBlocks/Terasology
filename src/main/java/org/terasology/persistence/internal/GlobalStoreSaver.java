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
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius
 */
final class GlobalStoreSaver {

    private EngineEntityManager entityManager;
    private EntityData.GlobalStore.Builder store;
    private EntitySerializer entitySerializer;

    private TIntSet nonPersistentIds = new TIntHashSet();

    public GlobalStoreSaver(EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.entityManager = entityManager;
        this.store = EntityData.GlobalStore.newBuilder();
        this.entitySerializer = new EntitySerializer(entityManager);

        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (ComponentMetadata<?> componentMetadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
            store.addComponentClass(componentMetadata.getUri().toString());
            componentIdTable.put(componentMetadata.getType(), componentIdTable.size());
        }
        entitySerializer.setComponentIdMapping(componentIdTable);

        prefabSerializer.setComponentIdMapping(componentIdTable);
        for (Prefab prefab : entityManager.getPrefabManager().listPrefabs()) {
            store.addPrefab(prefabSerializer.serialize(prefab));
        }
    }

    public void addStoreMetadata(StoreMetadata metadata) {
        EntityData.EntityStoreMetadata.Builder referenceSet = EntityData.EntityStoreMetadata.newBuilder();
        TIntIterator iterator = metadata.getExternalReferences().iterator();
        while (iterator.hasNext()) {
            int ref = iterator.next();
            if (!nonPersistentIds.contains(ref)) {
                referenceSet.addReference(ref);
            }
        }
        if (referenceSet.getReferenceCount() > 0) {
            metadata.getId().setUpIdentity(referenceSet);
            store.addStoreReferenceSet(referenceSet);
        }
    }

    public void store(EntityRef entity) {
        if (entity.isPersistent()) {
            store.addEntity(entitySerializer.serialize(entity));
        } else {
            nonPersistentIds.add(entity.getId());
        }
    }

    public EntityData.GlobalStore save() {
        writeIdInfo();

        return store.build();
    }

    private void writeIdInfo() {
        store.setNextEntityId(entityManager.getNextId());
        entityManager.getFreedIds().forEach(new TIntProcedure() {
            public boolean execute(int i) {
                store.addFreedEntityId(i);
                return true;
            }
        });
        nonPersistentIds.forEach(new TIntProcedure() {
            public boolean execute(int i) {
                store.addFreedEntityId(i);
                return true;
            }
        });
    }
}

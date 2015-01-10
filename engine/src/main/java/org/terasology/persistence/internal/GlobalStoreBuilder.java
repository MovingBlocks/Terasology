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

import java.util.Map;

/**
 * @author Immortius
 * @author Florian <florian@fkoeberle.de>
 */
final class GlobalStoreBuilder {

    private final long nextEntityId;
    private final EntityData.GlobalStore.Builder store;
    private final Map<Class<? extends Component>, Integer> componentIdTable;

    public GlobalStoreBuilder(EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.nextEntityId = entityManager.getNextId();
        this.store = EntityData.GlobalStore.newBuilder();

        // TODO move complete store creation in build method, so that the serialization happens off the main thread
        // when this is done then the build method needs a prefab manager.

        this.componentIdTable = Maps.newHashMap();
        for (ComponentMetadata<?> componentMetadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
            store.addComponentClass(componentMetadata.getUri().toString());
            componentIdTable.put(componentMetadata.getType(), componentIdTable.size());
        }
        prefabSerializer.setComponentIdMapping(componentIdTable);
        for (Prefab prefab : entityManager.getPrefabManager().listPrefabs()) {
            store.addPrefab(prefabSerializer.serialize(prefab));
        }
    }



    public EntityData.GlobalStore build(EngineEntityManager entityManager, Iterable<EntityRef> entities) {
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

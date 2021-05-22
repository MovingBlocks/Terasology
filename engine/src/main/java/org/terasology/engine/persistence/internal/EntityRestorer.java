// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.persistence.serializers.EntitySerializer;
import org.terasology.engine.persistence.serializers.PersistenceComponentSerializeCheck;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 */
final class EntityRestorer {

    private EngineEntityManager entityManager;

    EntityRestorer(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Map<String, EntityRef> restore(EntityData.EntityStore store) {
        EntitySerializer serializer = new EntitySerializer(entityManager);
        serializer.setComponentSerializeCheck(new PersistenceComponentSerializeCheck());
        Map<Class<? extends Component>, Integer> idMap = Maps.newHashMap();
        for (int i = 0; i < store.getComponentClassCount(); ++i) {
            ComponentMetadata<?> metadata = entityManager.getComponentLibrary().resolve(store.getComponentClass(i));
            if (metadata != null) {
                idMap.put(metadata.getType(), i);
            }
        }
        serializer.setComponentIdMapping(idMap);
        store.getEntityList().forEach(serializer::deserialize);

        Map<String, EntityRef> namedEntities = Maps.newHashMap();
        for (int i = 0; i < store.getEntityNameCount() && i < store.getEntityNamedCount(); ++i) {
            namedEntities.put(store.getEntityName(i), entityManager.getEntity(store.getEntityNamed(i)));
        }
        return namedEntities;
    }
}

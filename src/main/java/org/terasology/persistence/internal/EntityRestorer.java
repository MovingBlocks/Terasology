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
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius
 */
final class EntityRestorer implements EntityRefTypeHandler.EntityRefInterceptor {

    private EngineEntityManager entityManager;
    private TIntSet validRefs;

    public EntityRestorer(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Map<String, EntityRef> restore(EntityData.EntityStore store, TIntSet externalRefs) {
        validRefs = new TIntHashSet();
        if (externalRefs != null) {
            validRefs.addAll(externalRefs);
        }
        for (EntityData.Entity entity : store.getEntityList()) {
            validRefs.add(entity.getId());
        }

        EntitySerializer serializer = new EntitySerializer(entityManager);
        EntityRefTypeHandler.setReferenceInterceptor(this);
        Map<Class<? extends Component>, Integer> idMap = Maps.newHashMap();
        for (int i = 0; i < store.getComponentClassCount(); ++i) {
            ClassMetadata<? extends Component> metadata = entityManager.getComponentLibrary().getMetadata(store.getComponentClass(i));
            if (metadata != null) {
                idMap.put(metadata.getType(), i);
            }
        }
        serializer.setComponentIdMapping(idMap);
        for (EntityData.Entity entity : store.getEntityList()) {
            serializer.deserialize(entity);
        }
        EntityRefTypeHandler.setReferenceInterceptor(null);

        Map<String, EntityRef> namedEntities = Maps.newHashMap();
        for (int i = 0; i < store.getEntityNameCount() && i < store.getEntityNamedCount(); ++i) {
            namedEntities.put(store.getEntityName(i), entityManager.getEntity(store.getEntityNamed(i)));
        }
        return namedEntities;
    }

    @Override
    public boolean loadingRef(int id) {
        return validRefs.contains(id);
    }

    @Override
    public boolean savingRef(EntityRef ref) {
        return true;
    }
}

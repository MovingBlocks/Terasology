/*
 * Copyright 2013 Moving Blocks
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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
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
class EntityRestorer implements EntityRefTypeHandler.EntityRefInterceptor {

    private EngineEntityManager entityManager;
    private TIntSet validRefs;

    public EntityRestorer(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Map<String, EntityRef> restore(EntityData.EntityStore store, TIntSet validRefs) {
        this.validRefs = validRefs;

        EntitySerializer serializer = new EntitySerializer(entityManager);
        EntityRefTypeHandler.setReferenceInterceptor(this);
        Map<String, EntityRef> namedEntities = Maps.newHashMap();
        Map<Class<? extends Component>, Integer> idMap = Maps.newHashMap();
        for (int i = 0; i < store.getComponentClassCount(); ++i) {
            ClassMetadata<? extends Component> metadata = entityManager.getComponentLibrary().getMetadata(store.getComponentClass(i));
            if (metadata != null) {
                idMap.put(metadata.getType(), i);
            }
        }
        serializer.setComponentIdMapping(idMap);
        for (int i = 0; i < store.getEntityCount(); ++i) {
            EntityRef entity = serializer.deserialize(store.getEntity(i));
            if (!store.getEntityName(i).isEmpty()) {
                namedEntities.put(store.getEntityName(i), entity);
            }
        }
        EntityRefTypeHandler.setReferenceInterceptor(null);
        return namedEntities;
    }

    @Override
    public boolean loadingRef(int id) {
        return validRefs.contains(id);
    }

    @Override
    public void savingRef(int id) {
    }
}

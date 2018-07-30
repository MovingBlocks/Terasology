/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.extensionTypes;

import gnu.trove.iterator.TLongIterator;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.List;

/**
 */
public class EntityRefTypeHandler extends TypeHandler<EntityRef> {

    private EngineEntityManager entityManager;

    public EntityRefTypeHandler(EngineEntityManager engineEntityManager) {
        this.entityManager = engineEntityManager;
    }

    @Override
    public PersistedData serialize(EntityRef value, PersistedDataSerializer serializer) {
        if (value.exists() && value.isPersistent()) {
            return serializer.serialize(value.getId());
        }
        return serializer.serializeNull();
    }

    @Override
    public EntityRef deserialize(PersistedData data) {
        if (data.isNumber()) {
            return entityManager.getEntity(data.getAsLong());
        }
        return EntityRef.NULL;
    }

    private void addEntitiesFromLongArray(List<EntityRef> result, PersistedDataArray array) {
        TLongIterator iterator = array.getAsLongArray().iterator();
        while (iterator.hasNext()) {
            long item = iterator.next();
            result.add(entityManager.getEntity(item));
        }
    }

}

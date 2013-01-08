/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.metadata.extension;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityRefTypeHandler implements TypeHandler<EntityRef> {

    private PersistableEntityManager entityManager;

    public EntityRefTypeHandler(PersistableEntityManager persistableEntityManager) {
        this.entityManager = persistableEntityManager;
    }

    public EntityData.Value serialize(EntityRef value) {
        if (value.exists()) {
            return EntityData.Value.newBuilder().addInteger((value).getId()).build();
        }
        return null;
    }

    public EntityRef deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return entityManager.createEntityRefWithId(value.getInteger(0));
        }
        return EntityRef.NULL;
    }

    public EntityRef copy(EntityRef value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<EntityRef> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (EntityRef ref : value) {
            if (!ref.exists()) {
                result.addInteger(0);
            } else {
                result.addInteger((ref).getId());
            }
        }
        return result.build();
    }

    public List<EntityRef> deserializeList(EntityData.Value value) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
        for (Integer item : value.getIntegerList()) {
            result.add(entityManager.createEntityRefWithId(item));
        }
        return result;
    }

}

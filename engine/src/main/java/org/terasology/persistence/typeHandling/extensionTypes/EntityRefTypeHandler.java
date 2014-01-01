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

import com.google.common.collect.Lists;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityRefTypeHandler implements TypeHandler<EntityRef> {

    private static ThreadLocal<EntityRefInterceptor> refInterceptor = new ThreadLocal<>();
    private EngineEntityManager entityManager;

    public EntityRefTypeHandler(EngineEntityManager engineEntityManager) {
        this.entityManager = engineEntityManager;
    }

    public static void setReferenceInterceptor(EntityRefInterceptor interceptor) {
        refInterceptor.set(interceptor);
    }

    @Override
    public PersistedData serialize(EntityRef value, SerializationContext context) {
        if (value.exists()) {
            if (refInterceptor.get() == null || refInterceptor.get().savingRef(value)) {
                return context.create(value.getId());
            }
        }
        return context.createNull();
    }

    @Override
    public EntityRef deserialize(PersistedData data, DeserializationContext context) {
        if (data.isNumber()) {
            if (refInterceptor.get() == null || refInterceptor.get().loadingRef(data.getAsInteger())) {
                return entityManager.createEntityRefWithId(data.getAsInteger());
            }
        }
        return EntityRef.NULL;
    }

    @Override
    public PersistedData serializeCollection(Collection<EntityRef> value, SerializationContext context) {
        TIntList items = new TIntArrayList();
        for (EntityRef ref : value) {
            if (!ref.exists()) {
                items.add(0);
            } else {
                if (refInterceptor.get() == null || refInterceptor.get().savingRef(ref)) {
                    items.add((ref).getId());
                } else {
                    items.add(0);
                }
            }
        }
        return context.create(items.iterator());
    }

    @Override
    public List<EntityRef> deserializeCollection(PersistedData data, DeserializationContext context) {
        PersistedDataArray array = data.getAsArray();
        List<EntityRef> result = Lists.newArrayListWithCapacity(array.size());
        TIntIterator iterator = array.getAsIntegerArray().iterator();
        while (iterator.hasNext()) {
            int item = iterator.next();
            if (refInterceptor.get() == null || refInterceptor.get().loadingRef(item)) {
                result.add(entityManager.createEntityRefWithId(item));
            } else {
                result.add(EntityRef.NULL);
            }
        }
        return result;
    }

    public interface EntityRefInterceptor {
        /**
         * @param id
         * @return Whether to complete loading the ref. If false, EntityRef.NULL is used instead.
         */
        boolean loadingRef(int id);

        /**
         * @param ref The entity ref being saved
         * @return Whether to save this reference
         */
        boolean savingRef(EntityRef ref);

    }

}

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
package org.terasology.entitySystem.metadata.extension;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

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

    public EntityData.Value serialize(EntityRef value) {
        if (value.exists()) {
            if (refInterceptor.get() == null || refInterceptor.get().savingRef(value)) {
                return EntityData.Value.newBuilder().addInteger(value.getId()).build();
            }
        }
        return null;
    }

    public EntityRef deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            if (refInterceptor.get() == null || refInterceptor.get().loadingRef(value.getInteger(0))) {
                return entityManager.createEntityRefWithId(value.getInteger(0));
            }
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
                if (refInterceptor.get() == null || refInterceptor.get().savingRef(ref)) {
                    result.addInteger((ref).getId());
                } else {
                    result.addInteger(0);
                }
            }
        }
        return result.build();
    }

    public List<EntityRef> deserializeList(EntityData.Value value) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
        for (Integer item : value.getIntegerList()) {
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

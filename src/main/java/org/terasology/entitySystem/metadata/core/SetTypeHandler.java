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

package org.terasology.entitySystem.metadata.core;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Set;

/**
 * @author Immortius
 */
public class SetTypeHandler<T> extends AbstractTypeHandler<Set<T>> {
    private TypeHandler<T> contentsHandler;

    public SetTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

    @Override
    public EntityData.Value serialize(Set<T> value) {
        if (value.size() > 0) {
            return contentsHandler.serialize(value);
        }
        return null;
    }

    @Override
    public Set<T> deserialize(EntityData.Value value) {
        return Sets.newHashSet(contentsHandler.deserializeList(value));
    }

    @Override
    public Set<T> copy(Set<T> value) {
        if (value != null) {
            Set result = Sets.newHashSet();
            for (T item : value) {
                if (item != null) {
                    result.add(contentsHandler.copy(item));
                } else {
                    result.add(null);
                }
            }
            return result;
        }
        return null;
    }

    public TypeHandler getContentsHandler() {
        return contentsHandler;
    }
}

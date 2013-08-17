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
package org.terasology.entitySystem.metadata.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ListTypeHandler<T> extends AbstractTypeHandler<List<T>> {
    private TypeHandler<T> contentsHandler;

    public ListTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

    public EntityData.Value serialize(List<T> value) {
        if (value.size() > 0) {
            return contentsHandler.serialize(value);
        }
        return null;
    }

    public List<T> deserialize(EntityData.Value value) {
        return contentsHandler.deserializeList(value);
    }

    public List<T> copy(List<T> value) {
        if (value != null) {
            List result = Lists.newArrayList();
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

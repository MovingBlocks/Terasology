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
package org.terasology.persistence.typeSerialization.typeHandlers;

import com.google.common.collect.Lists;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * Abstract class for type handlers where collections of the type are simply handled by nesting individually serialized values into another value - that is there is
 * no special manner in which they are handled.
 *
 * @author Immortius <immortius@gmail.com>
 */
public abstract class SimpleTypeHandler<T> implements TypeHandler<T> {

    public EntityData.Value serializeCollection(Iterable<T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (T item : value) {
            result.addValue(serialize(item));
        }
        return result.build();
    }

    public List<T> deserializeCollection(EntityData.Value value) {
        List<T> result = Lists.newArrayListWithCapacity(value.getValueCount());
        for (EntityData.Value item : value.getValueList()) {
            result.add(deserialize(item));
        }
        return result;
    }
}

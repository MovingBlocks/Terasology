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
package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Abstract class for type handlers where collections of the type are simply handled by nesting individually serialized values into another value - that is there is
 * no special manner in which they are handled.
 *
 */
public abstract class SimpleTypeHandler<T> implements TypeHandler<T> {

    @Override
    public PersistedData serializeCollection(Collection<T> value, SerializationContext context) {
        List<PersistedData> rawList = Lists.newArrayList();
        for (T item : value) {
            rawList.add(serialize(item, context));
        }
        return context.create(rawList);
    }

    @Override
    public List<T> deserializeCollection(PersistedData data, DeserializationContext context) {
        if (data.isArray()) {
            PersistedDataArray array = data.getAsArray();
            List<T> result = Lists.newArrayListWithCapacity(array.size());
            for (PersistedData value : array) {
                result.add(deserialize(value, context));
            }
            return result;
        }
        return Lists.newArrayList();
    }

}

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
 */
public abstract class StringRepresentationTypeHandler<T> implements TypeHandler<T> {

    public abstract String getAsString(T item);

    public abstract T getFromString(String representation);

    @Override
    public PersistedData serialize(T value, SerializationContext context) {
        String stringValue = getAsString(value);
        if (stringValue == null) {
            return context.createNull();
        } else {
            return context.create(stringValue);
        }
    }

    @Override
    public T deserialize(PersistedData data, DeserializationContext context) {
        if (data.isString()) {
            return getFromString(data.getAsString());
        }
        return null;
    }

    @Override
    public PersistedData serializeCollection(Collection<T> value, SerializationContext context) {
        String[] result = new String[value.size()];
        int index = 0;
        for (T item : value) {
            if (item != null) {
                result[index++] = getAsString(item);
            } else {
                result[index++] = "";
            }
        }
        return context.create(result);
    }

    @Override
    public List<T> deserializeCollection(PersistedData data, DeserializationContext context) {
        List<T> result = Lists.newArrayList();
        for (String item : data.getAsArray().getAsStringArray()) {
            if (item == null || item.isEmpty()) {
                result.add(null);
            } else {
                result.add(getFromString(item));
            }
        }
        return result;
    }
}

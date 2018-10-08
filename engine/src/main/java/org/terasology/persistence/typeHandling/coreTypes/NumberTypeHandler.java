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

package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Collection;
import java.util.List;

/**
 */
public class NumberTypeHandler implements TypeHandler<Number> {

    @Override
    public PersistedData serialize(Number value, SerializationContext context) {
        if (value != null) {
            return context.create(value.doubleValue());
        }
        return context.createNull();
    }

    @Override
    public Number deserialize(PersistedData data, DeserializationContext context) {
        if (data.isNumber()) {
            return data.getAsDouble();
        }
        return null;
    }

    @Override
    public PersistedData serializeCollection(Collection<Number> value, SerializationContext context) {
        return context.create(Doubles.toArray(value));
    }

    @Override
    public List<Number> deserializeCollection(PersistedData data, DeserializationContext context) {
        List<Number> result = Lists.newArrayList();
        for (PersistedData item : data.getAsArray()) {
            result.add(item.getAsDouble());
        }
        return result;
    }
}

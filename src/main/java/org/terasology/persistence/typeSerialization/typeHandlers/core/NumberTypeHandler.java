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

package org.terasology.persistence.typeSerialization.typeHandlers.core;

import com.google.common.collect.Lists;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius
 */
public class NumberTypeHandler implements TypeHandler<Number> {

    @Override
    public EntityData.Value serialize(Number value) {
        return EntityData.Value.newBuilder().addDouble(value.doubleValue()).build();
    }

    @Override
    public Double deserialize(EntityData.Value value) {
        if (value.getDoubleCount() > 0) {
            return value.getDouble(0);
        }
        return null;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<Number> value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (Number val : value) {
            builder.addDouble(val.doubleValue());
        }
        return builder.build();
    }

    @Override
    public List<Number> deserializeCollection(EntityData.Value value) {
        return Lists.<Number>newArrayList(value.getDoubleList());
    }
}

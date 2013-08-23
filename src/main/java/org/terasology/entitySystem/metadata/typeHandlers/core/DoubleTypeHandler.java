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
package org.terasology.entitySystem.metadata.typeHandlers.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DoubleTypeHandler implements TypeHandler<Double> {

    public EntityData.Value serialize(Double value) {
        return EntityData.Value.newBuilder().addDouble(value).build();
    }

    public Double deserialize(EntityData.Value value) {
        if (value.getDoubleCount() > 0) {
            return value.getDouble(0);
        } else if (value.getFloatCount() > 0) {
            return (double) value.getFloat(0);
        } else if (value.getIntegerCount() > 0) {
            return (double) value.getInteger(0);
        } else if (value.getLongCount() > 0) {
            return (double) value.getLong(0);
        }
        return null;
    }

    public Double copy(Double value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Double> value) {
        return EntityData.Value.newBuilder().addAllDouble(value).build();
    }

    public List<Double> deserializeList(EntityData.Value value) {
        if (value.getDoubleCount() > 0) {
            return Lists.newArrayList(value.getDoubleList());
        } else if (value.getFloatCount() > 0) {
            List<Double> result = Lists.newArrayListWithCapacity(value.getFloatCount());
            for (int i = 0; i < value.getFloatCount(); ++i) {
                result.add((double) value.getFloat(i));
            }
            return result;
        } else if (value.getIntegerCount() > 0) {
            List<Double> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
            for (int i = 0; i < value.getIntegerCount(); ++i) {
                result.add((double) value.getInteger(i));
            }
            return result;
        } else if (value.getLongCount() > 0) {
            List<Double> result = Lists.newArrayListWithCapacity(value.getLongCount());
            for (int i = 0; i < value.getLongCount(); ++i) {
                result.add((double) value.getLong(i));
            }
            return result;
        }
        return Lists.newArrayList();
    }
}

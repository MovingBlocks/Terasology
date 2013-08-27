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
 * @author Immortius <immortius@gmail.com>
 */
public class FloatTypeHandler implements TypeHandler<Float> {

    @Override
    public EntityData.Value serialize(Float value) {
        return EntityData.Value.newBuilder().addFloat(value).build();
    }

    @Override
    public Float deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 0) {
            return value.getFloat(0);
        } else if (value.getDoubleCount() > 0) {
            return (float) value.getDouble(0);
        } else if (value.getIntegerCount() > 0) {
            return (float) value.getInteger(0);
        } else if (value.getLongCount() > 0) {
            return (float) value.getLong(0);
        }
        return null;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<Float> value) {
        return EntityData.Value.newBuilder().addAllFloat(value).build();
    }

    @Override
    public List<Float> deserializeCollection(EntityData.Value value) {
        if (value.getFloatCount() > 0) {
            return Lists.newArrayList(value.getFloatList());
        } else if (value.getDoubleCount() > 0) {
            List<Float> result = Lists.newArrayListWithCapacity(value.getDoubleCount());
            for (int i = 0; i < value.getDoubleCount(); ++i) {
                result.add((float) value.getDouble(i));
            }
            return result;
        } else if (value.getIntegerCount() > 0) {
            List<Float> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
            for (int i = 0; i < value.getIntegerCount(); ++i) {
                result.add((float) value.getInteger(i));
            }
            return result;
        } else if (value.getLongCount() > 0) {
            List<Float> result = Lists.newArrayListWithCapacity(value.getLongCount());
            for (int i = 0; i < value.getLongCount(); ++i) {
                result.add((float) value.getLong(i));
            }
            return result;
        }
        return Lists.newArrayList();
    }
}

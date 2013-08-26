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
import org.terasology.math.TeraMath;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class IntTypeHandler implements TypeHandler<Integer> {

    @Override
    public EntityData.Value serialize(Integer value) {
        return EntityData.Value.newBuilder().addInteger(value).build();
    }

    @Override
    public Integer deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return value.getInteger(0);
        } else if (value.getLongCount() > 0) {
            return (int) value.getLong(0);
        } else if (value.getFloatCount() > 0) {
            return TeraMath.floorToInt(value.getFloat(0));
        } else if (value.getDoubleCount() > 0) {
            return TeraMath.floorToInt((float) value.getDouble(0));
        }
        return null;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<Integer> value) {
        return EntityData.Value.newBuilder().addAllInteger(value).build();
    }

    @Override
    public List<Integer> deserializeCollection(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return Lists.newArrayList(value.getIntegerList());
        } else if (value.getLongCount() > 0) {
            List<Integer> result = Lists.newArrayListWithCapacity(value.getLongCount());
            for (int i = 0; i < value.getLongCount(); ++i) {
                result.add((int) value.getLong(i));
            }
            return result;
        } else if (value.getDoubleCount() > 0) {
            List<Integer> result = Lists.newArrayListWithCapacity(value.getDoubleCount());
            for (int i = 0; i < value.getDoubleCount(); ++i) {
                result.add(TeraMath.floorToInt((float) value.getDouble(i)));
            }
            return result;
        } else if (value.getFloatCount() > 0) {
            List<Integer> result = Lists.newArrayListWithCapacity(value.getFloatCount());
            for (int i = 0; i < value.getFloatCount(); ++i) {
                result.add(TeraMath.floorToInt(value.getFloat(i)));
            }
            return result;
        }
        return Lists.newArrayList();
    }
}

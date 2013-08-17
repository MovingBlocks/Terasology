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
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.math.TeraMath;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LongTypeHandler implements TypeHandler<Long> {

    public EntityData.Value serialize(Long value) {
        return EntityData.Value.newBuilder().addLong(value).build();
    }

    public Long deserialize(EntityData.Value value) {
        if (value.getLongCount() > 0) {
            return value.getLong(0);
        } else if (value.getIntegerCount() > 0) {
            return (long) value.getInteger(0);
        } else if (value.getFloatCount() > 0) {
            return (long) TeraMath.floorToInt(value.getFloat(0));
        } else if (value.getDoubleCount() > 0) {
            return (long) TeraMath.floorToInt((float) value.getDouble(0));
        }
        return null;
    }

    public Long copy(Long value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Long> value) {
        return EntityData.Value.newBuilder().addAllLong(value).build();
    }

    public List<Long> deserializeList(EntityData.Value value) {
        if (value.getLongCount() > 0) {
            return Lists.newArrayList(value.getLongList());
        } else if (value.getIntegerCount() > 0) {
            List<Long> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
            for (int i = 0; i < value.getIntegerCount(); ++i) {
                result.add((long) value.getInteger(i));
            }
            return result;
        } else if (value.getDoubleCount() > 0) {
            List<Long> result = Lists.newArrayListWithCapacity(value.getDoubleCount());
            for (int i = 0; i < value.getDoubleCount(); ++i) {
                result.add((long) TeraMath.floorToInt((float) value.getDouble(i)));
            }
            return result;
        } else if (value.getFloatCount() > 0) {
            List<Long> result = Lists.newArrayListWithCapacity(value.getFloatCount());
            for (int i = 0; i < value.getFloatCount(); ++i) {
                result.add((long) TeraMath.floorToInt(value.getFloat(i)));
            }
            return result;
        }
        return Lists.newArrayList();
    }
}

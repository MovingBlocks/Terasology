/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.List;

import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import com.google.common.collect.Lists;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class FloatTypeHandler implements TypeHandler<Float> {

    public EntityData.Value serialize(Float value) {
        return EntityData.Value.newBuilder().addFloat(value).build();
    }

    public Float deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 0) {
            return value.getFloat(0);
        }
        return null;
    }

    public Float copy(Float value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Float> value) {
        return EntityData.Value.newBuilder().addAllFloat(value).build();
    }

    public List<Float> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getFloatList());
    }
}

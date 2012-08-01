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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class IntTypeHandler implements TypeHandler<Integer> {

    public EntityData.Value serialize(Integer value) {
        return EntityData.Value.newBuilder().addInteger(value).build();
    }

    public Integer deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return value.getInteger(0);
        }
        return null;
    }

    public Integer copy(Integer value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Integer> value) {
        return EntityData.Value.newBuilder().addAllInteger(value).build();
    }

    public List<Integer> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getIntegerList());
    }
}

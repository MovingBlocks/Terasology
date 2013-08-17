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
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

    public EntityData.Value serialize(Boolean value) {
        return EntityData.Value.newBuilder().addBoolean(value).build();
    }

    public Boolean deserialize(EntityData.Value value) {
        if (value.getBooleanCount() > 0) {
            return value.getBoolean(0);
        }
        return null;
    }

    public Boolean copy(Boolean value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Boolean> value) {
        return EntityData.Value.newBuilder().addAllBoolean(value).build();
    }

    public List<Boolean> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getBooleanList());
    }
}

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
public class BooleanTypeHandler implements TypeHandler<Boolean> {

    @Override
    public EntityData.Value serialize(Boolean value) {
        return EntityData.Value.newBuilder().addBoolean(value).build();
    }

    @Override
    public Boolean deserialize(EntityData.Value value) {
        if (value.getBooleanCount() > 0) {
            return value.getBoolean(0);
        }
        return null;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<Boolean> value) {
        return EntityData.Value.newBuilder().addAllBoolean(value).build();
    }

    @Override
    public List<Boolean> deserializeCollection(EntityData.Value value) {
        return Lists.newArrayList(value.getBooleanList());
    }
}

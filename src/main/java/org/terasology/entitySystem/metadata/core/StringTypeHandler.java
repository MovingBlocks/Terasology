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
public class StringTypeHandler implements TypeHandler<String> {

    public EntityData.Value serialize(String value) {
        return EntityData.Value.newBuilder().addString(value).build();
    }

    public String deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return value.getString(0);
        }
        return null;
    }

    public String copy(String value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<String> value) {
        return EntityData.Value.newBuilder().addAllString(value).build();
    }

    public List<String> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getStringList());
    }
}

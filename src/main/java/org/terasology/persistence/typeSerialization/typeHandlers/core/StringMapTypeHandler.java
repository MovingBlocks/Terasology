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

import com.google.common.collect.Maps;
import org.terasology.persistence.typeSerialization.typeHandlers.SimpleTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class StringMapTypeHandler<T> extends SimpleTypeHandler<Map<String, T>> {

    TypeHandler<T> contentsHandler;

    public StringMapTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

    @Override
    public EntityData.Value serialize(Map<String, T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (Map.Entry<String, T> entry : value.entrySet()) {
            if (entry.getValue() != null) {
                EntityData.Value v = contentsHandler.serialize(entry.getValue());
                if (v != null) {
                    result.addNameValue(EntityData.NameValue.newBuilder().setName(entry.getKey()).setValue(v));
                }
            }
        }
        return result.build();
    }

    @Override
    public Map<String, T> deserialize(EntityData.Value value) {
        Map<String, T> result = Maps.newHashMap();
        for (EntityData.NameValue entry : value.getNameValueList()) {
            result.put(entry.getName(), contentsHandler.deserialize(entry.getValue()));
        }
        return result;
    }

}

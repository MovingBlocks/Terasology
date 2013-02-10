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

import java.util.Map;

import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import com.google.common.collect.Maps;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class StringMapTypeHandler<T> extends AbstractTypeHandler<Map<String, T>> {

    TypeHandler<T> contentsHandler;

    public StringMapTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

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

    public Map<String, T> deserialize(EntityData.Value value) {
        Map<String, T> result = Maps.newHashMap();
        for (EntityData.NameValue entry : value.getNameValueList()) {
            result.put(entry.getName(), contentsHandler.deserialize(entry.getValue()));
        }
        return result;
    }

    public Map<String, T> copy(Map<String, T> value) {
        if (value != null) {
            Map<String, T> result = Maps.newHashMap();
            for (Map.Entry<String, T> entry : value.entrySet()) {
                result.put(entry.getKey(), contentsHandler.copy(entry.getValue()));
            }
            return result;
        }
        return null;
    }
}

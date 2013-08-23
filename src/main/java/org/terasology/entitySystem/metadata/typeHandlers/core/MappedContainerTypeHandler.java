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
package org.terasology.entitySystem.metadata.typeHandlers.core;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.typeHandlers.SimpleTypeHandler;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class MappedContainerTypeHandler<T> extends SimpleTypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(MappedContainerTypeHandler.class);

    private Class<T> clazz;
    private Map<String, FieldMetadata> fields = Maps.newHashMap();

    public MappedContainerTypeHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void addField(FieldMetadata info) {
        fields.put(info.getName().toLowerCase(Locale.ENGLISH), info);
    }

    public EntityData.Value serialize(T value) {
        if (value == null) {
            return null;
        }

        EntityData.Value.Builder result = EntityData.Value.newBuilder();

        for (FieldMetadata fieldInfo : fields.values()) {
            EntityData.Value fieldValue = fieldInfo.serialize(value);
            if (fieldValue != null) {
                result.addNameValue(EntityData.NameValue.newBuilder().setName(fieldInfo.getName()).setValue(fieldValue).build());
            }
        }
        return result.build();
    }

    public T deserialize(EntityData.Value value) {
        try {
            T result = clazz.newInstance();
            for (EntityData.NameValue entry : value.getNameValueList()) {
                FieldMetadata fieldInfo = fields.get(entry.getName().toLowerCase(Locale.ENGLISH));
                if (fieldInfo != null) {
                    fieldInfo.deserializeOnto(result, entry.getValue());
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Unable to deserialize {}", value, e);
        }
        return null;
    }

    public T copy(T value) {
        if (value != null) {
            try {
                T result = clazz.newInstance();
                for (FieldMetadata field : fields.values()) {
                    field.setValue(result, field.getCopyOfValue(value));
                }
                return result;
            } catch (InstantiationException e) {
                logger.error("Unable to clone {}", value.getClass(), e);
            } catch (IllegalAccessException e) {
                logger.error("Unable to clone {}", value.getClass(), e);
            }
        }
        return null;
    }

}

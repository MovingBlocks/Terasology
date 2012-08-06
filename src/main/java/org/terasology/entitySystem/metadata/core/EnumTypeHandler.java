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
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EnumTypeHandler<T extends Enum> implements TypeHandler<T> {
    private Class<T> enumType;
    private Logger logger = Logger.getLogger(getClass().getName());

    public EnumTypeHandler(Class<T> enumType) {
        this.enumType = enumType;
    }

    public EntityData.Value serialize(T value) {
        return EntityData.Value.newBuilder().addString(value.toString()).build();
    }

    public T deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            try {
                Enum resultValue = Enum.valueOf(enumType, value.getString(0));
                return enumType.cast(resultValue);
            } catch (IllegalArgumentException iae) {
                // TODO: Temp code due to changed enum case, remove after next milestone
                if (enumType == ItemComponent.UsageType.class) {
                    if (value.equals("OnUser")) {
                        return enumType.cast(ItemComponent.UsageType.ON_USER);
                    } else if (value.equals("OnBlock")) {
                        return enumType.cast(ItemComponent.UsageType.ON_BLOCK);
                    } else if (value.equals("InDirection")) {
                        return enumType.cast(ItemComponent.UsageType.IN_DIRECTION);
                    }
                }
                logger.log(Level.WARNING, "Unable to deserialize enum: ", iae);
            }
        }
        return null;
    }

    public T copy(T value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (T item : value) {
            result.addString(value.toString());
        }
        return result.build();
    }

    public List<T> deserializeList(EntityData.Value value) {
        List<T> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            try {
                Enum resultValue = Enum.valueOf(enumType, item);
                result.add(enumType.cast(resultValue));
            } catch (IllegalArgumentException iae) {
                logger.log(Level.WARNING, "Unable to deserialize enum: ", iae);
            }
        }
        return result;
    }
}

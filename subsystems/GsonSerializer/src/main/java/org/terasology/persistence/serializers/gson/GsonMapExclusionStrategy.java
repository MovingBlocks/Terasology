// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.terasology.reflection.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Map;

public class GsonMapExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        Type fieldType = f.getDeclaredType();
        Class<?> fieldClass = ReflectionUtil.getRawType(fieldType);

        if (Map.class.isAssignableFrom(fieldClass)) {
            Type mapKeyType = ReflectionUtil.getTypeParameter(fieldType, 0);

            return String.class != mapKeyType;
        }

        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}

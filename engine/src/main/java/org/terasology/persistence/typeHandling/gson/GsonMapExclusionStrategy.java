/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.persistence.typeHandling.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.terasology.engine.utilities.ReflectionUtil;

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

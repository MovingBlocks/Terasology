/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.utilities;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 */
public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    // TODO - Improve parameter lookup to go up the inheritance tree more
    /**
     * Attempts to return the type of a paramater of a parameterised type. This uses compile-time information only - the
     * type should be obtained from a field with a the generic types bound.
     * @param type
     * @param index
     * @return The type of the generic paramater at index for the given type, or null if it cannot be obtained.
     */
    public static Type getTypeParameter(Field field, int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length < index + 1) {
            return null;
        }
        return parameterizedType.getActualTypeArguments()[index];
    }
}

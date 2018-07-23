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
package org.terasology.reflection;

import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;

/**
 * Represents the type {@link T}. The type information generated is more comprehensive than {@link Class},
 * and {@link #type} correctly represents {@link T} whether it is generic or a wildcard type.
 *
 * Clients must create a subclass so that type information for the type can be retrieved at run-time:
 *
 * {@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * @param <T> The type for which type information is to be generated.
 */
public class TypeInfo<T> {
    private final Class<? super T> rawType;
    private final Type type;
    private final int hashCode;

    /**
     * Constructs a new type literal. Derives represented class from type
     * parameter.
     */
    @SuppressWarnings("unchecked")
    protected TypeInfo() {
        this.type = ReflectionUtil.getTypeParameterForSuper(getClass(), TypeInfo.class, 0);
        this.rawType = (Class<? super T>) ReflectionUtil.getClassOfType(type);
        this.hashCode = type.hashCode();
    }

    public Class<? super T> getRawType() {
        return rawType;
    }

    public Type getType() {
        return type;
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }
}

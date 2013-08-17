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

package org.terasology.entitySystem.metadata;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.metadata.core.BooleanTypeHandler;
import org.terasology.entitySystem.metadata.core.ByteTypeHandler;
import org.terasology.entitySystem.metadata.core.DoubleTypeHandler;
import org.terasology.entitySystem.metadata.core.FloatTypeHandler;
import org.terasology.entitySystem.metadata.core.IntTypeHandler;
import org.terasology.entitySystem.metadata.core.LongTypeHandler;
import org.terasology.entitySystem.metadata.core.NumberTypeHandler;
import org.terasology.entitySystem.metadata.core.StringTypeHandler;

import java.util.Map;

/**
 * @author Immortius
 */
public class TypeHandlerLibraryBuilder {

    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    public TypeHandlerLibraryBuilder() {
        add(Boolean.class, new BooleanTypeHandler());
        add(Boolean.TYPE, new BooleanTypeHandler());
        add(Byte.class, new ByteTypeHandler());
        add(Byte.TYPE, new ByteTypeHandler());
        add(Double.class, new DoubleTypeHandler());
        add(Double.TYPE, new DoubleTypeHandler());
        add(Float.class, new FloatTypeHandler());
        add(Float.TYPE, new FloatTypeHandler());
        add(Integer.class, new IntTypeHandler());
        add(Integer.TYPE, new IntTypeHandler());
        add(Long.class, new LongTypeHandler());
        add(Long.TYPE, new LongTypeHandler());
        add(String.class, new StringTypeHandler());
        add(Number.class, new NumberTypeHandler());
    }

    public <T> TypeHandlerLibraryBuilder add(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
        return this;
    }

    public TypeHandlerLibraryBuilder addRaw(Class forClass, TypeHandler handler) {
        typeHandlers.put(forClass, handler);
        return this;
    }

    public TypeHandlerLibrary build() {
        return new TypeHandlerLibrary(typeHandlers);
    }
}

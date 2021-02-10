/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.context.internal;

import com.google.common.collect.Maps;
import org.terasology.context.Context;

import java.util.Map;

/**
 * Implements the {@link Context} interface.
 */
public class ContextImpl implements Context {
    private final Context parent;

    private final Map<Class<? extends Object>, Object> map = Maps.newConcurrentMap();


    /**
     *
     * @param parent can be null. If not null it will be used as a fallback if this context does not contain a
     *               requested object.
     */
    public ContextImpl(Context parent) {
        this.parent = parent;
    }

    public ContextImpl() {
        this.parent = null;
    }

    @Override
    public <T> T get(Class<? extends T> type) {
        if (type == Context.class) {
            return type.cast(this);
        }
        T result = type.cast(map.get(type));
        if (result != null) {
            return result;
        }
        if (parent != null) {
            return parent.get(type);
        }
        return result;
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object)  {
        map.put(type, object);
    }

}

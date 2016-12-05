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
import org.terasology.registry.DynamicInstanceProvider;

import java.util.Map;

/**
 * Implements the {@link Context} interface.
 */
public class ContextImpl implements Context {
    private final Context parent;

    private final Map<Class<?>, Object> map = Maps.newConcurrentMap();

    /**
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
        return null;
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object) {
        map.put(type, object);
    }

    // DynamicInstanceProvider stuff, kinda messy but it works

    private static final class DynamicInstanceProviderHolder<T> {
        private final Class<? extends DynamicInstanceProvider> providerClass;
        private final DynamicInstanceProvider<T> provider;

        private DynamicInstanceProviderHolder(Class<? extends DynamicInstanceProvider> providerClass, DynamicInstanceProvider<T> provider) {
            this.providerClass = providerClass;
            this.provider = provider;
        }

        private <U> DynamicInstanceProvider<U> get() {
            return providerClass.cast(provider);
        }
    }

    private final Map<Class<?>, DynamicInstanceProviderHolder<?>> providers = Maps.newConcurrentMap();

    @Override
    public <T, U extends T> void putInstanceProvider(Class<T> type, DynamicInstanceProvider<U> provider) {
        providers.put(type, new DynamicInstanceProviderHolder<>(provider.getClass(), provider));
    }

    @Override
    public <T> DynamicInstanceProvider<T> getInstanceProvider(Class<? extends T> type) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(new RuntimePermission("permGetInstanceProvider"));
        }

        DynamicInstanceProviderHolder<?> holder = providers.get(type);
        DynamicInstanceProvider<T> result = holder != null ? holder.get() : null;
        if (result != null) {
            return result;
        }
        if (parent != null) {
            return parent.getInstanceProvider(type);
        }
        return null;
    }
}

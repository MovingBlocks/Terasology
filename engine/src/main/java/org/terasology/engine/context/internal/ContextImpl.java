// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.context.internal;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.terasology.engine.context.Context;

import java.util.Map;

/**
 * Implements the {@link Context} interface.
 */
public class ContextImpl implements Context {
    private final Context parent;

    private final Map<Class<?>, Object> map = Maps.newConcurrentMap();


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
    public <T> T get(Class<T> type) {
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
    public <T, U extends T> void put(Class<T> type, U object)  {
        map.put(type, object);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("#", Integer.toHexString(hashCode()))
                .add("parent", parent)
                .toString();
    }
}

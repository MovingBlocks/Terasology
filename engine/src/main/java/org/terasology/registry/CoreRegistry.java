// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.registry;

import org.terasology.context.Context;

/**
 * Registry giving access to major singleton systems, via the interface they fulfill.
 *
 */
public final class CoreRegistry {
    private static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new InheritableThreadLocal<>();

    private CoreRegistry() {
    }

    /**
     * Registers an object. These objects will be removed when CoreRegistry.clear() is called (typically when game state changes)
     *
     * @param type   The interface which the system fulfils
     * @param object The system itself
     * @param <T>
     */
    public static <T, U extends T> U put(Class<T> type, U object) {
        if (CONTEXT_THREAD_LOCAL.get() == null) {
            return null;
        }
        CONTEXT_THREAD_LOCAL.get().put(type, object);
        return object;
    }

    /**
     * Sets the context that powers this class.
     * @param context
     */
    public static void setContext(Context context) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(new RuntimePermission("permRegister"));
        }
        CoreRegistry.CONTEXT_THREAD_LOCAL.set(context);
    }

    /**
     *
     * @param type
     * @param <T>
     * @return The system fulfilling the given interface
     */
    public static <T> T get(Class<T> type) {
        if (CONTEXT_THREAD_LOCAL.get() == null) {
            return null;
        }
        if (type == Context.class) {
            return type.cast(CONTEXT_THREAD_LOCAL.get());
        }
        return CONTEXT_THREAD_LOCAL.get().get(type);
    }

}

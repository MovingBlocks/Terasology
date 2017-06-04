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
package org.terasology.registry;

import org.terasology.context.Context;

/**
 * Registry giving access to major singleton systems, via the interface they fulfill.
 */
public final class CoreRegistry {
    private static Context context;

    private CoreRegistry() {
    }

    /**
     * Registers an object.
     *
     * @param type   The interface which the system fulfils.
     * @param object The system itself.
     * @param <T>    The type.
     * @return The object itself.
     */
    public static <T, U extends T> U put(Class<T> type, U object) {
        if (context == null) {
            return null;
        }
        context.put(type, object);
        return object;
    }

    /**
     * Sets the context that powers this class.
     *
     * @param context The context.
     */
    public static void setContext(Context context) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(new RuntimePermission("permRegister"));
        }
        CoreRegistry.context = context;
    }

    /**
     * Retrieves the object associated with the given type.
     *
     * @param type The type.
     * @param <T>  The type.
     * @return The system fulfilling the given interface.
     */
    public static <T> T get(Class<T> type) {
        if (context == null) {
            return null;
        }
        if (type == Context.class) {
            return type.cast(context);
        }
        return context.get(type);
    }

    /**
     * Returns this class as a Context object.
     *
     * @return A Context object representing this class.
     */
    public static Context asContext() {
        return CoreRegistryContext.INSTANCE;
    }

    private static final class CoreRegistryContext implements Context {
        private static final CoreRegistryContext INSTANCE = new CoreRegistryContext();

        private CoreRegistryContext() {
        }

        @Override
        public <T> T get(Class<? extends T> type) {
            return CoreRegistry.get(type);
        }

        @Override
        public <T, U extends T> void put(Class<T> type, U object) {
            CoreRegistry.put(type, object);
        }

        @Override
        public <T, U extends T> void putInstanceProvider(Class<T> type, DynamicInstanceProvider<U> provider) {
            if (context == null) {
                return;
            }
            context.putInstanceProvider(type, provider);
        }

        @Override
        public <T> DynamicInstanceProvider<T> getInstanceProvider(Class<? extends T> type) {
            // SANDBOXED ELSEWHERE
            if (context == null) {
                return null;
            }
            return context.getInstanceProvider(type);
        }
    }
}

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
import org.terasology.module.sandbox.API;

/**
 * Registry giving access to major singleton systems, via the interface they fulfil.
 *
 * @author Immortius
 */
@API
public final class CoreRegistry {
    private static Context context;

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
        context.put(type, object);
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
        CoreRegistry.context = context;
    }

    /**
     *
     * @param type
     * @param <T>
     * @return The system fulfilling the given interface
     */
    public static <T> T get(Class<T> type) {
        if (type == Context.class) {
            return context.get(type);
        }
        return context.get(type);
    }

    public static <T> void remove(Class<T> type) {
        context.remove(type);
    }

}

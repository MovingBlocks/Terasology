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
package org.terasology.persistence;

import org.terasology.engine.module.ModuleManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.naming.Name;
import org.terasology.registry.CoreRegistry;

/**
 */
public final class ModuleContext {

    private static ThreadLocal<Module> context = new ThreadLocal<>();

    private ModuleContext() {
    }

    public static Module getContext() {
        return context.get();
    }

    public static ContextSpan setContext(Module module) {
        return new ContextSpan(module);
    }

    public static ContextSpan setContext(Name module) {
        return new ContextSpan(CoreRegistry.get(ModuleManager.class).getEnvironment().get(module));
    }

    public static final class ContextSpan implements AutoCloseable {

        private Module lastContext;

        private ContextSpan(Module newContext) {
            lastContext = getContext();
            context.set(newContext);
        }

        @Override
        public void close() throws Exception {
            context.set(lastContext);
        }
    }
}

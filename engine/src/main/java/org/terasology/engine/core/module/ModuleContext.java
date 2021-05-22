// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.naming.Name;

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
        public void close() {
            context.set(lastContext);
        }
    }
}

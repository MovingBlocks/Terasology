// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.bootstrap;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.naming.Name;

import java.lang.annotation.Annotation;

public class ClassMetaLibraryImpl implements ClassMetaLibrary {

    private final ModuleManager moduleManager;

    /**
     * @param context the context that provides a {@link ModuleManager}.
     */
    public ClassMetaLibraryImpl(Context context) {
        moduleManager = context.get(ModuleManager.class);
    }

    @Override
    public <T> Iterable<Class<? extends T>> getSubtypesOf(Class<T> superClass, String typeId) {
        return moduleManager.getEnvironment().getSubtypesOf(superClass,
                type -> type.getSimpleName().equals(typeId));
    }

    @Override
    public <U> Iterable<Class<? extends U>> getSubtypesOf(Class<U> type) {
        return moduleManager.getEnvironment().getSubtypesOf(type);
    }

    @Override
    public Iterable<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return moduleManager.getEnvironment().getTypesAnnotatedWith(annotation);
    }

    @Override
    public Name getModuleProviding(Class<?> type) {
        return moduleManager.getEnvironment().getModuleProviding(type);
    }
}

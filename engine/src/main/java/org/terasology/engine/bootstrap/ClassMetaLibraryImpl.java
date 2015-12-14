/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.bootstrap;

import java.lang.annotation.Annotation;

import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.naming.Name;

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

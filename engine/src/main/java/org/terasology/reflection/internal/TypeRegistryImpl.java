/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.reflection.internal;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.TypeRegistry;
import org.terasology.utilities.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TypeRegistryImpl implements TypeRegistry {
    private Reflections reflections;

    public TypeRegistryImpl() {
        initializeReflections(TypeRegistryImpl.class.getClassLoader());
    }

    public void reload(ModuleEnvironment environment) {
        // FIXME: Reflection -- may break with updates to gestalt-module
        initializeReflections((ClassLoader) ReflectionUtil.readField(environment,"finalClassLoader"));
    }

    private void initializeReflections(ClassLoader classLoader) {
        List<ClassLoader> allClassLoaders = new ArrayList<>();

        while (classLoader != null) {
            allClassLoaders.add(classLoader);
            classLoader = classLoader.getParent();
        }

        reflections = new Reflections(
                allClassLoaders,
                new SubTypesScanner(),
                // TODO: Add string-based scanner that scans all objects (== getSubtypesOf(Object.class))
                new TypeAnnotationsScanner()
        );
    }

    @Override
    public <T> Set<Class<? extends T>> getSubtypesOf(Class<T> type) {
        return reflections.getSubTypesOf(type);
    }

    @Override
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotationType) {
        return reflections.getTypesAnnotatedWith(annotationType);
    }
}

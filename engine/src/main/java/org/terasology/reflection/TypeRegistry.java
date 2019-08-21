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
package org.terasology.reflection;

import com.google.common.collect.Lists;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.engine.module.ExternalApiWhitelist;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.sandbox.ModuleClassLoader;
import org.terasology.utilities.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeRegistry {
    private static final Set<String> WHITELISTED_CLASSES =
        ExternalApiWhitelist.CLASSES.stream().map(Class::getName).collect(Collectors.toSet());

    private Reflections reflections;
    private ClassLoader[] classLoaders;

    /**
     * Creates an empty {@link TypeRegistry}. No types are loaded when this constructor
     * is called -- to populate the registry use one of the other parameterized constructors.
     */
    public TypeRegistry() {}

    public TypeRegistry(ClassLoader classLoader) {
        this();
        initializeReflections(classLoader);
    }

    public TypeRegistry(ModuleEnvironment environment) {
        this();
        reload(environment);
    }

    private static boolean filterWhitelistedTypes(String typeName) {
        if (typeName == null) {
            return false;
        }

        typeName = typeName.replace(".class", "");

        int i = typeName.lastIndexOf('.');
        if (i == -1) {
            return false;
        }

        String packageName = typeName.substring(0, i);

        return ExternalApiWhitelist.PACKAGES.contains(packageName) || WHITELISTED_CLASSES.contains(typeName);
    }

    public void reload(ModuleEnvironment environment) {
        // FIXME: Reflection -- may break with updates to gestalt-module
        ClassLoader finalClassLoader = (ClassLoader) ReflectionUtil.readField(environment, "finalClassLoader");
        initializeReflections(finalClassLoader, environment);
    }

    private void initializeReflections(ClassLoader classLoader) {
        List<ClassLoader> allClassLoaders = Lists.newArrayList();

        while (classLoader != null) {
            allClassLoaders.add(classLoader);
            classLoader = classLoader.getParent();
        }

        // Here allClassLoaders contains child class loaders followed by their parent. The list is
        // reversed so that classes are loaded using the originally declaring/loading class loader,
        // not a child class loader (like a ModuleClassLoader, for example)
        Collections.reverse(allClassLoaders);

        classLoaders = allClassLoaders.toArray(new ClassLoader[0]);

        // TODO: Use caches if possible since scanning does not work on Android
        reflections = new Reflections(
            new ConfigurationBuilder()
                .setScanners(
                    new SubTypesScanner(false),
                    new TypeAnnotationsScanner()
                )
                .addClassLoaders(allClassLoaders)
                .addUrls(ClasspathHelper.forClassLoader(
                    allClassLoaders.stream()
                        .filter(loader -> !(loader instanceof ModuleClassLoader))
                        .toArray(ClassLoader[]::new)
                ))
                .filterInputsBy(TypeRegistry::filterWhitelistedTypes)
                .useParallelExecutor()
        );

    }

    private void initializeReflections(ClassLoader classLoader, ModuleEnvironment environment) {
        initializeReflections(classLoader);

        for (Module module : environment.getModulesOrderedByDependencies()) {
            if (!module.isCodeModule()) {
                continue;
            }

            reflections.merge(module.getReflectionsFragment());
        }
    }

    public <T> Set<Class<? extends T>> getSubtypesOf(Class<T> type) {
        Iterable<String> subTypes = reflections.getStore().getAll(SubTypesScanner.class.getSimpleName(), type.getName());
        return ReflectionUtil.loadClasses(subTypes, reflections.getConfiguration().getClassLoaders());
    }

    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotationType) {
        return reflections.getTypesAnnotatedWith(annotationType);
    }

    public Optional<Class<?>> load(String name) {
        return Optional.ofNullable(ReflectionUtils.forName(name, classLoaders));
    }
}
